package edu.bupt.jetcapture.vpn.tunnel

import java.nio.channels.Selector
import java.nio.channels.SocketChannel

class TcpLocalProxyTunnel(
    var mSocketChannel: SocketChannel,
    var mSelector: Selector
): TunnelCallBack {
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
}