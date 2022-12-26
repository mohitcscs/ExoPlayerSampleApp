package com.exoplayer.playersdk

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.contains
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// Create Video player using builder pattern and don't expose player used internally
class VideoPlayer private constructor(
    private val context: Context?,
    val showControls: Boolean = false,
    val sendAnalytics: Boolean = false
) {
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var styledPlayerView: StyledPlayerView
    private lateinit var clientViewGroup: ViewGroup
    private val playerScope = CoroutineScope(Dispatchers.Main)
    private var taskJob: Job? = null

    data class Builder(
        var context: Context? = null,
        var showControls: Boolean = false,
        var sendAnalytics: Boolean = false
    ) {
        fun showControls(showControls: Boolean) = apply { this.showControls = showControls }
        fun sendAnalytics(sendAnalytics: Boolean) = apply {
            this.sendAnalytics = sendAnalytics
        }

        fun context(context: Context) = apply { this.context = context }
        fun build() = VideoPlayer(context, showControls, sendAnalytics)
    }

    fun playMedia(
        playbackUrl: String,
        view: ViewGroup,
    ) {
        this.clientViewGroup = view
        val trackSelector = DefaultTrackSelector(context!!).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        exoPlayer = ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build()

        styledPlayerView = StyledPlayerView(context)
        styledPlayerView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        val mediaItem = MediaItem.fromUri(playbackUrl)
        exoPlayer.setMediaItem(mediaItem)
        styledPlayerView.player = exoPlayer
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun resume() {
        exoPlayer.play()
    }

    fun clearVideoPlayer() {
        cancelTasksJob()

        exoPlayer.stop()
        exoPlayer.release()
    }

    // Send video player callbacks as stream flow to the caller
    fun videoPlayerEvents(): Flow<VideoPlayerListener> = callbackFlow {
        val logEvents = LogEvents(sendAnalytics)
        val playerListener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                logEvents.logPlayerError(
                    error.errorCodeName,
                    error.localizedMessage
                )
                trySend(VideoPlayerListener.onError(error.localizedMessage))
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                logEvents.logPlayerStateChange(playbackState)
                trySend(VideoPlayerListener.onPlaybackStateChanged(playbackState))

                // Send player status every second while playing
                if (isPlaying())
                    logPlayerStatusPeriodically(
                        logEvents,
                        LogEvents.PLAYBACK_STATUS_LOG_INTERVAL
                    )

                // Stop sending player status
                if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                    taskJob?.cancel()
                }
            }

            override fun onRenderedFirstFrame() {
                super.onRenderedFirstFrame()
                logEvents.logVideoStarted(exoPlayer.currentPosition)
                trySend(VideoPlayerListener.onVideoStarted(true))
            }

            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isPlaying && exoPlayer.playbackState == Player.STATE_IDLE) {
                    clientViewGroup.removeView(styledPlayerView)
                } else if (!clientViewGroup.contains(styledPlayerView)){
                    clientViewGroup.addView(styledPlayerView)
                }
            }
        }

        exoPlayer.addListener(playerListener)
        awaitClose {
            exoPlayer.removeListener(playerListener)
        }
    }

    fun logPlayerStatusPeriodically(
        logEvents: LogEvents,
        timeIntervalInMillis: Long
    ) {
        cancelTasksJob()
        taskJob = playerScope.launch {
            while (isActive) {
                logEvents.logPlaybackStatus(
                    exoPlayer.currentPosition,
                    exoPlayer.currentMediaItem!!.mediaId
                )
                delay(timeIntervalInMillis)
            }
        }
    }

    private fun cancelTasksJob() {
        taskJob?.cancel()
        taskJob = null
    }

    fun isPlaying(): Boolean {
        return exoPlayer.playbackState == Player.STATE_READY && exoPlayer.playWhenReady
    }
}