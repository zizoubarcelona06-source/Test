package com.example.data.remote

import android.os.Build
import android.util.Log
import com.example.core.security.SecurityUtils
import com.example.core.security.SessionManager
import com.example.data.local.BtpDatabase
import com.example.data.local.entity.ChatChannelEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.CloudSyncMapEntity
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.EquipmentEntity
import com.example.data.local.entity.EquipmentFaultEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.MaterialEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.ScheduleTaskEntity
import com.example.data.local.entity.SiteEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.SyncOutboxEntity
import com.example.data.local.entity.UserAccountEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import java.util.UUID

data class CloudWorkspaceState(
    val isGoogleSignedIn: Boolean = false,
    val firebaseUid: String = "",
    val email: String = "",
    val companyId: String = "",
    val companyName: String = "",
    val inviteCode: String = "",
    val defaultInviteRole: String = "مهندس موقع",
    val isOwner: Boolean = false,
    val needsWorkspaceSetup: Boolean = false,
    val isSyncingNow: Boolean = false
)

class CloudSyncRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val db: BtpDatabase,
    private val sessionManager: SessionManager
) {
    companion object {
        private const val TAG = "CloudSyncRepository"
        private const val INVITE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        private val secureRandom = SecureRandom()
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val listenerLock = Any()
    private val activeListeners = mutableListOf<ListenerRegistration>()

    private val _workspaceState = MutableStateFlow(CloudWorkspaceState())
    val workspaceState: StateFlow<CloudWorkspaceState> = _workspaceState.asStateFlow()

    private val _companyMembers = MutableStateFlow<List<FsCompanyMember>>(emptyList())
    val companyMembers: StateFlow<List<FsCompanyMember>> = _companyMembers.asStateFlow()

    private fun newDocId(): String = UUID.randomUUID().toString().replace("-", "")

    private fun generateInviteCode(): String = buildString(8) {
        append("BTP")
        repeat(5) {
            append(INVITE_CHARS[secureRandom.nextInt(INVITE_CHARS.length)])
        }
    }

    fun stopCloudSync() {
        synchronized(listenerLock) {
            activeListeners.forEach { reg ->
                try {
                    reg.remove()
                } catch (_: Exception) {
                }
            }
            activeListeners.clear()
        }
        _companyMembers.value = emptyList()
        _workspaceState.value = CloudWorkspaceState()
    }

    suspend fun syncGoogleUserOnSignIn(firebaseUser: FirebaseUser): Result<UserAccountEntity> {
        val uid = firebaseUser.uid
        if (uid.isBlank()) {
            return Result.failure(IllegalStateException("Missing authenticated user ID"))
        }
        val userDocPath = "users/$uid"
        return try {
            val prefs = sessionManager.preferencesFlow.first()
            val userRef = firestore.collection("users").document(uid)
            val snap = userRef.get().await()
            val existingProfile = if (snap.exists()) {
                snap.toObject(
                    FsUserProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else {
                null
            }

            val resolvedName = existingProfile?.fullName?.takeIf { it.isNotBlank() }
                ?: firebaseUser.displayName?.takeIf { it.isNotBlank() }
                ?: firebaseUser.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
                ?: "BTP Member"
            val resolvedEmail = firebaseUser.email ?: existingProfile?.email.orEmpty()
            val resolvedPhoto = firebaseUser.photoUrl?.toString() ?: existingProfile?.photoUrl.orEmpty()
            val resolvedRole = existingProfile?.role?.takeIf { it.isNotBlank() } ?: "مهندس موقع"
            val resolvedBranch = existingProfile?.branchName?.takeIf { it.isNotBlank() } ?: "المقر الرئيسي"
            val resolvedLang = existingProfile?.preferredLanguageCode?.takeIf { it.isNotBlank() } ?: prefs.languageCode
            var resolvedCompanyId = existingProfile?.activeCompanyId.orEmpty()
            val resolvedPoints = existingProfile?.rewardPoints ?: 120

            val profilePayload = mapOf(
                "uid" to uid,
                "fullName" to resolvedName,
                "email" to resolvedEmail,
                "photoUrl" to resolvedPhoto,
                "role" to resolvedRole,
                "branchName" to resolvedBranch,
                "preferredLanguageCode" to resolvedLang,
                "activeCompanyId" to resolvedCompanyId,
                "active" to (existingProfile?.active ?: true),
                "rewardPoints" to resolvedPoints
            )
            if (!snap.exists()) {
                userRef.set(FirestoreMappers.withCreateTimestamps(profilePayload)).await()
            } else {
                userRef.update(FirestoreMappers.withUpdateTimestamps(profilePayload)).await()
            }

            registerDeviceInCloud(uid)

            // Resolve active company membership if present
            var activeRole = resolvedRole
            var companyName = prefs.companyName
            var currencySymbol = prefs.currencySymbol
            var inviteCode = ""
            var isOwner = false

            if (resolvedCompanyId.isNotBlank()) {
                val companySnap = firestore.collection("companies").document(resolvedCompanyId).get().await()
                val memberSnap = firestore.collection("companies").document(resolvedCompanyId)
                    .collection("members").document(uid).get().await()
                if (companySnap.exists() && memberSnap.exists()) {
                    val companyObj = companySnap.toObject(
                        FsCompany::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    val memberObj = memberSnap.toObject(
                        FsCompanyMember::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    if (companyObj != null && memberObj != null && memberObj.active) {
                        companyName = companyObj.name
                        currencySymbol = companyObj.currencySymbol
                        inviteCode = companyObj.inviteCode
                        isOwner = companyObj.ownerId == uid
                        activeRole = memberObj.role
                    } else {
                        resolvedCompanyId = ""
                    }
                } else {
                    resolvedCompanyId = ""
                }
            }

            // Upsert corresponding Room UserAccountEntity for offline continuity
            val cloudUsername = "g_${uid.take(12)}"
            val existingRoomUser = db.userAccountDao().getByUsername(cloudUsername)
            val localUser = if (existingRoomUser != null) {
                val updated = existingRoomUser.copy(
                    fullName = resolvedName,
                    role = activeRole,
                    preferredLanguageCode = resolvedLang,
                    branchName = resolvedBranch,
                    avatarUri = resolvedPhoto.ifBlank { existingRoomUser.avatarUri },
                    isActive = true,
                    lastLoginAt = System.currentTimeMillis(),
                    rewardPoints = resolvedPoints
                )
                db.userAccountDao().update(updated)
                updated
            } else {
                val salt = SecurityUtils.generateSalt()
                val hash = SecurityUtils.hashPassword(UUID.randomUUID().toString(), salt)
                val newUser = UserAccountEntity(
                    fullName = resolvedName,
                    username = cloudUsername,
                    phone = resolvedEmail,
                    role = activeRole,
                    passwordHash = hash,
                    salt = salt,
                    isActive = true,
                    lastLoginAt = System.currentTimeMillis(),
                    preferredLanguageCode = resolvedLang,
                    branchName = resolvedBranch,
                    avatarUri = resolvedPhoto,
                    rewardPoints = resolvedPoints
                )
                val id = db.userAccountDao().insert(newUser)
                newUser.copy(id = id)
            }

            sessionManager.saveLoginSession(localUser.id, remember = true)
            sessionManager.setCloudSession(
                firebaseUid = uid,
                companyId = resolvedCompanyId,
                inviteCode = inviteCode,
                companyName = companyName,
                currencySymbol = currencySymbol
            )

            _workspaceState.value = CloudWorkspaceState(
                isGoogleSignedIn = true,
                firebaseUid = uid,
                email = resolvedEmail,
                companyId = resolvedCompanyId,
                companyName = companyName,
                inviteCode = inviteCode,
                isOwner = isOwner,
                needsWorkspaceSetup = resolvedCompanyId.isBlank()
            )

            if (resolvedCompanyId.isNotBlank()) {
                startCompanyListeners(resolvedCompanyId, activeRole, uid)
                pushPendingOutboxToCloud()
            }

            Result.success(localUser)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.WRITE, userDocPath, auth)
            Result.failure(e)
        }
    }

    suspend fun createCompanyWorkspace(
        companyName: String,
        currencySymbol: String,
        defaultInviteRole: String = "مهندس موقع",
        currentUser: UserAccountEntity
    ): Result<String> {
        val firebaseUser = auth.currentUser
            ?: return Result.failure(IllegalStateException("Google Sign-In required"))
        val uid = firebaseUser.uid
        val cleanName = companyName.trim().ifBlank { "شركة البنيان للأشغال العمومية" }
        val cleanCurrency = currencySymbol.trim().ifBlank { "د.ج" }
        val companyId = "comp_${newDocId().take(16)}"
        val inviteCode = generateInviteCode()
        val path = "companies/$companyId"

        return try {
            // 1. Create /companies/{companyId}
            firestore.collection("companies").document(companyId).set(
                FirestoreMappers.withCreateTimestamps(
                    mapOf(
                        "name" to cleanName,
                        "currencySymbol" to cleanCurrency,
                        "inviteCode" to inviteCode,
                        "ownerId" to uid,
                        "memberUids" to listOf(uid)
                    )
                )
            ).await()

            // 2. Add owner to /companies/{companyId}/members/{uid} as مدير عام
            firestore.collection("companies").document(companyId)
                .collection("members").document(uid)
                .set(
                    mapOf(
                        "uid" to uid,
                        "companyId" to companyId,
                        "fullName" to currentUser.fullName.ifBlank { firebaseUser.displayName ?: "المدير العام" },
                        "email" to (firebaseUser.email ?: ""),
                        "photoUrl" to (firebaseUser.photoUrl?.toString() ?: currentUser.avatarUri),
                        "role" to "مدير عام",
                        "branchName" to currentUser.branchName.ifBlank { "المقر الرئيسي" },
                        "preferredLanguageCode" to currentUser.preferredLanguageCode,
                        "inviteCode" to inviteCode,
                        "active" to true,
                        "joinedAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()

            // 3. Create lookup invite doc /company_invites/{inviteCode}
            firestore.collection("company_invites").document(inviteCode).set(
                FirestoreMappers.withCreateTimestamps(
                    mapOf(
                        "companyId" to companyId,
                        "companyName" to cleanName,
                        "ownerId" to uid,
                        "defaultRole" to defaultInviteRole,
                        "active" to true
                    )
                )
            ).await()

            // 4. Update private /users/{uid} & local Room role
            firestore.collection("users").document(uid).update(
                FirestoreMappers.withUpdateTimestamps(
                    mapOf(
                        "activeCompanyId" to companyId,
                        "role" to "مدير عام"
                    )
                )
            ).await()

            val updatedLocal = currentUser.copy(role = "مدير عام")
            db.userAccountDao().update(updatedLocal)

            sessionManager.setCloudSession(
                firebaseUid = uid,
                companyId = companyId,
                inviteCode = inviteCode,
                companyName = cleanName,
                currencySymbol = cleanCurrency
            )

            _workspaceState.value = CloudWorkspaceState(
                isGoogleSignedIn = true,
                firebaseUid = uid,
                email = firebaseUser.email.orEmpty(),
                companyId = companyId,
                companyName = cleanName,
                inviteCode = inviteCode,
                defaultInviteRole = defaultInviteRole,
                isOwner = true,
                needsWorkspaceSetup = false
            )

            // Seed initial local data up to Firestore so joining employees see projects, workers, etc.
            uploadLocalDatabaseSnapshotToCloud(companyId, uid)
            startCompanyListeners(companyId, "مدير عام", uid)

            Result.success(inviteCode)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.CREATE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun joinCompanyByInviteCode(
        rawCode: String,
        currentUser: UserAccountEntity
    ): Result<String> {
        val firebaseUser = auth.currentUser
            ?: return Result.failure(IllegalStateException("Google Sign-In required"))
        val uid = firebaseUser.uid
        val code = rawCode.trim().uppercase()
        if (!code.matches(Regex("^[A-Za-z0-9_-]{4,24}$"))) {
            return Result.failure(IllegalArgumentException("ERR_INVALID_INVITE_CODE"))
        }
        val invitePath = "company_invites/$code"

        return try {
            val inviteSnap = firestore.collection("company_invites").document(code).get().await()
            if (!inviteSnap.exists()) {
                return Result.failure(IllegalArgumentException("ERR_INVALID_INVITE_CODE"))
            }
            val invite = inviteSnap.toObject(
                FsCompanyInvite::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: return Result.failure(IllegalArgumentException("ERR_INVALID_INVITE_CODE"))

            if (!invite.active || invite.companyId.isBlank()) {
                return Result.failure(IllegalArgumentException("ERR_INVALID_INVITE_CODE"))
            }

            val companyId = invite.companyId
            val assignedRole = invite.defaultRole.ifBlank { "مهندس موقع" }

            // 1. Join /companies/{companyId}/members/{uid}
            firestore.collection("companies").document(companyId)
                .collection("members").document(uid)
                .set(
                    mapOf(
                        "uid" to uid,
                        "companyId" to companyId,
                        "fullName" to currentUser.fullName.ifBlank { firebaseUser.displayName ?: "موظف" },
                        "email" to (firebaseUser.email ?: ""),
                        "photoUrl" to (firebaseUser.photoUrl?.toString() ?: currentUser.avatarUri),
                        "role" to assignedRole,
                        "branchName" to currentUser.branchName.ifBlank { "الميدان" },
                        "preferredLanguageCode" to currentUser.preferredLanguageCode,
                        "inviteCode" to code,
                        "active" to true,
                        "joinedAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()

            // 2. Append uid to /companies/{companyId}.memberUids
            firestore.collection("companies").document(companyId).update(
                mapOf(
                    "memberUids" to FieldValue.arrayUnion(uid),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val companySnap = firestore.collection("companies").document(companyId).get().await()
            val companyObj = companySnap.toObject(
                FsCompany::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            val resolvedCompanyName = companyObj?.name ?: invite.companyName
            val resolvedCurrency = companyObj?.currencySymbol ?: "د.ج"

            // 3. Update /users/{uid} and local Room user role
            firestore.collection("users").document(uid).update(
                FirestoreMappers.withUpdateTimestamps(
                    mapOf(
                        "activeCompanyId" to companyId,
                        "role" to assignedRole
                    )
                )
            ).await()

            db.userAccountDao().update(currentUser.copy(role = assignedRole))
            sessionManager.setCloudSession(
                firebaseUid = uid,
                companyId = companyId,
                inviteCode = code,
                companyName = resolvedCompanyName,
                currencySymbol = resolvedCurrency
            )

            // Replace demo local data mappings cleanly before pulling remote company data
            clearLocalCompanyTablesBeforeJoin(companyId)

            _workspaceState.value = CloudWorkspaceState(
                isGoogleSignedIn = true,
                firebaseUid = uid,
                email = firebaseUser.email.orEmpty(),
                companyId = companyId,
                companyName = resolvedCompanyName,
                inviteCode = code,
                defaultInviteRole = assignedRole,
                isOwner = companyObj?.ownerId == uid,
                needsWorkspaceSetup = false
            )

            startCompanyListeners(companyId, assignedRole, uid)
            Result.success(resolvedCompanyName)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.WRITE, invitePath, auth)
            Result.failure(e)
        }
    }

    private suspend fun clearLocalCompanyTablesBeforeJoin(newCompanyId: String) {
        db.cloudSyncMapDao().clearOtherCompanies(newCompanyId)
        db.clientDao().deleteAll()
        db.supplierDao().deleteAll()
        db.projectDao().deleteAll()
        db.siteDao().deleteAll()
        db.employeeDao().deleteAll()
        db.equipmentDao().deleteAll()
        db.materialDao().deleteAll()
        db.expenseDao().deleteAll()
        db.invoiceDao().deleteAll()
        db.scheduleTaskDao().deleteAll()
    }

    suspend fun updateMemberRole(memberUid: String, newRole: String): Result<Unit> {
        val state = _workspaceState.value
        val companyId = state.companyId
        if (!state.isGoogleSignedIn || companyId.isBlank()) {
            return Result.failure(IllegalStateException("Not linked to a company workspace"))
        }
        val path = "companies/$companyId/members/$memberUid"
        return try {
            firestore.document(path).update(
                FirestoreMappers.withUpdateTimestamps(mapOf("role" to newRole))
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.UPDATE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun updateInviteDefaultRole(newRole: String): Result<Unit> {
        val state = _workspaceState.value
        val inviteCode = state.inviteCode
        if (!state.isGoogleSignedIn || inviteCode.isBlank()) {
            return Result.failure(IllegalStateException("No active invite code"))
        }
        val path = "company_invites/$inviteCode"
        return try {
            firestore.document(path).update(
                FirestoreMappers.withUpdateTimestamps(mapOf("defaultRole" to newRole))
            ).await()
            _workspaceState.value = state.copy(defaultInviteRole = newRole)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.UPDATE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun syncUserPreferencesToCloud(user: UserAccountEntity) {
        val uid = auth.currentUser?.uid ?: return
        val userPath = "users/$uid"
        try {
            firestore.document(userPath).update(
                FirestoreMappers.withUpdateTimestamps(
                    mapOf(
                        "fullName" to user.fullName,
                        "branchName" to user.branchName,
                        "photoUrl" to user.avatarUri,
                        "preferredLanguageCode" to user.preferredLanguageCode
                    )
                )
            ).await()
            val companyId = _workspaceState.value.companyId
            if (companyId.isNotBlank()) {
                firestore.collection("companies").document(companyId)
                    .collection("members").document(uid)
                    .update(
                        FirestoreMappers.withUpdateTimestamps(
                            mapOf(
                                "fullName" to user.fullName,
                                "branchName" to user.branchName,
                                "photoUrl" to user.avatarUri,
                                "preferredLanguageCode" to user.preferredLanguageCode
                            )
                        )
                    ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.UPDATE, userPath, auth)
        }
    }

    private suspend fun registerDeviceInCloud(uid: String) {
        val brand = (Build.MANUFACTURER ?: "Android").replaceFirstChar { it.uppercase() }
        val model = Build.MODEL ?: "Device"
        val deviceName = if (model.startsWith(brand, ignoreCase = true)) model else "$brand $model"
        val release = Build.VERSION.RELEASE ?: "14"
        val deviceId = ("dev_" + deviceName.replace(Regex("[^A-Za-z0-9_-]"), "_")).take(60)
        val path = "users/$uid/devices/$deviceId"
        try {
            val docRef = firestore.document(path)
            val snap = docRef.get().await()
            val data = mapOf(
                "uid" to uid,
                "deviceName" to deviceName,
                "deviceModel" to "Android $release",
                "locationLabel" to "المقر / الميدان",
                "active" to true,
                "lastActiveAt" to FieldValue.serverTimestamp()
            )
            if (!snap.exists()) {
                docRef.set(
                    data + mapOf("createdAt" to FieldValue.serverTimestamp())
                ).await()
            } else {
                docRef.update(data).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.WRITE, path, auth)
        }
    }

    suspend fun pushPendingOutboxToCloud(): Result<Unit> {
        val firebaseUser = auth.currentUser ?: return Result.success(Unit)
        val prefs = sessionManager.preferencesFlow.first()
        val companyId = _workspaceState.value.companyId.ifBlank { prefs.activeCompanyId }
        if (companyId.isBlank()) return Result.success(Unit)

        val pending = db.syncOutboxDao().getPendingOrError()
        if (pending.isEmpty()) return Result.success(Unit)

        return try {
            for (item in pending) {
                pushSingleOutboxItem(item, companyId, firebaseUser.uid)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun pushSingleOutboxItem(
        item: SyncOutboxEntity,
        companyId: String,
        uid: String
    ) {
        val collectionName = when (item.entityType) {
            "PROJECT" -> "projects"
            "SITE" -> "sites"
            "TASK" -> "tasks"
            "EMPLOYEE" -> "employees"
            "EQUIPMENT" -> "equipment"
            "EQUIPMENT_FAULT" -> "equipment_faults"
            "MATERIAL" -> "materials"
            "EXPENSE" -> "expenses"
            "INVOICE" -> "invoices"
            "CLIENT" -> "clients"
            "SUPPLIER" -> "suppliers"
            "CHAT_CHANNEL" -> "chat_channels"
            "CHAT_MESSAGE" -> "chat_messages"
            else -> return
        }

        val existingMap = db.cloudSyncMapDao().getByLocal(item.entityType, item.entityId)
        val cloudId = existingMap?.cloudId ?: newDocId()
        val docPath = "companies/$companyId/$collectionName/$cloudId"

        try {
            if (item.operation == "DELETE") {
                if (existingMap != null) {
                    firestore.document(docPath).delete().await()
                    db.cloudSyncMapDao().deleteByLocal(item.entityType, item.entityId)
                }
                return
            }

            val payloadFields: Map<String, Any?> = when (item.entityType) {
                "PROJECT" -> db.projectDao().getById(item.entityId)?.let { projectToMap(it, companyId, uid) }
                "SITE" -> db.siteDao().getById(item.entityId)?.let { siteToMap(it, companyId, uid) }
                "TASK" -> db.scheduleTaskDao().getById(item.entityId)?.let { taskToMap(it, companyId, uid) }
                "EMPLOYEE" -> db.employeeDao().getById(item.entityId)?.let { employeeToMap(it, companyId, uid) }
                "EQUIPMENT" -> db.equipmentDao().getById(item.entityId)?.let { equipmentToMap(it, companyId, uid) }
                "EQUIPMENT_FAULT" -> db.equipmentFaultDao().getById(item.entityId)?.let { faultToMap(it, companyId, uid) }
                "MATERIAL" -> db.materialDao().getById(item.entityId)?.let { materialToMap(it, companyId, uid) }
                "EXPENSE" -> db.expenseDao().getById(item.entityId)?.let { expenseToMap(it, companyId, uid) }
                "INVOICE" -> db.invoiceDao().getById(item.entityId)?.let { invoiceToMap(it, companyId, uid) }
                "CLIENT" -> db.clientDao().getById(item.entityId)?.let { clientToMap(it, companyId, uid) }
                "SUPPLIER" -> db.supplierDao().getById(item.entityId)?.let { supplierToMap(it, companyId, uid) }
                "CHAT_CHANNEL" -> db.chatChannelDao().getById(item.entityId)?.let { channelToMap(it, companyId, uid) }
                "CHAT_MESSAGE" -> db.chatMessageDao().getById(item.entityId)?.let { msg ->
                    val channelCloudId = ensureCloudIdForLocal("CHAT_CHANNEL", msg.channelId, companyId)
                    messageToMap(msg, channelCloudId, companyId, uid)
                }
                else -> null
            } ?: return

            val docRef = firestore.document(docPath)
            if (existingMap != null && existingMap.syncedRemote && existingMap.companyId == companyId) {
                try {
                    docRef.update(FirestoreMappers.withUpdateTimestamps(payloadFields)).await()
                } catch (e: FirebaseFirestoreException) {
                    if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                        docRef.set(FirestoreMappers.withCreateTimestamps(payloadFields)).await()
                    } else {
                        throw e
                    }
                }
            } else {
                docRef.set(FirestoreMappers.withCreateTimestamps(payloadFields)).await()
            }

            db.cloudSyncMapDao().upsert(
                CloudSyncMapEntity(
                    entityType = item.entityType,
                    localId = item.entityId,
                    cloudId = cloudId,
                    companyId = companyId,
                    syncedRemote = true,
                    lastSyncedAt = System.currentTimeMillis()
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.WRITE, docPath, auth)
            throw e
        }
    }

    private suspend fun ensureCloudIdForLocal(entityType: String, localId: Long, companyId: String): String {
        val existing = db.cloudSyncMapDao().getByLocal(entityType, localId)
        if (existing != null && existing.companyId == companyId) return existing.cloudId
        val generated = newDocId()
        db.cloudSyncMapDao().upsert(
            CloudSyncMapEntity(
                entityType = entityType,
                localId = localId,
                cloudId = generated,
                companyId = companyId,
                syncedRemote = false
            )
        )
        return generated
    }

    private suspend fun uploadLocalDatabaseSnapshotToCloud(companyId: String, uid: String) {
        val batchItems = mutableListOf<Pair<String, Long>>()
        db.projectDao().getAll().forEach { batchItems += "PROJECT" to it.id }
        db.siteDao().getAll().forEach { batchItems += "SITE" to it.id }
        db.scheduleTaskDao().getAll().forEach { batchItems += "TASK" to it.id }
        db.employeeDao().getAll().forEach { batchItems += "EMPLOYEE" to it.id }
        db.equipmentDao().getAll().forEach { batchItems += "EQUIPMENT" to it.id }
        db.materialDao().getAll().forEach { batchItems += "MATERIAL" to it.id }
        db.clientDao().getAll().forEach { batchItems += "CLIENT" to it.id }
        db.supplierDao().getAll().forEach { batchItems += "SUPPLIER" to it.id }
        db.expenseDao().getAll().forEach { batchItems += "EXPENSE" to it.id }
        db.invoiceDao().getAll().forEach { batchItems += "INVOICE" to it.id }
        db.chatChannelDao().getAll().forEach { batchItems += "CHAT_CHANNEL" to it.id }
        db.chatMessageDao().getAll().forEach { batchItems += "CHAT_MESSAGE" to it.id }

        for ((type, id) in batchItems) {
            try {
                pushSingleOutboxItem(
                    SyncOutboxEntity(entityType = type, entityId = id, operation = "CREATE", payloadSummary = type),
                    companyId,
                    uid
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Skipped initial snapshot push for $type/$id: ${e.message}")
            }
        }
    }

    fun startCompanyListeners(companyId: String, role: String, uid: String) {
        stopCloudSync()
        _workspaceState.value = _workspaceState.value.copy(
            isGoogleSignedIn = true,
            firebaseUid = uid,
            companyId = companyId,
            needsWorkspaceSetup = false
        )

        val companyRef = firestore.collection("companies").document(companyId)

        val regCompany = companyRef.addSnapshotListener(MetadataChanges.INCLUDE) { snap, err ->
            if (err != null) {
                FirestoreErrorHandler.handleFirestoreError(err, OperationType.GET, "companies/$companyId", auth)
                return@addSnapshotListener
            }
            val company = snap?.toObject(FsCompany::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: return@addSnapshotListener
            scope.launch {
                sessionManager.setCloudSession(
                    firebaseUid = uid,
                    companyId = companyId,
                    inviteCode = company.inviteCode,
                    companyName = company.name,
                    currencySymbol = company.currencySymbol
                )
                _workspaceState.value = _workspaceState.value.copy(
                    isGoogleSignedIn = true,
                    firebaseUid = uid,
                    companyId = companyId,
                    companyName = company.name,
                    inviteCode = company.inviteCode,
                    isOwner = company.ownerId == uid,
                    needsWorkspaceSetup = false
                )
            }
        }

        val regMembers = companyRef.collection("members").addSnapshotListener { snap, err ->
            if (err != null) {
                FirestoreErrorHandler.handleFirestoreError(err, OperationType.LIST, "companies/$companyId/members", auth)
                return@addSnapshotListener
            }
            val members = snap?.documents?.mapNotNull {
                it.toObject(FsCompanyMember::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }.orEmpty()
            _companyMembers.value = members
            // Keep own local Room role synced if owner changed it
            val myMember = members.firstOrNull { it.uid == uid }
            if (myMember != null) {
                scope.launch {
                    val roomUser = db.userAccountDao().getByUsername("g_${uid.take(12)}")
                    if (roomUser != null && roomUser.role != myMember.role) {
                        db.userAccountDao().update(roomUser.copy(role = myMember.role, isActive = myMember.active))
                    }
                }
            }
        }

        val regEmployees = companyRef.collection("employees").addSnapshotListener { snap, err ->
            if (err != null) {
                FirestoreErrorHandler.handleFirestoreError(err, OperationType.LIST, "companies/$companyId/employees", auth)
                return@addSnapshotListener
            }
            val docs = snap?.documents ?: return@addSnapshotListener
            scope.launch {
                docs.forEach { d ->
                    val fs = d.toObject(FsEmployee::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: return@forEach
                    val existing = db.cloudSyncMapDao().getByCloud(companyId, "EMPLOYEE", d.id)
                    val entity = EmployeeEntity(
                        id = existing?.localId ?: 0L,
                        fullName = fs.fullName,
                        role = fs.role,
                        phone = fs.phone,
                        nationalId = fs.nationalId,
                        dailyWage = fs.dailyWage,
                        assignedSiteName = fs.assignedSiteName,
                        attendanceStatus = fs.attendanceStatus,
                        hireDate = fs.hireDate,
                        isActive = fs.active
                    )
                    if (existing != null) {
                        db.employeeDao().update(entity)
                    } else {
                        val localId = db.employeeDao().insert(entity)
                        db.cloudSyncMapDao().upsert(CloudSyncMapEntity("EMPLOYEE", localId, d.id, companyId, true))
                    }
                }
            }
        }

        val regProjects = companyRef.collection("projects").addSnapshotListener { snap, err ->
            if (err != null) {
                FirestoreErrorHandler.handleFirestoreError(err, OperationType.LIST, "companies/$companyId/projects", auth)
                return@addSnapshotListener
            }
            val docs = snap?.documents ?: return@addSnapshotListener
            scope.launch {
                docs.forEach { d ->
                    val fs = d.toObject(FsProject::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: return@forEach
                    val existing = db.cloudSyncMapDao().getByCloud(companyId, "PROJECT", d.id)
                    val entity = ProjectEntity(
                        id = existing?.localId ?: 0L,
                        code = fs.code,
                        name = fs.name,
                        clientName = fs.clientName,
                        location = fs.location,
                        budget = fs.budget,
                        spentAmount = fs.spentAmount,
                        startDate = fs.startDate,
                        endDate = fs.endDate,
                        progressPercent = fs.progressPercent,
                        status = fs.status,
                        description = fs.description
                    )
                    if (existing != null) db.projectDao().update(entity) else {
                        val localId = db.projectDao().insert(entity)
                        db.cloudSyncMapDao().upsert(CloudSyncMapEntity("PROJECT", localId, d.id, companyId, true))
                    }
                }
            }
        }

        val regSites = companyRef.collection("sites").addSnapshotListener { snap, err ->
            if (err != null) {
                FirestoreErrorHandler.handleFirestoreError(err, OperationType.LIST, "companies/$companyId/sites", auth)
                return@addSnapshotListener
            }
            val docs = snap?.documents ?: return@addSnapshotListener
            scope.launch {
                docs.forEach { d ->
                    val fs = d.toObject(FsSite::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: return@forEach
                    val existing = db.cloudSyncMapDao().getByCloud(companyId, "SITE", d.id)
                    val entity = SiteEntity(
                        id = existing?.localId ?: 0L,
                        projectId = null,
                        projectName = fs.projectName,
                        name = fs.name,
                        address = fs.address,
                        supervisorName = fs.supervisorName,
                        workerCount = fs.workerCount,
                        safetyStatus = fs.safetyStatus,
                        status = fs.status,
                        dailyNote = fs.dailyNote
                    )
                    if (existing != null) db.siteDao().update(entity) else {
                        val localId = db.siteDao().insert(entity)
                        db.cloudSyncMapDao().upsert(CloudSyncMapEntity("SITE", localId, d.id, companyId, true))
                    }
                }
            }
        }

        val regTasks = companyRef.collection("tasks").addSnapshotListener { snap, err ->
            if (err != null) {
                FirestoreErrorHandler.handleFirestoreError(err, OperationType.LIST, "companies/$companyId/tasks", auth)
                return@addSnapshotListener
            }
            val docs = snap?.documents ?: return@addSnapshotListener
            scope.launch {
                docs.forEach { d ->
                    val fs = d.toObject(FsTask::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: return@forEach
                    val existing = db.cloudSyncMapDao().getByCloud(companyId, "TASK", d.id)
                    val entity = ScheduleTaskEntity(
                        id = existing?.localId ?: 0L,
                        title = fs.title,
                        projectName = fs.projectName,
                        siteName = fs.siteName,
                        assignedTeam = fs.assignedTeam,
                        startDate = fs.startDate,
                        dueDate = fs.dueDate,
                        priority = fs.priority,
                        isCompleted = fs.completed,
                        progress = fs.progress,
                        assignedToUserId = null
                    )
                    if (existing != null) db.scheduleTaskDao().update(entity) else {
                        val localId = db.scheduleTaskDao().insert(entity)
                        db.cloudSyncMapDao().upsert(CloudSyncMapEntity("TASK", localId, d.id, companyId, true))
                    }
                }
            }
        }

        val regEquipment = companyRef.collection("equipment").addSnapshotListener { snap, err ->
            if (err != null) {
                FirestoreErrorHandler.handleFirestoreError(err, OperationType.LIST, "companies/$companyId/equipment", auth)
                return@addSnapshotListener
            }
            val docs = snap?.documents ?: return@addSnapshotListener
            scope.launch {
                docs.forEach { d ->
                    val fs = d.toObject(FsEquipment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: return@forEach
                    val existing = db.cloudSyncMapDao().getByCloud(companyId, "EQUIPMENT", d.id)
                    val entity = EquipmentEntity(
                        id = existing?.localId ?: 0L,
                        name = fs.name,
                        code = fs.code,
                        category = fs.category,
                        status = fs.status,
                        assignedSiteName = fs.assignedSiteName,
                        nextMaintenanceDate = fs.nextMaintenanceDate,
                        hourlyCost = fs.hourlyCost
                    )
                    if (existing != null) db.equipmentDao().update(entity) else {
                        val localId = db.equipmentDao().insert(entity)
                        db.cloudSyncMapDao().upsert(CloudSyncMapEntity("EQUIPMENT", localId, d.id, companyId, true))
                    }
                }
            }
        }

        val regMaterials = companyRef.collection("materials").addSnapshotListener { snap, err ->
            if (err != null) {
                FirestoreErrorHandler.handleFirestoreError(err, OperationType.LIST, "companies/$companyId/materials", auth)
                return@addSnapshotListener
            }
            val docs = snap?.documents ?: return@addSnapshotListener
            scope.launch {
                docs.forEach { d ->
                    val fs = d.toObject(FsMaterial::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: return@forEach
                    val existing = db.cloudSyncMapDao().getByCloud(companyId, "MATERIAL", d.id)
                    val entity = MaterialEntity(
                        id = existing?.localId ?: 0L,
                        name = fs.name,
                        sku = fs.sku,
                        unit = fs.unit,
                        quantityInStock = fs.quantityInStock,
                        minThreshold = fs.minThreshold,
                        unitPrice = fs.unitPrice,
                        supplierName = fs.supplierName,
                        warehouseLocation = fs.warehouseLocation
                    )
                    if (existing != null) db.materialDao().update(entity) else {
                        val localId = db.materialDao().insert(entity)
                        db.cloudSyncMapDao().upsert(CloudSyncMapEntity("MATERIAL", localId, d.id, companyId, true))
                    }
                }
            }
        }

        val regChannels = companyRef.collection("chat_channels").addSnapshotListener { snap, err ->
            if (err != null) {
                FirestoreErrorHandler.handleFirestoreError(err, OperationType.LIST, "companies/$companyId/chat_channels", auth)
                return@addSnapshotListener
            }
            val docs = snap?.documents ?: return@addSnapshotListener
            scope.launch {
                docs.forEach { d ->
                    val fs = d.toObject(FsChatChannel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: return@forEach
                    val existing = db.cloudSyncMapDao().getByCloud(companyId, "CHAT_CHANNEL", d.id)
                    val entity = ChatChannelEntity(
                        id = existing?.localId ?: 0L,
                        name = fs.name,
                        channelType = fs.channelType,
                        projectName = fs.projectName,
                        participantIdsCsv = fs.participantUidsCsv,
                        lastMessagePreview = fs.lastMessagePreview,
                        lastMessageAt = fs.lastMessageAtMs,
                        unreadCount = fs.unreadCount
                    )
                    if (existing != null) db.chatChannelDao().update(entity) else {
                        val localId = db.chatChannelDao().insert(entity)
                        db.cloudSyncMapDao().upsert(CloudSyncMapEntity("CHAT_CHANNEL", localId, d.id, companyId, true))
                    }
                }
            }
        }

        val regMessages = companyRef.collection("chat_messages").addSnapshotListener { snap, err ->
            if (err != null) {
                FirestoreErrorHandler.handleFirestoreError(err, OperationType.LIST, "companies/$companyId/chat_messages", auth)
                return@addSnapshotListener
            }
            val docs = snap?.documents ?: return@addSnapshotListener
            scope.launch {
                docs.forEach { d ->
                    val fs = d.toObject(FsChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: return@forEach
                    val existing = db.cloudSyncMapDao().getByCloud(companyId, "CHAT_MESSAGE", d.id)
                    var localChannelId = db.cloudSyncMapDao().getByCloud(companyId, "CHAT_CHANNEL", fs.channelCloudId)?.localId
                    if (localChannelId == null) {
                        val chSnap = companyRef.collection("chat_channels").document(fs.channelCloudId).get().await()
                        val fsCh = chSnap.toObject(FsChatChannel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        if (fsCh != null) {
                            val chId = db.chatChannelDao().insert(
                                ChatChannelEntity(
                                    name = fsCh.name,
                                    channelType = fsCh.channelType,
                                    projectName = fsCh.projectName,
                                    participantIdsCsv = fsCh.participantUidsCsv,
                                    lastMessagePreview = fsCh.lastMessagePreview,
                                    lastMessageAt = fsCh.lastMessageAtMs,
                                    unreadCount = fsCh.unreadCount
                                )
                            )
                            db.cloudSyncMapDao().upsert(CloudSyncMapEntity("CHAT_CHANNEL", chId, fs.channelCloudId, companyId, true))
                            localChannelId = chId
                        }
                    }
                    val resolvedChannelId = localChannelId ?: return@forEach
                    val entity = ChatMessageEntity(
                        id = existing?.localId ?: 0L,
                        channelId = resolvedChannelId,
                        senderUserId = if (fs.senderUid == uid) 1L else 2L,
                        senderName = fs.senderName,
                        senderRole = fs.senderRole,
                        content = fs.content,
                        attachmentType = fs.attachmentType,
                        attachmentName = fs.attachmentName,
                        attachmentUri = fs.attachmentUri,
                        timestamp = fs.timestampMs,
                        isRead = fs.read,
                        syncStatus = "SYNCED"
                    )
                    if (existing != null) db.chatMessageDao().update(entity) else {
                        val localId = db.chatMessageDao().insert(entity)
                        db.cloudSyncMapDao().upsert(CloudSyncMapEntity("CHAT_MESSAGE", localId, d.id, companyId, true))
                    }
                }
            }
        }

        val newRegs = mutableListOf(
            regCompany, regMembers, regEmployees, regProjects, regSites,
            regTasks, regEquipment, regMaterials, regChannels, regMessages
        )

        // Finance-restricted listeners only attached for permitted roles (matches firestore.rules)
        if (role == "مدير عام" || role == "محاسب مالي") {
            newRegs += companyRef.collection("expenses").addSnapshotListener { snap, err ->
                if (err != null) {
                    FirestoreErrorHandler.handleFirestoreError(err, OperationType.LIST, "companies/$companyId/expenses", auth)
                    return@addSnapshotListener
                }
                val docs = snap?.documents ?: return@addSnapshotListener
                scope.launch {
                    docs.forEach { d ->
                        val fs = d.toObject(FsExpense::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: return@forEach
                        val existing = db.cloudSyncMapDao().getByCloud(companyId, "EXPENSE", d.id)
                        val entity = ExpenseEntity(
                            id = existing?.localId ?: 0L,
                            title = fs.title,
                            category = fs.category,
                            amount = fs.amount,
                            date = fs.date,
                            projectName = fs.projectName,
                            paymentMethod = fs.paymentMethod,
                            notes = fs.notes
                        )
                        if (existing != null) db.expenseDao().update(entity) else {
                            val localId = db.expenseDao().insert(entity)
                            db.cloudSyncMapDao().upsert(CloudSyncMapEntity("EXPENSE", localId, d.id, companyId, true))
                        }
                    }
                }
            }
            newRegs += companyRef.collection("invoices").addSnapshotListener { snap, err ->
                if (err != null) {
                    FirestoreErrorHandler.handleFirestoreError(err, OperationType.LIST, "companies/$companyId/invoices", auth)
                    return@addSnapshotListener
                }
                val docs = snap?.documents ?: return@addSnapshotListener
                scope.launch {
                    docs.forEach { d ->
                        val fs = d.toObject(FsInvoice::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: return@forEach
                        val existing = db.cloudSyncMapDao().getByCloud(companyId, "INVOICE", d.id)
                        val entity = InvoiceEntity(
                            id = existing?.localId ?: 0L,
                            invoiceNumber = fs.invoiceNumber,
                            type = fs.type,
                            partyName = fs.partyName,
                            projectName = fs.projectName,
                            totalAmount = fs.totalAmount,
                            paidAmount = fs.paidAmount,
                            issueDate = fs.issueDate,
                            dueDate = fs.dueDate,
                            status = fs.status
                        )
                        if (existing != null) db.invoiceDao().update(entity) else {
                            val localId = db.invoiceDao().insert(entity)
                            db.cloudSyncMapDao().upsert(CloudSyncMapEntity("INVOICE", localId, d.id, companyId, true))
                        }
                    }
                }
            }
        }

        synchronized(listenerLock) {
            activeListeners.addAll(newRegs)
        }
    }

    private fun projectToMap(e: ProjectEntity, companyId: String, uid: String) = mapOf(
        "code" to e.code,
        "name" to e.name,
        "clientName" to e.clientName,
        "location" to e.location,
        "budget" to e.budget,
        "spentAmount" to e.spentAmount,
        "startDate" to e.startDate,
        "endDate" to e.endDate,
        "progressPercent" to e.progressPercent,
        "status" to e.status,
        "description" to e.description,
        "companyId" to companyId,
        "updatedBy" to uid
    )

    private fun siteToMap(e: SiteEntity, companyId: String, uid: String) = mapOf(
        "projectName" to e.projectName,
        "name" to e.name,
        "address" to e.address,
        "supervisorName" to e.supervisorName,
        "workerCount" to e.workerCount,
        "safetyStatus" to e.safetyStatus,
        "status" to e.status,
        "dailyNote" to e.dailyNote,
        "companyId" to companyId,
        "updatedBy" to uid
    )

    private fun taskToMap(e: ScheduleTaskEntity, companyId: String, uid: String) = mapOf(
        "title" to e.title,
        "projectName" to e.projectName,
        "siteName" to e.siteName,
        "assignedTeam" to e.assignedTeam,
        "startDate" to e.startDate,
        "dueDate" to e.dueDate,
        "priority" to e.priority,
        "completed" to e.isCompleted,
        "progress" to e.progress,
        "assignedToUid" to uid,
        "companyId" to companyId,
        "updatedBy" to uid
    )

    private fun employeeToMap(e: EmployeeEntity, companyId: String, uid: String) = mapOf(
        "fullName" to e.fullName,
        "role" to e.role,
        "phone" to e.phone,
        "nationalId" to e.nationalId,
        "dailyWage" to e.dailyWage,
        "assignedSiteName" to e.assignedSiteName,
        "attendanceStatus" to e.attendanceStatus,
        "hireDate" to e.hireDate,
        "active" to e.isActive,
        "companyId" to companyId,
        "updatedBy" to uid
    )

    private fun equipmentToMap(e: EquipmentEntity, companyId: String, uid: String) = mapOf(
        "name" to e.name,
        "code" to e.code,
        "category" to e.category,
        "status" to e.status,
        "assignedSiteName" to e.assignedSiteName,
        "nextMaintenanceDate" to e.nextMaintenanceDate,
        "hourlyCost" to e.hourlyCost,
        "companyId" to companyId,
        "updatedBy" to uid
    )

    private fun faultToMap(e: EquipmentFaultEntity, companyId: String, uid: String) = mapOf(
        "equipmentName" to e.equipmentName,
        "siteName" to e.siteName,
        "severity" to e.severity,
        "description" to e.description,
        "reportedByUid" to uid,
        "reportedByName" to e.reportedByName,
        "status" to e.status,
        "companyId" to companyId,
        "updatedBy" to uid
    )

    private fun materialToMap(e: MaterialEntity, companyId: String, uid: String) = mapOf(
        "name" to e.name,
        "sku" to e.sku,
        "unit" to e.unit,
        "quantityInStock" to e.quantityInStock,
        "minThreshold" to e.minThreshold,
        "unitPrice" to e.unitPrice,
        "supplierName" to e.supplierName,
        "warehouseLocation" to e.warehouseLocation,
        "companyId" to companyId,
        "updatedBy" to uid
    )

    private fun expenseToMap(e: ExpenseEntity, companyId: String, uid: String) = mapOf(
        "title" to e.title,
        "category" to e.category,
        "amount" to e.amount,
        "date" to e.date,
        "projectName" to e.projectName,
        "paymentMethod" to e.paymentMethod,
        "notes" to e.notes,
        "companyId" to companyId,
        "updatedBy" to uid
    )

    private fun invoiceToMap(e: InvoiceEntity, companyId: String, uid: String) = mapOf(
        "invoiceNumber" to e.invoiceNumber,
        "type" to e.type,
        "partyName" to e.partyName,
        "projectName" to e.projectName,
        "totalAmount" to e.totalAmount,
        "paidAmount" to e.paidAmount,
        "issueDate" to e.issueDate,
        "dueDate" to e.dueDate,
        "status" to e.status,
        "companyId" to companyId,
        "updatedBy" to uid
    )

    private fun clientToMap(e: ClientEntity, companyId: String, uid: String) = mapOf(
        "name" to e.name,
        "companyName" to e.companyName,
        "phone" to e.phone,
        "email" to e.email,
        "address" to e.address,
        "taxId" to e.taxId,
        "notes" to e.notes,
        "companyId" to companyId,
        "updatedBy" to uid
    )

    private fun supplierToMap(e: SupplierEntity, companyId: String, uid: String) = mapOf(
        "name" to e.name,
        "category" to e.category,
        "phone" to e.phone,
        "email" to e.email,
        "address" to e.address,
        "balanceOwed" to e.balanceOwed,
        "notes" to e.notes,
        "companyId" to companyId,
        "updatedBy" to uid
    )

    private fun channelToMap(e: ChatChannelEntity, companyId: String, uid: String) = mapOf(
        "name" to e.name,
        "channelType" to e.channelType,
        "projectName" to e.projectName,
        "participantUidsCsv" to e.participantIdsCsv,
        "lastMessagePreview" to e.lastMessagePreview,
        "lastMessageAtMs" to e.lastMessageAt,
        "unreadCount" to e.unreadCount,
        "companyId" to companyId,
        "updatedBy" to uid
    )

    private fun messageToMap(
        e: ChatMessageEntity,
        channelCloudId: String,
        companyId: String,
        uid: String
    ) = mapOf(
        "channelCloudId" to channelCloudId,
        "senderUid" to uid,
        "senderName" to e.senderName,
        "senderRole" to e.senderRole,
        "content" to e.content,
        "attachmentType" to e.attachmentType,
        "attachmentName" to e.attachmentName,
        "attachmentUri" to e.attachmentUri,
        "timestampMs" to e.timestamp,
        "read" to e.isRead,
        "companyId" to companyId,
        "updatedBy" to uid
    )
}
