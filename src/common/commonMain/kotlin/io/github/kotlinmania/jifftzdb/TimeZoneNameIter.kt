package io.github.kotlinmania.jifftzdb

/**
 * An iterator over all time zone names embedded into this crate.
 *
 * There are no API guarantees on the order of this iterator.
 *
 * This iterator is created by the [available] function.
 */
public class TimeZoneNameIter internal constructor(
    private var startIndex: Int,
    private var endExclusive: Int,
) {
    /** Advances the iterator from the front and returns the next time zone name, or `null`. */
    public fun next(): String? {
        if (startIndex >= endExclusive) {
            return null
        }
        val name = TZNAME_TO_OFFSET[startIndex].name
        startIndex += 1
        return name
    }

    /** Advances the iterator from the back and returns the next time zone name, or `null`. */
    public fun nextBack(): String? {
        if (startIndex >= endExclusive) {
            return null
        }
        endExclusive -= 1
        return TZNAME_TO_OFFSET[endExclusive].name
    }

    /** Returns the exact remaining length of this iterator. */
    public fun len(): Int = endExclusive - startIndex

    /** Returns `true` when no remaining time zone names exist in this iterator. */
    public fun isEmpty(): Boolean = startIndex >= endExclusive

    /** Consumes the remaining elements and returns the count yielded. */
    public fun count(): Int {
        val remaining = len()
        startIndex = endExclusive
        return remaining
    }

    /** Consumes the remaining elements in forward order into a [List]. */
    public fun toList(): List<String> {
        val out = ArrayList<String>(len())
        while (startIndex < endExclusive) {
            out.add(TZNAME_TO_OFFSET[startIndex].name)
            startIndex += 1
        }
        return out
    }

    /** Returns an independent copy of this iterator positioned at the same bounds. */
    public fun clone(): TimeZoneNameIter = TimeZoneNameIter(startIndex, endExclusive)
}
