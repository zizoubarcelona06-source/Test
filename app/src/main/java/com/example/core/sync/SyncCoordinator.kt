package com.example.core.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.data.local.BtpDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class SyncState {
    ONLINE,
    OFFLINE,
    SYNCHRONIZING,
    SYNC_ERROR
}

data class SyncStatusSnapshot(
    val syncState: SyncState = SyncState.ONLINE,
    val isNetworkAvailable: Boolean = true,
    val pendingCount: Int = 0,
    val errorCount: Int = 0,
    val syncedCount: Int = 0,
    val lastSyncedAt: Long? = null,
    val lastErrorMessage: String = ""
)

class SyncCoordinator(
    context: Context,
    private val db: BtpDatabase,
    private var cloudPushHandler: suspend () -> Result<Unit> = { Result.success(Unit) }
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    fun setCloudPushHandler(handler: suspend () -> Result<Unit>) {
        cloudPushHandler = handler
    }

    private val _isNetworkOnline = MutableStateFlow(checkCurrentConnectivity())
    private val _isSyncing = MutableStateFlow(false)
    private val _lastSyncedAt = MutableStateFlow<Long?>(null)
    private val _lastError = MutableStateFlow("")

    private val _snapshot = MutableStateFlow(
        SyncStatusSnapshot(
            syncState = if (_isNetworkOnline.value) SyncState.ONLINE else SyncState.OFFLINE,
            isNetworkAvailable = _isNetworkOnline.value
        )
    )
    val snapshot: StateFlow<SyncStatusSnapshot> = _snapshot.asStateFlow()

    init {
        registerNetworkCallback()
        observeOutboxAndConnectivity()
    }

    private fun checkCurrentConnectivity(): Boolean {
        val cm = connectivityManager ?: return true
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun registerNetworkCallback() {
        val cm = connectivityManager ?: return
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isNetworkOnline.value = true
                    triggerAutoSync()
                }

                override fun onLost(network: Network) {
                    _isNetworkOnline.value = checkCurrentConnectivity()
                }
            })
        } catch (_: Exception) {
            _isNetworkOnline.value = checkCurrentConnectivity()
        }
    }

    private fun observeOutboxAndConnectivity() {
        scope.launch {
            combine(
                _isNetworkOnline,
                _isSyncing,
                _lastSyncedAt,
                _lastError,
                db.syncOutboxDao().observeAll()
            ) { online, syncing, lastSynced, lastErr, outbox ->
                val pending = outbox.count { it.status == "PENDING" || it.status == "SYNCING" }
                val errors = outbox.count { it.status == "ERROR" }
                val synced = outbox.count { it.status == "SYNCED" }
                val state = when {
                    syncing -> SyncState.SYNCHRONIZING
                    !online -> SyncState.OFFLINE
                    errors > 0 -> SyncState.SYNC_ERROR
                    else -> SyncState.ONLINE
                }
                SyncStatusSnapshot(
                    syncState = state,
                    isNetworkAvailable = online,
                    pendingCount = pending,
                    errorCount = errors,
                    syncedCount = synced,
                    lastSyncedAt = lastSynced,
                    lastErrorMessage = lastErr
                )
            }.collect { snap ->
                _snapshot.value = snap
                if (snap.isNetworkAvailable && snap.pendingCount > 0 && !_isSyncing.value) {
                    triggerAutoSync()
                }
            }
        }
    }

    fun triggerAutoSync(force: Boolean = false) {
        scope.launch {
            if (_isSyncing.value) return@launch
            val onlineNow = checkCurrentConnectivity()
            _isNetworkOnline.value = onlineNow
            if (!onlineNow) {
                if (force) {
                    _lastError.value = "No internet connection available"
                    val pending = db.syncOutboxDao().getPendingOrError()
                    pending.forEach {
                        db.syncOutboxDao().updateStatus(
                            id = it.id,
                            status = "ERROR",
                            lastError = "Offline during manual sync",
                            retryCount = it.retryCount + 1
                        )
                    }
                }
                return@launch
            }

            val pendingItems = db.syncOutboxDao().getPendingOrError()
            if (pendingItems.isEmpty() && !force) return@launch

            _isSyncing.value = true
            _lastError.value = ""
            try {
                pendingItems.forEach { item ->
                    db.syncOutboxDao().updateStatus(item.id, "SYNCING", "", item.retryCount)
                }

                val pushResult = cloudPushHandler()
                pushResult.onSuccess {
                    pendingItems.forEach { item ->
                        db.syncOutboxDao().updateStatus(item.id, "SYNCED", "", item.retryCount)
                    }
                    db.chatMessageDao().markAllSynced()
                    _lastSyncedAt.value = System.currentTimeMillis()
                }.onFailure { err ->
                    val errMsg = err.message ?: "Cloud sync error"
                    _lastError.value = errMsg
                    pendingItems.forEach { item ->
                        db.syncOutboxDao().updateStatus(
                            item.id,
                            "ERROR",
                            errMsg,
                            item.retryCount + 1
                        )
                    }
                }
            } catch (e: Exception) {
                _lastError.value = e.message ?: "Sync failed"
                pendingItems.forEach { item ->
                    db.syncOutboxDao().updateStatus(
                        item.id,
                        "ERROR",
                        e.message ?: "Sync exception",
                        item.retryCount + 1
                    )
                }
            } finally {
                _isSyncing.value = false
            }
        }
    }

    suspend fun clearSyncedHistory() {
        db.syncOutboxDao().clearSynced()
    }
}
