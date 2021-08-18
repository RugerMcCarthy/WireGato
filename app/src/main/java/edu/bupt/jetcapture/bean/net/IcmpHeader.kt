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
 * ICMP messages are sent using the basic IP header. The first octet of the data portion of the
 * datagram is a ICMP type field; the value of this field determines the format of the remaining
 * data. Any field labeled "unused" is reserved for later extensions and must be zero when sent,
 * but receivers should not use these fields (except to include them in the checksum).
 * Unless otherwise noted under the individual format descriptions, the values of the internet
 * header fields are as follows:
 *
 * 0                   1                   2                   3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |     Type      |     Code      |          Checksum             |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                              TBD                              |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                            Optional                           |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 * See https://tools.ietf.org/html/rfc792
 *
 * @author Megatron King
 * @since 2018-10-10 23:04
 */
class IcmpHeader(val ipHeader: IpHeader, packet: ByteArray?, offset: Int) : Header(
    packet!!, offset
) {
    val type: Byte
        get() = readByte(offset + OFFSET_TYPE)
    val code: Byte
        get() = readByte(offset + OFFSET_CODE)
    var crc: Short
        get() = readShort(offset + OFFSET_CRC)
        set(crc) {
            writeShort(crc, offset + OFFSET_CRC)
        }

    fun updateChecksum() {
        crc = 0.toShort()
        crc = computeChecksum()
    }

    private fun computeChecksum(): Short {
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

    companion object {
        private const val OFFSET_TYPE: Short = 0
        private const val OFFSET_CODE: Short = 1
        private const val OFFSET_CRC: Short = 2
    }
}