package io.github.orangain.prettyjsonlog.json

import junit.framework.TestCase


class NotFoldingTest : TestCase() {
    private data class Param(
        val description: String,
        val text: String
    )

    private val params = listOf(
        Param("Log message", "[12:34:56.789] INFO: This is a log message"),
        Param("Empty line", ""),
        Param(
            "Multiline SQL", """
            SELECT
                id,
                name
            FROM
                table
            WHERE
                id = 1
        """.trimIndent()
        ),
        Param("Starts with space and double quote", """  "foo" """),
    )

    fun testNotFolding() {
        params.forEach { param ->
            assertAllLinesNotFolded(param.description, param.text)
        }
    }

    private fun assertAllLinesNotFolded(description: String, text: String) {
        val lines = text.lines()
        assertTrue("[$description] Lines should not be empty", lines.isNotEmpty())
        lines.forEachIndexed { index, line ->
            assertFalse("[$description] Line $index should not be folded", isPartOfPrettyJson(line))
        }
    }
}