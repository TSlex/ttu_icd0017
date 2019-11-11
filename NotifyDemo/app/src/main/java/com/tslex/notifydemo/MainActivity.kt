package com.tslex.notifydemo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                C.CHANNEL_ID,
                "Default channel",
                NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Default channel for Channel Demo"
            var notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun notify1(view: View) {
        val intent = Intent(this, MainActivity::class.java)

        val builder = NotificationCompat
            .Builder(this, C.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setContentTitle("Sample Notification")
            .setContentText("Hello!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(this).notify(0, builder.build())
    }

    fun notify2(view: View) {

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat
            .Builder(this, C.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setContentTitle("Sample Notification")
            .setContentText("Hello!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(this).notify(0, builder.build())
    }
    fun notify3(view: View) {
        val notificationView = RemoteViews(packageName, R.layout.simple_notification_layout)

        val builder = NotificationCompat
            .Builder(this, C.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        builder.setContent(notificationView)


        NotificationManagerCompat.from(this).notify(0, builder.build())
    }
    fun notify4(view: View) {}
}
