package org.lightbox.power

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Process
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File

@Singleton
class PowerGovernorImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PowerGovernor {

    private var schedBoostPath: String? = null

    init {
        schedBoostPath = listOf(
            "/proc/sys/kernel/sched_boost",
            "/proc/sys/hyperos/sched_boost"
        ).firstOrNull { File(it).exists() }
    }

    override val batteryLevel: Flow<Float> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (scale > 0) trySend(level.toFloat() / scale)
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        awaitClose { context.unregisterReceiver(receiver) }
    }.distinctUntilChanged().flowOn(Dispatchers.Main)

    override fun applyOptimalFrequencies(level: PowerGovernor.LoadLevel) {
        val boostValue = when (level) {
            PowerGovernor.LoadLevel.IDLE -> 0
            PowerGovernor.LoadLevel.LIGHT -> 1
            PowerGovernor.LoadLevel.HEAVY -> 3
        }
        val path = schedBoostPath
        if (path != null) {
            try {
                File(path).writeText(boostValue.toString())
            } catch (_: Exception) {
                fallbackPriority(level)
            }
        } else {
            fallbackPriority(level)
        }
    }

    private fun fallbackPriority(level: PowerGovernor.LoadLevel) {
        val priority = when (level) {
            PowerGovernor.LoadLevel.IDLE -> Process.THREAD_PRIORITY_BACKGROUND
            PowerGovernor.LoadLevel.LIGHT -> Process.THREAD_PRIORITY_DEFAULT
            PowerGovernor.LoadLevel.HEAVY -> Process.THREAD_PRIORITY_FOREGROUND
        }
        Process.setThreadPriority(priority)
    }
}
