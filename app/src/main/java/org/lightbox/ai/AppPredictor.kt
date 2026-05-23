package org.lightbox.ai

import kotlinx.coroutines.flow.SharedFlow

interface AppPredictor {
    sealed class InteractionEvent {
        object TouchDown : InteractionEvent()
        object TouchUp : InteractionEvent()
        object Idle : InteractionEvent()
    }
    val interactionFlow: SharedFlow<InteractionEvent>
    fun onTouchDown()
    fun onTouchUp()
    fun onIdle()
    fun onAppLaunch(packageName: String)
}
