package org.lightbox.engine

import android.os.Process
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CpuAffinityImpl @Inject constructor() : CpuAffinity {

    companion object {
        private const val TAG = "CpuAffinity"
    }

    override fun bindAllToLittle() {
        runCatching {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            Log.d(TAG, "bindAllToLittle")
        }.onFailure { Log.w(TAG, "little failed", it) }
    }

    override fun bindUiToPerformance() {
        runCatching {
            Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND)
            Log.d(TAG, "bindUiToPerformance")
        }.onFailure { Log.w(TAG, "performance failed", it) }
    }

    override fun balancedProfile() {
        runCatching {
            Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT)
            Log.d(TAG, "balancedProfile")
        }.onFailure { Log.w(TAG, "balanced failed", it) }
    }

    override fun bindToX4Exclusive() {
        runCatching {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY)
            Log.d(TAG, "bindToX4Exclusive")
        }.onFailure { Log.w(TAG, "x4 failed", it) }
    }
}
