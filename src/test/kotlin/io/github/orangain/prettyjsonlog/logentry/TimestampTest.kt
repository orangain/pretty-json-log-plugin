package io.github.orangain.prettyjsonlog.logentry

import junit.framework.TestCase
import java.time.Instant

private val testCases: List<Pair<Long, String>> = listOf(
    // seconds
    0L to "1970-01-01T00:00:00Z",
    1L to "1970-01-01T00:00:01Z",
    1000L to "1970-01-01T00:16:40Z",
    1000000000L to "2001-09-09T01:46:40Z",
    1000000001L to "2001-09-09T01:46:41Z",
    1723644284L to "2024-08-14T14:04:44Z",
    9999999999L to "2286-11-20T17:46:39Z",
    // milliseconds
    10000000000L to "1970-04-26T17:46:40Z",
    10000000001L to "1970-04-26T17:46:40.001Z",
    1723644284001L to "2024-08-14T14:04:44.001Z",
    9999999999999L to "2286-11-20T17:46:39.999Z",
    // microseconds
    10000000000000L to "1970-04-26T17:46:40Z",
    10000000000001L to "1970-04-26T17:46:40.000001Z",
    1723644284000001L to "2024-08-14T14:04:44.000001Z",
    9999999999999999L to "2286-11-20T17:46:39.999999Z",
    // nanoseconds
    10000000000000000L to "1970-04-26T17:46:40Z",
    10000000000000001L to "1970-04-26T17:46:40.000000001Z",
    1723644284000000001L to "2024-08-14T14:04:44.000000001Z",
    9223372036854775807L to "2262-04-11T23:47:16.854775807Z", // Long.MAX_VALUE
)

class TimestampTest : TestCase() {
    fun testFromEpoch() {
        testCases.forEach { (input, expected) ->
            val actual = Timestamp.fromEpoch(input)
            assertEquals("Timestamp.fromEpoch($input)", Timestamp.Parsed(Instant.parse(expected)), actual)
        }
    }
}
