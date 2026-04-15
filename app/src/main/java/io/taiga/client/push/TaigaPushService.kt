package io.taiga.client.push

import android.content.Context
import dagger.hilt.android.AndroidEntryPoint
import org.unifiedpush.android.connector.MessagingReceiver
import javax.inject.Inject

@AndroidEntryPoint
class TaigaPushService : MessagingReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onMessage(context: Context, message: ByteArray, instance: String) {
        val content = message.decodeToString()
        val title = content.lineSequence().firstOrNull()?.takeIf { it.isNotBlank() } ?: "Taiga"
        val body = content.lineSequence().drop(1).firstOrNull()?.takeIf { it.isNotBlank() } ?: content
        notificationHelper.showNotification(
            id = System.currentTimeMillis().toInt(),
            title = title,
            message = body,
            channelId = NotificationHelper.CHANNEL_GENERAL,
        )
    }

    override fun onNewEndpoint(context: Context, endpoint: String, instance: String) {
        val prefs = context.getSharedPreferences("push_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("unifiedpush_endpoint", endpoint).apply()
    }

    override fun onRegistrationFailed(context: Context, instance: String) {
        // Registration failed
    }

    override fun onUnregistered(context: Context, instance: String) {
        val prefs = context.getSharedPreferences("push_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("unifiedpush_endpoint").apply()
    }
}
