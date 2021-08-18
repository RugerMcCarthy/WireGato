package edu.bupt.jetcapture.vpn

import android.net.VpnService
import android.os.SystemClock
import android.util.Log
import edu.bupt.jetcapture.bean.net.IpHeader
import edu.bupt.jetcapture.bean.net.Protocol
import edu.bupt.jetcapture.bean.vpn.CPVpnConfig
import edu.bupt.jetcapture.vpn.forwarder.ProtocolForwarder
import edu.bupt.jetcapture.vpn.forwarder.TcpProxyForwarder
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
class PackageTransfer(val vpnService: VpnService, val config: CPVpnConfig) {
    private var mSupportedProtocolForwarder = mutableMapOf<Protocol, ProtocolForwarder>()


    init {
        mSupportedProtocolForwarder[Protocol.TCP] =
            TcpProxyForwarder(vpnService, config.address.address!!, config.mtu)
    }

    fun prepare() {
        mSupportedProtocolForwarder.values.forEach {
            it.prepare()
        }
    }

    fun transfer(input: InputStream, output: OutputStream, mtu: Int) {
        var packet = ByteArray(mtu)
        var readLength = input.read(packet)
        if (readLength < 0) {
            throw IOException("Read -1 from vpn FileDescriptor.")
        }
        if (readLength == 0) {
            SystemClock.sleep(5)
            return
        }
        transfer(packet, readLength, output)
    }

    private fun transfer(packet: ByteArray, len: Int, output: OutputStream) {
        if (len < IpHeader.MIN_HEADER_LENGTH) {
            Log.e("gzz", "Ip header length < " + IpHeader.MIN_HEADER_LENGTH)
            return
        }
        val ipHeader = IpHeader(packet, 0)
        val protocol = Protocol.parse(ipHeader.protocol.toInt())
        val forwarder: ProtocolForwarder? = mSupportedProtocolForwarder[protocol]
        if (forwarder != null) {
            forwarder.forward(packet, len, output)
        } else {
            Log.e("gzz", "Unknown ip protocol: " + ipHeader.protocol)
        }
    }
}