package edu.bupt.jetcapture.vpn.tunnel

import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

open class TunnelState {
    val mPendingBuffers: Queue<ByteBuffer> = ConcurrentLinkedDeque()

    private var mIsClosed = false

    fun isClosed() = mIsClosed

    open fun close() {
        mIsClosed = true
        mPendingBuffers.clear()
    }

}