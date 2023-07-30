package com.example.test.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test.R
import com.example.test.application.SharedManager
import com.example.test.ui.intro.IntroActivity
import com.example.test.ui.main.MainActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val sharedManager = SharedManager.getInstance()

        if (sharedManager.getCurrentUser()?.username?.isNotBlank() == true
            && sharedManager.getBearerToken().isNotBlank()
        ) {
            Handler(Looper.getMainLooper()).postDelayed({
                Toast.makeText(
                    this,
                    "${sharedManager.getCurrentUser()?.username}님 자동 로그인 되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 2000)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, IntroActivity::class.java))
                finish()
            }, 2000)
        }
    }
}
