package io.taiga.client.push

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.unifiedpush.android.connector.UnifiedPush
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushRegistrationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun register() {
        val distributors = UnifiedPush.getDistributors(context)
        if (distributors.isNotEmpty()) {
            UnifiedPush.saveDistributor(context, distributors.first())
            UnifiedPush.registerApp(context)
        }
    }

    fun unregister() {
        UnifiedPush.unregisterApp(context)
    }

    fun getEndpoint(): String? {
        return context.getSharedPreferences("push_prefs", Context.MODE_PRIVATE)
            .getString("unifiedpush_endpoint", null)
    }

    fun hasDistributor(): Boolean {
        return UnifiedPush.getDistributors(context).isNotEmpty()
    }
}
