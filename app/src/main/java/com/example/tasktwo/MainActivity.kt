package com.example.tasktwo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var tokenTextView: TextView
    private lateinit var subscribeButton: Button
    private lateinit var unsubscribeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        Log.d("MainActivity", "App started successfully")

        tokenTextView = findViewById(R.id.tokenTextView)
        tokenTextView.text = "Initializing..."

        subscribeButton = findViewById(R.id.subscribeButton)
        unsubscribeButton = findViewById(R.id.unsubscribeButton)

        requestNotificationPermission()
        getFCMToken()

        subscribeButton.setOnClickListener {
            FirebaseMessaging.getInstance().subscribeToTopic("cloud")
                .addOnCompleteListener { task ->
                    val msg = if (task.isSuccessful) "Subscribed to topic: cloud" else "Subscription failed"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    Log.d("MainActivity", msg)
                }
        }

        unsubscribeButton.setOnClickListener {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("cloud")
                .addOnCompleteListener { task ->
                    val msg = if (task.isSuccessful) "Unsubscribed from topic: cloud" else "Unsubscription failed"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    Log.d("MainActivity", msg)
                }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("MainActivity", "FCM Token: $token")
                tokenTextView.text = "FCM Token:\n$token"
                Toast.makeText(this, "Token received!", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("MainActivity", "Failed to get token", task.exception)
                tokenTextView.text = "Error: ${task.exception?.message}"
            }
        }
    }
}
