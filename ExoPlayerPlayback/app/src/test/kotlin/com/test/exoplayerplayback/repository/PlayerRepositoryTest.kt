package com.test.exoplayerplayback.repository

import org.junit.Assert.*

import org.junit.Before
import org.junit.Test

class PlayerRepositoryTest {

    private lateinit var playerRepository: PlayerRepository

    @Before
    fun setUp() {
        playerRepository = PlayerRepository()
    }

    @Test
    fun getPlaybackUrl() {
        assertEquals(playerRepository.getPlaybackUrl(), playerRepository.PLAYBACK_URL)
    }
}