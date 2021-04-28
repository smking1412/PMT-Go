package com.shingetsu.mitago

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //hiede status bar and make full screen
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val logoImage : ImageView = findViewById(R.id.img_logo_splash)
        val logoAnimation = AnimationUtils.loadAnimation(this,R.anim.top_animation)
        logoImage.startAnimation(logoAnimation)

        val logoName : TextView = findViewById(R.id.tv_logo_name)
        val logoNameAnimation = AnimationUtils.loadAnimation(this,R.anim.bottom_animation)
        logoName.startAnimation(logoNameAnimation)

        Handler().postDelayed({
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        },2000)
    }
}