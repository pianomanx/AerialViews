package com.neilturner.aerialviews.ui.screensaver

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import com.neilturner.aerialviews.R
import com.neilturner.aerialviews.utils.WindowHelper

class TestActivity : Activity() {
    private var videoController: VideoController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.app_name)
        videoController = VideoController(this)
        setContentView(videoController!!.view)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP) {
            Log.i("", "${event.keyCode}")

            if (event.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                videoController!!.skipVideo()
                return true
            }
        }

        return super.dispatchKeyEvent(event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            WindowHelper.hideSystemUI(window, videoController!!.view)
        }
    }

    override fun onStop() {
        videoController!!.stop()
        super.onStop()
    }
}