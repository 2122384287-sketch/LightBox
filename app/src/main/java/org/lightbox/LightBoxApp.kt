package org.lightbox

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.lightbox.engine.VirtualCore
import javax.inject.Inject

@HiltAndroidApp
class LightBoxApp : Application() {
    @Inject lateinit var virtualCore: VirtualCore
    override fun onCreate() {
        super.onCreate()
        virtualCore.init()
    }
}
