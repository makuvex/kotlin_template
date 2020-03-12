package com.jungbae.mask.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.jungbae.mask.R
import com.jungbae.mask.network.Store
import com.jungbae.mask.network.preference.PreferenceManager


class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            if(!PreferenceManager.permissionNotice) {
                showMaterialDialog()
            } else {
                moveMainActivity()
                finish()
            }
        }, 500)
    }

    fun moveMainActivity() {
        startActivity(Intent(this@IntroActivity, MainActivity::class.java)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun showMaterialDialog() {
        MaterialDialog(this).show {
            positiveButton(text = "동의하고 시작하기") {
                PreferenceManager.permissionNotice = true

                startActivity(Intent(this@IntroActivity, NoticeActivity::class.java)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })

                finish()
            }
            negativeButton(text = "종료하기") {
                finish()
            }
            onShow {
                title(text = getString(R.string.app_name) + " 앱 안내" )
                val msg = "\n필수 접근 권한 : 위치\n(현재 위치를 읽어와 근처 마스크 판매점 정보를 검색하는데 필요합니다.)"
                message(text = msg)
            }

        }
    }
}
