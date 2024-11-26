package com.clerami.universe.ui.register

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.clerami.universe.R
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.clerami.universe.databinding.ActivityRegisterBinding
import com.clerami.universe.ui.login.LoginActivity
import com.clerami.universe.ui.viewmodel.RegisterViewModel
import com.clerami.universe.ui.viewmodel.RegisterViewModelFactory
import com.clerami.universe.utils.Resource


class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
        registerViewModel = ViewModelProvider(this, RegisterViewModelFactory(apiService)).get(RegisterViewModel::class.java)

        setUpClickableSpan()

        // Handle Register Button Click
        binding.register.setOnClickListener {
            handleRegister()
        }

    }


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
                    // Navigate to LoginActivity with a success message
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
