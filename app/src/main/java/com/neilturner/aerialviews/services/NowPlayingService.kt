package com.neilturner.aerialviews.services

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.util.Log
import androidx.core.content.getSystemService
import com.neilturner.aerialviews.models.prefs.GeneralPrefs
import com.neilturner.aerialviews.utils.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus


class NowPlayingService(private val context: Context, private val prefs: GeneralPrefs) :
    MediaSessionManager.OnActiveSessionsChangedListener
{
    private val _nowPlaying = MutableSharedFlow<String>(replay = 0)
    val nowPlaying
        get() = _nowPlaying.asSharedFlow()

    private var artistAndSong = ""
    private val coroutineScope = CoroutineScope(Dispatchers.Main) + Job()
    private val notificationListener = ComponentName(context, NotificationService::class.java)
    private val hasPermission = PermissionHelper.hasNotificationListenerPermission(context)
    private var sessionManager: MediaSessionManager? = null
    private var controllers = listOf<MediaController>()

    private val metadataListener = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            super.onMetadataChanged(metadata)
            updateNowPlaying(metadata, null)
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            super.onPlaybackStateChanged(state)

            if (state == null) {
                return
            }

            val active = isActive(state.state)
            updateNowPlaying(null, active)
        }
    }

    init {
        coroutineScope.launch {
            if (hasPermission) {
                setupSession()
            } else {
                Log.i(TAG, "No permission given to access media sessions")
            }
        }
    }

    private fun setupSession() {
        sessionManager = context.getSystemService<MediaSessionManager>()

        // Set metadata for active sessions
        onActiveSessionsChanged(sessionManager?.getActiveSessions(notificationListener))
        if (controllers.isNotEmpty()) {
            val activeController = controllers.first()
            val active = isActive(activeController.playbackState?.state)
            updateNowPlaying(activeController.metadata, active)
        }
        // Listen for future changes to active sessions
        sessionManager?.addOnActiveSessionsChangedListener(this, notificationListener)
    }

    private fun updateNowPlaying(metadata: MediaMetadata?, active: Boolean?) {
        if (metadata != null) {
            val song = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)?.take(40)
            val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)?.take(40)
            Log.i(TAG, "Metadata: $song - $artist")
            artistAndSong = "$song · $artist"
        }

        active.let {
            Log.i(TAG, "Active: $active")
            var update = ""
            if (active == true) {
                update = artistAndSong
            }
            coroutineScope.launch {
                _nowPlaying.emit(update)
            }
        }
    }

    override fun onActiveSessionsChanged(controllers: MutableList<MediaController>?) {
        Log.i(TAG, "onActiveSessionsChanged")
        unregisterAll()
        initControllers(controllers)
    }

    private fun initControllers(newControllers: List<MediaController>?) {
        controllers = if (!newControllers.isNullOrEmpty()) newControllers else emptyList()

        Log.i(TAG, "Registering controllers: ${controllers.size}")
        controllers.forEach { controller ->
            controller.registerCallback(metadataListener)
        }
    }

    private fun unregisterAll() {
        Log.i(TAG, "Unregistering controllers: ${controllers.size}")
        controllers.forEach { controller ->
            controller.unregisterCallback(metadataListener)
        }
    }

    fun stop() {
        unregisterAll()
        sessionManager?.removeOnActiveSessionsChangedListener(this)
        //sessionManager = null
        //coroutineScope.cancel()
    }

    private fun isActive(state: Int?): Boolean {
        return (state != PlaybackState.STATE_STOPPED
                && state != PlaybackState.STATE_PAUSED
                && state != PlaybackState.STATE_ERROR
                && state != PlaybackState.STATE_BUFFERING
                && state != PlaybackState.STATE_NONE)
    }

    companion object {
        private const val TAG = "NowPlayingService"
    }
}
