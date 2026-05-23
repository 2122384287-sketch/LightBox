package org.lightbox.hyperos

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuperIslandNotifierImpl @Inject constructor() : SuperIslandNotifier {

    companion object {
        private const val CHANNEL_ID = "lightbox_super_island"
        private const val EXTRA_MIUI_FOCUS = "miui.focus.param"
    }

    override fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "LightBox 跨设备控制",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }

    override fun buildCastingNotification(
        context: Context,
        deviceName: String,
        isCasting: Boolean,
        stopPendingIntent: PendingIntent?
    ): Notification {
        val focusJson = JSONObject()
            .put("type", if (isCasting) "casting" else "paused")
            .put("device", deviceName)
            .put("title", "LightBox")
            .put("content", if (isCasting) "投射中" else "投射已暂停")
        val extras = Bundle().apply {
            putString(EXTRA_MIUI_FOCUS, focusJson.toString())
        }
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("LightBox")
            .setContentText(if (isCasting) "投射中 → $deviceName" else "投射已暂停")
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .setOngoing(isCasting)
            .setExtras(extras)
            .apply {
                if (stopPendingIntent != null) {
                    addAction(
                        android.R.drawable.ic_media_pause,
                        "停止投射",
                        stopPendingIntent
                    )
                }
            }
            .build()
    }
}
