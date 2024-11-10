package com.example.HTTPDemo

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.HTTPDemo.entity.*
import com.example.HTTPDemo.service.*
import com.example.HTTPDemo.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest
import android.util.Log
import com.google.firebase.messaging.Constants.MessageNotificationKeys.TAG
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability



class MainActivity : ComponentActivity() {

    private var token = ""
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        enableEdgeToEdge()
        checkGooglePlayServices()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get the registration token
            token = task.result
            Log.d(TAG, "FCM registration token: $token")
            // Send the token to your server or save it locally
        }

        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TAG", "onCreate: subscribeToTopic", )
                }else{
                    Log.e("TAG", "onCreate: subscribeToTopic failed", )
                }
            }

        setContent {
            HTTPDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    ChatroomScreen()
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatroomScreen() {
        var chatrooms by remember { mutableStateOf(listOf<Chatroom>()) }

        // Automatically start GET request when the composable is first displayed
        LaunchedEffect(Unit) {
            CoroutineScope(Dispatchers.Default).launch {
                // GET chatrooms
                val url = "http://192.168.101.231:8000/get_chatrooms"
                val response = getChatrooms(url)
                // Update data on the main thread
                withContext(Dispatchers.Main) {
                    if (response != null && response.status == "OK") {
                        chatrooms = response.data
                    }
                }
            }
        }


        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopAppBar(
                title = { Text("IEMS5722") },
                navigationIcon = {
                    IconButton(onClick = {finish()}) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Exit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Gray // Set the background color of the TopAppBar to gray
                )
            )

            // Display chatrooms in a LazyColumn
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                items(chatrooms) { chatroom ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable {
                                // Start ChatActivity and pass chatroom name
                                val intent = Intent(this@MainActivity, ChatActivity::class.java)
                                intent.putExtra("chatroom_id", chatroom.id)
                                intent.putExtra("chatroom_name", chatroom.name)
                                intent.putExtra("token", token)
                                startActivity(intent)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = chatroom.name,
                            fontSize = 25.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(10.dp)) // Adds spacing before divider
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp
                        ) // Add a divider between items
                    }

                }
            }
        }
    }

    private fun requestNotificationPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
        // The code below is related to create notification channel for later use
        val channel = NotificationChannel("MyNotification","MyNotification",
            NotificationManager.IMPORTANCE_DEFAULT)
        val manager = getSystemService(NotificationManager::class.java) as
                NotificationManager;
        manager.createNotificationChannel(channel)

    }

    private fun checkGooglePlayServices() {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            // 处理未安装或更新 Google Play 服务的情况
            googleApiAvailability.getErrorDialog(this, resultCode, 0)?.show()
        }
    }
    override fun onResume() {
        super.onResume()
        // 每次恢复时检查 Google Play 服务
        checkGooglePlayServices()
    }
}





