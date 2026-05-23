package org.lightbox.security

import android.content.Context

interface DeviceTrustChecker {
    enum class TrustState { SAFE, UNSAFE, UNKNOWN }
    fun init(context: Context)
    fun isSafeForCasting(): Boolean
}
