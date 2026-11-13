#if canImport(Testing)
import Testing
import JiffTzdb

@Suite("JiffTzdb Swift Export Tests")
struct JiffTzdbExportTests {
    @Test("JiffTzdb swift module imported cleanly")
    func testSwiftModuleLoads() throws {
        #expect(Bool(true), "JiffTzdb swift module imported cleanly")
    }
}
#elseif canImport(XCTest)
import XCTest
import JiffTzdb

final class JiffTzdbExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "JiffTzdb swift module imported cleanly")
    }
}
#endif
