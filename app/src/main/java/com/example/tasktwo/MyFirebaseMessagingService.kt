package com.example.tasktwo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var database: DatabaseReference

    override fun onCreate() {
        super.onCreate()

        // Connect to Task1 database
        val firebaseDatabase = FirebaseDatabase.getInstance("https://task1-e93a1-default-rtdb.europe-west1.firebasedatabase.app/")
        database = firebaseDatabase.getReference("notifications")

        Log.d(TAG, "Connected to Task1 Firebase Database")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        // Store notification in database
        storeNotificationInDatabase(remoteMessage)

        // Handle notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Notification Title: ${it.title}")
            Log.d(TAG, "Notification Body: ${it.body}")
            showNotification(it.title ?: "No Title", it.body ?: "No Body")
        }

        // Handle data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        }
    }

    private fun storeNotificationInDatabase(remoteMessage: RemoteMessage) {
        val notificationId = database.push().key ?: return

        val notificationData = hashMapOf(
            "timestamp" to System.currentTimeMillis(),
            "from" to (remoteMessage.from ?: "Unknown"),
            "notificationTitle" to (remoteMessage.notification?.title ?: ""),
            "notificationBody" to (remoteMessage.notification?.body ?: ""),
            "dataPayload" to remoteMessage.data.toString(),
            "messageId" to (remoteMessage.messageId ?: "")
        )

        database.child(notificationId).setValue(notificationData)
            .addOnSuccessListener {
                Log.d(TAG, "Notification stored successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to store notification", e)
            }
    }

    private fun showNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        // Send token to your server if needed
    }

    companion object {
        private const val TAG = "FCMService"
    }
}