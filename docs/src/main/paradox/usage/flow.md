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

@@@ note

Because this logger executes blocks of computation and may optionally decorate it with logging, it does <b>not</b> implement the @scaladoc[OnConditionMixin](com.tersesystems.blindsight.api.mixins.OnConditionMixin) and should not be used with conditional logging logic.  If conditional logging is required, it is generally safer to do it in the logging framework by using a deny filter with a marker defined in the FlowBehavior.

@@@

## Flow Behavior

Implementing a flow behavior comes down to creating @scaladoc[Markers](com.tersesystems.blindsight.api.Markers) and @scaladoc[Statement](com.tersesystems.blindsight.api.Statement) for entry, exit, and exceptions.

@@snip [Flow.scala](../../../test/scala/flow/SimpleFlowBehavior.scala) { #flow_behavior }

From there, using the flow API is a matter of plugging in the relevant flow behavior.
 
@@snip [Flow.scala](../../../test/scala/flow/SimpleFlow.scala) { #flow_example }

This produces the following output:

```text
FgcMMra7u2U6O0Qbm7EAAA 17:00:47.067 [INFO ] e.flow.FlowMain -  About to execute number flow
FgcMMra7u8QdHaINzdiAAA 17:00:47.166 [TRACE] e.flow.FlowMain -  example.flow.FlowMain.flowMethod entry source.arguments={arg1=1, arg2=2}
FgcMMra7u9CdHaINzdiAAA 17:00:47.191 [TRACE] e.flow.FlowMain -  example.flow.FlowMain.flowMethod exit with result 3
This is 3
```

## Integrating with Tracing

If you integrate with [logback-tracing](https://tersesystems.github.io/terse-logback/guide/tracing/), then you can also log to Honeycomb using a `FlowBehavior`.  To do this, add the following resolver:

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