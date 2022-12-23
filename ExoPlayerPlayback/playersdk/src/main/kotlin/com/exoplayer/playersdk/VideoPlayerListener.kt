package com.exoplayer.playersdk

sealed class VideoPlayerListener {
   object PlayerListener: VideoPlayerListener()
   data class onError(val error: String) : VideoPlayerListener()
   data class onBuffering(val currentPosition: Int) : VideoPlayerListener()
   data class onPlaybackStateChanged(val playerState: Int) : VideoPlayerListener()
   data class onVideoStarted(val isPlaying: Boolean) : VideoPlayerListener()
}

interface test {
   fun onError(error: String)
}