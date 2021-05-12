package com.shingetsu.mitago.Utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.shingetsu.mitago.Models.RiderMiTa
import com.shingetsu.mitago.R
import java.lang.StringBuilder

/**
 * Created by Phạm Minh Tân - Shin on 4/30/2021.
 */
object Common {
    var TAG: String = "PMTAN"
    val NOTI_TITLE: String = "title"
    val NOTI_BODY: String = "body"
    val TOKEN_REFERENCE: String = "Token"

    var currentUser: RiderMiTa? = null
    val RIDER_INFO_REFERENCE: String = "RiderInfo"
    val RIDER_LOCATION_REFERENCE: String = "RiderLocation"


    fun buildWelcomeMessage(): String {
        return StringBuilder("Xin chào, ")
            .append(currentUser!!.firstName)
            .append(" ")
            .append(currentUser!!.lastName)
            .toString()
    }
    fun showNotificaion(context: Context, id: Int, title: String?, body: String?, intent: Intent?) {
        var pendingIntent : PendingIntent? = null
        if (intent != null){
            pendingIntent = PendingIntent.getActivity(context,id,intent!!,PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val NOTIFICATION_CHANNEL_ID = "mita_rider"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,"MiTa Go",
                NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "App for rider MiTa"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0,1000,500,1000)
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }

        val builder = NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setSmallIcon(R.drawable.ic_cartoon_mita_go)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources,R.drawable.ic_cartoon_mita_go))

        if (pendingIntent != null){
            builder.setContentIntent(pendingIntent!!)

        }
        val notification = builder.build()
        notificationManager.notify(id,notification)
    }
}