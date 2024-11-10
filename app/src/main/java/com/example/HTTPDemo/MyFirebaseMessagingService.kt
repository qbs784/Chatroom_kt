package com.example.HTTPDemo

import android.util.Log;
import com.example.HTTPDemo.service.postMessage
import com.google.firebase.messaging.Constants.TAG
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        val newToken = Token(0,token)
        CoroutineScope(Dispatchers.Default).launch {
            // GET chatrooms
            val url = "http://192.168.101.231:8000/submit_push_token/"
            val response = sendRegistrationToServer(url, newToken)
            withContext(Dispatchers.Main) {
            }
        }

    }
    private fun sendRegistrationToServer(url: String, token: Token): String {
        // Implement your own logic to submit toke
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.doInput = true
        conn.readTimeout = 15000
        conn.connectTimeout = 15000
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "application/json")
        // 2. build JSON object
        val message = Json.encodeToString(token)
        // 3. add JSON content to POST request body
        val os: OutputStream = conn.outputStream
        val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
        writer.write(message)
        writer.flush()
        writer.close()
        os.close()
        // 4. return response message
        val responseCode = conn.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK)
            return conn.responseMessage + ""
        return "ERROR"
    }
    // This callback function will be called when an FCM message is received
    // (except for a notification message is received when app is in background)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle FCM messages here
        Log.d(TAG, "From: ${remoteMessage.from}")
        // Check if the message contains data payload
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            // Handle data payload
        }
        // Check if the message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            // Show notification
        }
    }

    @Serializable
    data class Token(val user_id: Int,
                       val token: String, )
}
