package edu.bupt.jetcapture

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import edu.bupt.jetcapture.bean.net.IpAddress
import edu.bupt.jetcapture.bean.vpn.CPVpnConfig
import edu.bupt.jetcapture.vpn.CPVpnManager
import edu.bupt.jetcapture.ui.theme.JetCaptureTheme

class MainActivity : ComponentActivity() {
    private val REQUEST_VPN_CODE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetCaptureTheme {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                      .fillMaxSize()
                ) {
                    Button(onClick = {
                        prepareStartVPN()
                        // Log.d("gzz", "click over")
                    }) {
                        Text("Start VPN")
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_VPN_CODE -> {
                startVPN()
            }
        }
    }

    private fun prepareStartVPN() {
        var intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, REQUEST_VPN_CODE)
            return
        }
        startVPN()
    }

    private fun startVPN() {
        var builder = CPVpnConfig.Builder()
        builder.setMtu(4096)
            .setAddress(IpAddress("10.1.10.1", 32))
            .setSession("NetBare")
            .addRoute(IpAddress("0.0.0.0", 0))
        CPVpnManager.startCPVpn(builder.build())
    }
}
