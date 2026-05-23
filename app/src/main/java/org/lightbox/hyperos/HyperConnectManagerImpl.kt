package org.lightbox.hyperos

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HyperConnectManagerImpl @Inject constructor() : HyperConnectManager {
    private val _castState = MutableStateFlow(HyperConnectManager.CastState.IDLE)
    override val castState: StateFlow<HyperConnectManager.CastState> = _castState.asStateFlow()

    override fun startCasting(context: Context, targetDeviceId: String, displayId: Int, width: Int, height: Int) {
        _castState.value = HyperConnectManager.CastState.CASTING
    }

    override fun stopCasting(context: Context) {
        _castState.value = HyperConnectManager.CastState.IDLE
    }
}
