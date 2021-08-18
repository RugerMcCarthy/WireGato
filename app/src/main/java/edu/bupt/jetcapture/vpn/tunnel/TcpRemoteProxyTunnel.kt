package edu.bupt.jetcapture.vpn.tunnel

import android.net.VpnService
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

class TcpRemoteProxyTunnel(
    var mVpnService: VpnService,
    mSocketChannel: SocketChannel,
    mSelector: Selector
): TcpTunnel(mSocketChannel, mSelector) {

    override fun connect(ipAddress: InetSocketAddress) {
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
        if (mSocketChannel.finishConnect()) {
            mCallBack?.onConnected()
        } else {
            throw Throwable("[TCP]The tunnel socket is not connected.")
        }
    }
}