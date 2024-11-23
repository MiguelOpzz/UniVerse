package com.clerami.universe.ui.register

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.clerami.universe.R
import com.clerami.universe.databinding.ActivityRegisterBinding
import com.clerami.universe.ui.login.LoginActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setUpClickableSpan()

    }

    private fun setUpClickableSpan(){
        val text =binding.alreadyHaveAccount.text.toString()
        val spannableString = SpannableString(text)
        val loginText = "Login"
        val startIndex = text.indexOf(loginText)
        val endIndex = startIndex + loginText.length


        val clickableSpan = object : ClickableSpan(){
            override fun onClick(widget: View) {
                val intent = Intent(this@RegisterActivity, LoginActivity ::class.java)
                startActivity(intent)
                finish()
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = resources.getColor(R.color.blue_four, theme)
                ds.isUnderlineText = true
            }
        }

        spannableString.setSpan(clickableSpan,startIndex,endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.alreadyHaveAccount.text = spannableString
        binding.alreadyHaveAccount.movementMethod= LinkMovementMethod.getInstance()
    }
}
