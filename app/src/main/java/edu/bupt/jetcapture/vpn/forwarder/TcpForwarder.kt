package edu.bupt.jetcapture.vpn.forwarder

import android.net.VpnService
import android.util.Log
import edu.bupt.jetcapture.bean.net.IpHeader
import edu.bupt.jetcapture.bean.net.Protocol
import edu.bupt.jetcapture.bean.net.TcpHeader
import edu.bupt.jetcapture.bean.vpn.Session
import edu.bupt.jetcapture.vpn.SessionProvider
import edu.bupt.jetcapture.vpn.server.TcpProxyServer
import java.io.IOException
import java.io.OutputStream

class TcpProxyForwarder(
    val vpnService: VpnService,
    val ip: String,
    val mtu: Int
): ProtocolForwarder {
    var mSessionProvider = SessionProvider()
    var mTcpProxyServer = TcpProxyServer(vpnService, ip, mtu, mSessionProvider)

    override fun prepare() {
        mTcpProxyServer.startServer()
    }

    override fun forward(packet: ByteArray, len: Int, output: OutputStream) {
        val ipHeader = IpHeader(packet, 0)
        val tcpHeader = TcpHeader(ipHeader, packet, ipHeader.headerLength)

        // Src IP & Port
        val localIp = ipHeader.sourceIp
        val localPort = tcpHeader.sourcePort

        // Dest IP & Port
        val remoteIp = ipHeader.destinationIp
        val remotePort = tcpHeader.destinationPort

        // TCP data size
        val tcpDataSize = ipHeader.dataLength - tcpHeader.headerLength

//        Log.d("gzz", "接收到数据包：" +
//                "sourceIP: " + NetBareUtils.convertIp(localIp) +
//                ", sourcePort: " + NetBareUtils.convertPort(localPort) +
//                ", DesIp: " + NetBareUtils.convertIp(remoteIp) +
//                ", DesPort: " + NetBareUtils.convertPort(remotePort)
//        );

        // Request
        if (localPort != mTcpProxyServer.bindPort) {
            Log.d("gzz", "发送: $tcpHeader [${mTcpProxyServer.bindPort}]")
            val session = mSessionProvider.ensureQuery(
                Protocol.TCP,
                localPort,
                remotePort,
                remoteIp
            )
            session.packetIndex++
            ipHeader.sourceIp = remoteIp
            ipHeader.destinationIp = mTcpProxyServer.bindIp
            tcpHeader.destinationPort = mTcpProxyServer.bindPort
            // Client requests to server
            ipHeader.updateChecksum()
            tcpHeader.updateChecksum()
            session.sendDataSize += tcpDataSize
        } else {
            Log.d("gzz", "接收: $tcpHeader")
            val session: Session? = mSessionProvider.query(remotePort)
            if (session == null) {
                Log.e("gzz","No session saved with key: $remotePort")
                return
            }
            ipHeader.sourceIp = remoteIp
            ipHeader.destinationIp = mTcpProxyServer.bindIp
            tcpHeader.sourcePort = session.remotePort

            ipHeader.updateChecksum()
            tcpHeader.updateChecksum()
            session.receiveDataSize += tcpDataSize
        }
        try {
            output.write(packet, 0, len)
        } catch (e: IOException) {
            e.message?.let { Log.e("gzz", it) }
        }
    }

    override fun release() {

    }
}