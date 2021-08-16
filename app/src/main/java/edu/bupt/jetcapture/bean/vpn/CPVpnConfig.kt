package edu.bupt.jetcapture.bean.vpn

import edu.bupt.jetcapture.bean.net.IpAddress
import java.lang.RuntimeException

class CPVpnConfig private constructor() {
    lateinit var session: String
        private set

    lateinit var address: IpAddress
        private set

    var routes = mutableSetOf<IpAddress>()
        private set

    var mtu = 0
        private set

    fun checkValid() {
        if (mtu <= 0) {
            throw RuntimeException("Must set mtu in NetBareConfig")
        }
        if (!this::address.isInitialized) {
            throw RuntimeException("Must set address in NetBareConfig")
        }
    }

    class Builder {
        val mConfig = CPVpnConfig()

        fun setSession(session: String): Builder {
            mConfig.session = session
            return this
        }

        fun setAddress(address: IpAddress): Builder {
            mConfig.address = address
            return this
        }

        fun addRoute(route: IpAddress): Builder {
            mConfig.routes.add(route)
            return this
        }

        fun setMtu(mtu: Int): Builder {
            mConfig.mtu = mtu
            return this
        }

        fun build(): CPVpnConfig {
            return mConfig
        }
    }
}