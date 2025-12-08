package com.rcsi.wellby
// Wellby App initialisation
import android.app.Application
//import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.room.Room
import com.rcsi.wellby.toolkitTab.toDoList.ToDoDatabase

class WellbyApp : Application() {
    lateinit var database: ToDoDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        // Initialize your Room database
        database = Room.databaseBuilder(
            applicationContext,
            ToDoDatabase::class.java, "todo-database"
        ).build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channel_id",
                "Channel Name",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}