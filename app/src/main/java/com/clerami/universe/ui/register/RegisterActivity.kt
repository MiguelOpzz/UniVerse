package com.clerami.universe.ui.register

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.clerami.universe.MainActivity
import com.clerami.universe.R
import com.clerami.universe.databinding.ActivityRegisterBinding
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.clerami.universe.ui.login.LoginActivity
import com.clerami.universe.utils.Resource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up edge-to-edge window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize ViewModel with ApiConfig
        val apiService = ApiConfig.getApiService()
        registerViewModel = ViewModelProvider(
            this,
            RegisterViewModelFactory(apiService)
        ).get(RegisterViewModel::class.java)

        setUpClickableSpan()

        // Handle Register Button Click
        binding.register.setOnClickListener {
            handleRegister()
        }

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Make sure this is correct
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // Handle Google Sign-In Button Click
        binding.googleButton.setOnClickListener {
            googleSignIn() // Correct method call to start Google sign-in flow
        }
    }

    // Method to trigger Google Sign-In flow
    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    // Handling Google Sign-In result
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account) // Firebase authentication with Google account
                }
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Google Sign-In failed", e)
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }

    // Authenticate with Firebase using Google account credentials
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in successful, navigate to the next screen
                    Toast.makeText(this, "Google Sign-In Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Sign-in failed
                    Log.w("GoogleSignIn", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Handle registration logic (email/password)
    private fun handleRegister() {
        val email = binding.email.text.toString().trim()
        val username = binding.username.text.toString().trim()
        val password = binding.password.text.toString().trim()
        val confirmPassword = binding.confirmPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        registerViewModel.register(email, password, username).observe(this) { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    val intent = Intent(this, LoginActivity::class.java).apply {
                        putExtra("REGISTRATION_SUCCESS", "Account created successfully. Please log in.")
                    }
                    startActivity(intent)
                    finish()
                }
                Resource.Status.ERROR -> {
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
                Resource.Status.LOADING -> {
                    binding.loading.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setUpClickableSpan() {
        val text = binding.alreadyHaveAccount.text.toString()
        val spannableString = SpannableString(text)
        val loginText = "Login"
        val startIndex = text.indexOf(loginText)
        val endIndex = startIndex + loginText.length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = resources.getColor(R.color.blue_four, theme)
                ds.isUnderlineText = true
            }
        }

        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.alreadyHaveAccount.text = spannableString
        binding.alreadyHaveAccount.movementMethod = LinkMovementMethod.getInstance()
    }


}
