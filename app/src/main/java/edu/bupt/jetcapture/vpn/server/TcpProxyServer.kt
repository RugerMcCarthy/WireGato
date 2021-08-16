package edu.bupt.jetcapture.vpn.server

import android.net.VpnService
import android.util.Log
import edu.bupt.jetcapture.utils.NetBareUtils
import kotlinx.coroutines.processNextEventInCurrentThread
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel

class TcpProxyServer(
    vpnService: VpnService,
    ip: String,
    mtu: Int
): Thread() {
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
            process()
        }
        Log.d("gzz", "test end")

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

                    }
                }
            } finally {
                iterator.remove()
            }
        }
    }

    private fun onAccept() {

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