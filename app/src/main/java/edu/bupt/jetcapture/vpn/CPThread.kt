package edu.bupt.jetcapture.vpn

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import edu.bupt.jetcapture.bean.vpn.CPVpnConfig
import java.io.FileInputStream
import java.io.FileOutputStream

class CPThread(val vpnService: VpnService, val config: CPVpnConfig): Thread("CPThread") {

    private var packageTransfer: PackageTransfer = PackageTransfer(vpnService, config)

    private var mRunning = false

    override fun start() {
        super.start()
        mRunning = true
    }

    override fun interrupt() {
        super.interrupt()
        mRunning = false
    }

    override fun run() {
        super.run()
        if (!mRunning) {
            return
        }
        var vpnDescriptor = establishConnection()
        if (vpnDescriptor == null) {
            Log.e("gzz", "tun0设备描述符返回NULL")
            return
        }
        var input = FileInputStream(vpnDescriptor.fileDescriptor)
        var output = FileOutputStream(vpnDescriptor.fileDescriptor)
        packageTransfer.prepare()
        try {
            while (mRunning) {
                packageTransfer.transfer(input, output, config.mtu)
            }
        } catch (e: Exception) {
            Log.e("gzz", "Transfer Over "  + Log.getStackTraceString(e))
        }
    }

    private fun establishConnection(): ParcelFileDescriptor? {
        return with(vpnService.Builder()) {
            setSession(config.session)
            setMtu(config.mtu)
            addAddress(config.address.address, config.address.prefixLength)
            config.routes.forEach {
                addRoute(it.address, it.prefixLength)
            }
            establish()
        }
    }
}