package edu.bupt.jetcapture.vpn.tunnel

import edu.bupt.jetcapture.bean.vpn.Session
import java.net.InetSocketAddress


class TcpTunnelBridge(
    private var mLocalProxyTunnel: TcpLocalProxyTunnel,
    private var mRemoteProxyTunnel: TcpRemoteProxyTunnel,
    private var mSession: Session,
    private var mMtu: Int
) {
    init {

    }


    fun connect(ipAddress: InetSocketAddress) {
        mRemoteProxyTunnel.connect(ipAddress)
    }
}