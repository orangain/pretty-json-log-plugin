package io.github.orangain.prettyjsonlog

import io.github.orangain.prettyjsonlog.console.parseJson
import junit.framework.TestCase
import java.time.Instant

private data class ExtractParam(
    val description: String,
    val json: String,
    val expectedTimestamp: Timestamp?,
    val expectedLevel: String?,
    val expectedMessage: String?
)

private val params = listOf(
    // https://cloud.google.com/logging/docs/structured-logging
    ExtractParam(
        "Cloud Logging",
        """{"severity":"ERROR", "message":"There was an error in the application.", "httpRequest":{"requestMethod":"GET"},"time":"2020-10-12T07:20:50.52Z"}""",
        Timestamp.Parsed(Instant.parse("2020-10-12T07:20:50.52Z")),
        "ERROR",
        "There was an error in the application.",
    ),
    // https://pkg.go.dev/golang.org/x/exp/slog
    ExtractParam(
        "Go slog",
        """{"time":"2022-11-08T15:28:26.000000000-05:00","level":"INFO","msg":"hello","count":3}""",
        Timestamp.Parsed(Instant.parse("2022-11-08T20:28:26Z")),
        "INFO",
        "hello",
    ),
    // https://github.com/trentm/node-bunyan
    ExtractParam(
        "Bunyan",
        """{"name":"myapp","hostname":"banana.local","pid":40161,"level":30,"msg":"hi","time":"2013-01-04T18:46:23.851Z","v":0}""",
        Timestamp.Parsed(Instant.parse("2013-01-04T18:46:23.851Z")),
        "INFO",
        "hi",
    ),
)

class ExtractTest : TestCase() {
    fun testExtractTimestamp() {
        params.forEach { param ->
            val node = parseJson(param.json)!!
            val actual = extractTimestamp(node)
            assertEquals(param.description, param.expectedTimestamp, actual)
        }
    }
}