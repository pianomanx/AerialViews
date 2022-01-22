package com.neilturner.aerialviews.ui.screensaver

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import androidx.databinding.DataBindingUtil
import com.neilturner.aerialviews.R
import com.neilturner.aerialviews.databinding.AerialActivityBinding
import com.neilturner.aerialviews.databinding.VideoViewBinding
import com.neilturner.aerialviews.models.LocationInformationStyle
import com.neilturner.aerialviews.models.VideoPlaylist
import com.neilturner.aerialviews.models.prefs.GeneralPrefs
import com.neilturner.aerialviews.models.prefs.UITextPrefs
import com.neilturner.aerialviews.models.videos.AerialVideo
import com.neilturner.aerialviews.services.VideoService
import com.neilturner.aerialviews.ui.screensaver.ExoPlayerView.OnPlayerEventListener
import kotlinx.coroutines.runBlocking

class VideoController(context: Context) : OnPlayerEventListener {
    private val binding: AerialActivityBinding
    private var playlist: VideoPlaylist? = null
    private var canSkip: Boolean
    private var previousVideo: Boolean

    init {
        val inflater = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate(inflater, R.layout.aerial_activity, null, false)

        binding.textPrefs = UITextPrefs

        binding.videoView0.controller = binding.videoView0.videoView
        binding.videoView0.videoView.setOnPlayerListener(this)

        val service = VideoService(context)
        runBlocking { playlist = service.fetchVideos() }

        canSkip = true
        previousVideo = false

        binding.root.post { start() }
    }

    val view: View
        get() = binding.root

    private fun start() {
        loadVideo(binding.videoView0, playlist!!.nextVideo())
    }

    fun stop() {
        binding.videoView0.videoView.release()
    }

    fun skipVideo(previous: Boolean = false) {
        previousVideo = previous
        fadeOutCurrentVideo()
    }

    private fun fadeOutCurrentVideo() {
        if (!canSkip) return
        canSkip = false

        binding.loadingView
            .animate()
            .alpha(1f)
            .setDuration(ExoPlayerView.DURATION)
            .withStartAction {
                binding.loadingView.visibility = View.VISIBLE
            }
            .withEndAction{
                val video = if (!previousVideo) {
                    playlist!!.nextVideo()
                } else {
                    playlist!!.previousVideo()
                }
                previousVideo = false

                loadVideo(binding.videoView0, video)

                if (UITextPrefs.alternateTextPosition) {
                    binding.videoView0.isAlternateRun = !binding.videoView0.isAlternateRun
                }
            }.start()
    }

    private fun fadeInNextVideo() {
        if (binding.loadingView.visibility == View.VISIBLE) {
            binding.loadingView
                .animate()
                .alpha(0f)
                .setDuration(ExoPlayerView.DURATION)
                .withEndAction {
                    binding.loadingView.visibility = View.GONE
                    canSkip = true
                }.start()
        }
    }

    private var currentPositionProgressHandler: (()->Unit)? = null

    private fun loadVideo(videoBinding: VideoViewBinding, video: AerialVideo) {
        Log.i("LoadVideo", "Playing: ${video.location} - ${video.uri} (${video.poi})")
        videoBinding.location.text = if (UITextPrefs.locationInfoStyle == LocationInformationStyle.POINT_OF_INTEREST) video.poi[0] ?: video.location else video.location

        if (UITextPrefs.locationInfoStyle == LocationInformationStyle.POINT_OF_INTEREST && video.poi.size > 1) { // everything else is static anyways
            val poiTimes = video.poi.keys.sorted()
            var lastPoi = 0
            currentPositionProgressHandler = {
                val time = videoBinding.videoView.currentPosition / 1000
                val poi = poiTimes.findLast { it <= time } ?: 0
                val update = poi != lastPoi
                if (update) {
                    lastPoi = poi
                    videoBinding.location.animate().alpha(0f).setDuration(1000).withEndAction {
                        videoBinding.location.text = video.poi[poi]
                        videoBinding.location.animate().alpha(0.7f).setDuration(1000).start()
                    }.start()
                }
                videoBinding.location.postDelayed ({
                    currentPositionProgressHandler?.let { it() }
                }, if (update) 3000 else 1000)
            }
            videoBinding.location.postDelayed ({
                currentPositionProgressHandler?.let { it() }
            },1000)
        } else {
            currentPositionProgressHandler = null
        }

        videoBinding.videoView.setUri(video.uri)
        videoBinding.videoView.start()
    }

    override fun onPrepared(view: ExoPlayerView?) {
        fadeInNextVideo()
    }

    override fun onAlmostFinished(view: ExoPlayerView?) {
        fadeOutCurrentVideo()
    }

    override fun onError(view: ExoPlayerView?) {
        binding.root.post { start() }
    }
}
