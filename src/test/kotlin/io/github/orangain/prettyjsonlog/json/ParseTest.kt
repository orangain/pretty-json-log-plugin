package io.github.orangain.prettyjsonlog.json

import junit.framework.TestCase

class ParseTest : TestCase() {
    fun testParseJsonLine() {
        val result = parseJson("""{"key": "value"}""")
        assertNotNull(result)
        val (node, rest) = result!!
        assertEquals("""{"key":"value"}""", node.toString())
        assertEquals("", rest)
    }

    fun testParseJsonLineWithSpaces() {
        val result = parseJson(""" {"key": "value"}  """)
        assertNotNull(result)
        val (node, rest) = result!!
        assertEquals("""{"key":"value"}""", node.toString())
        assertEquals("  ", rest)
    }

    fun testParseBrokenJsonLine() {
        val result = parseJson("""{"key": "value" """)
        assertNull(result)
    }

    fun testParseEmptyString() {
        val result = parseJson("")
        assertNull(result)
    }
}