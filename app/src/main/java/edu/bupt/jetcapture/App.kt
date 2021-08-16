package edu.bupt.jetcapture

import android.app.Application
import edu.bupt.jetcapture.vpn.CPVpnManager

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        CPVpnManager.attach(this)
    }
}