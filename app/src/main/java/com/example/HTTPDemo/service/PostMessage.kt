package com.example.HTTPDemo.service

import kotlinx.serialization.json.Json
import com.example.HTTPDemo.entity.*
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.serialization.encodeToString

fun postMessage(url: String,request: Message): String
{
    // 1. create HttpURLConnection
    val conn = URL(url).openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.doOutput = true
    conn.doInput = true
    conn.readTimeout = 15000
    conn.connectTimeout = 15000
    conn.setRequestProperty("Content-Type", "application/json")
    conn.setRequestProperty("Accept", "application/json")
    // 2. build JSON object
    val message = Json.encodeToString(request)
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