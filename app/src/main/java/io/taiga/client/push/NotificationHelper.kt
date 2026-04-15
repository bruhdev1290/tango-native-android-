package io.taiga.client.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.taiga.client.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val CHANNEL_ASSIGNED = "taiga_assigned"
        const val CHANNEL_PROJECT = "taiga_project"
        const val CHANNEL_GENERAL = "taiga_general"
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channels = listOf(
            NotificationChannel(
                CHANNEL_ASSIGNED,
                "Assigned Items",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Notifications for items assigned to you" },
            NotificationChannel(
                CHANNEL_PROJECT,
                "Project Activity",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Notifications for project updates and changes" },
            NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "General app notifications" },
        )

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannels(channels)
    }

    fun showNotification(
        id: Int,
        title: String,
        message: String,
        channelId: String = CHANNEL_GENERAL,
    ) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(id, builder.build())
    }
}
