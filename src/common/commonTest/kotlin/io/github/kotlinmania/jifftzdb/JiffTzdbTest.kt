package io.github.kotlinmania.jifftzdb

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JiffTzdbTest {
    @Test
    fun versionMatchesBundledTzdb() {
        assertEquals("2025a", VERSION)
    }

    @Test
    fun getFindsKnownTimeZonesAndCanonicalizesAsciiCase() {
        assertNotNull(get("America/New_York"))
        assertNotNull(get("america/new_york"))
        assertNull(get("America/NewYork"))
        assertNull(get(""))
        assertNull(get("Africa/Abidja"))
        assertNull(get("Africa/Abidjan2"))

        val entry = assertNotNull(get("america/new_york"))
        val (canonicalName, data) = entry
        assertEquals("America/New_York", canonicalName)
        assertTrue(data.size >= 4)
        assertEquals("TZif", data.decodeToString(0, 4))

        val canonicalEntry = assertNotNull(get("America/New_York"))
        assertEquals(canonicalEntry, entry)
        assertEquals(canonicalEntry.hashCode(), entry.hashCode())
        assertContentEquals(canonicalEntry.data, data)
    }

    @Test
    fun availableYieldsAllEntriesForwardAndBackward() {
        val iter = available()
        assertEquals(597, iter.len())
        assertFalse(iter.isEmpty())

        val cloned = iter.clone()
        assertEquals(597, cloned.count())
        assertTrue(cloned.isEmpty())
        assertEquals(0, cloned.len())
        assertNull(cloned.next())
        assertNull(cloned.nextBack())

        val names = available().toList()
        assertEquals(597, names.size)
        assertEquals(TZNAME_TO_OFFSET.first().name, names.first())
        assertEquals(TZNAME_TO_OFFSET.last().name, names.last())

        val doubleEnded = available()
        val frontCollected = mutableListOf<String>()
        val backCollected = mutableListOf<String>()
        while (!doubleEnded.isEmpty()) {
            doubleEnded.next()?.let { frontCollected.add(it) }
            doubleEnded.nextBack()?.let { backCollected.add(it) }
        }
        assertNull(doubleEnded.next())
        assertNull(doubleEnded.nextBack())
        val reconstructed = frontCollected + backCollected.asReversed()
        assertEquals(names, reconstructed)
    }

    @Test
    fun sortedAsciiCaseInsensitive() {
        for (i in 0 until TZNAME_TO_OFFSET.size - 1) {
            val name1 = TZNAME_TO_OFFSET[i].name
            val name2 = TZNAME_TO_OFFSET[i + 1].name
            assertTrue(
                cmpIgnoreAsciiCase(name1, name2) < 0,
                "$name1 should be less than $name2",
            )
        }
    }

    @Test
    fun allEmbeddedEntriesHaveValidTzifHeaderAndBounds() {
        assertEquals(TZIF_DATA_TOTAL_BYTES, TZIF_DATA.size)
        for (entry in TZNAME_TO_OFFSET) {
            assertTrue(entry.offset >= 0, "Negative offset for ${entry.name}")
            assertTrue(entry.length >= 4, "Payload too short for ${entry.name}")
            assertTrue(
                entry.offset + entry.length <= TZIF_DATA.size,
                "Out of bounds slice for ${entry.name}",
            )
            assertEquals('T'.code.toByte(), TZIF_DATA[entry.offset])
            assertEquals('Z'.code.toByte(), TZIF_DATA[entry.offset + 1])
            assertEquals('i'.code.toByte(), TZIF_DATA[entry.offset + 2])
            assertEquals('f'.code.toByte(), TZIF_DATA[entry.offset + 3])

            val lookedUp = assertNotNull(get(entry.name.lowercase()), "Lookup failed for ${entry.name}")
            assertEquals(entry.name, lookedUp.canonicalName)
            assertEquals(entry.length, lookedUp.data.size)
        }
        assertNotEquals(
            assertNotNull(get("America/New_York")),
            assertNotNull(get("Europe/London")),
        )
    }
}
