package edu.bupt.jetcapture.vpn.server

import android.net.VpnService
import android.util.Log
import edu.bupt.jetcapture.utils.NetBareUtils
import edu.bupt.jetcapture.vpn.SessionProvider
import edu.bupt.jetcapture.vpn.tunnel.TcpLocalProxyTunnel
import edu.bupt.jetcapture.vpn.tunnel.TcpRemoteProxyTunnel
import edu.bupt.jetcapture.vpn.tunnel.TcpTunnelBridge
import edu.bupt.jetcapture.vpn.tunnel.TunnelCallBack
import java.io.IOException
import java.lang.Exception
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

class TcpProxyServer(
    vpnService: VpnService,
    ip: String,
    mtu: Int,
    sessionProvider: SessionProvider
): Thread("TCP_Thread") {
    private var mSessionProvider: SessionProvider = sessionProvider
    private var mVpnService: VpnService = vpnService
    val bindIp = NetBareUtils.convertIp(ip)
    val bindPort
        get() = mServerSocketChannel.socket().localPort.toShort()
    private var mMtu = mtu
    private var mSelector: Selector = Selector.open()
    private var mServerSocketChannel: ServerSocketChannel = ServerSocketChannel.open()
    private var mIsRunning = false

    init {
        mServerSocketChannel.configureBlocking(false);
        mServerSocketChannel.socket().bind(InetSocketAddress(0))
        mServerSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT)
    }

    override fun run() {
        super.run()
        while (mIsRunning) {
            try {
                process()
            } catch (e: Exception) {
                Log.e("gzz", Log.getStackTraceString(e))
            }
        }
        mSelector.close()
        mServerSocketChannel.close()
    }

    private fun process() {
        val select = mSelector.select()
        if (select == 0) {
            return
        }
        val selectedKeys = mSelector.selectedKeys() ?: return
        val iterator = selectedKeys.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            try {
                if (key.isValid) {
                    if (key.isAcceptable) {
                        onAccept()
                    } else {
                        var callback = key.attachment()
                        if (callback is TunnelCallBack) {
                            try {
                                if (key.isConnectable) {
                                    callback.onConnected()
                                } else if (key.isReadable) {
                                    callback.onRead()
                                } else if (key.isWritable) {
                                    callback.onWrite()
                                }
                            } catch (e: IOException) {
                                Log.e("gzz", Log.getStackTraceString(e))
                                callback.onClosed()
                            }
                        }
                    }
                }
            } finally {
                iterator.remove()
            }
        }
    }

    private fun onAccept() {
        var clientSocketChannel = mServerSocketChannel.accept()
        var clientSocket = clientSocketChannel.socket()
        var localPort = clientSocket.port
        var session = mSessionProvider.query(localPort.toShort())
        if (session == null) {
            Log.e("gzz", "Session Not Found")
            return
        }
        var remotePort = session.remotePort
        var remoteIP = session.remoteIp
        var localProxyTunnel = TcpLocalProxyTunnel(clientSocketChannel, mSelector)
        var remoteProxyTunnel = TcpRemoteProxyTunnel(mVpnService, SocketChannel.open(), mSelector)
        var tunnelBridge = TcpTunnelBridge(
            localProxyTunnel,
            remoteProxyTunnel,
            session,
            mMtu,
        )
        try {
            tunnelBridge.connect(InetSocketAddress(NetBareUtils.convertIp(remoteIP), remotePort.toInt()))
        } catch (e: Exception) {
            localProxyTunnel.close()
            remoteProxyTunnel.close()
            throw e
        }
    }

    fun startServer() {
        mIsRunning = true
        start()
    }

    fun stopServer() {
        interrupt()
        mIsRunning = false
    }
}