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
 * An abstract header object for ip protocol packets, provides some common apis.
 *
 * @author Megatron King
 * @since 2018-10-09 16:28
 */
/* package */
abstract class Header     /* package */(var packet: ByteArray, var offset: Int) {
    fun readByte(offset: Int): Byte {
        return packet[offset]
    }

    fun writeByte(value: Byte, offset: Int) {
        packet[offset] = value
    }

    fun readShort(offset: Int): Short {
        val r: Int = packet[offset].toInt() and 0xFF shl 8 or (packet[offset + 1].toInt() and 0xFF)
        return r.toShort()
    }

    fun writeShort(value: Short, offset: Int) {
        packet[offset] = (value.toInt() shr 8).toByte()
        packet[offset + 1] = value.toByte()
    }

    fun readInt(offset: Int): Int {
        return (packet[offset].toInt() and 0xFF shl 24
                or (packet[offset + 1].toInt() and 0xFF shl 16)
                or (packet[offset + 2].toInt() and 0xFF shl 8)
                or (packet[offset + 3].toInt() and 0xFF))
    }

    fun writeInt(value: Int, offset: Int) {
        packet[offset] = (value shr 24).toByte()
        packet[offset + 1] = (value shr 16).toByte()
        packet[offset + 2] = (value shr 8).toByte()
        packet[offset + 3] = value.toByte()
    }

    fun getSum(offset: Int, len: Int): Long {
        var offset = offset
        var len = len
        var sum: Long = 0
        while (len > 1) {
            sum += (readShort(offset).toInt() and 0xFFFF).toLong()
            offset += 2
            len -= 2
        }
        if (len > 0) {
            sum += (packet[offset].toInt() and 0xFF shl 8).toLong()
        }
        return sum
    }
}