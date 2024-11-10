package com.example.HTTPDemo.entity

import kotlinx.serialization.Serializable

@Serializable
data class Message(val chatroom_id: Int,
                   val user_id: Int,
                   val name: String,
                   val message: String,
                   val message_time: String)

@Serializable
data class MessageData(
    val messages: List<Message>
)

@Serializable
data class MessageResponse(
    val data: MessageData,
    val status: String
)

