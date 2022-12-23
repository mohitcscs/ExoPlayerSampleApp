package com.test.exoplayerplayback.viewmodel

import android.util.Log
import android.view.ViewGroup
import androidx.lifecycle.*
import com.exoplayer.playersdk.VideoPlayer
import com.exoplayer.playersdk.VideoPlayerListener
import com.test.exoplayerplayback.repository.PlayerRepository
import kotlinx.coroutines.*

class PlayerViewModel constructor(
    private val playerRepository: PlayerRepository,
    private val videoPlayer: VideoPlayer
) : ViewModel() {

    private val TAG = "PlayerViewModel"

    val errorMessage = MutableLiveData<String>()
    val playbackUrl = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

    fun getVideoPlaybackUrl() {
        loading.value = true

        viewModelScope.launch {
            var response: String
            withContext(Dispatchers.IO) {
                response = playerRepository.getPlaybackUrl()
            }
            playbackUrl.postValue(response)
            loading.value = false
        }
    }

    companion object {
        fun provideFactory(
            playerRepository: PlayerRepository,
            videoPlayer: VideoPlayer
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory() {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return PlayerViewModel(playerRepository, videoPlayer) as T
                }
            }
    }

    private suspend fun addPlayerListener() {
        videoPlayer.videoPlayerEvents().collect {
            when (it) {
                is VideoPlayerListener.onError -> {
                    errorMessage.value = it.error
                    loading.value = false
                }
                is VideoPlayerListener.onVideoStarted -> {
                    loading.value = false
                }
                is VideoPlayerListener.onPlaybackStateChanged -> {
                    val playbackState = it.playerState
                }
                else -> {
                    Log.d(TAG, it.toString())
                }
            }
        }
    }

    fun startPlayBack(
        playbackUrl: String,
        view: ViewGroup,
    ) {
        loading.value = true

        videoPlayer.playMedia(playbackUrl, view)
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                addPlayerListener()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        videoPlayer.clearVideoPlayer()
    }
}