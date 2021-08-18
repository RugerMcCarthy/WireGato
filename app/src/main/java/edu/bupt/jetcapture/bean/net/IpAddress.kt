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

import android.os.Parcelable
import edu.bupt.jetcapture.bean.net.IpAddress
import android.os.Parcel
import java.util.*

class IpAddress : Parcelable {
    var address: String?
    var prefixLength: Int

    constructor(address: String?, prefixLength: Int) {
        this.address = address
        this.prefixLength = prefixLength
    }

    override fun toString(): String {
        return "$address/$prefixLength"
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        return if (o !is IpAddress) {
            false
        } else toString() == o.toString()
        // Compare string value.
    }

    override fun hashCode(): Int {
        return Objects.hash(address, prefixLength)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(address)
        dest.writeInt(prefixLength)
    }

    private constructor(`in`: Parcel) {
        address = `in`.readString()
        prefixLength = `in`.readInt()
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<IpAddress> = object : Parcelable.Creator<IpAddress> {
            override fun createFromParcel(source: Parcel): IpAddress? {
                return IpAddress(source)
            }

            override fun newArray(size: Int): Array<IpAddress?> {
                return arrayOfNulls(size)
            }
        }
    }
}