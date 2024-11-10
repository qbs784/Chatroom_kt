package com.example.HTTPDemo.entity

import kotlinx.serialization.Serializable

@Serializable
data class Chatroom(val id: Int, val name: String)

@Serializable
data class ChatroomResponse(val data: List<Chatroom>, val status: String)