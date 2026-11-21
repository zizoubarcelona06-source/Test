package com.example.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType {
    CREATE,
    UPDATE,
    DELETE,
    LIST,
    GET,
    WRITE
}

object FirestoreErrorHandler {
    private const val TAG = "FirestoreAuthError"

    fun handleFirestoreError(
        error: Exception,
        operationType: OperationType,
        path: String?,
        auth: FirebaseAuth
    ): String {
        val currentUser = auth.currentUser
        val providerArray = JSONArray()
        currentUser?.providerData?.forEach { profile ->
            providerArray.put(
                JSONObject().apply {
                    put("providerId", profile.providerId)
                    put("displayName", profile.displayName ?: JSONObject.NULL)
                    put("email", profile.email ?: JSONObject.NULL)
                    put("photoUrl", profile.photoUrl?.toString() ?: JSONObject.NULL)
                }
            )
        }
        val errJson = JSONObject().apply {
            put("error", error.message ?: "Unknown Firestore error")
            put("operationType", operationType.name.lowercase())
            put("path", path ?: JSONObject.NULL)
            put(
                "authInfo",
                JSONObject().apply {
                    put("userId", currentUser?.uid ?: JSONObject.NULL)
                    put("email", currentUser?.email ?: JSONObject.NULL)
                    put("emailVerified", currentUser?.isEmailVerified ?: false)
                    put("isAnonymous", currentUser?.isAnonymous ?: false)
                    put("tenantId", currentUser?.tenantId ?: JSONObject.NULL)
                    put("providerInfo", providerArray)
                }
            )
        }.toString()

        Log.e(TAG, "Firestore Error: $errJson", error)
        return errJson
    }
}
