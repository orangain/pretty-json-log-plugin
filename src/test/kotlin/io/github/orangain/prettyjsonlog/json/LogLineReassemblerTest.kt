package io.github.orangain.prettyjsonlog.json

import junit.framework.TestCase

class LogLineReassemblerTest : TestCase() {

    private fun chunk(s: String, size: Int): List<String> =
        s.chunked(size)

    fun testCompleteLineInOneFragment() {
        val reassembler = LogLineReassembler()
        val result = reassembler.feed("""{"key": "value"}""" + "\n")
        assertTrue(result is ParseResult.Complete)
        result as ParseResult.Complete
        assertEquals("""{"key":"value"}""", result.node.toString())
        assertEquals("\n", result.suffixWhitespaces)
    }

    fun testLargeJsonSplitIntoFragmentsIsReassembled() {
        // Build a single minified JSON line larger than the platform's 8192-char read buffer.
        val sb = StringBuilder("""{"message":"start"""")
        for (i in 0 until 2000) {
            sb.append(""","field$i":"value$i"""")
        }
        sb.append("}")
        val line = sb.toString() + "\n"
        assertTrue("test line should exceed the 8 KB read buffer", line.length > 16384)

        val fragments = chunk(line, 8192)
        assertTrue("test should produce multiple fragments", fragments.size >= 2)

        val reassembler = LogLineReassembler()
        for (i in 0 until fragments.size - 1) {
            assertEquals(
                "fragment $i should be buffered, not emitted",
                ParseResult.Buffering,
                reassembler.feed(fragments[i])
            )
        }
        val last = reassembler.feed(fragments.last())
        assertTrue("last fragment should complete the line", last is ParseResult.Complete)
        last as ParseResult.Complete

        // Reassembled node must equal the node parsed from the whole line at once.
        val whole = parseJson(line)!!
        assertEquals(whole.first, last.node)
        assertEquals(whole.second, last.suffixWhitespaces)
    }

    fun testLargeNonJsonIsPassedThroughWithoutLoss() {
        val line = "{" + "x".repeat(20000) + "\n" // starts like JSON, but is not valid JSON
        val fragments = chunk(line, 8192)

        val reassembler = LogLineReassembler()
        for (i in 0 until fragments.size - 1) {
            assertEquals(ParseResult.Buffering, reassembler.feed(fragments[i]))
        }
        val last = reassembler.feed(fragments.last())
        assertTrue("non-JSON line should be passed through", last is ParseResult.Passthrough)
        last as ParseResult.Passthrough
        // The whole original text must be preserved (nothing suppressed is lost).
        assertEquals(line, last.text)
    }

    fun testShortNonJsonLineIsLeftUnchanged() {
        val reassembler = LogLineReassembler()
        assertEquals(ParseResult.NotJson, reassembler.feed("plain log message\n"))
    }

    fun testShortUnterminatedJsonStartIsNotBuffered() {
        // Below the split threshold, an incomplete '{'-line is not buffered: it would only be
        // split by the platform if it were ~8 KB, so we treat it as today (leave unchanged).
        val reassembler = LogLineReassembler()
        assertEquals(ParseResult.NotJson, reassembler.feed("""{"key": "value" """))
    }

    fun testDrainReturnsAndClearsBufferedText() {
        val firstFragment = "{" + "x".repeat(9000) // > threshold, no newline -> buffered
        val reassembler = LogLineReassembler()
        assertEquals(ParseResult.Buffering, reassembler.feed(firstFragment))

        assertEquals(firstFragment, reassembler.drain())
        // After draining, the buffer is empty again.
        assertNull(reassembler.drain())
    }
}
