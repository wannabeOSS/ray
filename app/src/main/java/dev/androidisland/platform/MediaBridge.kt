package dev.androidisland.platform

import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import dev.androidisland.MediaInfo

class MediaBridge(private val context: Context, private val changed: (MediaInfo?) -> Unit) {
    private val manager = context.getSystemService(MediaSessionManager::class.java)
    private val component = ComponentName(context, IslandNotificationListener::class.java)
    private val controllers = mutableListOf<MediaController>()
    private var registered = false
    private var selected: MediaController? = null
    private val callback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = publish()
        override fun onPlaybackStateChanged(state: PlaybackState?) = publish()
        override fun onSessionDestroyed() { reconcile() }
    }
    private val listener = MediaSessionManager.OnActiveSessionsChangedListener { reconcile() }
    fun start() {
        if (context.getSystemService(KeyguardManager::class.java).isKeyguardLocked) { stop(); return }
        runCatching {
            if (!registered) {
                manager.addOnActiveSessionsChangedListener(listener, component, Handler(Looper.getMainLooper()))
                registered = true
            }
            reconcile()
        }.onFailure { stop() }
    }
    private fun reconcile() {
        runCatching {
            controllers.forEach { it.unregisterCallback(callback) }
            controllers.clear()
            controllers.addAll(manager.getActiveSessions(component).take(8))
            controllers.forEach { it.registerCallback(callback, Handler(Looper.getMainLooper())) }
            publish()
        }.onFailure { stop() }
    }
    private fun publish() {
        if (context.getSystemService(KeyguardManager::class.java).isKeyguardLocked) { stop(); return }
        selected = controllers.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING } ?: controllers.firstOrNull()
        val c = selected ?: run { changed(null); return }
        val playback = c.playbackState
        val actions = playback?.actions ?: 0L
        val playing = playback?.state == PlaybackState.STATE_PLAYING
        val playPause = if (playing) PlaybackState.ACTION_PAUSE else PlaybackState.ACTION_PLAY
        changed(MediaInfo(
            c.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)?.take(160) ?: "Media session",
            c.metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)?.take(160) ?: "",
            playing, actions and (playPause or PlaybackState.ACTION_PLAY_PAUSE) != 0L,
            actions and PlaybackState.ACTION_SKIP_TO_NEXT != 0L,
        ))
    }
    fun perform(next: Boolean) {
        val c = selected ?: return
        val state = c.playbackState ?: return
        val playing = state.state == PlaybackState.STATE_PLAYING
        val required = if (next) PlaybackState.ACTION_SKIP_TO_NEXT else if (playing) PlaybackState.ACTION_PAUSE else PlaybackState.ACTION_PLAY
        val supported = state.actions and required != 0L || (!next && state.actions and PlaybackState.ACTION_PLAY_PAUSE != 0L)
        if (!supported) return
        runCatching { if (next) c.transportControls.skipToNext() else if (playing) c.transportControls.pause() else c.transportControls.play() }
    }
    fun openPlayer() { runCatching { selected?.sessionActivity?.send() } }
    fun stop() {
        if (registered) manager.removeOnActiveSessionsChangedListener(listener)
        registered = false
        controllers.forEach { it.unregisterCallback(callback) }
        controllers.clear(); selected = null; changed(null)
    }
}
