package org.lightbox.net

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.VpnService
import android.os.ParcelFileDescriptor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.net.InetAddress

@AndroidEntryPoint
class VpnProxyService : VpnService() {

    @Inject lateinit var connectivityManager: ConnectivityManager

    private var vpnInterface: ParcelFileDescriptor? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var callbackRegistered = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                if (!callbackRegistered) {
                    registerNetworkCallback()
                    callbackRegistered = true
                }
                scope.launch { startVpn() }
                return START_STICKY
            }
        }
    }

    private fun startVpn(): Boolean {
        val builder = Builder()
            .setSession("LightBox")
            .addAddress("10.8.0.2", 30)
            .addRoute("0.0.0.0", 0)
            .addRoute("::", 0)
            .addDnsServer(InetAddress.getByName("1.1.1.1"))
            .addDnsServer(InetAddress.getByName("9.9.9.9"))
            .addDisallowedApplication(packageName)

        val established = builder.establish() ?: return false
        vpnInterface?.close()
        vpnInterface = established
        protect(established.fileDescriptor)
        return true
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(
            request,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    scope.launch {
                        // 后续可刷新 DNS / 路由策略
                    }
                }
            }
        )
    }

    override fun onDestroy() {
        scope.cancel()
        vpnInterface?.close()
        vpnInterface = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "STOP"
    }
}
