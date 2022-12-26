package com.exoplayer.playeranalytics

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@RunWith(MockitoJUnitRunner::class)
class AnalyticsLoggerTest {

    @Test
    fun testAddDeviceDetails_When_AllowedToSend() {

        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0

        AnalyticsLogger.Logger.sendEvents(true)

        val dataMap = mutableMapOf<String, String>()
        dataMap["current_position"] = "100"
        dataMap["playback_stated"] = true.toString()

        AnalyticsLogger.Logger.logEvents(dataMap)

        val allMemberProperties = AnalyticsLogger.Logger::class.memberProperties
        val eventsQueue = allMemberProperties.find { it.name == "events" }
        eventsQueue?.isAccessible = true

        assertTrue(dataMap.containsKey("timestamp"))
    }
}