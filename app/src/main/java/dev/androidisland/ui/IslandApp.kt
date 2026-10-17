package dev.androidisland.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.androidisland.*
import dev.androidisland.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date
import java.util.Locale

private val Accent = Color(0xFFC6ADFF)
private val Muted = Color(0xFFAAA6B9)
private val Surface = Color(0xFF20202C)
private enum class Page { HOME, APPS, CLIPBOARD, EMOJI, SETTINGS }

@Composable
fun IslandApp(graph: AppGraph, torch: () -> Unit, overlay: () -> Unit, overlaySettings: () -> Unit,
              mediaSettings: () -> Unit, notifications: () -> Unit, importClip: () -> Unit) {
    val state by graph.state.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var page by remember { mutableStateOf(Page.HOME) }
    var editing by remember { mutableStateOf<LaunchableApp?>(null) }
    var grid by remember { mutableStateOf(true) }
    val entries = remember(state.apps, state.favorites, state.aliases, state.hidden) {
        state.apps.map { SearchEntry(it.id, it.label, "Application", state.aliases[it.id] ?: emptySet(), it.id in state.favorites, hidden = it.id in state.hidden) }
    }
    val results = remember(entries, query) { SearchRanker.search(entries, query) }
    val calculation = remember(query) { if (Regex("^[\\d(+\\-.]|^(sin|cos|tan|sqrt|floor|ceil|abs|ln|log|pi)\\b").containsMatchIn(query.trim())) Expression.evaluate(query).getOrNull() else null }
    val conversion = remember(query) { UnitConverter.convert(query) }
    val graphs by produceState<List<GraphSeries>>(emptyList(), query) {
        value = withContext(Dispatchers.Default) { GraphSampler.sample(query) }
    }
    val colors = darkColorScheme(primary = Accent, background = Color(0xFF101016), surface = Surface,
        onSurface = Color(0xFFF3EFFA), secondary = Color(0xFFDBD4EC))
    MaterialTheme(colorScheme = colors) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF282138), Color(0xFF101016), Color(0xFF15151E))))) {
            Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding().padding(horizontal = 18.dp).widthIn(max = 700.dp).align(Alignment.TopCenter)) {
                Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("ISLAND", style = MaterialTheme.typography.labelLarge, color = Accent, letterSpacing = 3.sp)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { page = Page.SETTINGS; query = "" }) { Text("Settings") }
                }
                IslandPreview(state, graph, torch, overlay)
                Spacer(Modifier.height(22.dp))
                Text(if (page == Page.HOME) "A little space.\nFor everything." else when(page) {
                    Page.APPS -> "Your apps"; Page.CLIPBOARD -> "Clipboard"; Page.EMOJI -> "Emoji search"; else -> "Make it yours"
                }, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = query, onValueChange = { query = it.take(512) }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true, shape = RoundedCornerShape(18.dp),
                    placeholder = { Text(when(page) { Page.CLIPBOARD -> "Search imported clips…"; Page.EMOJI -> "Search emoji…"; else -> "Search apps, calculate, or find a command…" }, style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Text("⌕", style = MaterialTheme.typography.headlineSmall, color = Accent) },
                    trailingIcon = { if (query.isNotEmpty()) TextButton(onClick = { query = "" }) { Text("Clear") } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search))
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(Page.HOME to "All", Page.APPS to "Apps", Page.CLIPBOARD to "Clips", Page.EMOJI to "Emoji").forEach { (target, label) ->
                        FilterChip(selected = page == target, onClick = { page = target; query = "" }, label = { Text(label) })
                    }
                }
                state.message?.let { message ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp)).background(Surface).padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(message, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = Accent)
                        TextButton(onClick = { graph.message(null) }) { Text("Dismiss") }
                    }
                }
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
                    if (page == Page.SETTINGS) {
                        item { Section("CAPABILITIES") }
                        item { ActionRow("◉", "Display over other apps", "Optional · needed for the floating island", overlaySettings) }
                        item { ActionRow("♫", "Media access", if (state.listenerConnected) "Connected · media controls available" else "Optional · Android notification-listener settings", mediaSettings) }
                        item { ActionRow("◌", "Session notifications", "Allow a visible session notification with Stop", notifications) }
                        item { ActionRow("▦", "Manage apps & aliases", "Pin, hide and add search keywords", { page = Page.APPS; query = "" }) }
                        item { ActionRow("✦", "AI, live rates & lyrics", "Providers are not connected in this development build", { graph.message("Provider integration is planned. Local tools work offline.") }) }
                        item { Text("Clipboard imports stay in memory and clear on lock. Notification message content is not read in this build. External AI agents are not connected.", color = Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp)) }
                    } else if (page == Page.CLIPBOARD) {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = importClip, enabled = !state.clipboardPaused) { Text("Import") }
                                OutlinedButton(onClick = { graph.pauseClipboard() }) { Text(if (state.clipboardPaused) "Resume" else "Pause") }
                                TextButton(onClick = { graph.clearClips() }) { Text("Clear all") }
                            }
                        }
                        item { Text("Manual imports while this screen is focused. ${state.clips.size}/20 items · session only", color = Muted, style = MaterialTheme.typography.bodySmall) }
                        val clips = state.clips.filter { it.text.contains(query, ignoreCase = true) }
                        if (clips.isEmpty()) item { EmptyCard("Your clipboard, by choice", "Copy some text, return here and tap Import. Background capture is not enabled.") }
                        items(clips, key = { it.id }) { clip ->
                            CardBox {
                                Text(clip.text, maxLines = 4, overflow = TextOverflow.Ellipsis)
                                Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(clip.importedAt)), color = Muted, style = MaterialTheme.typography.labelSmall)
                                Row { TextButton(onClick = { graph.copy(clip.text) }) { Text("Copy") }; TextButton(onClick = { graph.deleteClip(clip.id) }) { Text("Delete") } }
                            }
                        }
                    } else if (page == Page.EMOJI) {
                        EmojiCatalog.search(query).groupBy { it.category }.forEach { (category, emoji) ->
                            item { Section(category.uppercase()) }
                            emoji.chunked(4).forEach { row ->
                                item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { entry ->
                                        Box(Modifier.weight(1f).heightIn(min = 68.dp).clip(RoundedCornerShape(14.dp)).background(Surface)
                                            .semantics { contentDescription = "Copy ${entry.name}" }.clickable { graph.copy(entry.symbol) }, contentAlignment = Alignment.Center) {
                                            Text(entry.symbol, style = MaterialTheme.typography.headlineMedium)
                                        }
                                    }
                                } }
                            }
                        }
                    } else {
                        if (page == Page.HOME && query.isNotBlank()) {
                            val commands = listOf(
                                Triple("Flashlight", "torch light", torch),
                                Triple("Run agent demo", "agent task progress simulation", { graph.runAgentDemo(); Unit }),
                                Triple("Clipboard", "paste history clips", { page = Page.CLIPBOARD; query = "" }),
                                Triple("Emoji search", "smile gestures symbols", { page = Page.EMOJI; query = "" }),
                                Triple("Settings", "permissions configuration", { page = Page.SETTINGS; query = "" }),
                                Triple("Toggle floating island", "overlay start stop", overlay),
                            ).filter { (it.first + " " + it.second).contains(query.trim(), ignoreCase = true) }
                            items(commands, key = { "command-${it.first}" }) { command ->
                                ActionRow("⌘", command.first, "Command", command.third)
                            }
                            calculation?.let { value -> item { ValueCard("CALCULATOR", format(value), "Calculated locally · radians", { graph.copy(format(value)) }) } }
                            conversion?.let { (value, unit) -> item { ValueCard("CONVERSION", "${format(value)} $unit", "Offline unit conversion", { graph.copy("${format(value)} $unit") }) } }
                            if (graphs.isNotEmpty()) item { GraphCard(graphs) }
                            if (Regex("(?i)\\b(eth|btc|usd|inr|eur)\\b|\\d(?:eth|btc)$").containsMatchIn(query)) item {
                                EmptyCard("Live conversion", "Connect a rate provider to show a timestamped quote and price history. No live rate is configured.")
                            }
                        }
                        if (page == Page.HOME && query.isBlank()) {
                            state.media?.let { media -> item { MediaCard(media, graph) } }
                            item {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Section("FAVORITES", Modifier.weight(1f))
                                    TextButton(onClick = { grid = !grid }) { Text(if (grid) "List" else "Grid") }
                                }
                            }
                            val favorites = state.apps.filter { it.id in state.favorites && it.id !in state.hidden }.take(8)
                            if (favorites.isEmpty()) item { Text("Pin an app with its Edit button to keep it here.", color = Muted, style = MaterialTheme.typography.bodySmall) }
                            if (grid) favorites.chunked(4).forEach { row -> item {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { app -> Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Surface).clickable { graph.launchApp(app.id) }.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        AppBadge(app.label); Spacer(Modifier.height(8.dp)); Text(app.label, maxLines = 1, style = MaterialTheme.typography.labelSmall, overflow = TextOverflow.Ellipsis)
                                    } }
                                }
                            } } else items(favorites, key = { "fav-${it.id}" }) { app -> AppRow(app, state, graph) { editing = app } }
                            item { Section("QUICK COMMANDS") }
                            item { ActionRow("🔦", "Flashlight", if (state.torch?.enabled == true) "On · tap to turn off" else "Tap to toggle", torch) }
                            item { ActionRow("✦", "Run agent demo", "Simulated progress · no model or external task", { graph.runAgentDemo() }) }
                            item { ActionRow("∿", "Graph a function", "Try y=sin(x*2)*5,y2=floor(x)", { query = "y=sin(x*2)*5,y2=floor(x)" }) }
                            item { ActionRow("📋", "Clipboard", "Explicit imports, search and copy", { page = Page.CLIPBOARD }) }
                        }
                        if (query.isNotBlank() || page == Page.APPS) {
                            item { Section(if (page == Page.APPS) "APPS · EDIT TO PIN, HIDE OR ADD ALIASES" else "APP RESULTS · ${results.size}") }
                            // Manage view includes hidden apps, so the user can restore visibility.
                            val apps = if (page == Page.APPS) state.apps.filter { query.isBlank() || it.label.contains(query, true) || state.aliases[it.id]?.any { a -> a.contains(query, true) } == true } else results.mapNotNull { entry -> state.apps.find { it.id == entry.id } }
                            items(apps, key = { it.id }) { app -> AppRow(app, state, graph) { editing = app } }
                            if (apps.isEmpty() && calculation == null && conversion == null && graphs.isEmpty()) item { EmptyCard("No local result", "Try an app name, 12*8, 10 km to mi, or open Emoji.") }
                        }
                        if (page == Page.HOME && query.isNotBlank()) item { ActionRow("✦", "Ask AI: ${query.take(60)}", "Provider not connected · nothing will be sent", { graph.message("Choose an AI provider in a future setup step. No query has been sent.") }) }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date()), color = Muted, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.weight(1f))
                    Text("LOCAL FIRST  ·  DEV 0.1", color = Accent, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        editing?.let { app ->
            var aliases by remember(app.id, state.aliases) { mutableStateOf(state.aliases[app.id]?.joinToString(", ") ?: "") }
            AlertDialog(onDismissRequest = { editing = null }, title = { Text(app.label) }, text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text("Favorite", Modifier.weight(1f)); Switch(app.id in state.favorites, { graph.toggleFavorite(app.id) }) }
                    Row(verticalAlignment = Alignment.CenterVertically) { Text("Visible in search", Modifier.weight(1f)); Switch(app.id !in state.hidden, { graph.toggleHidden(app.id) }) }
                    OutlinedTextField(aliases, { aliases = it.take(160) }, label = { Text("Aliases, separated by commas") }, placeholder = { Text("yt, video") })
                    Text("App shortcuts and priority controls are planned separately.", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }, confirmButton = { TextButton(onClick = { graph.saveAliases(app.id, aliases); editing = null }) { Text("Save") } }, dismissButton = { TextButton(onClick = { editing = null }) { Text("Close") } })
        }
    }
}

@Composable private fun IslandPreview(state: UiState, graph: AppGraph, torch: () -> Unit, overlay: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Color(0xFF08080C)).border(1.dp, Color(0xFF3F354E), RoundedCornerShape(28.dp)).padding(16.dp)) {
        Row(Modifier.fillMaxWidth().clickable { expanded = !expanded }.heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).background(if (state.overlayRunning) Color(0xFFA5E4BA) else Accent, CircleShape))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                val agent = state.agent?.takeIf { it.status in setOf(AgentStatus.QUEUED, AgentStatus.RUNNING, AgentStatus.WAITING) }
                Text(when { agent != null -> "Demo agent · ${agent.progress ?: 0}%"; state.torch?.enabled == true -> "Flashlight is on"; state.media?.playing == true -> "Music is playing"; else -> "Your island, at a glance" }, fontWeight = FontWeight.Medium)
                Text(if (state.overlayRunning) "Floating session active" else "In-app preview · tap to expand", color = Muted, style = MaterialTheme.typography.labelSmall)
            }
            Text(if (expanded) "−" else "+", color = Accent, style = MaterialTheme.typography.headlineSmall)
        }
        if (expanded) {
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color(0xFF302B38))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = torch) { Text(if (state.torch?.enabled == true) "Torch off" else "Torch on") }
                TextButton(onClick = { graph.runAgentDemo() }) { Text("Demo agent") }
                if (state.agent?.status in setOf(AgentStatus.RUNNING, AgentStatus.QUEUED)) TextButton(onClick = { graph.cancelDemo() }) { Text("Cancel") }
            }
            state.agent?.let { Text("${it.label}: ${it.status.name.lowercase()} · ${it.progress ?: 0}%", color = Muted, style = MaterialTheme.typography.bodySmall) }
            TextButton(onClick = overlay) { Text(if (state.overlayRunning) "Stop floating island" else "Start floating island") }
            Text("Shows active music, flashlight or demo progress. Hides when locked or in landscape.", color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable private fun CardBox(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Surface).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
}
@Composable private fun Section(text: String, modifier: Modifier = Modifier) { Text(text, modifier.padding(top = 10.dp, bottom = 4.dp), color = Muted, style = MaterialTheme.typography.labelSmall) }
@Composable private fun ActionRow(symbol: String, title: String, subtitle: String, action: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).clickable(onClick = action).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(symbol, Modifier.width(36.dp), color = Accent, style = MaterialTheme.typography.titleLarge)
        Column(Modifier.weight(1f)) { Text(title, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium); Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall) }
        Text("›", color = Muted)
    }
}
@Composable private fun EmptyCard(title: String, detail: String) { CardBox { Text(title, fontWeight = FontWeight.Medium); Text(detail, color = Muted, style = MaterialTheme.typography.bodySmall) } }
@Composable private fun ValueCard(label: String, value: String, detail: String, copy: () -> Unit) { CardBox { Section(label); Text(value, color = Accent, style = MaterialTheme.typography.headlineMedium); Text(detail, color = Muted, style = MaterialTheme.typography.bodySmall); TextButton(onClick = copy) { Text("Copy result") } } }
@Composable private fun AppBadge(label: String) { Box(Modifier.size(36.dp).background(Color(0xFF39304C), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Text(label.take(1).uppercase(), color = Accent, fontWeight = FontWeight.Bold) } }
@Composable private fun AppRow(app: LaunchableApp, state: UiState, graph: AppGraph, edit: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f).clickable { graph.launchApp(app.id) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            AppBadge(app.label); Spacer(Modifier.width(12.dp)); Column {
                Text(app.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (app.id in state.hidden) "Hidden from search" else if (app.id in state.favorites) "Favorite" else state.aliases[app.id]?.joinToString(" · ") ?: "Application", color = Muted, style = MaterialTheme.typography.labelSmall)
            }
        }
        TextButton(onClick = edit) { Text("Edit") }
    }
}
@Composable private fun MediaCard(media: MediaInfo, graph: AppGraph) { CardBox {
    Section("NOW PLAYING"); Text(media.title, fontWeight = FontWeight.SemiBold); Text(media.artist, color = Muted)
    Row {
        TextButton(onClick = { graph.mediaAction() }, enabled = media.canPlayPause) { Text(if (media.playing) "Pause" else "Play") }
        TextButton(onClick = { graph.mediaAction(true) }, enabled = media.canNext) { Text("Next") }
        TextButton(onClick = { graph.openPlayer() }) { Text("Open player") }
    }
} }
@Composable private fun GraphCard(series: List<GraphSeries>) { CardBox {
    Section("GRAPH · RADIANS · −10 ≤ X, Y ≤ 10")
    val colors = listOf(Color(0xFFFF4E9A), Color(0xFFB9EB66), Accent, Color(0xFF6CD6FF))
    Canvas(Modifier.fillMaxWidth().height(180.dp).semantics { contentDescription = "Function graph: ${series.joinToString { it.expression }}" }) {
        for (i in 0..4) {
            val x = size.width * i / 4; val y = size.height * i / 4
            drawLine(Color(0xFF3A3742), Offset(x, 0f), Offset(x, size.height))
            drawLine(Color(0xFF3A3742), Offset(0f, y), Offset(size.width, y))
        }
        series.forEachIndexed { i, graph ->
            val path = Path(); var previous: Offset? = null
            graph.points.forEach { point ->
                val y = point.y?.takeIf { it in -10.0..10.0 }
                if (y == null) previous = null else {
                    val p = Offset(((point.x + 10) / 20 * size.width).toFloat(), ((10 - y) / 20 * size.height).toFloat())
                    if (previous == null || kotlin.math.abs(previous!!.y - p.y) > size.height / 2) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                    previous = p
                }
            }
            drawPath(path, colors[i % colors.size], style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
        }
    }
    series.forEachIndexed { i, graph -> Text("●  ${graph.expression}", color = colors[i % colors.size], style = MaterialTheme.typography.labelSmall) }
} }
private fun format(value: Double) = String.format(Locale.ROOT, "%.10g", value)
