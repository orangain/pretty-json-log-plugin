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
    // https://github.com/pinojs/pino
    ExtractParam(
        "Pino",
        """{"level":30,"time":1531171074631,"msg":"hello world","pid":657,"hostname":"Davids-MBP-3.fritz.box"}""",
        Timestamp.Parsed(Instant.parse("2018-07-09T21:17:54.631Z")),
        "INFO",
        "hello world",
    ),
    // https://github.com/logfellow/logstash-logback-encoder
    ExtractParam(
        "Logstash Logback Encoder",
        """{"@timestamp":"2019-11-03T10:15:30.123+01:00","@version":"1","message":"My message","logger_name":"org.company.stack.Sample","thread_name":"main","level":"INFO","level_value":20000}""",
        Timestamp.Parsed(Instant.parse("2019-11-03T09:15:30.123Z")),
        "INFO",
        "My message",
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