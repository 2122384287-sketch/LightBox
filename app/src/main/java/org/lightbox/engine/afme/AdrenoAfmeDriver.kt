package org.lightbox.engine.afme

import android.os.Build
import android.util.Log
import android.view.Surface
import java.lang.reflect.Method
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdrenoAfmeDriver @Inject constructor() : AfmeDriver {

    companion object {
        private const val TAG = "AdrenoAfmeDriver"
        private const val AFME_FLAG = 0x40000002
    }

    private var surface: Surface? = null
    private var afmeMethod: Method? = null
    private var supported = false

    override fun isSupported(): Boolean = supported

    override fun initialize(surface: Surface, maxFps: Int): Result<Unit> {
        return runCatching {
            this.surface = surface
            val method = Surface::class.java.getMethod(
                "setAdvancedInt",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            )
            afmeMethod = method
            val multiplier = (maxFps / 60).coerceIn(1, 4)
            method.invoke(surface, AFME_FLAG, multiplier)
            supported = true
            Log.i(TAG, "AFME init sdk=${Build.VERSION.SDK_INT}, multiplier=$multiplier")
        }.onFailure {
            supported = false
            afmeMethod = null
            Log.w(TAG, "AFME unavailable", it)
        }
    }

    override fun enable(): Result<Unit> {
        return runCatching {
            val s = surface ?: error("surface null")
            val m = afmeMethod ?: error("method null")
            m.invoke(s, AFME_FLAG, 4)
            Log.d(TAG, "AFME enabled")
        }
    }

    override fun disable(): Result<Unit> {
        return runCatching {
            val s = surface ?: return@runCatching
            afmeMethod?.invoke(s, AFME_FLAG, 0)
            Log.d(TAG, "AFME disabled")
        }
    }

    override fun release() {
        surface = null
        afmeMethod = null
        supported = false
    }
}
