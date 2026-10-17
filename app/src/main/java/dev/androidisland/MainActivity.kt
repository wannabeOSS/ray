package dev.androidisland

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import dev.androidisland.platform.IslandOverlayService
import dev.androidisland.ui.IslandApp

class MainActivity : ComponentActivity() {
    private val graph get() = (application as IslandApplication).graph
    private val cameraRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) graph.toggleTorch() else graph.message("Flashlight control needs camera permission")
    }
    private val notificationRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        graph.message(if (granted) "Session notifications enabled" else "Use the app to stop the session; Android still shows active foreground services")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        setContent { IslandApp(graph, ::toggleTorch, ::toggleOverlay, ::overlaySettings, ::notificationSettings,
            ::requestSessionNotifications, { graph.importClipboard(hasWindowFocus()) }) }
    }
    override fun onStart() { super.onStart(); graph.acquire() }
    override fun onResume() { super.onResume(); if (graph.locked()) graph.clearPrivateState() else graph.refreshMedia() }
    override fun onStop() { if (graph.locked()) graph.clearPrivateState(); graph.release(); super.onStop() }
    private fun toggleTorch() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) graph.toggleTorch()
        else cameraRequest.launch(Manifest.permission.CAMERA)
    }
    private fun toggleOverlay() {
        if (graph.state.value.overlayRunning) { stopService(Intent(this, IslandOverlayService::class.java)); return }
        if (!Settings.canDrawOverlays(this)) { graph.message("Grant display-over-other-apps access, return, then tap Start island"); overlaySettings(); return }
        runCatching { ContextCompat.startForegroundService(this, Intent(this, IslandOverlayService::class.java)) }
            .onFailure { graph.message("Android could not start the session. Try again from this screen.") }
    }
    private fun overlaySettings() { openSettings(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:$packageName".toUri())) }
    private fun notificationSettings() { openSettings(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
    private fun requestSessionNotifications() {
        if (Build.VERSION.SDK_INT >= 33) notificationRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
        else graph.message("This Android version uses notification channel settings")
    }
    private fun openSettings(intent: Intent) { runCatching { startActivity(intent) }.onFailure { graph.message("Open this app's permissions in Android Settings") } }
}
