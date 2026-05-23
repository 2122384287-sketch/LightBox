package org.lightbox.hyperos

import android.app.Notification
import android.app.PendingIntent
import android.content.Context

interface SuperIslandNotifier {
    fun createChannel(context: Context)
    fun buildCastingNotification(
        context: Context,
        deviceName: String,
        isCasting: Boolean,
        stopPendingIntent: PendingIntent?
    ): Notification
}
