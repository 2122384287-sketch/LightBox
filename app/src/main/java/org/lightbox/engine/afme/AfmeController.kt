package org.lightbox.engine.afme

import android.view.Surface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AfmeController @Inject constructor(
    private val driver: AfmeDriver
) {
    private val _enabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _enabled
    val isSupported: Boolean get() = driver.isSupported()

    fun initialize(surface: Surface, maxFps: Int = 240): Result<Unit> =
        driver.initialize(surface, maxFps)

    fun setEnabled(enabled: Boolean) {
        if (!driver.isSupported()) return
        val success = if (enabled) driver.enable() else driver.disable()
        if (success.isSuccess) _enabled.value = enabled
    }

    fun release() {
        driver.release()
        _enabled.value = false
    }
}
