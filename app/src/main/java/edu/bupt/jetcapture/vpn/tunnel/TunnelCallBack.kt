package edu.bupt.jetcapture.vpn.tunnel


interface TunnelCallBack {

    fun onConnected()

    fun onRead()

    fun onWrite()

    fun onClosed()
}