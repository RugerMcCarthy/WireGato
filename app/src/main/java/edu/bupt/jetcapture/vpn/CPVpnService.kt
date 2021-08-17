package edu.bupt.jetcapture.vpn

import android.content.Intent
import android.net.VpnService
import android.util.Log

class CPVpnService: VpnService() {
    companion object{
        const val ACTION_START = "edu.bupt.jetcapture.action.Start"
        const val ACTION_STOP = "edu.bupt.jetcapture.action.Stop"
    }

    var isRunning = false
    lateinit var capturePackageThread: CPThread

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_NOT_STICKY
        }
        when (intent.action) {
            ACTION_START -> {
                if (!isRunning) {
                    prepareCapture()
                    isRunning = true
                }
            }
            ACTION_STOP -> {
                stopSelf()
                isRunning = false
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun prepareCapture() {
        capturePackageThread = CPThread(this, CPVpnManager.getConfig())
        capturePackageThread.start()
    }
}