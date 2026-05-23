package org.lightbox.ui

import android.app.Activity
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.lightbox.engine.VirtualCore

@Composable
fun VirtualDesktopView(
    activity: Activity,
    virtualCore: VirtualCore
) {
    val surfaceView = remember { SurfaceView(activity) }

    DisposableEffect(surfaceView) {
        val callback = object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                virtualCore.attachHostSurface(holder.surface)
            }
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) { }
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                virtualCore.detachHostSurface()
            }
        }
        surfaceView.holder.addCallback(callback)
        onDispose { surfaceView.holder.removeCallback(callback) }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { surfaceView }
    )
}
