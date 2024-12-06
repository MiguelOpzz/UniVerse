package com.clerami.universe.ui.register

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.CustomCredential
import com.clerami.universe.R
import com.clerami.universe.utils.SessionManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleSignIn(
    private val context: Context,
    private val sessionManager: SessionManager // Pass sessionManager as a parameter
) {
    private val tag = "Google Sign Client"
    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth = FirebaseAuth.getInstance()

    // Check if user is already signed in
    private fun isSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    // Sign-in function
    suspend fun signIn(): Boolean {
        if (isSignedIn()) {
            Log.d(tag, "Already signed in")
            return true
        }

        try {
            // Step 1: Get credentials from Google
            val result = buildCredentialRequest()

            // Step 2: Handle the sign-in response and validate
            return handleSignIn(result)
        } catch (e: Exception) {
            Log.e(tag, "Sign-in error: ${e.message}", e)
            if (e is CancellationException) throw e
            return false
        }
    }

    // Handle the credentials returned by Google
    private suspend fun handleSignIn(result: GetCredentialResponse): Boolean {
        val credential = result.credential

        // Ensure the credential is a valid Google ID Token
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                // Parse the ID Token
                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                Log.d(tag, "User Info: Name = ${tokenCredential.displayName}, Email = ${tokenCredential.id}, Image = ${tokenCredential.profilePictureUri}")

                // Step 3: Sign in to Firebase with the obtained ID Token
                val authCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()

                // After successful sign-in, save user info to session
                if (authResult.user != null) {
                    saveUserInfoToSession(tokenCredential)
                }

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

    // Save user information to the session manager after successful sign-in
    private fun saveUserInfoToSession(tokenCredential: GoogleIdTokenCredential) {
        val token = tokenCredential.idToken
        val email = tokenCredential.id ?: ""
        val username = tokenCredential.displayName ?: "Guest"
        val profileImageUri = tokenCredential.profilePictureUri?.toString() ?: ""

        // Save the data to SessionManager
        sessionManager.saveSession(token = token ?: "", email = email, username = username)

        // Optionally, you can store profile picture URI if you need it
        Log.d(tag, "User profile picture: $profileImageUri")
        Log.d(tag,"$token")
    }

    // Build the request for getting credentials from Google
    private suspend fun buildCredentialRequest(): GetCredentialResponse {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)  // Disable filtering by authorized accounts
                    .setServerClientId(context.getString(R.string.default_web_client_id)) // Your server client ID from Firebase
                    .setAutoSelectEnabled(false) // Disable auto-select to prompt user explicitly
                    .build()
            )
            .build()

        return credentialManager.getCredential(request = request, context = context)
    }

    // Sign out the user and clear session data
    suspend fun signOut() {
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        firebaseAuth.signOut()
        sessionManager.clearSession()  // Clear session data after sign-out
    }
}
