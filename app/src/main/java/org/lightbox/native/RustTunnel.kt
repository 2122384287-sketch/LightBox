package org.lightbox.native

import java.nio.ByteBuffer
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object RustTunnel {
    private var nativeHandle: Long = 0
    private val lock = ReentrantLock()
    private var loaded = false

    fun init(): Result<Unit> = runCatching {
        System.loadLibrary("lightbox_tunnel")
        nativeHandle = nativeCreate()
        loaded = true
    }

    fun send(data: ByteBuffer): Result<Unit> {
        if (!loaded) return Result.failure(IllegalStateException("not loaded"))
        lock.withLock { runCatching { nativeSend(nativeHandle, data) } }
    }

    fun destroy() {
        lock.withLock {
            if (nativeHandle != 0L) {
                nativeDestroy(nativeHandle)
                nativeHandle = 0
            }
        }
    }

    private external fun nativeCreate(): Long
    private external fun nativeSend(handle: Long, data: ByteBuffer)
    private external fun nativeDestroy(handle: Long)
}
