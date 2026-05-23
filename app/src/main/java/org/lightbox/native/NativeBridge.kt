package org.lightbox.native

import android.util.Log
import android.view.Surface
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

object NativeBridge {
    private const val TAG = "AOSP_NativeBridge"
    private val isNativeLoaded = AtomicBoolean(false)
    private val mockDisplayIdCounter = AtomicInteger(1000)
    private val mockPidCounter = AtomicInteger(20000)

    fun loadLibrary(): Result<Unit> {
        return runCatching {
            System.loadLibrary("virtualcore")
            isNativeLoaded.set(true)
            Log.i(TAG, "Native library 'virtualcore' loaded successfully.")
        }.onFailure { e ->
            isNativeLoaded.set(false)
            Log.w(TAG, "Native library unavailable. Operating in Pure SDK Soft-Fallback mode.", e)
        }
    }

    fun injectBinderHook(): Result<Int> {
        if (!isNativeLoaded.get()) {
            Log.w(TAG, "Soft-Fallback: Bypassing Binder hook injection.")
            return Result.success(NativeError.SKIPPED.code)
        }
        return runCatching { nativeInjectBinderHook() }.onFailure { Log.e(TAG, "Binder hook injection failed", it) }
    }

    fun createVirtualDisplay(surface: Surface, width: Int, height: Int, dpi: Int): Result<Int> {
        if (!isNativeLoaded.get()) {
            val mockId = mockDisplayIdCounter.incrementAndGet()
            Log.i(TAG, "Soft-Fallback: Mock Virtual Display allocated (ID: $mockId).")
            return Result.success(mockId)
        }
        return runCatching {
            val id = nativeCreateVirtualDisplay(surface, width, height, dpi)
            if (id < 0) throw NativeException("Hardware virtual display allocation collapsed.", NativeError.fromCode(id))
            id
        }
    }

    fun forkVirtualProcess(pkg: String, displayId: Int, cpuCluster: Int): Result<Int> {
        if (!isNativeLoaded.get()) {
            val mockPid = mockPidCounter.incrementAndGet()
            Log.i(TAG, "Soft-Fallback: Mock Virtual Process (Pkg: $pkg, PID: $mockPid).")
            return Result.success(mockPid)
        }
        return runCatching {
            val pid = nativeForkVirtualProcess(pkg, displayId, cpuCluster)
            if (pid < 0) throw NativeException("Process fork mechanism rejected.", NativeError.fromCode(pid))
            pid
        }
    }

    fun destroy() {
        if (!isNativeLoaded.getAndSet(false)) return
        runCatching { nativeDestroy() }.onFailure { Log.e(TAG, "Native destroy failed", it) }
        Log.i(TAG, "Native bounds cleared.")
    }

    @JvmStatic private external fun nativeInjectBinderHook(): Int
    @JvmStatic private external fun nativeCreateVirtualDisplay(surface: Surface, width: Int, height: Int, dpi: Int): Int
    @JvmStatic private external fun nativeForkVirtualProcess(pkg: String, displayId: Int, cpuCluster: Int): Int
    @JvmStatic private external fun nativeDestroy()
}

class NativeException(message: String, val nativeError: NativeError) : Exception(message)
enum class NativeError(val code: Int) {
    OK(0), SKIPPED(1), BINDER_HOOK_FAILED(-1), DISPLAY_CREATE_FAILED(-2), FORK_FAILED(-3), UNKNOWN(-999);
    companion object { fun fromCode(code: Int): NativeError = entries.find { it.code == code } ?: UNKNOWN }
}
