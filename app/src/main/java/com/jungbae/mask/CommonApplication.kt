package com.jungbae.mask

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.messaging.FirebaseMessaging
import com.jungbae.mask.activity.MainActivity
import com.jungbae.mask.network.preference.PreferenceManager
import com.jungbae.mask.UTF8
import com.jungbae.mask.network.Store
import com.jungbae.mask.showToast
import kotlin.properties.Delegates

fun Int.isEqualHigherOreo(): Boolean {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return true
    }
    return false
}

class  CommonApplication : Application() {

    companion object {
        var notificationId: Int = 0
        var context: Context by Delegates.notNull()
            private set

        val androidId: String
            get() {
                var androidId = AdvertisingIdClient.getAdvertisingIdInfo(context).id
                Log.e("@@@","@@@ androidId $androidId")
                return androidId
            }

        lateinit var preferences: PreferenceManager

        fun sendNotification(data: Map<String, String>) {
            val name = data.get("name") as? String
            val body = data.get("body") as? String
            val stock_at = data.get("stock_at") as? String

            val lat = data.get("lat") as? String
            val lng = data.get("lng") as? String

            Log.e("@@@","@@@ sendNotification link $stock_at")
            val intent = Intent(context, MainActivity::class.java)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                val link: String = lat + "," + lng
                putExtra("link", link)
            }
            val pendingIntent = PendingIntent.getActivity(context,
                0 /* Request code */,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT)

            val channelId = context.getString(R.string.default_notification_channel_id)
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.man)
                .setContentTitle(name)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            @SuppressWarnings("ConstantConditions")
            if (Build.VERSION.SDK_INT.isEqualHigherOreo()) {
                val channel = NotificationChannel(channelId, context.getString(R.string.default_notification_channel_id), NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(notificationId /* ID of notification */, notificationBuilder.build())
            if(Int.MAX_VALUE <= notificationId) notificationId = 0 else notificationId += 1
        }

        fun subscribeTopic(topic: String) {
            //topic.UTF8()?.let {
                FirebaseMessaging.getInstance().subscribeToTopic(topic)
                    .addOnCompleteListener {
                        //context.showToast(topic + "구독 완료")

                    }
            //}
        }

        fun unsubscribeTopic(topic: String) {
            //topic.UTF8()?.let {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                    .addOnCompleteListener {
                        //context.showToast(topic + "구독 해지 완료")

                    }
            //}
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        preferences = PreferenceManager()
        //createNotificationChannel()

        // 에어팟2 -> %EC%97%90%EC%96%B4%ED%8C%9F2

//        var str = ""
//        val bytes = "에어팟".commonAsUtf8ToByteArray()
//        for (b in bytes) {
//            val st = String.format("%02X", b)
//            str += "%" + st
//
//        }
//
//        Log.e("@@@","@@@ str $str")
//        FirebaseMessaging.getInstance().subscribeToTopic("1234")
//            .addOnCompleteListener { task ->
//                var msg = "토픽"
//                if (!task.isSuccessful) {
//                    msg = "토픽 에러"
//                }
//
//                Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
//            }
    }

//    fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            val notificationChannel =
//                NotificationChannel("네모딜", "네모딜 알림", NotificationManager.IMPORTANCE_DEFAULT).apply {
//                    description = "키워드 알림"
//                    enableLights(true)
//                    lightColor = Color.GREEN
//                    enableVibration(true)
//                    vibrationPattern = longArrayOf(100, 200, 100, 200)
//                    lockscreenVisibility = Notification.VISIBILITY_PRIVATE
//                }
//            notificationManager.createNotificationChannel(notificationChannel)
//        }
//    }


    fun sendBroadcastWith(intent: Intent) {
        applicationContext.startActivity(intent)
    }
}

