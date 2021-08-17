package edu.bupt.jetcapture.vpn.tunnel

import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

class TcpLocalProxyTunnel(
    var mSocketChannel: SocketChannel,
    var mSelector: Selector
): TunnelCallBack, TunnelState() {
    private var mSelectionKey: SelectionKey? = null
    private var mCallBack: TunnelCallBack? = null
    fun setCallback(callBack: TunnelCallBack) {
        mCallBack = callBack
    }

    override fun onConnected() {
        mCallBack?.onConnected()
    }

    override fun onRead() {
        mCallBack?.onRead()
    }

    override fun onWrite() {
        mCallBack?.onWrite()
    }

    override fun onClosed() {
        mCallBack?.onClosed()
    }

    fun read(buffer: ByteBuffer): Int {
        buffer.clear()
        val len: Int = mSocketChannel.read(buffer)
        if (len > 0) {
            buffer.flip()
        }
        return len
    }

    fun write() {
        while (!mPendingBuffers.isEmpty()) {
            val buffer: ByteBuffer = mPendingBuffers.poll()
            val remaining = buffer.remaining()
            val sent: Int = mSocketChannel.write(buffer)
            if (sent < remaining) {
                // Should wait next onWrite.
                mPendingBuffers.offer(buffer)
                return
            }
        }
        interestRead()
    }

    fun prepareWrite(buffer: ByteBuffer) {
        if (isClosed()) {
            return
        }
        if (!buffer.hasRemaining()) {
            return
        }
        mPendingBuffers.offer(buffer)
        interestWrite()
    }

    fun prepareRead() {
        if (mSocketChannel.isBlocking) {
            mSocketChannel.configureBlocking(false)
        }
        mSelector.wakeup()
        mSelectionKey = mSocketChannel.register(mSelector, SelectionKey.OP_READ, this)
    }


    private fun interestWrite() {
        if (mSelectionKey != null) {
            mSelector.wakeup()
            mSelectionKey!!.interestOps(SelectionKey.OP_WRITE)
        }
    }

    private fun interestRead() {
        if (mSelectionKey != null) {
            mSelector.wakeup()
            mSelectionKey!!.interestOps(SelectionKey.OP_READ)
        }
    }

    override fun close() {
        super.close()
        mSocketChannel.close()
    }
}