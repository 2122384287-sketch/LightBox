package org.lightbox.ui

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import org.lightbox.ai.AppPredictor
import org.lightbox.engine.VirtualCore
import javax.inject.Inject

@AndroidEntryPoint
class DesktopActivity : ComponentActivity() {

    @Inject lateinit var virtualCore: VirtualCore
    @Inject lateinit var appPredictor: AppPredictor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VirtualDesktopView(activity = this, virtualCore = virtualCore)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                appPredictor.onTouchDown()
                true
            }
            MotionEvent.ACTION_UP -> {
                appPredictor.onTouchUp()
                true
            }
            else -> super.onTouchEvent(event)
        }
    }
}
