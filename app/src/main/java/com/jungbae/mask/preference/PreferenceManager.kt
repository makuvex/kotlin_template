package com.jungbae.mask.network.preference

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.rxkprefs.RxkPrefs
import com.afollestad.rxkprefs.rxkPrefs
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.jungbae.mask.CommonApplication




object PreferencesConstant {
    val fcm_token   = "FCM_TOKEN"
    val user_seq    = "USER_SEQUENCE"

    val PERMISSION_NOTICE = "PERMISSION_NOTICE"
}

class PreferenceManager {

    companion object {
        private var instance: RxkPrefs? = null
        private val self: PreferenceManager = PreferenceManager()
        //private lateinit var schoolCodeList: MutableList<SimpleSchoolData>

        init {
            Log.e("@@@","@@@ PreferenceManager init")
            instance = instance ?: rxkPrefs(CommonApplication.context, CommonApplication.context.packageName, AppCompatActivity.MODE_PRIVATE)
        }

        @JvmStatic
        var fcmToken: String?
            get() {
                instance?.run {
                    return string(PreferencesConstant.fcm_token, "").get()
                }
                return null
            }
            set(data) {
                instance?.let {} ?: return
                data?.let {
                    instance?.string(PreferencesConstant.fcm_token, "")?.set(it)
                }
            }

        @JvmStatic
        var userSeq: Int?
            get() {
                instance?.run {
                    return integer(PreferencesConstant.user_seq, -1).get()
                }
                return null
            }
            set(data) {
                instance?.let {} ?: return
                data?.let {
                    instance?.integer(PreferencesConstant.user_seq, -1)?.set(it)
                }
            }

        @JvmStatic
        var permissionNotice: Boolean
            get() {
                instance?.run {
                    return boolean(PreferencesConstant.PERMISSION_NOTICE, false).get()
                }
                return false
            }
            set(value) {
                instance?.let {
                    it.boolean(PreferencesConstant.PERMISSION_NOTICE, false).set(value)
                }
            }
    }

}



