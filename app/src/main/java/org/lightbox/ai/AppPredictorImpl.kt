package org.lightbox.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPredictorImpl @Inject constructor() : AppPredictor {

    private val _events = MutableStateFlow<AppPredictor.InteractionEvent>(
        AppPredictor.InteractionEvent.Idle
    )

    override val interactionFlow = _events.asStateFlow()

    override fun onTouchDown() {
        _events.value = AppPredictor.InteractionEvent.TouchDown
    }

    override fun onTouchUp() {
        _events.value = AppPredictor.InteractionEvent.TouchUp
    }

    override fun onIdle() {
        _events.value = AppPredictor.InteractionEvent.Idle
    }

    override fun onAppLaunch(packageName: String) {
        // 后续接入 NPU 推理
    }
}
