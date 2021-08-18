package edu.bupt.jetcapture.vpn.tunnel

import android.util.Log
import edu.bupt.jetcapture.bean.vpn.Session
import java.nio.ByteBuffer


class VirtualGateway(
    val session: Session,
    private val localProxyTunnel: TcpTunnel,
    private val remoteProxyTunnel: TcpTunnel
) {

    private var mRequestFinished = false
    private var mResponseFinished = false

    fun onResponse(buffer: ByteBuffer) {
        if (mResponseFinished) {
            Log.e("gzz", "Drop a buffer due to response has finished.")
            return
        }
        localProxyTunnel.prepareWrite(buffer)
    }

    fun onRequest(buffer: ByteBuffer) {
        if (mRequestFinished) {
            Log.e("gzz", "Drop a buffer due to request has finished.")
            return
        }
        remoteProxyTunnel.prepareWrite(buffer)
    }


    fun onRequestFinished() {
        if (mRequestFinished) {
            return
        }
        mRequestFinished = true
    }

    fun onResponseFinished() {
        if (mResponseFinished) {
            return
        }
        mResponseFinished = true
    }

    fun close() {
        onRequestFinished()
        onResponseFinished()
    }
}