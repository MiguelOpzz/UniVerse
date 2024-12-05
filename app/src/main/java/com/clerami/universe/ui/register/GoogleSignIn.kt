package com.clerami.universe.ui.register

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.CustomCredential
import com.clerami.universe.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleSignIn(
    private val context: Context
) {
    private val tag = "Google Sign Client"
    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth = FirebaseAuth.getInstance()


    private fun isSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }


    suspend fun signIn(): Boolean {
        if (isSignedIn()) {
            Log.d(tag, "Already signed in")
            return true
        }

        try {
            // Step 1: Get credentials
            val result = buildCredentialRequest()

            // Step 2: Handle the sign-in response
            return handleSignIn(result)
        } catch (e: Exception) {
            Log.e(tag, "Sign-in error: ${e.message}", e)
            if (e is CancellationException) throw e
            return false
        }
    }


    private suspend fun handleSignIn(result: GetCredentialResponse): Boolean {
        val credential = result.credential

        // Ensure the credential is a Google ID Token credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                // Parse the ID Token
                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                Log.d(tag, "User Info: Name = ${tokenCredential.displayName}, Email = ${tokenCredential.id}, Image = ${tokenCredential.profilePictureUri}")

                // Step 3: Sign in to Firebase with the obtained ID Token
                val authCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()

                return authResult.user != null
            } catch (e: GoogleIdTokenParsingException) {
                Log.e(tag, "Google ID token parsing failed: ${e.message}", e)
                return false
            }
        } else {
            Log.e(tag, "Credential is not a valid Google ID token credential")
            return false
        }
    }


    private suspend fun buildCredentialRequest(): GetCredentialResponse {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)  // Disable filtering by authorized accounts
                    .setServerClientId(R.string.default_web_client_id.toString()) // Your server client ID
                    .setAutoSelectEnabled(false) // Disable auto-select to prompt user explicitly
                    .build()
            )
            .build()

        return credentialManager.getCredential(request = request, context = context)
    }


    suspend fun signOut() {
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        firebaseAuth.signOut()
    }
}
