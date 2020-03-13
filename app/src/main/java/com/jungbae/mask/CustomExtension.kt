package com.jungbae.mask

import android.content.Context
import android.util.Log
import android.widget.Toast
import okio.internal.commonAsUtf8ToByteArray
import android.view.ViewGroup
import androidx.core.view.children


fun String.UTF8(): String? {
    var str = ""
    this.commonAsUtf8ToByteArray().map {
        val st = String.format("%02X", it)
        str += "%" + st
    }
    Log.e("@@@","@@@ str $str")
    return str
}

fun Int.True(): Boolean {
    return this == 1
}

fun Context.showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun ViewGroup.enableDisableViewGroup(enabled: Boolean) {
    val childCount = this.childCount
    for (i in 0 until childCount) {
        val view = this.getChildAt(i)
        view.isEnabled = enabled
        if (view is ViewGroup) {
            view.enableDisableViewGroup(enabled)
        }
    }
}