package com.example.HTTPDemo.service

import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.InputStreamReader
import java.io.BufferedReader
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.HttpClientBuilder
import android.util.Log
import java.io.IOException
import com.example.HTTPDemo.entity.*

fun getMessage(url: String?): MessageResponse? {
    val inputStream: InputStream?
    var result: String? = null
    try {
        // 1. create HttpClient
        val httpclient: HttpClient = HttpClientBuilder.create().build()
        // 2. make GET request to the given URL
        val httpResponse: HttpResponse = httpclient.execute(HttpGet(url))
        // 3. receive response as inputStream
        inputStream = httpResponse.entity.content
        // 4. convert inputstream to string
        result = if (inputStream != null) convertInputStreamToString(inputStream)
        else "Did not work!"
    } catch (e: Exception) {
        e.localizedMessage?.let { Log.d("InputStream", it) }
    }

    // 5. Deserialize the result to ChatroomResponse
    return result?.let { Json.decodeFromString<MessageResponse>(it) }
}


@Throws(IOException::class)
private fun convertInputStreamToString(inputStream: InputStream): String? {
    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
    var line: String?
    var result: String? = ""
    while ((bufferedReader.readLine().also { line = it }) != null)
        result += line
    inputStream.close()
    return result
}