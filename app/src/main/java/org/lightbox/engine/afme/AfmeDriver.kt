package org.lightbox.engine.afme

import android.view.Surface

interface AfmeDriver {
    fun isSupported(): Boolean
    fun initialize(surface: Surface, maxFps: Int): Result<Unit>
    fun enable(): Result<Unit>
    fun disable(): Result<Unit>
    fun release()
}
