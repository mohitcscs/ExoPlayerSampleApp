package com.test.exoplayerplayback

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.exoplayer.playersdk.VideoPlayer
import com.test.exoplayerplayback.databinding.ActivityPlayerBinding
import com.test.exoplayerplayback.repository.PlayerRepository
import com.test.exoplayerplayback.viewmodel.PlayerViewModel

class PlayerActivity : AppCompatActivity() {
    lateinit var binding: ActivityPlayerBinding

    private val playerViewModel: PlayerViewModel by viewModels {
        PlayerViewModel.provideFactory(PlayerRepository(), getVideoPlayer())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        binding = ActivityPlayerBinding.inflate(layoutInflater)

        setContentView(binding.root)

        playerViewModel.errorMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        playerViewModel.loading.observe(this) {
            if (it) {
                binding.progressDialog.visibility = View.VISIBLE
            } else {
                binding.progressDialog.visibility = View.GONE
            }
        }

        playerViewModel.playbackUrl.observe(this) {
            startPlayback(it)
        }

        getPlaybackUrl()
    }

    // Get playback url
    private fun getPlaybackUrl() {
        playerViewModel.getVideoPlaybackUrl()
    }

    // Start playback with the url
    private fun startPlayback(playBackUrl: String) {
        playerViewModel.startPlayBack(
            playBackUrl,
            binding.root
        )
    }

    // Get video player object from player sdk
    private fun getVideoPlayer(): VideoPlayer {
        return VideoPlayer.Builder()
            .context(this)
            .showControls(true)
            .sendAnalytics(true)
            .build()
    }
}