package org.lightbox.engine

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalErrorHandler @Inject constructor() {
    private val errorCount = AtomicInteger(0)
    val handler = CoroutineExceptionHandler { _, throwable ->
        Log.e("LightBox", "Unhandled coroutine exception", throwable)
        errorCount.incrementAndGet()
    }
    fun getErrorCount(): Int = errorCount.get()
    fun resetErrorCount() { errorCount.set(0) }
}
