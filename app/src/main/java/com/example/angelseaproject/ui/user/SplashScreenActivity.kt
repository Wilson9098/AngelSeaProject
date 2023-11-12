package com.example.angelseaproject.ui.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.example.angelseaproject.R

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splashscreen1)

        // 获取ImageView
        val logoImageView = findViewById<ImageView>(R.id.logoImageView)
        // 设置ImageView初始为不可见
        logoImageView.alpha = 0f

        // 加载淡入动画资源
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        // 开始淡入动画
        logoImageView.startAnimation(fadeIn)
        // 在动画开始后，逐渐设置ImageView为可见
        logoImageView.animate().alpha(1f).setDuration(2000).setListener(null)

        // 使用Handler来延迟加载登录页面
        Handler().postDelayed({
            val intent = Intent(this@SplashScreenActivity, com.example.angelseaproject.ui.user.loginPage::class.java)
            startActivity(intent)
            finish()
        }, 2500) // 延迟时间，2000ms = 2秒
    }
}
