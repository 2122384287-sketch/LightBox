package org.lightbox.engine

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CgroupManagerImpl @Inject constructor() : CgroupManager {
    override fun init(): Result<Unit> =
        Result.failure(UnsupportedOperationException("cgroup requires root"))
    override fun movePidToSandbox(pid: Int): Result<Unit> =
        Result.failure(UnsupportedOperationException("cgroup requires root"))
}
