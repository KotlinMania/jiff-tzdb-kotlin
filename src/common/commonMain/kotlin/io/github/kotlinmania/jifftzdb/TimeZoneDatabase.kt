package io.github.kotlinmania.jifftzdb

/**
 * The version of the IANA Time Zone Database that was bundled.
 *
 * If this bundled database was generated from a pre-existing system copy
 * of the Time Zone Database, then it's possible no version information was
 * available.
 */
public val VERSION: String? = TZNAME_VERSION

/**
 * Canonical time zone identifier and its raw RFC 8536 TZif byte payload returned by [get].
 */
public class TimeZoneEntry(
    public val canonicalName: String,
    public val data: ByteArray,
) {
    public operator fun component1(): String = canonicalName

    public operator fun component2(): ByteArray = data

    override fun equals(other: Any?): Boolean =
        this === other ||
            (
                other is TimeZoneEntry &&
                    canonicalName == other.canonicalName &&
                    data.contentEquals(other.data)
            )

    override fun hashCode(): Int = 31 * canonicalName.hashCode() + data.contentHashCode()

    override fun toString(): String = "TimeZoneEntry(canonicalName=$canonicalName, dataSize=${data.size})"
}

/**
 * Returns the binary TZif data for the time zone name given.
 *
 * This also returns the canonical name for the time zone. Namely, since this
 * lookup is performed without regard to ASCII case, the given name may not be
 * the canonical capitalization of the time zone.
 *
 * If no matching time zone data exists, then `null` is returned.
 */
public fun get(name: String): TimeZoneEntry? {
    val idx = index(name) ?: return null
    val entry = TZNAME_TO_OFFSET[idx]
    val slice = TZIF_DATA.copyOfRange(entry.offset, entry.offset + entry.length)
    return TimeZoneEntry(canonicalName = entry.name, data = slice)
}

/**
 * Returns an iterator over all available time zone names bundled into this crate.
 *
 * There are no API guarantees on the order of the sequence returned.
 */
public fun available(): TimeZoneNameIter = TimeZoneNameIter(startIndex = 0, endExclusive = TZNAME_TO_OFFSET.size)

/**
 * Finds the index of a matching entry in [TZNAME_TO_OFFSET].
 *
 * If the given time zone doesn't exist, then `null` is returned.
 */
internal fun index(queryName: String): Int? {
    var low = 0
    var high = TZNAME_TO_OFFSET.size - 1
    while (low <= high) {
        val mid = (low + high) ushr 1
        val cmp = cmpIgnoreAsciiCase(TZNAME_TO_OFFSET[mid].name, queryName)
        when {
            cmp < 0 -> low = mid + 1
            cmp > 0 -> high = mid - 1
            else -> return mid
        }
    }
    return null
}

/**
 * Compares two strings lexicographically by UTF-8 bytes ignoring ASCII case, matching Rust's
 * `cmp_ignore_ascii_case`.
 */
internal fun cmpIgnoreAsciiCase(
    s1: String,
    s2: String,
): Int {
    val bytes1 = s1.encodeToByteArray()
    val bytes2 = s2.encodeToByteArray()
    val minLen = minOf(bytes1.size, bytes2.size)
    for (i in 0 until minLen) {
        val b1 = asciiLowerByte(bytes1[i])
        val b2 = asciiLowerByte(bytes2[i])
        if (b1 != b2) {
            return b1 - b2
        }
    }
    return bytes1.size.compareTo(bytes2.size)
}

private fun asciiLowerByte(b: Byte): Int {
    val unsigned = b.toInt() and 0xFF
    return if (unsigned in 'A'.code..'Z'.code) {
        unsigned + ('a'.code - 'A'.code)
    } else {
        unsigned
    }
}
