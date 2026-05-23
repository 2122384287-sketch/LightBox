package org.lightbox.domain.system

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.lightbox.ai.AppPredictor
import org.lightbox.engine.afme.AfmeController
import org.lightbox.hyperos.HyperConnectManager
import org.lightbox.native.CpuAffinity
import org.lightbox.power.PowerGovernor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemCoordinator @Inject constructor(
    private val powerGovernor: PowerGovernor,
    private val afmeController: AfmeController,
    private val hyperConnectManager: HyperConnectManager,
    private val appPredictor: AppPredictor,
    private val cpuAffinity: CpuAffinity
) {
    private val _mode = MutableStateFlow(SystemMode.IDLE)
    val mode: StateFlow<SystemMode> = _mode.asStateFlow()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch {
            combine(
                appPredictor.interactionFlow,
                powerGovernor.batteryLevel,
                hyperConnectManager.castState,
                afmeController.isEnabled
            ) { interaction, battery, cast, _ ->
                computeSystemMode(interaction, battery, cast)
            }.distinctUntilChanged()
             .collect { applyMode(it) }
        }
    }

    internal fun computeSystemMode(
        interaction: AppPredictor.InteractionEvent,
        battery: Float,
        cast: HyperConnectManager.CastState
    ): SystemMode = when (cast) {
        HyperConnectManager.CastState.CASTING -> SystemMode.CASTING
        HyperConnectManager.CastState.CONNECTING -> SystemMode.BALANCED
        HyperConnectManager.CastState.ERROR -> SystemMode.BALANCED
        HyperConnectManager.CastState.IDLE -> {
            when {
                battery < 0.15f -> SystemMode.LOW_BATTERY
                else -> when (interaction) {
                    is AppPredictor.InteractionEvent.TouchDown -> SystemMode.HEAVY
                    is AppPredictor.InteractionEvent.TouchUp -> SystemMode.BALANCED
                    is AppPredictor.InteractionEvent.Idle -> SystemMode.IDLE
                }
            }
        }
    }

    private suspend fun applyMode(mode: SystemMode) {
        _mode.value = mode
        when (mode) {
            SystemMode.CASTING -> {
                afmeController.setEnabled(false)
                delay(16)
                powerGovernor.applyOptimalFrequencies(PowerGovernor.LoadLevel.HEAVY)
                cpuAffinity.bindToX4Exclusive()
            }
            SystemMode.LOW_BATTERY -> {
                afmeController.setEnabled(false)
                powerGovernor.applyOptimalFrequencies(PowerGovernor.LoadLevel.IDLE)
                cpuAffinity.bindAllToLittle()
            }
            SystemMode.HEAVY -> {
                afmeController.setEnabled(false)
                powerGovernor.applyOptimalFrequencies(PowerGovernor.LoadLevel.HEAVY)
                cpuAffinity.bindUiToPerformance()
            }
            SystemMode.BALANCED -> {
                afmeController.setEnabled(true)
                powerGovernor.applyOptimalFrequencies(PowerGovernor.LoadLevel.LIGHT)
                cpuAffinity.balancedProfile()
            }
            SystemMode.IDLE -> {
                afmeController.setEnabled(true)
                powerGovernor.applyOptimalFrequencies(PowerGovernor.LoadLevel.IDLE)
                cpuAffinity.bindAllToLittle()
            }
        }
    }
}

enum class SystemMode { IDLE, BALANCED, HEAVY, CASTING, LOW_BATTERY }
