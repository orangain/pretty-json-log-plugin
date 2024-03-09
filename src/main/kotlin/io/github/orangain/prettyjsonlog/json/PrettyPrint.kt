package io.github.orangain.prettyjsonlog.json

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.JsonNode

private val writer = mapper.writer(MyPrettyPrinter())

fun prettyPrintJson(node: JsonNode): String {
    return writer.writeValueAsString(node)
}

class MyPrettyPrinter : DefaultPrettyPrinter() {
    init {
        _objectFieldValueSeparatorWithSpaces = ": "
    }

    override fun createInstance(): DefaultPrettyPrinter {
        return MyPrettyPrinter()
    }
}

private val prettyJsonPartRegex = Regex("""^([{}]| {2,}(".*": |}))""")

fun isPartOfPrettyJson(line: String): Boolean {
    return prettyJsonPartRegex.containsMatchIn(line)
}