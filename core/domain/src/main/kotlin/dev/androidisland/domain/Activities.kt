package dev.androidisland.domain

enum class AgentStatus { QUEUED, RUNNING, WAITING, COMPLETED, FAILED, CANCELED }
data class AgentTask(val id: String, val revision: Long, val label: String, val status: AgentStatus, val progress: Int? = null) {
    init {
        require(id.isNotBlank() && id.length <= 128)
        require(label.length <= 160 && revision >= 0)
        require(progress == null || progress in 0..100)
    }
    fun accepts(next: AgentTask): Boolean {
        if (next.id != id || next.revision <= revision) return false
        if (status in setOf(AgentStatus.COMPLETED, AgentStatus.FAILED, AgentStatus.CANCELED)) return false
        return when (status) {
            AgentStatus.QUEUED -> next.status in setOf(AgentStatus.RUNNING, AgentStatus.CANCELED, AgentStatus.FAILED)
            AgentStatus.RUNNING, AgentStatus.WAITING -> next.status != AgentStatus.QUEUED
            else -> false
        }
    }
}

enum class ActivityKind { NOTIFICATION, CHARGING, AGENT, TORCH, MEDIA }
data class IslandItem(val id: String, val kind: ActivityKind, val label: String, val detail: String, val expiresAt: Long? = null)

/** Called from a single serialized owner; content is bounded and never persisted. */
class IslandEngine {
    private val items = linkedMapOf<String, IslandItem>()
    private var expandedId: String? = null
    fun put(item: IslandItem) {
        require(item.id.length <= 128 && item.label.length <= 160 && item.detail.length <= 320)
        items.remove(item.id)
        items[item.id] = item
        while (items.size > 32) {
            val victim = items.keys.firstOrNull { it != expandedId } ?: items.keys.first()
            items.remove(victim)
        }
    }
    fun remove(id: String) { items.remove(id); if (expandedId == id) expandedId = null }
    fun expand(id: String?) { expandedId = id?.takeIf { it in items } }
    fun clear() { items.clear(); expandedId = null }
    fun select(now: Long, allowed: Boolean): IslandItem? {
        if (!allowed) { clear(); return null }
        items.values.filter { it.expiresAt?.let { deadline -> deadline <= now } == true }.map { it.id }.forEach(::remove)
        expandedId?.let { items[it]?.let { selected -> return selected } }
        return items.values.minWithOrNull(compareBy<IslandItem> { it.kind.ordinal }.thenBy { it.id })
    }
    val size: Int get() = items.size
}
