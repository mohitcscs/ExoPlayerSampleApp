package com.exoplayer.playersdk

import com.exoplayer.playeranalytics.AnalyticsLogger

class LogEvents constructor(sendAnalytics: Boolean = false) {

    init {
        AnalyticsLogger.Logger.sendEvents(sendAnalytics)
    }

    companion object {
        const val PLAYBACK_STATUS_LOG_INTERVAL: Long = 1000
        const val ERROR_CODE = "error_code"
        const val ERROR_DESC = "error_desc"
        const val PLAYER_STATE = "player_state"
        const val CURRENT_POSITION = "current_position"
        const val PLAYBACK_STARTED = "playback_stated"
        const val MEDIA_INFORMATION = "media_information"
    }

    fun logPlayerError(
        errorCode: String,
        errorDetails: String
    ) {
        val dataMap = mutableMapOf<String, String>()
        dataMap[ERROR_CODE] = errorCode
        dataMap[ERROR_DESC] = errorDetails
        AnalyticsLogger.Logger.logEvents(dataMap)
    }

    fun logVideoStarted(currentPosition: Long) {
        val dataMap = mutableMapOf<String, String>()
        dataMap[CURRENT_POSITION] = currentPosition.toString()
        dataMap[PLAYBACK_STARTED] = true.toString()
        AnalyticsLogger.Logger.logEvents(dataMap)
    }

    // Player states should map with ExoPlayer provided states.
    // Used in data analytics
    fun logPlayerStateChange(playbackState: Int) {
        val dataMap = mutableMapOf<String, String>()
        val playerState: String = when (playbackState) {
            1 -> "STATE_IDLE"
            2 -> "STATE_BUFFERING"
            3 -> "STATE_READY"
            4 -> "STATE_ENDED"
            else -> {
                "Unknown"
            }
        }
        dataMap[PLAYER_STATE] = playerState
        AnalyticsLogger.Logger.logEvents(dataMap)
    }

    fun logPlaybackStatus(
        currentPosition: Long,
        mediaInformation: String
    ) {
        val dataMap = mutableMapOf<String, String>()
        dataMap[CURRENT_POSITION] = currentPosition.toString()
        dataMap[MEDIA_INFORMATION] = mediaInformation

        AnalyticsLogger.Logger.logEvents(dataMap)
    }
}