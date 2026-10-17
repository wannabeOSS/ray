package dev.androidisland

import android.app.KeyguardManager
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.SystemClock
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.core.content.ContextCompat
import dev.androidisland.domain.AgentStatus
import dev.androidisland.domain.AgentTask
import dev.androidisland.platform.MediaBridge
import dev.androidisland.platform.TorchBridge
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import java.io.IOException

private val Context.preferences by preferencesDataStore("island_preferences")
data class LaunchableApp(val id: String, val label: String)
data class ImportedClip(val id: Long, val text: String, val importedAt: Long)
data class MediaInfo(val title: String, val artist: String, val playing: Boolean, val canPlayPause: Boolean, val canNext: Boolean)
data class TorchInfo(val enabled: Boolean, val available: Boolean)
data class UiState(
    val apps: List<LaunchableApp> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val hidden: Set<String> = emptySet(),
    val aliases: Map<String, Set<String>> = emptyMap(),
    val clips: List<ImportedClip> = emptyList(),
    val clipboardPaused: Boolean = false,
    val media: MediaInfo? = null,
    val listenerConnected: Boolean = false,
    val torch: TorchInfo? = null,
    val agent: AgentTask? = null,
    val overlayRunning: Boolean = false,
    val message: String? = null,
)

class AppGraph(private val context: Context) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutable = MutableStateFlow(UiState())
    val state = mutable.asStateFlow()
    private val favoritesKey = stringSetPreferencesKey("favorites")
    private val hiddenKey = stringSetPreferencesKey("hidden")
    private val aliasesKey = stringPreferencesKey("aliases")
    val media = MediaBridge(context, ::setMedia)
    private val torch = TorchBridge(context) { value -> mutable.update { it.copy(torch = value) } }
    private var clients = 0
    private var demoJob: Job? = null

    init {
        ContextCompat.registerReceiver(context, object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_SCREEN_OFF) clearPrivateState()
                if (intent?.action == Intent.ACTION_USER_PRESENT) refreshMedia()
            }
        }, IntentFilter().apply { addAction(Intent.ACTION_SCREEN_OFF); addAction(Intent.ACTION_USER_PRESENT) }, ContextCompat.RECEIVER_NOT_EXPORTED)
        scope.launch {
            context.preferences.data.catch { cause ->
                if (cause is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw cause
            }.collect { prefs ->
                val aliases = runCatching {
                    val json = JSONObject(prefs[aliasesKey] ?: "{}")
                    json.keys().asSequence().associateWith { key ->
                        json.getString(key).split(',').filter { it.isNotBlank() }.toSet()
                    }
                }.getOrDefault(emptyMap())
                mutable.update { it.copy(favorites = prefs[favoritesKey] ?: emptySet(), hidden = prefs[hiddenKey] ?: emptySet(), aliases = aliases) }
            }
        }
        refreshApps()
    }
    fun refreshApps() = scope.launch {
        val apps = withContext(Dispatchers.IO) {
            @Suppress("DEPRECATION")
            context.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
                .map { LaunchableApp(ComponentName(it.activityInfo.packageName, it.activityInfo.name).flattenToString(), it.loadLabel(context.packageManager).toString().take(80)) }
                .distinctBy { it.id }.sortedBy { it.label.lowercase() }
        }
        mutable.update { it.copy(apps = apps) }
    }
    fun acquire() { if (clients++ == 0) torch.start() }
    fun release() { clients = (clients - 1).coerceAtLeast(0); if (clients == 0) torch.stop() }
    fun message(text: String?) { mutable.update { it.copy(message = text) } }
    fun setOverlay(running: Boolean) { mutable.update { it.copy(overlayRunning = running) } }
    private fun setMedia(value: MediaInfo?) { mutable.update { it.copy(media = value) } }
    fun listener(connected: Boolean) {
        mutable.update { it.copy(listenerConnected = connected, media = if (connected) it.media else null) }
        if (connected) media.start() else media.stop()
    }
    fun toggleFavorite(id: String) = scope.launch {
        context.preferences.edit { prefs ->
            val current = prefs[favoritesKey] ?: emptySet()
            prefs[favoritesKey] = if (id in current) current - id else current + id
        }
    }
    fun toggleHidden(id: String) = scope.launch {
        context.preferences.edit { prefs ->
            val current = prefs[hiddenKey] ?: emptySet()
            prefs[hiddenKey] = if (id in current) current - id else current + id
        }
    }
    fun saveAliases(id: String, value: String) = scope.launch {
        val normalized = value.take(160).split(',').map { it.trim().lowercase() }.filter { it.isNotEmpty() }.take(8)
        context.preferences.edit { prefs ->
            val json = runCatching { JSONObject(prefs[aliasesKey] ?: "{}") }.getOrDefault(JSONObject())
            json.put(id, normalized.joinToString(","))
            prefs[aliasesKey] = json.toString()
        }
        message("Aliases saved")
    }
    fun launchApp(id: String) {
        val component = ComponentName.unflattenFromString(id) ?: return
        runCatching { context.startActivity(Intent.makeMainActivity(component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            .onFailure { message("This app is no longer available") }
    }
    fun copy(text: String) {
        context.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Android Island", text.take(4096)))
        message("Copied")
    }
    fun importClipboard(hasFocus: Boolean) {
        if (!hasFocus || state.value.clipboardPaused || locked()) { message("Open this screen and resume imports first"); return }
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        val clip = clipboard.primaryClip
        if (clip == null || clip.itemCount == 0) { message("Clipboard has no readable text"); return }
        val sensitiveKey = if (android.os.Build.VERSION.SDK_INT >= 33) ClipDescription.EXTRA_IS_SENSITIVE else "android.content.extra.IS_SENSITIVE"
        if (clip.description.extras?.getBoolean(sensitiveKey, false) == true) { message("Sensitive clipboard items are not imported"); return }
        val text = clip.getItemAt(0).text?.toString()?.take(4096)
        if (text.isNullOrBlank()) { message("Only text clips can be imported"); return }
        val now = SystemClock.elapsedRealtimeNanos()
        mutable.update { it.copy(clips = (listOf(ImportedClip(now, text, System.currentTimeMillis())) + it.clips.filterNot { old -> old.text == text }).take(20)) }
    }
    fun deleteClip(id: Long) { mutable.update { it.copy(clips = it.clips.filterNot { clip -> clip.id == id }) } }
    fun clearClips() { mutable.update { it.copy(clips = emptyList()) } }
    fun pauseClipboard() { mutable.update { it.copy(clipboardPaused = !it.clipboardPaused) } }
    fun locked() = context.getSystemService(KeyguardManager::class.java).isKeyguardLocked
    fun toggleTorch() { if (!locked()) torch.toggle() { message(it) } }
    fun mediaAction(next: Boolean = false) { if (!locked()) media.perform(next) }
    fun openPlayer() { if (!locked()) media.openPlayer() }
    fun clearPrivateState() { clearClips(); media.stop(); mutable.update { it.copy(media = null) } }
    fun refreshMedia() { if (state.value.listenerConnected && !locked()) media.start() }
    fun runAgentDemo() {
        if (demoJob?.isActive == true) return
        val id = "demo-${SystemClock.elapsedRealtimeNanos()}"
        mutable.update { it.copy(agent = AgentTask(id, 0, "Demo agent · simulation", AgentStatus.QUEUED)) }
        demoJob = scope.launch {
            repeat(10) { index ->
                delay(600)
                val previous = state.value.agent ?: return@launch
                val next = previous.copy(revision = previous.revision + 1, status = AgentStatus.RUNNING, progress = index * 10)
                if (previous.accepts(next)) mutable.update { it.copy(agent = next) }
            }
            val previous = state.value.agent ?: return@launch
            mutable.update { it.copy(agent = previous.copy(revision = previous.revision + 1, status = AgentStatus.COMPLETED, progress = 100)) }
        }
    }
    fun cancelDemo() {
        demoJob?.cancel()
        mutable.update { state ->
            val task = state.agent
            state.copy(agent = task?.takeIf { it.status in setOf(AgentStatus.QUEUED, AgentStatus.RUNNING, AgentStatus.WAITING) }
                ?.copy(revision = task.revision + 1, status = AgentStatus.CANCELED) ?: task)
        }
    }
}
