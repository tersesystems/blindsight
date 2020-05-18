# SLF4J API

The default Logger API has the same logger methods and (roughly) the type signature as SLF4J.

The biggest difference is that methods take a type class instance of @scaladoc[Markers](com.tersesystems.blindsight.Markers) and @scaladoc[Arguments](com.tersesystems.blindsight.Arguments), if you have them defined.

## Markers

You can pass in something that is not a marker, and provided you have a @scaladoc[ToMarkers](com.tersesystems.blindsight.ToMarkers) in implicit scope, you can get it auto-converted through type annotation.  The various logging statement will only take a `Markers`.

```scala
val marker: Markers = MarkerFactory.getDetachedMarker("SOME_MARKER")
logger.info(markers, "message with marker")
```

You can also combine markers:

```scala
val markersOnePlusTwo: Markers = markers1 + markers2
```

@@@ note

You should **not** try to manipulate the marker through `marker.add` or `marker.remove`.  It's better to treat a `org.slf4j.Marker` instance as completely immutable, and manage any composition through @scaladoc[Markers](com.tersesystems.blindsight.Markers).

@@@

Getting at the underlying marker is a `lazy val` that is computed once:

```scala
val marker: org.slf4j.Marker = markersOnePlusTwo.marker
```

You can also convert your own objects into appropriate markers.

```scala
import com.tersesystems.blindsight._

import com.tersesystems.blindsight.slf4j._
import org.slf4j.MarkerFactory

object Slf4jMain {
  final case class FeatureFlag(flagName: String)

  object FeatureFlag {
    implicit val toMarkers: ToMarkers[FeatureFlag] = ToMarkers { instance =>
      Markers(MarkerFactory.getDetachedMarker(instance.flagName))
    }
  }

  def main(args: Array[String]): Unit = {
    val logger = LoggerFactory.getLogger(getClass)

    val featureFlag = FeatureFlag("flag.enabled")
    if (logger.isDebugEnabled(featureFlag)) {
      logger.debug("this is a test")
    }
  }
}
```

@ref:[Logstash Markers](structured.md) are encouraged here, as they can make marker specification much easier:

```scala
import com.tersesystems.blindsight._
import com.tersesystems.blindsight.MarkersEnrichment._
logger.info(bobj("markerKey" -> "markerValue").asMarkers, "marker and argument")
```

Generally, you should not need to use markers explicitly in messages, as they can be used with @ref:[context](context.md) more effectively.

## Arguments 

Arguments in Blindsight are type checked, in constrast to the SLF4J API, which takes an `Any`.  There **must** be a type class instance of @scaladoc[ToArguments](com.tersesystems.blindsight.ToArguments) in scope.  This is to prevent awkward `toString` matches on object instances, and ensure that structured logging is taking place. 

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

Not everything is defined as an implicit out of the box, but it's easy to define defaults.  For example, for dates you may want to define some defaults:

```scala
import java.time.format.DateTimeFormatter

implicit val dateToArgument: ToArgument[Date] = ToArgument[java.util.Date] { date =>
  new Argument(DateTimeFormatter.ISO_INSTANT.format(date.toInstant))
}

implicit val instantToArgument: ToArgument[java.time.Instant] = ToArgument[java.time.Instant] { instant =>
  new Argument(DateTimeFormatter.ISO_INSTANT.format(instant))
}

logger.info("date is {}", new java.util.Date())
logger.info("instant is {}", Instant.now())
```

Exceptions come after arguments, and are not included in the list.  For example:

```scala
val e = new Exception("something is horribly wrong")
logger.error("this is an error with argument {}", Arguments("a" -> "b"), e)
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