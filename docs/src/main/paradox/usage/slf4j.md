# SLF4J API

The default Logger API has the same logger methods and (roughly) the type signature as SLF4J.

The biggest difference is that methods take a type class instance of @scaladoc[Markers](com.tersesystems.blindsight.Markers) and @scaladoc[Arguments](com.tersesystems.blindsight.Arguments), if you have them defined.

## Markers

Markers work the same way, but must be an instance of @scaladoc[Markers](com.tersesystems.blindsight.Markers).

```scala
val marker = MarkerFactory.getDetachedMarker("foo")
logger.info(Markers(marker), "hello")
```

Using the @ref:[DSL](dsl.md) with marker enrichment is encouraged here, as it can make marker specification much easier:

```scala
import com.tersesystems.blindsight._
import com.tersesystems.blindsight.MarkersEnrichment._
logger.info(bobj("markerKey" -> "markerValue").asMarkers, "marker and argument")
```

Generally, you should not need to use markers explicitly in messages, as they can be used with @ref:[context](context.md) more effectively.

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

If you have more than two arguments, you will need to wrap them so they are provided as a single @scaladoc[Arguments](com.tersesystems.blindsight.Arguments) instance:

```scala
logger.info("arg {}, arg {}, arg {}", Arguments(1, "2", false))
```

comes out as:

```
FgEddUhGnXE6O0Qbm7EAAA 17:36:55.142 [INFO ] e.s.Slf4jMain$ -  arg 1, arg 2, arg false
```

Exceptions come after arguments, and are not included in the list.  For example:

```scala
val e = new Exception("something is horribly wrong")
logger.error("this is an error with argument {}", Arguments("a" -> "b"), e)
```

## Lazy Blocks

There is a block oriented API available.  This is useful for diagnostic logging and @ref:[conditional logging](conditional.md) to avoid unnecessary @ref:[memory allocation](../performance/memory.md).  The block is only executed if the conditions for logging have been met, and returns a handle to the method itself.

```scala
// block only executed if logger is DEBUG or TRACE level.
logger.debug { debug =>
  val debugInfo = ...
  debug(st"I am a debugging statement with lots of extra $debugInfo")
}
```

## Unchecked API

An unchecked API is available that does not use type class inference at all, and looks just like SLF4J with the additions of conditions and markers.

This is easier to use, but since `Any` cannot be type checked, it can output information you don't expect.  For example, it could output credit card information because calling `toString` exposes the case class data:

```scala
val logger = LoggerFactory.getLogger

val unchecked: SLF4JLogger[UncheckedSLF4JMethod] = logger.unchecked

// Uses Any, renders credit card as "toString"
val creditCard = CreditCard("4111111111111")

 // case class tostring renders CC number, which is unsafe!
unchecked.info("this is risky unchecked {}", creditCard)
```

Where there are several arguments, the @scaladoc[Arguments](com.tersesystems.blindsight.Arguments) must be used rather than `Seq` or `Array`, as it is very awkward to use `Seq` and `Array` with varadic input:

```scala
unchecked.info("this is risky unchecked {}, {}, {}", Arguments("1", 2, true))
```

In the unchecked API, you can set `-Dblindsight.anywarn=true` as a system property, and output will be written to `System.out.error` when calls to `Any` are made.