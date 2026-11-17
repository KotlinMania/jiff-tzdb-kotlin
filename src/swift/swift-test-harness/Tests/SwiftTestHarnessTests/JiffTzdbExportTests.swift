import Testing
import JiffTzdb

@Suite("JiffTzdb Swift Export Tests")
struct JiffTzdbExportTests {
    @Test("VERSION and time zone lookups match IANA TZDB 2025a")
    func testVersionAndLookup() throws {
        #expect(VERSION == "2025a")

        let lower = try #require(get(name: "america/new_york"))
        #expect(lower.canonicalName == "America/New_York")
        let data = lower.data
        #expect(data.size >= 4)
        #expect(data[0] == 0x54) // 'T'
        #expect(data[1] == 0x5A) // 'Z'
        #expect(data[2] == 0x69) // 'i'
        #expect(data[3] == 0x66) // 'f'

        #expect(get(name: "America/NewYork") == nil)
    }

    @Test("available iterator yields 597 time zone names forward and backward")
    func testAvailableIterator() throws {
        let iter = available()
        #expect(iter.len() == 597)
        #expect(!iter.isEmpty())

        let first = try #require(iter.next())
        #expect(first == "Africa/Abidjan")

        let last = try #require(iter.nextBack())
        #expect(last == "Zulu")
        #expect(iter.len() == 595)

        let remainingCount = iter.count()
        #expect(remainingCount == 595)
        #expect(iter.isEmpty())
    }
}
