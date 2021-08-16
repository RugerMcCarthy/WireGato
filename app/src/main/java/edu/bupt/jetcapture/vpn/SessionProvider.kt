package edu.bupt.jetcapture.vpn

import edu.bupt.jetcapture.bean.net.Protocol
import edu.bupt.jetcapture.bean.vpn.Session
import java.util.concurrent.ConcurrentHashMap

class SessionProvider {
    private val MAX_SESSION = 100
    private val mSessions = ConcurrentHashMap<Short, Session>(MAX_SESSION)

    fun query(localPort: Short): Session? {
        return mSessions[localPort]
    }

    fun ensureQuery(
        protocol: Protocol,
        localPort: Short,
        remotePort: Short,
        remoteIp: Int
    ): Session {
        var session = mSessions[localPort]
        if (session != null) {
            if (session.protocol !== protocol || session.localPort !== localPort || session.remotePort !== remotePort || session.remoteIp !== remoteIp) {
                session = null
            }
        }
        if (session == null) {
            session = Session(
                protocol = protocol,
                localPort = localPort,
                remotePort = remotePort,
                remoteIp = remoteIp
            )
            mSessions[localPort] = session
        }
        return session
    }
}