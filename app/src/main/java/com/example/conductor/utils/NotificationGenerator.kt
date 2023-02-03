package com.example.conductor.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.conductor.MainActivity
import com.example.conductor.R
import com.example.conductor.utils.Constants.NOTIFICATION_CHANNEL_ID


fun notificationGenerator(context: Context, messageBody: String) {
    val intent = Intent(context, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

    //val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    val notificationCompatBuilder = NotificationCompat.Builder(context,  NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Atención")
        .setContentText(messageBody)
        .setAutoCancel(true)
        //.setSound(defaultSoundUri)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Since android Oreo notification channel is needed.
    val channel = NotificationChannel(
        NOTIFICATION_CHANNEL_ID,
        "Channel human readable title",
        NotificationManager.IMPORTANCE_DEFAULT
    )
    notificationManager.createNotificationChannel(channel)

    notificationManager.notify(0, notificationCompatBuilder.build())
}