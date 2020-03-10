package com.jungbae.mask.view

import android.content.Context
import android.graphics.Rect
import android.view.TouchDelegate
import android.view.View
import android.widget.Toast

//fun Activity.increaseTouchArea(view: View, increaseBy: Int) {
//    val parent = view.parent
//    (parent as View).post({
//        val rect = Rect()
//        view.getHitRect(rect)
//        val intValue = increaseBy.toInt()
//        rect.top -= intValue    // increase top hit area
//        rect.left -= intValue   // increase left hit area
//        rect.bottom += intValue // increase bottom hit area
//        rect.right += intValue  // increase right hit area
//        parent.setTouchDelegate(TouchDelegate(rect, view));
//    });
//}

fun View.increaseTouchArea(view: View, increaseBy: Int) {
    val parent = view.parent
    (parent as View).post({
        val rect = Rect()
        view.getHitRect(rect)
        val intValue = increaseBy.toInt()
        rect.top -= intValue    // increase top hit area
        rect.left -= intValue   // increase left hit area
        rect.bottom += intValue // increase bottom hit area
        rect.right += intValue  // increase right hit area
        parent.setTouchDelegate(TouchDelegate(rect, view));
    });
}

fun Context.increaseTouchArea(view: View, increaseBy: Int) {
    val parent = view.parent
    (parent as View).post {
        val rect = Rect().apply {
            top -= increaseBy
            left -= increaseBy
            bottom += increaseBy
            right += increaseBy
        }
        view.getHitRect(rect)
        parent.setTouchDelegate(TouchDelegate(rect, view));
    }
}

fun Context.showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}