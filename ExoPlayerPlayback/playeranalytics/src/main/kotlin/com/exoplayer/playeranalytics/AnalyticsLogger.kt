package com.exoplayer.playeranalytics

import android.annotation.SuppressLint
import android.util.Log
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

class AnalyticsLogger {
    object Logger {
        private val TAG = "AnalyticsLogger"
        private var events: Queue<String> = LinkedList()

        // Configure Gson
        private val gson = GsonBuilder().create()
        private var sendEvents: Boolean = false

        // Send events if user consent or privacy allows
        fun sendEvents(sendEvents: Boolean) {
            this.sendEvents = sendEvents
        }

        fun logEvents(dataMap: MutableMap<String, String>) =
            GlobalScope.launch(Dispatchers.Default) {
                if (sendEvents) {
                    withContext(Dispatchers.Default) {
                        events.add(addDeviceDetails(dataMap))
                        sendEvents()
                    }
                }
            }

        // Add critical data related to device
        private fun addDeviceDetails(dataMap: MutableMap<String, String>): String {
            dataMap.put("timestamp", getCurrentTimeStamp())
            return gson.toJson(dataMap)
        }

        @SuppressLint("NewApi")
        private fun getCurrentTimeStamp(): String {
            return DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        }

        // Send events to network or set logs as per requirement
        private fun sendEvents() {
            while (!events.isEmpty()) {
                Log.i(TAG, events.poll())
            }
        }
    }
}