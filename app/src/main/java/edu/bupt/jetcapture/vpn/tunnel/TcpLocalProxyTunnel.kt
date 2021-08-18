package edu.bupt.jetcapture.vpn.tunnel

import java.nio.channels.Selector
import java.nio.channels.SocketChannel

class TcpLocalProxyTunnel(
    mSocketChannel: SocketChannel,
    mSelector: Selector
): TcpTunnel(mSocketChannel, mSelector) {

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