package org.lightbox.hyperos

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

interface HyperConnectManager {
    enum class CastState { IDLE, CONNECTING, CASTING, ERROR }
    val castState: StateFlow<CastState>
    fun startCasting(context: Context, targetDeviceId: String, displayId: Int, width: Int, height: Int)
    fun stopCasting(context: Context)
}
