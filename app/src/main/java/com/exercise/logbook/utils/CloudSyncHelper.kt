package com.exercise.logbook.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.exercise.logbook.model.Event
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object CloudSyncHelper {

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun uploadAllEvents(context: Context, events: List<Event>, endpointUrl: String, onResult: (Boolean, String) -> Unit) {
        if (!isNetworkAvailable(context)) {
            onResult(false, "No active internet connection found. Upload aborted.")
            return
        }

        Thread {
            try {
                val jsonArray = JSONArray()
                for (event in events) {
                    val eventObj = JSONObject().apply {
                        put("id", event.id)
                        put("name", event.name)
                        put("date", event.date)
                        put("location", event.location)
                        put("organiser", event.organiser)
                        put("type", event.type)
                        put("status", event.status)
                        put("description", event.description)
                        put("contactEmail", event.contactEmail)
                        put("contactPhone", event.contactPhone)
                    }
                    jsonArray.put(eventObj)
                }

                val url = URL(endpointUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonArray.toString())
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    onResult(true, "Successfully uploaded ${events.size} events to the cloud!")
                } else {
                    onResult(false, "Cloud server error. Response code: $responseCode")
                }
                connection.disconnect()

            } catch (e: Exception) {
                onResult(false, "Upload failed: ${e.localizedMessage}")
            }
        }.start()
    }
}