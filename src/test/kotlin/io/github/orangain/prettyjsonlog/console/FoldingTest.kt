package io.github.orangain.prettyjsonlog.console

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import junit.framework.TestCase

class FoldingTest : TestCase() {
    private data class Param(
        val description: String,
        val jsonCreator: (ObjectNode) -> Unit
    )

    private val mapper = jacksonObjectMapper()

    private val params = listOf(
        Param("Empty object") { },
        Param("Simple object") {
            it.put("string", "x")
            it.put("number", 1)
            it.put("true", true)
            it.put("false", false)
            it.putNull("null")
        },
        Param("Nested object") {
            it.putObject("nested").apply {
                put("string", "x")
                put("number", 1)
                put("true", true)
                put("false", false)
                putNull("null")
            }
        },
        Param("Array") {
            it.putArray("array").apply {
                add("x")
                add(1)
                add(true)
                add(false)
                addNull()
                addObject().apply {
                    put("foo", "bar")
                }
            }
        },
        Param("Nested array") {
            it.putObject("nested").apply {
                putArray("array").apply {
                    add("x")
                    add(1)
                    add(true)
                    add(false)
                    addNull()
                }
            }
        },
        Param("Array with many elements") {
            it.putArray("array").apply {
                for (i in 1..100) {
                    add(i)
                }
            }
        },
    )

    fun testFolding() {
        params.forEach { param ->
            val node = mapper.createObjectNode().apply(param.jsonCreator)
            assertAllLinesFolded(param.description, node)
        }
    }

    private fun assertAllLinesFolded(description: String, node: JsonNode) {
        val json = prettyPrintJson(node)
        println(json)
        val lines = json.lines()
        assertTrue("[$description] Lines should not be empty", lines.isNotEmpty())
        lines.forEachIndexed { index, line ->
            assertTrue("[$description] Line $index should be folded", isPartOfPrettyJson(line))
        }
    }
}