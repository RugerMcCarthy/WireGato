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
 * The enum defines all supported IP protocols.
 *
 * Internet Protocol numbers see:
 * https://en.wikipedia.org/wiki/List_of_IP_protocol_numbers
 *
 * @author Megatron King
 * @since 2018-10-11 00:13
 */
enum class Protocol(val number: Byte) {
    /**
     * Internet Control Message Protocol.
     */
    ICMP(1.toByte()),

    /**
     * Transmission Control Protocol.
     */
    TCP(6.toByte()),

    /**
     * User Datagram Protocol.
     */
    UDP(17.toByte());

    companion object {
        /**
         * Parse the protocol by number.
         *
         * @param number Protocol number.
         * @return The supported protocol number or null.
         */
        fun parse(number: Int): Protocol? {
            for (protocol in values()) {
                if (protocol.number.toInt() == number) {
                    return protocol
                }
            }
            return null
        }
    }
}