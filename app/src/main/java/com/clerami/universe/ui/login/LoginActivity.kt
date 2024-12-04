package com.clerami.universe.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.clerami.universe.MainActivity
import com.clerami.universe.R
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.clerami.universe.databinding.ActivityLoginBinding
import com.clerami.universe.ui.register.RegisterActivity
import com.clerami.universe.utils.Resource


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val apiService = ApiConfig.getApiService()
        loginViewModel = ViewModelProvider(this, LoginViewModelFactory(apiService)).get(LoginViewModel::class.java)

        setupSignUpClickableSpan()


        attachTextWatchers()


        binding.login.setOnClickListener {
            val usernameOrEmail = binding.username.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (usernameOrEmail.isEmpty()) {
                fadeIn(binding.usernameError)
            } else {
                fadeOut(binding.usernameError)
            }

            if (password.isEmpty()) {
                fadeIn(binding.passwordError)
            } else {
                fadeOut(binding.passwordError)
            }


            if (usernameOrEmail.isNotEmpty() && password.isNotEmpty()) {
                performLogin(usernameOrEmail, password)
            }
        }


        val successMessage = intent.getStringExtra("REGISTRATION_SUCCESS")
        if (!successMessage.isNullOrEmpty()) {
            Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun attachTextWatchers() {
        binding.username.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    fadeOut(binding.usernameError)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    fadeOut(binding.passwordError)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performLogin(usernameOrEmail: String, password: String) {
        binding.loading.visibility = View.VISIBLE

        loginViewModel.login(usernameOrEmail, password).observe(this) { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    binding.loading.visibility = View.GONE
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                Resource.Status.ERROR -> {
                    binding.loading.visibility = View.GONE
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
                Resource.Status.LOADING -> {
                    binding.loading.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupSignUpClickableSpan() {
        val text = binding.dontHaveAccount.text.toString()
        val spannableString = SpannableString(text)
        val signUpText = "Sign up"
        val startIndex = text.indexOf(signUpText)
        val endIndex = startIndex + signUpText.length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: android.text.TextPaint) {
                super.updateDrawState(ds)
                ds.color = resources.getColor(R.color.blue_four, theme)
                ds.isUnderlineText = true
            }
        }

        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.dontHaveAccount.text = spannableString
        binding.dontHaveAccount.movementMethod = android.text.method.LinkMovementMethod.getInstance()
    }

    private fun fadeIn(view: TextView) {
        view.visibility = View.VISIBLE
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(500)
            .setListener(null)
    }

    private fun fadeOut(view: TextView) {
        view.animate()
            .alpha(0f)
            .setDuration(500)
            .withEndAction {
                view.visibility = View.GONE
            }
    }
}