package org.lightbox.engine

interface CgroupManager {
    fun init(): Result<Unit>
    fun movePidToSandbox(pid: Int): Result<Unit>
}
