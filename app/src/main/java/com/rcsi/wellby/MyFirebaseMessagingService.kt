package com.rcsi.wellby
// class for handling new message notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Log data to Log Cat
        remoteMessage.data.isNotEmpty().let {
            // Process message data here...
            sendNotification(remoteMessage.data["title"] ?: "New Message", remoteMessage.data["message"] ?: "You have received a new message")
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            sendNotification(it.title ?: "New Message", it.body ?: "You have received a new message")
        }
    }

    override fun onNewToken(token: String) {
        // Log and toast
        super.onNewToken(token)
        // If necessary, send the token to your app server.
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        // Specify FLAG_IMMUTABLE for the PendingIntent
        val pendingIntentFlags =
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE

        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, pendingIntentFlags)

        val channelId = getString(R.string.default_notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since Android Oreo, notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}
