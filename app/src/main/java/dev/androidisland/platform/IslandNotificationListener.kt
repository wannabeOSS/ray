package dev.androidisland.platform

import android.service.notification.NotificationListenerService
import dev.androidisland.IslandApplication

/** Initial integration uses listener authorization for media; message content is not read. */
class IslandNotificationListener : NotificationListenerService() {
    private val graph get() = (application as IslandApplication).graph
    override fun onListenerConnected() { super.onListenerConnected(); graph.listener(true) }
    override fun onListenerDisconnected() { graph.listener(false); super.onListenerDisconnected() }
    override fun onDestroy() { graph.listener(false); super.onDestroy() }
}
