package com.clerami.universe.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.clerami.universe.MainActivity
import com.clerami.universe.R
import com.clerami.universe.databinding.ActivityLoginBinding
import com.clerami.universe.ui.register.RegisterActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        updateLoginButtonState(false)


        binding.username.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isUsernameFilled = !s.isNullOrEmpty()
                updateLoginButtonState(isUsernameFilled)


                if (isUsernameFilled) {
                    fadeOut(binding.usernameError)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        binding.login.setOnClickListener {
            val username = binding.username.text.toString()
            if (username.isEmpty()) {

                fadeIn(binding.usernameError)
                fadeIn(binding.usernameError)
            } else {

                binding.loading.visibility = View.VISIBLE
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }


        setupSignUpClickableSpan()
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
        }

        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.dontHaveAccount.text = spannableString
        binding.dontHaveAccount.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun updateLoginButtonState(isEnabled: Boolean) {

        val buttonColor = if (isEnabled) {
            ContextCompat.getColor(this, R.color.blue_four)
        } else {
            ContextCompat.getColor(this, R.color.gray_two)
        }
        binding.login.setBackgroundColor(buttonColor)
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
