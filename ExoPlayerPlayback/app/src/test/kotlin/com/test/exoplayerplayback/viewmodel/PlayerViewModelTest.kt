package com.test.exoplayerplayback.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.exoplayer.playersdk.VideoPlayer
import com.test.exoplayerplayback.TestCoroutineRule
import com.test.exoplayerplayback.repository.PlayerRepository
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class PlayerViewModelTest : TestCase() {

    @Mock
    private lateinit var videoPlayer: VideoPlayer

    @Mock
    private lateinit var repository: PlayerRepository

    private lateinit var playerViewModel: PlayerViewModel

    @get:Rule
    val testInstantExecutionRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Mock
    private lateinit var loadingObserver: Observer<Boolean>

    @Mock
    private lateinit var playbackUrlObserver: Observer<String>

    @Before
    public override fun setUp() {
        playerViewModel = PlayerViewModel(repository, videoPlayer)
        playerViewModel.playbackUrl.observeForever(playbackUrlObserver)
        playerViewModel.loading.observeForever(loadingObserver)
    }

    val PLAYBACK_URL =
        "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8"

    @Test
    fun `when fetching playback url then return playback url`() {
        testCoroutineRule.runBlockingTest {
            `when`(repository.getPlaybackUrl()).thenAnswer { PLAYBACK_URL }
        }
        playerViewModel.getVideoPlaybackUrl()
        assertNotNull(playerViewModel.playbackUrl)
    }

    @Test
    fun `when fetching playback url then return loading state`() {
        testCoroutineRule.runBlockingTest {
            playerViewModel.getVideoPlaybackUrl()
            verify(loadingObserver).onChanged(true)
        }
    }

    @Test
    fun `when fetching results fails then return an error`() {
        val exception = mock(Exception::class.java)
        testCoroutineRule.runBlockingTest {
            `when`(repository.getPlaybackUrl()).thenAnswer {
                exception
            }
            playerViewModel.getVideoPlaybackUrl()
            assertNull(playerViewModel.playbackUrl.value)
        }
    }

    @After
    public override fun tearDown() {
        playerViewModel.playbackUrl.removeObserver(playbackUrlObserver)
        playerViewModel.loading.removeObserver(loadingObserver)
    }
}