package com.test.exoplayerplayback.repository

class PlayerRepository {

    val PLAYBACK_URL = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8"

    fun getPlaybackUrl() : String {
        return PLAYBACK_URL
    }
 }