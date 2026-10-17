package dev.androidisland.platform

import android.app.*
import android.content.*
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dev.androidisland.IslandApplication
import dev.androidisland.MainActivity
import dev.androidisland.R
import dev.androidisland.UiState
import dev.androidisland.domain.AgentStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

/** Small native window for the feasibility slice; Compose owns the focused palette. */
class IslandOverlayService : Service() {
    private val graph get() = (application as IslandApplication).graph
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var windows: WindowManager
    private var window: LinearLayout? = null
    private var expanded = false
    private var active = false
    private var registered = false
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) { detach(); graph.clearPrivateState() }
            if (intent?.action == Intent.ACTION_USER_PRESENT) { graph.refreshMedia(); render(graph.state.value) }
        }
    }
    override fun onCreate() {
        super.onCreate()
        windows = getSystemService(WindowManager::class.java)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("island_session", getString(R.string.overlay_channel), NotificationManager.IMPORTANCE_LOW))
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val stop = PendingIntent.getService(this, 1, Intent(this, javaClass).setAction("stop"), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(this, "island_session")
            .setSmallIcon(R.drawable.ic_island).setContentTitle(getString(R.string.overlay_running))
            .setContentText(getString(R.string.overlay_description)).setContentIntent(open)
            .setOngoing(true).addAction(0, getString(R.string.stop), stop).build()
        try { startForeground(1, notification) } catch (_: RuntimeException) {
            graph.message("Android could not start the island session"); stopSelf(); return
        }
        active = true
        graph.acquire(); graph.setOverlay(true)
        ContextCompat.registerReceiver(this, receiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF); addAction(Intent.ACTION_USER_PRESENT)
        }, ContextCompat.RECEIVER_NOT_EXPORTED)
        registered = true
        scope.launch { graph.state.collect { render(it) } }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "stop") stopSelf()
        return START_NOT_STICKY
    }
    private fun render(state: UiState) {
        if (!active) return
        if (!Settings.canDrawOverlays(this)) { detach(); stopSelf(); return }
        val power = getSystemService(android.os.PowerManager::class.java)
        if (!power.isInteractive || graph.locked() || resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) { detach(); return }
        val agent = state.agent?.takeIf { it.status in setOf(AgentStatus.QUEUED, AgentStatus.RUNNING, AgentStatus.WAITING) }
        val title: String
        val detail: String
        when {
            agent != null -> { title = "Demo agent · ${agent.progress ?: 0}%"; detail = "Simulation · ${agent.status.name.lowercase()}" }
            state.torch?.enabled == true -> { title = "Flashlight is on"; detail = "Open commands for flashlight controls" }
            state.media?.playing == true -> { title = "Music is playing"; detail = "Open commands for playback controls" }
            else -> { detach(); return }
        }
        val root = window ?: LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(8), dp(16), dp(8))
            background = GradientDrawable().apply { setColor(Color.rgb(17, 17, 23)); cornerRadius = dp(28).toFloat(); setStroke(dp(1), Color.rgb(83, 72, 110)) }
        }
        root.removeAllViews()
        root.addView(TextView(this).apply {
            text = title; textSize = 15f; setTextColor(Color.rgb(224, 211, 255)); setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER; minHeight = dp(48)
            contentDescription = "$title. Tap to ${if (expanded) "collapse" else "expand"}"
            isClickable = true; isFocusable = true
            setOnClickListener { expanded = !expanded; render(graph.state.value) }
        })
        if (expanded) {
            root.addView(TextView(this).apply { text = detail; textSize = 12f; setTextColor(Color.LTGRAY); gravity = Gravity.CENTER })
            root.addView(Button(this).apply { text = getString(R.string.open_commands); setOnClickListener {
                startActivity(Intent(this@IslandOverlayService, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } })
            root.addView(Button(this).apply { text = getString(R.string.stop_island); setOnClickListener { stopSelf() } })
        }
        val bounds = if (android.os.Build.VERSION.SDK_INT >= 30) windows.currentWindowMetrics.bounds.width() else resources.displayMetrics.widthPixels
        val top = if (android.os.Build.VERSION.SDK_INT >= 30) windows.currentWindowMetrics.windowInsets
            .getInsetsIgnoringVisibility(WindowInsets.Type.statusBars() or WindowInsets.Type.displayCutout()).top else dp(32)
        val params = WindowManager.LayoutParams(
            minOf(dp(300), (bounds - dp(32)).coerceAtLeast(dp(100))), WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_SECURE,
            PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = top + dp(8) }
        try {
            if (window == null) { windows.addView(root, params); window = root } else windows.updateViewLayout(root, params)
        } catch (_: RuntimeException) { detach(); graph.message("Island window unavailable. Open the app to restart."); stopSelf() }
    }
    override fun onConfigurationChanged(newConfig: Configuration) { super.onConfigurationChanged(newConfig); detach(); render(graph.state.value) }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun detach() { window?.let { runCatching { windows.removeViewImmediate(it) } }; window = null; expanded = false }
    override fun onDestroy() {
        scope.cancel(); detach()
        if (registered) unregisterReceiver(receiver)
        if (active) { graph.release(); graph.setOverlay(false); graph.clearClips() }
        active = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
