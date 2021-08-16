package edu.bupt.jetcapture.bean.vpn

import edu.bupt.jetcapture.bean.net.Protocol
import java.util.*

data class Session(

    /**
     * IP protocol.
     */
    val protocol: Protocol,

    /**
     * Local vpn port.
     */
    val localPort: Short,

    /**
     * Remote server port.
     */
    val remotePort: Short,

    /**
     * Remote server IP.
     */
    val remoteIp: Int,

    /**
     * An unique id uses to identify this session.
     */
    var id: String = UUID.randomUUID().toString(),

    /**
     * Session started time.
     */
    var time: Long = System.currentTimeMillis(),

    /**
     * Remote server host.
     */
    var host: String = "",

    /**
     * The process id that the session belongs to.
     */
    var uid: Int = 0,

    /**
     * Packet counts.
     */
    var packetIndex: Int = 0,

    /**
     * The total size of the packets that sends to remote server.
     */
    var sendDataSize: Int = 0,

    /**
     * The total size of the packets that received from remote server.
     */
    var receiveDataSize: Int = 0
)
