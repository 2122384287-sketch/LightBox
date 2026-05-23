package org.lightbox.power

import kotlinx.coroutines.flow.Flow

interface PowerGovernor {
    enum class LoadLevel { IDLE, LIGHT, HEAVY }
    val batteryLevel: Flow<Float>
    fun applyOptimalFrequencies(level: LoadLevel)
}
