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

/**
 * A summary of the contents of the internet header follows:
 *
 * 0                   1                   2                   3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |Version|  IHL  |Type of Service|          Total Length         |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |         Identification        |Flags|      Fragment Offset    |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |  Time to Live |    Protocol   |         Header Checksum       |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                       Source Address                          |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                    Destination Address                        |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                    Options                    |    Padding    |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 * See https://tools.ietf.org/html/rfc791#section-3.1
 *
 * @author Megatron King
 * @since 2018-10-10 21:02
 */
class IpHeader(packet: ByteArray, offset: Int) : Header(
    packet, offset
) {
    var protocol: Byte
        get() = packet[offset + OFFSET_PROTOCOL]
        set(value) {
            packet[offset + OFFSET_PROTOCOL] = value
        }
    var headerLength: Int
        get() = (packet[offset + OFFSET_VER_IHL].toInt() and 0x0F) * 4
        set(value) {
            packet[offset + OFFSET_VER_IHL] = (4 shl 4 or value / 4).toByte()
        }
    var sourceIp: Int
        get() = readInt(offset + OFFSET_SRC_IP)
        set(ip) {
            writeInt(ip, offset + OFFSET_SRC_IP)
        }
    var destinationIp: Int
        get() = readInt(offset + OFFSET_DEST_IP)
        set(ip) {
            writeInt(ip, offset + OFFSET_DEST_IP)
        }
    val dataLength: Int
        get() = totalLength - headerLength
    val totalLength: Int
        get() = readShort(offset + OFFSET_TLEN).toInt() and 0xFFFF

    fun setTotalLength(len: Short) {
        writeShort(len, offset + OFFSET_TLEN)
    }

    var crc: Short
        get() = readShort(offset + OFFSET_CRC)
        set(crc) {
            writeShort(crc, offset + OFFSET_CRC)
        }

    fun updateChecksum() {
        crc = 0.toShort()
        crc = computeChecksum()
    }

    // length 8 = src ip(4) + dest ip(4)
    val ipSum: Long
        get() =// length 8 = src ip(4) + dest ip(4)
            getSum(offset + OFFSET_SRC_IP, 8)
    val version: Int
        get() = packet[offset].toInt() shr 4 and 0xF

    private fun computeChecksum(): Short {
        // The checksum field is the 16 bit one's complement of the one's complement sum of all
        // 16 bit words in the header.
        val len = headerLength
        var sum = getSum(offset, len)
        while (sum shr 16 > 0) {
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        return sum.inv().toShort()
    }

    companion object {
        const val MIN_HEADER_LENGTH = 20
        private const val OFFSET_PROTOCOL = 9
        private const val OFFSET_VER_IHL = 0
        private const val OFFSET_SRC_IP = 12
        private const val OFFSET_DEST_IP = 16
        private const val OFFSET_TLEN = 2
        private const val OFFSET_CRC = 10
    }
}