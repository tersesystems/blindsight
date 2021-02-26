# Event Buffers

Blindsight can buffer logged events before they are sent to SLF4J.  An @scaladoc[Event](com.tersesystems.blindsight.EventBuffer.Event) contain a timestamp, the logging level, the logger name, and the @scaladoc[Entry](com.tersesystems.blindsight.Entry) that is about to be logged.  Internally, event buffers are implemented as a special case of @ref:[entry transformation](transform.md).

@@@ note

Because the event is generated just before being sent to SLF4J, the timestamp of the event and the logging timestamp may differ, typically by milliseconds.

@@@

## Usage 

Blindsight does not mandate the use of an event buffer, and so you must provide an implementation to your application.  The easiest way to do this is to add the @ref:[ringbuffer implementation](../setup/index.md) as a library dependency, and then use the factory constructor which will call the implementation through a service loader pattern:

```scala
import com.tersesystems.blindsight._

val eventBuffer = EventBuffer(50) // buffers 50 elements
```

You can then add the buffer to the logger with `withEventBuffer`:

```text
val logger = LoggerFactory.getLogger.withEventBuffer(eventBuffer)
```

Or you can target an individual level for buffering:

```text
import org.slf4j.event.Level
val logger = LoggerFactory.getLogger.withEventBuffer(Level.WARN, eventBuffer)
```

The logger will append events to the buffer when they pass the criteria for logging:

```scala
logger.warn("I am a warning message")
val e = eventBuffer.take(1)
println(s"${e.timestamp}: ${e.level} - ${e.entry.message}")
```

Adding events to the ring buffer will mean that entries will live longer than usual (adding memory allocations and CPU), and will be accessible inside the application, which may have security implications.  

You can clear the buffer at any time by calling `clear`:

```scala
eventBuffer.clear()
```

## Patterns

Because event buffers are a new feature, the question of how to use buffers effectively is still open.  Some examples of buffer patterns are provided below.  

Brian Marick's [Using Ring Buffer Logging to Help Find Bugs](http://www.exampler.com/writing/ring-buffer.pdf) is also gem of good practices, and worth reading.

### Buffers in Testing

Using buffers for testing is relatively straightforward.  You can add an event buffer, and verify that the markers, messages, and arguments translate appropriately to SLF4J.

```scala
"log something and see it in buffer" in {
  val queueBuffer = EventBuffer(1)
  val logger      = createLogger.withEventBuffer(queueBuffer)

  logger.info("Hello world")

  val el = queueBuffer.head
  el.entry.marker must be(None)
  el.entry.message must be("Hello world")
  el.entry.args must be(empty)
}
```

### Buffers in Debugging

Using buffers in debugging can be useful to provide more context in a running program, to provide detailed history on demand and show a record.  It can be particularly helpful in the context of a debugger, because you can see the history of the events in the array -- essentially using the buffers as logs, but without having to context switch.

One problem in debugging is that there can be an overwhelming amount of information, and hunting down exactly when and where something went wrong can be an issue.  One way to handle this to create several buffers so you can filter out fine details.

```scala
val logger = LoggerFactory.getLogger
    .withEventBuffer(Level.DEBUG, debugBuffer)
    .withEventBuffer(Level.TRACE, traceBuffer)

logger.debug { log =>
  log("general info")
}

logger.trace { log => 
  log("verbose info")
}
```

You can also dump and search through buffers while debugging for a richer development experience:

```scala
val bufferList = buffer.take(buffer.size)
bufferList.find(_.entry.message.startsWith("IMPORTANT")).foreach { e =>
  println(s"Found important event $e")
}
```

### Buffers in Production

Buffers can be enabled in production, but be specifically [targeted](https://tersesystems.com/blog/2019/07/22/targeted-diagnostic-logging-in-production/) using
@ref[conditional logging](conditional.md)so that debugging and tracing information is only buffered by request. 

@@@ note

You may also need to enable the logger level as `TRACE` to ensure that logging passes the built-in predicates.  If you want `DEBUG` and `TRACE` statements to be logged **only** to the buffer, you should use [threshold filters](http://logback.qos.ch/manual/filters.html#thresholdFilter) on the appenders to suppress any output.

@@@
 
```scala
// enable buffer logging based off feature flags...
private def bufferEnabled(markers: Markers) = ...

val logCondition = Condition { (level: Level, markers: Markers) =>
  level.toInt.compareTo(Level.DEBUG.toInt) > 0 || bufferEnabled(markers)
}

val logger = baseLogger
    .withMarkers(operationMarkers)
    .withCondition(logCondition)
    .withEventBuffer(Level.DEBUG, buffer)
    .withEventBuffer(Level.TRACE, buffer)

logger.debug { log =>
  log("logging debug information that will go to buffer")
}
```

You can then set up the events as [breadcrumbs](https://github.com/tersesystems/terse-logback-showcase/blob/master/app/handlers/SentryHandler.java) to your error handling system:

```scala
try {
  // throw exception
} catch {
  case e: Exception =>
    val bufferList = buffer.take(buffer.size)
    val breadcrumbs = buildBreadcrumbs(bufferList)
    val errorEvent = buildEvent(usefulException, breadcrumbs);
    sentryClient.sendEvent(errorEvent);
}
```
