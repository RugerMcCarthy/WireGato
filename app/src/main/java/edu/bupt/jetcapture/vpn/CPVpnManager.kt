package edu.bupt.jetcapture.vpn

import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import edu.bupt.jetcapture.App
import edu.bupt.jetcapture.bean.vpn.CPVpnConfig
import java.lang.Exception

object CPVpnManager {
    private lateinit var mApp: App
    private lateinit var mConfig: CPVpnConfig

    fun getConfig(): CPVpnConfig {
        return mConfig
    }

    fun attach(app: App) {
        mApp = app
    }

    fun startCPVpn(config: CPVpnConfig) {
        try {
            config.checkValid()
            mConfig = config
            var intent = Intent(CPVpnService.ACTION_START)
            intent.`package` = mApp.packageName
            ContextCompat.startForegroundService(mApp, intent)
        } catch (e: Exception) {
            Log.d("gzz", Log.getStackTraceString(e))
        }
    }
}