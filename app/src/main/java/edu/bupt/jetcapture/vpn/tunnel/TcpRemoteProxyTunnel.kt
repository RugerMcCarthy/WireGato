package edu.bupt.jetcapture.vpn.tunnel

import android.net.VpnService
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

class TcpRemoteProxyTunnel(
    var mVpnService: VpnService,
    var mSocketChannel: SocketChannel,
    var mSelector: Selector
): TunnelCallBack, TunnelState() {

    private var mSelectionKey: SelectionKey? = null
    private var mCallBack: TunnelCallBack? = null
    fun setCallback(callBack: TunnelCallBack) {
        mCallBack = callBack
    }

    fun connect(ipAddress: InetSocketAddress) {
        if (mVpnService.protect(mSocketChannel.socket())) {
            if (mSocketChannel.isBlocking) {
                mSocketChannel.configureBlocking(false)
            }
            mSocketChannel.register(mSelector, SelectionKey.OP_CONNECT, this)
            mSocketChannel.connect(ipAddress)
        } else {
            throw IOException("[TCP]Can not protect remote tunnel socket.")
        }
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

    override fun onConnected() {
        if (mSocketChannel.finishConnect()) {
            mCallBack?.onConnected()
        } else {
            throw Throwable("[TCP]The tunnel socket is not connected.")
        }
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
}