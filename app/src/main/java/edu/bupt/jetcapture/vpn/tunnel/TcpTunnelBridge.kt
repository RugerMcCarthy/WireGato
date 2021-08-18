package edu.bupt.jetcapture.vpn.tunnel

import edu.bupt.jetcapture.bean.vpn.Session
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer


class TcpTunnelBridge(
    private val mLocalProxyTunnel: TcpTunnel,
    private val mRemoteProxyTunnel: TcpTunnel,
    private val mSession: Session,
    private val mMtu: Int
) {
    val mGateway =  VirtualGateway(mSession, mLocalProxyTunnel, mRemoteProxyTunnel)

    init {
        setCallback()
    }

    fun connect(ipAddress: InetSocketAddress) {
        mRemoteProxyTunnel.connect(ipAddress)
    }

    private fun setCallback() {
        mLocalProxyTunnel.setCallback(object: TunnelCallBack {
            override fun onConnected() {
                throw Throwable("Local Proxy Tunnel Don't Need Connect")
            }

            override fun onRead() {
                if (mLocalProxyTunnel.isClosed()) {
                    mGateway.onResponseFinished()
                    throw Throwable("LocalProxyTunnel is Closed!")
                }
                val buffer = ByteBuffer.allocate(mMtu)
                var len = -1
                try {
                    len = mLocalProxyTunnel.read(buffer)
                } catch (e: IOException) {
                    throw e
                }
                if (len < 0 || mRemoteProxyTunnel.isClosed()) {
                    mLocalProxyTunnel.close()
                    mGateway.onResponseFinished()
                    return
                    //throw Throwable("RemoteProxyTunnel is Closed!")
                }
                mGateway.onRequest(buffer)
            }

            override fun onWrite() {
                mLocalProxyTunnel.write()
            }

            override fun onClosed() {
                mGateway.close()
            }
        })

        mRemoteProxyTunnel.setCallback(object: TunnelCallBack {
            override fun onConnected() {
                mLocalProxyTunnel.prepareRead()
                mRemoteProxyTunnel.prepareRead()
            }

            override fun onRead() {
                if (mRemoteProxyTunnel.isClosed()) {
                    mGateway.onRequestFinished()
                    throw Throwable("RemoteProxyTunnel is Closed!")
                }
                val buffer = ByteBuffer.allocate(mMtu)
                var len = -1
                try {
                    len = mRemoteProxyTunnel.read(buffer)
                } catch (e: IOException) {
                    throw e
                }
                if (len < 0 || mLocalProxyTunnel.isClosed()) {
                    mRemoteProxyTunnel.close()
                    mGateway.onRequestFinished()
                    return
                    // throw Throwable("LocalProxyTunnel is Closed!")
                }
                mGateway.onResponse(buffer)
            }

            override fun onWrite() {
                mRemoteProxyTunnel.write()
            }

            override fun onClosed() {
                mGateway.close()
            }
        })
    }
}