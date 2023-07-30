package com.example.test.ui.intro

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.test.R
import com.example.test.databinding.ActivityIntroBinding
import com.example.test.ui.main.MainActivity
import kotlinx.coroutines.launch

class IntroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIntroBinding
    private lateinit var viewModel: IntroViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_intro)
        viewModel = ViewModelProvider(this)[IntroViewModel::class.java]

        binding.btnlogin.setOnClickListener {
            val name = binding.emailArea.text.toString().trim()
            val password = binding.passwordArea.text.toString().trim()

            lifecycleScope.launch {
                try {
                    viewModel.login(name, password)

                    val intent = Intent(this@IntroActivity, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    startActivity(intent)
                    finish()

                } catch (_: Exception) {
                    Toast.makeText(
                        this@IntroActivity,
                        "아이디 또는 비밀번호를 다시 확인해 주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}



