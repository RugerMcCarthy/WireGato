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

import java.nio.ByteBuffer
import java.util.*

/**
 * The UDP module  must be able to determine the source and destination internet addresses and
 * the protocol field from the internet header.
 *
 * UDP Header Format:
 *
 * 0      7 8     15 16    23 24    31
 * +--------+--------+--------+--------+
 * |     Source      |   Destination   |
 * |      Port       |      Port       |
 * +--------+--------+--------+--------+
 * |                 |                 |
 * |     Length      |    Checksum     |
 * +--------+--------+--------+--------+
 * |
 * |          data octets ...
 * +---------------- ...
 *
 * See https://tools.ietf.org/html/rfc768
 *
 * @author Megatron King
 * @since 2018-10-10 23:04
 */
class UdpHeader(val ipHeader: IpHeader, packet: ByteArray?, offset: Int) : Header(
    packet!!, offset
) {
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
    var crc: Short
        get() = readShort(offset + OFFSET_CRC)
        set(crc) {
            writeShort(crc, offset + OFFSET_CRC)
        }
    val headerLength: Int
        get() = 8
    val totalLength: Int
        get() = readShort(offset + OFFSET_TLEN).toInt() and 0xFFFF

    fun setTotalLength(len: Short) {
        writeShort(len, offset + OFFSET_TLEN)
    }

    fun updateChecksum() {
        crc = 0.toShort()
        crc = computeChecksum()
    }

    private fun computeChecksum(): Short {
        // Sum = Ip Sum(Source Address + Destination Address) + Protocol + UDP Length
        // Checksum is the 16-bit one's complement of the one's complement sum of a
        // pseudo header of information from the IP header, the UDP header, and the
        // data,  padded  with zero octets  at the end (if  necessary)  to  make  a
        // multiple of two octets.
        val dataLength = ipHeader.dataLength
        var sum = ipHeader.ipSum
        sum += (ipHeader.protocol.toInt() and 0xFF).toLong()
        sum += dataLength.toLong()
        sum += getSum(offset, dataLength)
        while (sum shr 16 > 0) {
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        return sum.inv().toShort()
    }

    override fun toString(): String {
        return String.format(
            Locale.getDefault(), "%d -> %d", sourcePort.toInt() and 0xFFFF,
            destinationPort.toInt() and 0xFFFF
        )
    }

    fun copy(): UdpHeader {
        val copyArray = packet.copyOf(packet.size)
        val ipHeader = IpHeader(copyArray, 0)
        return UdpHeader(ipHeader, copyArray, offset)
    }

    fun data(): ByteBuffer {
        val size = ipHeader.dataLength - headerLength
        val dataOffset = ipHeader.headerLength + headerLength
        val data = ByteArray(size)
        System.arraycopy(packet, dataOffset, data, 0, size)
        return ByteBuffer.wrap(data)
    }

    fun buffer(): ByteBuffer {
        return ByteBuffer.wrap(packet, 0, ipHeader.totalLength)
    }

    companion object {
        private const val OFFSET_SRC_PORT: Short = 0
        private const val OFFSET_DEST_PORT: Short = 2
        private const val OFFSET_TLEN: Short = 4
        private const val OFFSET_CRC: Short = 6
    }
}