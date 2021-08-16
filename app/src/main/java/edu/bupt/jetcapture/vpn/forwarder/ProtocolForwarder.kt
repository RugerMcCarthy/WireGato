package edu.bupt.jetcapture.vpn.forwarder

import java.io.OutputStream

interface ProtocolForwarder {
    fun prepare()

    fun forward(packet: ByteArray, len: Int, output: OutputStream)

    fun release()
}