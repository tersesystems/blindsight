# SLF4J API

The default Logger API has the same logger methods and (roughly) the type signature as SLF4J.

The biggest difference is that methods take a type class instance of @scaladoc[Markers](com.tersesystems.blindsight.Markers) and @scaladoc[Arguments](com.tersesystems.blindsight.Arguments), if you have them defined.

## Arguments

Arguments in Blindsight are type checked, in constrast to the SLF4J API, which takes an `Any`.  There **must** be a type class instance of @scaladoc[ToArgument](com.tersesystems.blindsight.ToArgument) in scope.  This is to prevent awkward `toString` matches on object instances, and ensure that structured logging is taking place. 

```scala
// Will not compile, because no ToArgument[SomeRandomObject] is found in implicit scope!
logger.info("one argument {}", new SomeRandomObject()) 
```

Default @scaladoc[ToArgument](com.tersesystems.blindsight.ToArgument) are determined for the primitives (`String`, `Int`, etc):

```scala
logger.info("one argument {}", 42) // works, because default
```

If you have more than twenty arguments, you will need to wrap them so they are provided as a single @scaladoc[Arguments](com.tersesystems.blindsight.Arguments) instance:

```scala
val arguments: Arguments = ...
logger.info(template, arguments)
```

Like SLF4J, an exception must be at the end of the parameter list to be included with the stacktrace.  For example:

```scala
val e = new Exception("something is horribly wrong")
logger.error("this is an error with argument {}", ("a" -> "b"), e)
```

## Markers

Markers work the same way, but must be an instance of @scaladoc[Markers](com.tersesystems.blindsight.Markers).

```scala
val marker = MarkerFactory.getDetachedMarker("foo")
logger.info(Markers(marker), "hello")
```

Using the @ref:[DSL](dsl.md) with marker enrichment is encouraged here, as it can make marker specification much easier:

```scala
import com.tersesystems.DSL._
import com.tersesystems.blindsight._
import com.tersesystems.blindsight.MarkersEnrichment._
val marker = bobj("markerKey" -> "markerValue").asMarkers
logger.info(marker, "marker")
```

Generally, you should not need to use markers explicitly in messages, as they can be used with @ref:[context](context.md) more effectively.

## Lazy Blocks

There is a block oriented API available.  This is useful for diagnostic logging and @ref:[conditional logging](conditional.md) to avoid unnecessary @ref:[memory allocation](../performance/memory.md).  The block is only executed if the conditions for logging have been met, and returns a handle to the method itself.

```scala
// block only executed if logger is DEBUG or TRACE level.
logger.debug { debug =>
  val debugInfo = ...
  debug(st"I am a debugging statement with lots of extra $debugInfo")
}
```
