package com.example.HTTPDemo

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Refresh
import com.example.HTTPDemo.service.getMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import com.example.HTTPDemo.entity.*
import com.example.HTTPDemo.service.postMessage

class ChatActivity : ComponentActivity() {
    var user_id=-1
    var user_name=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chatroomId = intent.getIntExtra("chatroom_id",-1)
        val chatroomName = intent.getStringExtra("chatroom_name")
        val token = intent.getStringExtra("token")

        if(token == "d08vz9mNSDCXBtcdZ0JBb5:APA91bEQdtBpigtDvU3nNo9rGIQ7i-ZcF4xDtJKzyKB8FP7J8o6WBK4HUEpamOHtl0zXDOJuoioGRSkgB0p5ClveGsyZbDVAci4S0xicRWJPg6y8M4n3j0o"){
            user_id = 0
            user_name = "CCC"
        } else if(token == "eYXCFD_OShC5jdV-itsTsT:APA91bHEwi1HYWwSBF686-kjsYiDkafYBjvRZR6EXuxOwp0lrODgidbfA9UKXfxyviJdIfIA6kJYl8zgL2eK0cUiAMOa_nKbD8im4p0QzOuPJA6zvZt8bXg"){
            user_id = 1
            user_name = "KKK"
        }
        setContent {

            ChatScreen(chatroomId,chatroomName,user_id,user_name)
        }
    }

    @SuppressLint("WeekBasedYear")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatScreen(chatroomId: Int?,chatroomName: String?,user_id: Int,user_name: String) {
        var message by remember { mutableStateOf("") }
        var messages by remember { mutableStateOf(listOf<Message>()) }
        val listState = rememberLazyListState()

        // Automatically start GET request when the composable is first displayed
        LaunchedEffect(Unit) {
            CoroutineScope(Dispatchers.Default).launch {
                // GET chatrooms
                val url = "http://192.168.101.231:8000/get_messages/?chatroom_id=${chatroomId}"
                val response = getMessage(url)
                // Update data on the main thread
                withContext(Dispatchers.Main) {
                    if (response != null && response.status == "OK") {
                        messages = response.data.messages
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    if (chatroomName != null) {
                        Text(chatroomName)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        CoroutineScope(Dispatchers.Default).launch {
                            val url = "http://192.168.101.231:8000/get_messages/?chatroom_id=${chatroomId}"
                            val response = getMessage(url)
                            withContext(Dispatchers.Main) {
                                if (response != null && response.status == "OK") {
                                    messages = response.data.messages
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Gray
                )

            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                reverseLayout = true
            ) {
                items(messages.reversed()) { msg ->
                    MessageItem(msg)
                }
            }
            LaunchedEffect(messages.size) {
                listState.animateScrollToItem(0)
            }
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.weight(1f).padding(8.dp),
                    decorationBox = { innerTextField ->
                        if (message.isEmpty()) {
                            Text("Type a message...", color = Color.Gray)
                        }
                        innerTextField()
                    }
                )
                IconButton(onClick = {
                    if (message.isNotEmpty()) {
                        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                        val newMessage =
                            chatroomId?.let { Message(it, user_id, user_name, message, currentTime) }
                        CoroutineScope(Dispatchers.Default).launch {
                            // GET chatrooms
                            val url = "http://192.168.101.231:8000/send_message/"
                            val response = newMessage?.let { postMessage(url, it) }
                            withContext(Dispatchers.Main) {
                                if (response == "OK") {
                                    messages = messages.toMutableList().apply {
                                        add(0, newMessage)
                                    }
                                }
                            }
                        }
                        CoroutineScope(Dispatchers.Default).launch {
                            val url = "http://192.168.101.231:8000/get_messages/?chatroom_id=${chatroomId}"
                            val response = getMessage(url)
                            withContext(Dispatchers.Main) {
                                if (response != null && response.status == "OK") {
                                    messages = response.data.messages
                                }
                            }
                        }
                        message = ""
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }

    @Composable
    fun MessageItem(message: Message) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = if (message.user_id==user_id) Arrangement.End else Arrangement.Start
        ) {
            Column(
                modifier = Modifier
                    .background(
                        if (message.user_id!=user_id) Color(0xFFE0E0E0) else Color(0xFFA5D6A7),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = "User: "+message.name+"\n"+message.message,
                    color = Color.Black,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text =message.message_time,
                    color = Color.Gray,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        ChatScreen(-1,"Chatroom",-1,"")
    }

}




