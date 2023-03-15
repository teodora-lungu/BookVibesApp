package com.example.bookvibes

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        //hide action bar
        supportActionBar?.hide()

        //Animations
        val topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        val bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)

        val image = findViewById<ImageView>(R.id.BookView)
        val txt1 = findViewById<TextView>(R.id.textView2)
        val txt2 = findViewById<TextView>(R.id.textView3)
        val txt3 = findViewById<TextView>(R.id.textView4)

        image.startAnimation(bottomAnim)
        txt1.startAnimation(topAnim)
        txt2.startAnimation(topAnim)
        txt3.startAnimation(topAnim)


        val updateHandler = Handler().postDelayed(Runnable {
                // go to Login screen
            // TODO() change class

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)

        }, 5000)


    }
}
