# Flow API

The flow API is used when logging surrounds an execution flow.  This is a good fit when you want tracing or [domain-oriented observability](https://martinfowler.com/articles/domain-oriented-observability.html).
 
You can access the flow logger from Blindsight using `logger.flow`:

```scala
import com.tersesystems.blindsight._

val logger = LoggerFactory.getLogger
val flowLogger = logger.flow
```

## Usage

The flow logger takes a block of execution, and returns the result transparently, according to the log level.  

@@snip [Flow.scala](../../../test/scala/flow/SimpleFlow.scala) { #flow_method }

The result should have a type class instance of @scaladoc[ToArguments](com.tersesystems.blindsight.api.ToArguments), so that it can be considered in logging.  For example, if the return type is `Person`, then you must have a `ToArguments[Person]` in scope:

@@snip [Flow.scala](../../../test/scala/flow/SimpleFlow.scala) { #flow_person_definition }

If logging is enabled, then the execution is wrapped to capture the result or execution, and then the result is returned or execution rethrown.  If the logging level is not enabled or logging execution is denied by a filter, then execution of the block still proceeds but is not wrapped by a `Try` block.

## Flow Behavior

What happens in a flow is determined by the @scaladoc[FlowBehavior](com.tersesystems.blindsight.flow.FlowBehavior).  Implementing a flow behavior comes down to creating @scaladoc[Markers](com.tersesystems.blindsight.api.Markers) and @scaladoc[Statement](com.tersesystems.blindsight.api.Statement) for entry, exit, and exceptions.

There are two out of the box behaviors provided: @scaladoc[SimpleFlowBehavior](com.tersesystems.blindsight.flow.SimpleFlowBehavior) and @scaladoc[XLoggerFlowBehavior](com.tersesystems.blindsight.flow.XLoggerFlowBehavior).  These are modelled after [pos](https://github.com/JohnReedLOL/pos) and [XLogger](http://www.slf4j.org/extensions.html#extended_logger), respectively.

## Integrating with Tracing

If you integrate with [logback-tracing](https://tersesystems.github.io/terse-logback/guide/tracing/), then you can also log to Honeycomb using a @scaladoc[FlowBehavior](com.tersesystems.blindsight.flow.FlowBehavior).  To do this, add the following resolver:

```
resolvers += Resolver.bintrayRepo("tersesystems", "maven")
```

and the following dependencies:

@@dependency[sbt,Maven,Gradle] {
  group="com.tersesystems.logback"
  artifact="logback-tracing"
  version="latest.version"
}

@@dependency[sbt,Maven,Gradle] {
  group="com.tersesystems.logback"
  artifact="logback-uniqueid-appender"
  version="latest.version"
}

And you can produce Honeycomb manual traces with the following:

@@snip [HoneycombFlow.scala](../../../test/scala/flow/HoneycombFlow.scala) { #honeycomb_flow_example }

See [play-blindsight](http://github.com/wsargent/play-blindsight) for a worked example.