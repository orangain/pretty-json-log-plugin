package io.github.orangain.prettyjsonlog.logentry

import io.github.orangain.prettyjsonlog.json.parseJson
import junit.framework.TestCase
import java.time.Instant

private data class ExtractParam(
    val description: String,
    val json: String,
    val expectedTimestamp: Timestamp?,
    val expectedLevel: Level?,
    val expectedMessage: String?,
    val expectedStackTrace: String?
)

private val params = listOf(
    // https://github.com/GoogleCloudPlatform/spring-cloud-gcp/blob/main/spring-cloud-gcp-logging/src/main/java/com/google/cloud/spring/logging/StackdriverJsonLayout.java
    ExtractParam(
        "Google Cloud Json Layout from com.google.cloud.spring.logging.StackdriverJsonLayout",
        """{"context": "default","logger": "com.example.MyClass","message": "Hello, world!","severity": "INFO","thread": "main","timestampNanos": 69022000,"timestampSeconds": 1729071565}""",
        Timestamp.Parsed(Instant.parse("2024-10-16T09:39:25.069022Z")),
        Level.INFO,
        "Hello, world!",
        null,
    ),
    // https://cloud.google.com/logging/docs/structured-logging
    ExtractParam(
        "Cloud Logging",
        """{"severity":"ERROR", "message":"There was an error in the application.", "httpRequest":{"requestMethod":"GET"},"time":"2020-10-12T07:20:50.52Z"}""",
        Timestamp.Parsed(Instant.parse("2020-10-12T07:20:50.52Z")),
        Level.ERROR,
        "There was an error in the application.",
        null,
    ),
    // https://cloud.google.com/error-reporting/docs/formatting-error-messages#log-error
    ExtractParam(
        "Cloud Logging with stack trace in the stack_trace field for Error Reporting",
        """{"severity":"ERROR", "message":"There was an error in the application.","stack_trace": "com.example.shop.TemplateCartDiv retrieveCart: Error\njava.lang.IndexOutOfBoundsException: Index: 4, Size: 4\n\tat java.util.ArrayList.rangeCheck(ArrayList.java:635)\n","time":"2020-10-12T07:20:50.52Z"}""",
        Timestamp.Parsed(Instant.parse("2020-10-12T07:20:50.52Z")),
        Level.ERROR,
        "There was an error in the application.",
        "com.example.shop.TemplateCartDiv retrieveCart: Error\n"
                + "java.lang.IndexOutOfBoundsException: Index: 4, Size: 4\n"
                + "\tat java.util.ArrayList.rangeCheck(ArrayList.java:635)\n",
    ),
    // https://cloud.google.com/error-reporting/docs/formatting-error-messages#log-error
    ExtractParam(
        "Cloud Logging with stack trace in the exception field for Error Reporting",
        """{"severity":"ERROR", "message":"There was an error in the application.","exception": "com.example.shop.TemplateCartDiv retrieveCart: Error\njava.lang.IndexOutOfBoundsException: Index: 4, Size: 4\n\tat java.util.ArrayList.rangeCheck(ArrayList.java:635)\n","time":"2020-10-12T07:20:50.52Z"}""",
        Timestamp.Parsed(Instant.parse("2020-10-12T07:20:50.52Z")),
        Level.ERROR,
        "There was an error in the application.",
        "com.example.shop.TemplateCartDiv retrieveCart: Error\n"
                + "java.lang.IndexOutOfBoundsException: Index: 4, Size: 4\n"
                + "\tat java.util.ArrayList.rangeCheck(ArrayList.java:635)\n",
    ),
    // https://pkg.go.dev/log/slog
    ExtractParam(
        "log/slog in Go",
        """{"time":"2022-11-08T15:28:26.000000000-05:00","level":"INFO","msg":"hello","count":3}""",
        Timestamp.Parsed(Instant.parse("2022-11-08T20:28:26Z")),
        Level.INFO,
        "hello",
        null,
    ),
    // https://github.com/trentm/node-bunyan
    ExtractParam(
        "Bunyan",
        """{"name":"myapp","hostname":"banana.local","pid":40161,"level":30,"msg":"hi","time":"2013-01-04T18:46:23.851Z","v":0,"err":{"message":"boom","name":"TypeError","stack":"TypeError: boom\n    at Object.<anonymous> ..."}}""",
        Timestamp.Parsed(Instant.parse("2013-01-04T18:46:23.851Z")),
        Level.INFO,
        "hi",
        "TypeError: boom\n    at Object.<anonymous> ...",
    ),
    // https://github.com/pinojs/pino
    ExtractParam(
        "Pino",
        """{"level":30,"time":1531171074631,"msg":"hello world","pid":657,"hostname":"Davids-MBP-3.fritz.box"}""",
        Timestamp.Parsed(Instant.parse("2018-07-09T21:17:54.631Z")),
        Level.INFO,
        "hello world",
        null,
    ),
    // https://github.com/logfellow/logstash-logback-encoder
    ExtractParam(
        "Logstash Logback Encoder",
        """{"@timestamp":"2019-11-03T10:15:30.123+01:00","@version":"1","message":"My message","logger_name":"org.company.stack.Sample","thread_name":"main","level":"INFO","level_value":20000}""",
        Timestamp.Parsed(Instant.parse("2019-11-03T09:15:30.123Z")),
        Level.INFO,
        "My message",
        null,
    ),
    // https://logging.apache.org/log4j/2.x/manual/json-template-layout.html
    ExtractParam(
        "Log4j2 with EcsLayout.json",
        """{"@timestamp":"2024-07-15T03:36:52.899Z","ecs.version":"1.2.0","error.message":null,"error.stack_trace":"javax.transaction.xa.XAException\n\tat org.apache.activemq.artemis.core.protocol.core.impl.ActiveMQSessionContext.xaCommit(ActiveMQSessionContext.java:495)\n","error.type":"javax.transaction.xa.XAException","log.level":"WARN","log.logger":"com.atomikos.datasource.xa.XAResourceTransaction","message": "XA resource 'jms': commit for XID '27726F6F742D6170706C69636174696F6E27313732303738363038353131323031303130:27726F6F742D6170706C69636174696F6E27393136' raised -4: the supplied XID is invalid for this XA resource","process.thread.name":"Atomikos:5"}""",
        Timestamp.Parsed(Instant.parse("2024-07-15T03:36:52.899Z")),
        Level.WARN,
        "XA resource 'jms': commit for XID '27726F6F742D6170706C69636174696F6E27313732303738363038353131323031303130:27726F6F742D6170706C69636174696F6E27393136' raised -4: the supplied XID is invalid for this XA resource",
        "javax.transaction.xa.XAException\n" +
                "\tat org.apache.activemq.artemis.core.protocol.core.impl.ActiveMQSessionContext.xaCommit(ActiveMQSessionContext.java:495)\n",
    ),
    // https://docs.spring.io/spring-boot/reference/features/logging.html#features.logging.structured.ecs
    ExtractParam(
        "Spring Boot ECS nested format",
        """{"@timestamp":"2025-08-30T13:30:22.880349487Z","log":{"level":"ERROR","logger":"com.example.spring.logging.HelloController"},"process":{"pid":57692,"thread":{"name":"http-nio-8080-exec-2"}},"service":{"name":"structured-logging","node":{}},"message":"exception","error":{"type":"java.lang.RuntimeException","message":"Oops!!","stack_trace":"java.lang.RuntimeException: Oops!!\n\tat com.example.spring.logging.HelloController.message(HelloController.java:27)\n\tat java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)\n"},"ecs":{"version":"8.11"}}""",
        Timestamp.Parsed(Instant.parse("2025-08-30T13:30:22.880349487Z")),
        Level.ERROR,
        "exception",
        "java.lang.RuntimeException: Oops!!\n" +
                "\tat com.example.spring.logging.HelloController.message(HelloController.java:27)\n" +
                "\tat java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)\n",
    ),
    // https://pkg.go.dev/go.uber.org/zap
    ExtractParam(
        "Zap Logger Production Default",
        """{"caller": "devorer/main.go:60", "level": "info", "msg": "application starting...", "ts": 1.7235729053485353E9}""",
        Timestamp.Parsed(Instant.parse("2024-08-13T18:15:05Z")),
        Level.INFO,
        "application starting...",
        null,
    ),
    // https://github.com/serilog/serilog-formatting-compact?tab=readme-ov-file#format-details
    ExtractParam(
        "Serilog Rendered Compact JSON",
        """{"@t":"2024-09-07T03:50:13.7292340Z","@m":"Unhandled exception","@i":"f80f533c","@l":"Error","@x":"System.InvalidOperationException: Oops...\n   at Program.<Main>${'$'}(String[] args) in /Users/orange/RiderProjects/ConsoleApp1/ConsoleApp1/Program.cs:line 19"}""",
        Timestamp.Parsed(Instant.parse("2024-09-07T03:50:13.7292340Z")),
        Level.ERROR,
        "Unhandled exception",
        """
            System.InvalidOperationException: Oops...
               at Program.<Main>${'$'}(String[] args) in /Users/orange/RiderProjects/ConsoleApp1/ConsoleApp1/Program.cs:line 19
        """.trimIndent(),
    ),
    // https://github.com/serilog/serilog/blob/main/src/Serilog/Formatting/Json/JsonFormatter.cs
    ExtractParam(
        "Serilog Rendered JSON",
        """{"Timestamp":"2024-09-07T15:48:19.6174980+09:00","Level":"Error","MessageTemplate":"Unhandled exception","RenderedMessage":"Unhandled exception","Exception":"System.InvalidOperationException: Oops...\n   at Program.<Main>${'$'}(String[] args) in /Users/orange/RiderProjects/ConsoleApp1/ConsoleApp1/Program.cs:line 19"}""",
        Timestamp.Parsed(Instant.parse("2024-09-07T06:48:19.617498Z")),
        Level.ERROR,
        "Unhandled exception",
        """
            System.InvalidOperationException: Oops...
               at Program.<Main>${'$'}(String[] args) in /Users/orange/RiderProjects/ConsoleApp1/ConsoleApp1/Program.cs:line 19
        """.trimIndent(),
    ),
)

class ExtractTest : TestCase() {
    fun testExtractTimestamp() {
        params.forEach { param ->
            val (node, _) = parseJson(param.json)!!
            val actual = extractTimestamp(node)
            assertEquals(param.description, param.expectedTimestamp, actual)
        }
    }

    fun testExtractLevel() {
        params.forEach { param ->
            val (node, _) = parseJson(param.json)!!
            val actual = extractLevel(node)
            assertEquals(param.description, param.expectedLevel, actual)
        }
    }

    fun testExtractMessage() {
        params.forEach { param ->
            val (node, _) = parseJson(param.json)!!
            val actual = extractMessage(node)
            assertEquals(param.description, param.expectedMessage, actual)
        }
    }

    fun testStackTrace() {
        params.forEach { param ->
            val (node, _) = parseJson(param.json)!!
            val actual = extractStackTrace(node)
            assertEquals(param.description, param.expectedStackTrace, actual)
        }
    }
}
