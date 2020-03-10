package com.jungbae.mask.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.jungbae.mask.R


class IntroActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            moveMainActivity()
            finish()
        }, 500)
    }

    fun moveMainActivity() {
        startActivity(Intent(this@IntroActivity, MainActivity::class.java)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

}
