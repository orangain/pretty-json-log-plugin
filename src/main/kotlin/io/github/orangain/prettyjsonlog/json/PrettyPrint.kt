package io.github.orangain.prettyjsonlog.json

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.JsonNode
import java.io.StringReader
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

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

fun prettifyXml(xml: String): String {
    val transformer = TransformerFactory.newInstance().newTransformer().apply {
        setOutputProperty(OutputKeys.INDENT, "yes")
        setOutputProperty("{http:://xml.apache.org/xslt}indent-amount", "2")
    }
    val xmlInput = StreamSource(StringReader(xml))
    val writer = StringWriter()
    transformer.transform(xmlInput, StreamResult(writer))
    return writer.toString()
}
