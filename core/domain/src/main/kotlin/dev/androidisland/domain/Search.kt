package dev.androidisland.domain

import java.util.Locale

data class SearchEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val aliases: Set<String> = emptySet(),
    val favorite: Boolean = false,
    val priority: Int = 0,
    val hidden: Boolean = false,
)

object SearchRanker {
    fun search(entries: List<SearchEntry>, query: String): List<SearchEntry> {
        val q = query.trim().lowercase(Locale.ROOT).take(512)
        return entries.asSequence().filterNot { it.hidden }.mapNotNull { entry ->
            val name = entry.title.lowercase(Locale.ROOT)
            val aliases = entry.aliases.map { it.lowercase(Locale.ROOT) }
            val match = when {
                q.isEmpty() -> 0
                name == q || q in aliases -> 1000
                name.startsWith(q) || aliases.any { it.startsWith(q) } -> 700
                name.contains(q) || aliases.any { it.contains(q) } -> 400
                else -> return@mapNotNull null
            }
            entry to (match + entry.priority.coerceIn(-100, 100) + if (entry.favorite) 50 else 0)
        }.sortedWith(compareByDescending<Pair<SearchEntry, Int>> { it.second }
            .thenBy { it.first.title.lowercase(Locale.ROOT) }.thenBy { it.first.id })
            .take(40).map { it.first }.toList()
    }
}

object UnitConverter {
    private data class Unit(val dimension: String, val scale: Double)
    private val units = mapOf(
        "m" to Unit("length", 1.0), "km" to Unit("length", 1000.0),
        "cm" to Unit("length", 0.01), "mi" to Unit("length", 1609.344),
        "ft" to Unit("length", 0.3048), "in" to Unit("length", 0.0254),
        "kg" to Unit("mass", 1.0), "g" to Unit("mass", 0.001),
        "lb" to Unit("mass", 0.45359237), "oz" to Unit("mass", 0.028349523125),
    )
    fun convert(query: String): Pair<Double, String>? {
        val m = Regex("^\\s*(-?\\d+(?:\\.\\d+)?)\\s*([a-z]+)\\s+(?:to|in)\\s+([a-z]+)\\s*$")
            .matchEntire(query.lowercase(Locale.ROOT)) ?: return null
        val value = m.groupValues[1].toDoubleOrNull()?.takeIf { it.isFinite() } ?: return null
        val from = m.groupValues[2]; val to = m.groupValues[3]
        if (from in setOf("c", "f", "k") && to in setOf("c", "f", "k")) {
            val c = when (from) { "f" -> (value - 32) * 5 / 9; "k" -> value - 273.15; else -> value }
            if (c < -273.15) return null
            return when (to) { "f" -> c * 9 / 5 + 32; "k" -> c + 273.15; else -> c } to to.uppercase(Locale.ROOT)
        }
        val a = units[from] ?: return null; val b = units[to] ?: return null
        return if (a.dimension == b.dimension) (value * a.scale / b.scale) to to else null
    }
}

data class EmojiEntry(val symbol: String, val name: String, val category: String)
object EmojiCatalog {
    val entries = listOf(
        EmojiEntry("😀", "grinning happy smile", "Faces"), EmojiEntry("😂", "laugh tears joy", "Faces"),
        EmojiEntry("🥳", "party celebrate", "Faces"), EmojiEntry("🤔", "thinking", "Faces"),
        EmojiEntry("😭", "cry sad", "Faces"), EmojiEntry("😴", "sleep tired", "Faces"),
        EmojiEntry("👍", "thumbs up yes", "Gestures"), EmojiEntry("👎", "thumbs down no", "Gestures"),
        EmojiEntry("👋", "wave hello", "Gestures"), EmojiEntry("🙌", "hands celebrate", "Gestures"),
        EmojiEntry("🤝", "handshake agreement", "Gestures"), EmojiEntry("🙏", "thanks pray", "Gestures"),
        EmojiEntry("✌️", "peace victory", "Gestures"), EmojiEntry("🤘", "rock horns", "Gestures"),
        EmojiEntry("👌", "okay hand", "Gestures"), EmojiEntry("❤️", "love heart", "Symbols"),
        EmojiEntry("✨", "sparkles magic", "Symbols"), EmojiEntry("🔥", "fire hot", "Symbols"),
        EmojiEntry("🚀", "rocket launch", "Objects"), EmojiEntry("🤖", "robot agent ai", "Objects"),
        EmojiEntry("🎵", "music note", "Objects"), EmojiEntry("🔦", "flashlight torch", "Objects"),
        EmojiEntry("✅", "check done", "Symbols"), EmojiEntry("📋", "clipboard", "Objects"),
    )
    fun search(query: String) = entries.filter {
        query.isBlank() || (it.name + " " + it.category).contains(query.trim(), ignoreCase = true)
    }
}
