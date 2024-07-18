import java.time.Instant

fun String.escapeJsonString(): String {
    return this
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
        .replace("\b", "\\b")
}

fun log(level: String, message: String, traceId: String) {
    val timestamp = Instant.now().toString()
    val pid = ProcessHandle.current().pid()
    val thread = Thread.currentThread().name
    val escapedMessage = message.escapeJsonString()
    val json =
        """{"timestamp":"$timestamp","pid":$pid,"thread":"$thread","level":"$level","message":"$escapedMessage","traceId":"$traceId"}"""
    println(json)
}

fun logException(message: String, e: Exception, traceId: String) {
    val level = "ERROR"
    val timestamp = Instant.now().toString()
    val pid = ProcessHandle.current().pid()
    val thread = Thread.currentThread().name
    val escapedMessage = message.escapeJsonString()
    val escapedStackTrace = e.stackTraceToString().escapeJsonString()
    val json =
        """{"timestamp":"$timestamp","pid":$pid,"thread":"$thread","level":"$level","message":"$escapedMessage","stack_trace":"$escapedStackTrace","traceId":"$traceId"}"""
    println(json)
}

val trace1 = "0af7651916cd43dd8448eb211c80319c"
log("INFO", "Starting request", trace1)
log("DEBUG", "Processing request", trace1)
log("WARN", "Request is too large", trace1)
log("DEBUG", "Still processing request", trace1)
log("INFO", "200 OK: GET /", trace1)

Thread.sleep(200)

val trace2 = "4bf92f3577b34da6a3ce929d0e0e4736"
try {
    log("INFO", "Starting request", trace2)
    log("DEBUG", "Processing request", trace2)
    throw RuntimeException("Something went wrong!")
} catch (e: Exception) {
    log("ERROR", e.stackTraceToString(), trace2)
    logException(e.message ?: "", e, trace2)
    log("INFO", "500 Internal Server Error: GET /", trace2)
}