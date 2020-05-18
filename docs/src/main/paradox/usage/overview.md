# Overview

This usage page will give a quick overview of Blindsight.

## Logger Resolvers 

The simplest possible @scaladoc[Logger](com.tersesystems.blindsight.Logger) is created from a @scaladoc[LoggerFactory](com.tersesystems.blindsight.LoggerFactory$):

```scala
import com.tersesystems.blindsight.LoggerFactory
val logger = LoggerFactory.getLogger(getClass)
```

There is also a macro based version which finds the enclosing class name and hands it to you:

```scala
val loggerFromEnclosing = LoggerFactory.getLogger
```

The  @scaladoc[LoggerFactory](com.tersesystems.blindsight.LoggerFactory$) @scaladoc[LoggerResolver](com.tersesystems.blindsight.LoggerResolver) type class under the hood, which means you have the option of creating your own logger resolver.  This is useful when you want to get away from class based logging, and use a naming strategy based on a correlation id.  For example:

```scala
trait LoggerResolver[T] {
  def resolveLogger(instance: T): org.slf4j.Logger
}
```

means you can resolve a logger directly from a request:

```scala
implicit val requestToResolver: LoggerResolver[Request] = LoggerResolver { (instance: Request) => 
  org.slf4j.LoggerFactory.getLoggerFactory.getLogger("requests." + instance.requestId())
}
```

And from then on, you can do:

```scala
val myRequest: Request = ...
val logger = LoggerFactory.getLogger(myRequest)
```

See @ref:[Logger Resolvers](resolvers.md) for more details.

## SLF4J API

The default logger provides an SLF4J-like API:

```scala
logger.info("I am an SLF4J-like logger")
```

This converts to a @scaladoc[Message](com.tersesystems.blindsight.api.Message) class, and logs only if the level of the SLF4J logger is set to at least `INFO`.  Very roughly:

```scala
val infoMethod: InfoMethod = logger.info
infoMethod.apply(() => Message("I am an SLF4J-like logger"))

// rough implementation
class InfoMethod {
  def apply(msg: => Message) {
    if (slf4jLogger.isInfoEnabled()) {
       slf4jLogger.info(msg.toString)
    }
  }
}
```

First, let's explain @scaladoc[Message](com.tersesystems.blindsight.api.Message) and its compatriots.

A single logging statement in SLF4J consists of a set of parameters in combination:

```java
Marker marker = ...
String message = ...
Object[] arguments = ...
logger.info(marker, message, arguments);
```

All of these together make a logging statement.

Blindsight keeps the same concept these parameters, but creates specific types; @scaladoc[Markers](com.tersesystems.blindsight.api.Markers), @scaladoc[Message](com.tersesystems.blindsight.api.Message), and @scaladoc[Argument](com.tersesystems.blindsight.api.Argument), with a @scaladoc[Statement](com.tersesystems.blindsight.api.Statement) that encompasses all the above.

```scala


val markers: Markers = Markers(marker1, marker2)
val message: Message = Message("some message")
val argument1: Argument = Argument("arg1")

logger.info(markers, message, argument1);
```

Where possible, Blindsight provides type class mappings to automatically convert to the appropriate type.  So @scaladoc[Markers](com.tersesystems.blindsight.api.Markers) has a @scaladoc[ToMarkers](com.tersesystems.blindsight.api.ToMarkers) type class, @scaladoc[Message](com.tersesystems.blindsight.api.Message) has @scaladoc[ToMessage](com.tersesystems.blindsight.api.ToMessage), and @scaladoc[Argument](com.tersesystems.blindsight.api.Argument) has @scaladoc[ToArgument](com.tersesystems.blindsight.api.ToArgument).

There is an implicit conversion from `String` to Message:

```scala
logger.info("this is a message");
```

And the API takes `ToArgument` for automatic conversion:

```scala
logger.info("this is a message", "arg1", "arg2");
```

The @scaladoc[Arguments](com.tersesystems.blindsight.api.Arguments) class aggregates multiple arguments together when there are more than two arguments.  The `Arguments()` method takes a varadic list of arguments that can be hetrogeneous.

```scala
val arguments: Arguments = Arguments("arg1", 42, true)
logger.info(markers, message, arguments);
```

Exceptions must be at the end of the statement, and are not aggregated with arguments.  This is to encourage type safety and make it impossible to include an exception as an argument by accident.

```scala
logger.info("Message with arguments and exceptions", arguments, exception);
```

You can use type class instances to extend Blindsight's functionality.  For example, you can pass a feature flag into `isDebugEnabled` and it will convert it into a `Markers`:

```scala
object Slf4jMain {
  final case class FeatureFlag(flagName: String)
  object FeatureFlag {
    implicit val toMarkers: ToMarkers[FeatureFlag] = ToMarkers { instance =>
      Markers(MarkerFactory.getDetachedMarker(instance.flagName))
    }
  }

  def main(args: Array[String]): Unit = {
    val logger: Logger = LoggerFactory.getLogger(getClass)

    val featureFlag = FeatureFlag("flag.enabled")
    // this is not a marker, but is converted via type class.
    if (logger.isDebugEnabled(featureFlag)) {
      logger.debug("this is a test")
    }
  }
}
```

See the @ref:[SLF4J API](slf4j.md) page for more details.

## Fluent API

The @scaladoc[Logger](com.tersesystems.blindsight.Logger) instance provides access to a [fluent builder](https://www.martinfowler.com/bliki/FluentInterface.html) logger API.  A @scaladoc[FluentLogger](com.tersesystems.blindsight.fluent.FluentLogger) is accessible through `logger.fluent`:

```scala
import com.tersesystems.blindsight.fluent.FluentLogger

val fluentLogger: FluentLogger = logger.fluent
fluentLogger.info.message("I am a fluent logger").log()
```

See @ref:[Fluent API](fluent.md) for details.

## Semantic API

The @scaladoc[Logger](com.tersesystems.blindsight.Logger) instance provides access to a [semantic, strongly typed logging](https://github.com/microsoft/perfview/blob/master/documentation/TraceEvent/TraceEventProgrammersGuide.md) API.  A @scaladoc[SemanticLogger](com.tersesystems.blindsight.semantic.SemanticLogger) is accessible through `logger.semantic`:

```scala

import com.tersesystems.blindsight.semantic.SemanticLogger

val semanticLogger: SemanticLogger[UserEvent] = logger.semantic[UserEvent]
val userLoggedInEvent = UserLoggedInEvent(name = "steve")
semanticLogger.info(userEvent)
```

The semantic API takes a single strongly typed argument, and the logger is instantiated with the type that is acceptable.  This makes it very useful for event logging and [domain oriented observability](https://martinfowler.com/articles/domain-oriented-observability.html).

See @ref:[Semantic API](semantic.md) for details.

## Flow API

The @scaladoc[Logger](com.tersesystems.blindsight.Logger) instance provides access to a control flow based logging wrapper.  A @scaladoc[FlowLogger](com.tersesystems.blindsight.flow.FlowLogger) is accessible through `logger.flow`:

```scala
import com.tersesystems.blindsight.ToArgument

import com.tersesystems.blindsight.flow._

implicit def flowBehavior[B: ToArgument]: FlowBehavior[B] = ...

def flowMethod(arg1: Int, arg2: Int): Int = logger.flow.trace {
  arg1 + arg2
}
```

The flow API is used to render the entry and exit of a given method.  It is tied together with a flow behavior which provides the relevant @scaladoc[Statement](com.tersesystems.blindsight.api.Statement) on entry and exit.  This can also be used for timers and hierarchical tracing.

See @ref:[Flow API](flow.md) for more details.

## Structured Logging

Blindsight provides structured logging using type class mapping, which maps between tuples, arrays, and JSON nodes to [Markers and StructuredArguments](https://github.com/logstash/logstash-logback-encoder#event-specific-custom-fields), if you are using Logback as the SLF4J implementation.  This makes structured logging easy and intuitive.

Type class mapping is available through the `blindsight-logstash` library using @ref:[logback](../setup/logback.md), and must be brought into lexical scope using the @scaladoc[Implicits](com.tersesystems.blindsight.logstash.Implicits$) singleton object:

```scala
import com.tersesystems.blindsight.logstash.Implicits._
```

This provides a number of type class mappings like:

```scala
implicit val tupleStringToMarkers: ToMarkers[(String, String)] = ToMarkers {
  case (k, v) => Markers(LogstashMarkers.append(k, v))
}
```

When you write out a logstash marker using a tuple in the position of a marker, it will compile:

```scala
logger.info("a" -> "b", "some message")
```

And when you are using the [`LogstashEncoder`](https://www.javadoc.io/doc/net.logstash.logback/logstash-logback-encoder/latest/net/logstash/logback/encoder/LogstashEncoder.html) that writes out structured logs as JSON, you'll get the following:

```json
{
  "@timestamp": "2020-04-05T23:09:08.436Z",
  "@version": "1",
  "message": "some message",
  "logger_name": "example.Main$",
  "a": "b"
}
```

See @ref:[Structured Logging](structured.md) for more details.

## Conditional Logging

No matter how fast your logging is, it's always faster not to log a statement at all.  Blindsight does its best to allow the system to **not** log as much as it makes it possible to log, allowing you to dynamically manage the CPU and memory pressure demands of logging.

Most of the loggers have an `onCondition` method that returns a conditional logger of the same type.
 
This logger will only log if the condition is true:

```scala
def booleanCondition: Boolean = ...
val conditionalLogger = logger.onCondition(booleanCondition)
conditionalLogger.info("Only logs when condition is true")
```

By contrast, `when` is used on methods, rather than the logger, and provides a block that is executed only when the condition is true:

```scala
logger.info.when(booleanCondition) { info =>
  info("log")
}
```

See @ref:[Conditional Logging](conditional.md) for more details.

## Contextual Logging

Blindsight builds up context with markers.  You can use `withMarkers` to add markers that don't have to explicitly added to a statement.

```scala
import net.logstash.logback.marker.{Markers => LogstashMarkers}
val loggerWithMarkers = logger.withMarkers(LogstashMarkers.append("user", "will"))
```

See @ref:[Context](context.md) for more details.

## Source Code

SLF4J can give access to the line and file of source code, but this is done at runtime and is very expensive.  Blindsight provides this information for free, at compile time, through [sourcecode](https://github.com/lihaoyi/sourcecode) macros, using the @scaladoc[SourceInfoMixin](com.tersesystems.blindsight.api.mixins.SourceInfoMixin) on the logger.

When using `blindsight-generic`, this returns `Markers.empty`, but when using `blindsight-logstash`, this adds `source.line`, `source.file` and `source.enclosing` to the JSON logs automatically:

```json
{
  "@timestamp": "2020-04-12T17:58:45.410Z",
  "@version": "1",
  "message": "this is a test",
  "logger_name": "example.slf4j.Slf4jMain$",
  "thread_name": "run-main-0",
  "level": "DEBUG",
  "level_value": 10000,
  "source.line": 39,
  "source.file": "/home/wsargent/work/blindsight/example/src/main/scala/example/slf4j/Slf4jMain.scala",
  "source.enclosing": "example.slf4j.Slf4jMain.main"
}
```

This is the default behavior, and you can override `sourceInfoMarker` in your own implementation to return whatever you like.

See @ref:[Source Code](sourcecode.md) for more details.
