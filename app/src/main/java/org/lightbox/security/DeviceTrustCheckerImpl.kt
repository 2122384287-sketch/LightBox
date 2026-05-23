package org.lightbox.security

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceTrustCheckerImpl @Inject constructor() : DeviceTrustChecker {
    private var state: DeviceTrustChecker.TrustState = DeviceTrustChecker.TrustState.UNKNOWN
    override fun init(context: Context) { state = DeviceTrustChecker.TrustState.SAFE }
    override fun isSafeForCasting(): Boolean = state == DeviceTrustChecker.TrustState.SAFE
}
