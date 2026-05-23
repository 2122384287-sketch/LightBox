package org.lightbox.engine

import android.content.Context
import android.util.Log
import android.view.Surface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import org.lightbox.ai.AppPredictor
import org.lightbox.domain.system.SystemCoordinator
import org.lightbox.native.NativeBridge
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VirtualCore @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val systemCoordinator: SystemCoordinator,
    private val appPredictor: AppPredictor,
    private val cgroupManager: CgroupManager,
    private val errorHandler: GlobalErrorHandler
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + errorHandler.handler)
    private var hostSurface: Surface? = null
    private var virtualDisplayId: Int = -1

    fun init() {
        NativeBridge.loadLibrary().onFailure {
            Log.e("VirtualCore", "Failed to load native library", it)
        }
        NativeBridge.injectBinderHook()
        scope.launch { systemCoordinator.start() }
    }

    fun attachHostSurface(surface: Surface) {
        hostSurface = surface
        NativeBridge.createVirtualDisplay(surface, 1080, 2400, 440)
            .onSuccess { id -> virtualDisplayId = id }
            .onFailure { e ->
                Log.e("VirtualCore", "createVirtualDisplay failed", e)
                virtualDisplayId = -1
            }
    }

    fun startApp(packageName: String): Result<Int> {
        if (virtualDisplayId == -1) return Result.failure(IllegalStateException("No virtual display"))
        return NativeBridge.forkVirtualProcess(packageName, virtualDisplayId, CpuCluster.PERFORMANCE.id)
            .onSuccess { pid ->
                cgroupManager.movePidToSandbox(pid).onFailure { e ->
                    Log.w("VirtualCore", "cgroup move failed", e)
                }
                appPredictor.onAppLaunch(packageName)
            }
    }

    fun detachHostSurface() {
        hostSurface = null
    }
}

enum class CpuCluster(val id: Int) { X4(0), PERFORMANCE(1), LITTLE(2) }
