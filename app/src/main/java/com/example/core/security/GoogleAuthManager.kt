package com.example.core.security

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class GoogleSignInOutcome {
    data class Success(val user: FirebaseUser) : GoogleSignInOutcome()
    data object Cancelled : GoogleSignInOutcome()
    data object NoSavedCredential : GoogleSignInOutcome()
    data class Error(val message: String) : GoogleSignInOutcome()
}

class GoogleAuthManager(private val appContext: Context) {

    companion object {
        private const val TAG = "GoogleAuthManager"
    }

    private val credentialManager by lazy { CredentialManager.create(appContext) }

    val currentFirebaseUser: FirebaseUser?
        get() = Firebase.auth.currentUser

    suspend fun signInWithGoogle(activityContext: Context): GoogleSignInOutcome {
        return try {
            val webClientId = appContext.getString(R.string.default_web_client_id)
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )
            exchangeGoogleCredential(result.credential)
        } catch (_: GetCredentialCancellationException) {
            GoogleSignInOutcome.Cancelled
        } catch (e: Exception) {
            Log.e(TAG, "Explicit Google Sign-In failed", e)
            GoogleSignInOutcome.Error(e.message ?: "Google Sign-In failed")
        }
    }

    suspend fun attemptAutoSignIn(activityContext: Context): GoogleSignInOutcome {
        if (Firebase.auth.currentUser != null) {
            return GoogleSignInOutcome.Success(Firebase.auth.currentUser!!)
        }
        return try {
            val webClientId = appContext.getString(R.string.default_web_client_id)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )
            exchangeGoogleCredential(result.credential)
        } catch (_: NoCredentialException) {
            GoogleSignInOutcome.NoSavedCredential
        } catch (_: GetCredentialCancellationException) {
            GoogleSignInOutcome.Cancelled
        } catch (e: Exception) {
            Log.w(TAG, "Auto sign-in skipped", e)
            GoogleSignInOutcome.NoSavedCredential
        }
    }

    private suspend fun exchangeGoogleCredential(credential: androidx.credentials.Credential): GoogleSignInOutcome {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCred = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            val authResult = Firebase.auth.signInWithCredential(firebaseCred).await()
            val user = authResult.user
                ?: return GoogleSignInOutcome.Error("Firebase user was null after sign-in")
            return GoogleSignInOutcome.Success(user)
        }
        return GoogleSignInOutcome.Error("Unsupported credential type returned by Credential Manager")
    }

    suspend fun signOut(onStopCloudSync: suspend () -> Unit) {
        withContext(NonCancellable) {
            try {
                onStopCloudSync()
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping cloud sync during sign-out", e)
            }
            try {
                Firebase.auth.signOut()
            } catch (e: Exception) {
                Log.w(TAG, "Firebase signOut warning", e)
            }
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "ClearCredentialState warning", e)
            }
        }
    }
}
