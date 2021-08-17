package edu.bupt.jetcapture.vpn.tunnel

import android.net.VpnService
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

class TcpRemoteProxyTunnel(
    var mVpnService: VpnService,
    var mSocketChannel: SocketChannel,
    var mSelector: Selector
): TunnelCallBack {

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
}