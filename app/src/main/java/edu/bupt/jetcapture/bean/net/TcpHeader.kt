/*  NetBare - An android network capture and injection library.
 *  Copyright (C) 2018-2019 Megatron King
 *  Copyright (C) 2018-2019 GuoShi
 *
 *  NetBare is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU General Public License as published by the Free Software Found-
 *  ation, either version 3 of the License, or (at your option) any later version.
 *
 *  NetBare is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 *  PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with NetBare.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.bupt.jetcapture.bean.net

import java.util.*

/**
 * TCP segments are sent as internet datagrams. The Internet Protocol header carries several
 * information fields, including the source and destination host addresses. A TCP header follows
 * the internet header, supplying information specific to the TCP protocol. This division allows
 * for the existence of host level protocols other than TCP.
 *
 * TCP Header Format:
 *
 * 0                   1                   2                   3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |          Source Port          |       Destination Port        |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                        Sequence Number                        |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                    Acknowledgment Number                      |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |  Data |           |U|A|P|R|S|F|                               |
 * | Offset| Reserved  |R|C|S|S|Y|I|            Window             |
 * |       |           |G|K|H|T|N|N|                               |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |           Checksum            |         Urgent Pointer        |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                    Options                    |    Padding    |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                             data                              |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 * See https://tools.ietf.org/html/rfc793#section-3.1
 *
 * @author Megatron King
 * @since 2018-10-10 22:19
 */
class TcpHeader(private val mIpHeader: IpHeader, packet: ByteArray?, offset: Int) : Header(
    packet!!, offset
) {
    fun updateOffset(offset: Int) {
        this.offset = offset
    }

    var sourcePort: Short
        get() = readShort(offset + OFFSET_SRC_PORT)
        set(port) {
            writeShort(port, offset + OFFSET_SRC_PORT)
        }
    var destinationPort: Short
        get() = readShort(offset + OFFSET_DEST_PORT)
        set(port) {
            writeShort(port, offset + OFFSET_DEST_PORT)
        }
    val headerLength: Int
        get() {
            val lenres: Int = packet[offset + OFFSET_LENRES].toInt() and 0xFF
            return (lenres shr 4) * 4
        }
    var crc: Short
        get() = readShort(offset + OFFSET_CRC)
        set(crc) {
            writeShort(crc, offset + OFFSET_CRC)
        }
    val flag: Byte
        get() = packet[offset + OFFSET_FLAG]
    val seqID: Int
        get() = readInt(offset + OFFSET_SEQ)
    val ackID: Int
        get() = readInt(offset + OFFSET_ACK)

    fun updateChecksum() {
        crc = 0.toShort()
        crc = computeChecksum()
    }

    private fun computeChecksum(): Short {
        // Sum = Ip Sum(Source Address + Destination Address) + Protocol + TCP Length
        // The checksum field is the 16 bit one's complement of the one's complement sum of all 16
        // bit words in the header and text.
        val dataLength = mIpHeader.dataLength
        var sum = mIpHeader.ipSum
        sum += (mIpHeader.protocol.toInt() and 0xFF).toLong()
        sum += dataLength.toLong()
        sum += getSum(offset, dataLength)
        while (sum shr 16 > 0) {
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        return sum.inv().toShort()
    }

    override fun toString(): String {
        return String.format(
            Locale.getDefault(), "%s%s%s%s%s%s %d -> %d %s:%s",
            if (flag.toInt() and SYN == SYN) "SYN" else "",
            if (flag.toInt() and ACK == ACK) "ACK" else "",
            if (flag.toInt() and PSH == PSH) "PSH" else "",
            if (flag.toInt() and RST == RST) "RST" else "",
            if (flag.toInt() and FIN == FIN) "FIN" else "",
            if (flag.toInt() and URG == URG) "URG" else "",
            sourcePort.toInt() and 0xFFFF,
            destinationPort.toInt() and 0xFFFF,
            seqID,
            ackID
        )
    }

    companion object {
        private const val OFFSET_SRC_PORT = 0
        private const val OFFSET_DEST_PORT = 2
        private const val OFFSET_LENRES = 12
        private const val OFFSET_CRC = 16
        private const val OFFSET_FLAG = 13
        private const val OFFSET_SEQ = 4
        private const val OFFSET_ACK = 8
        private const val FIN = 1
        private const val SYN = 2
        private const val RST = 4
        private const val PSH = 8
        private const val ACK = 16
        private const val URG = 32
    }
}