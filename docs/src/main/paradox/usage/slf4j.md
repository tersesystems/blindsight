# SLF4J API

The default Logger API has the same logger methods and (roughly) the type signature as SLF4J.

The biggest difference is that methods take a type class instance of @scaladoc[Markers](com.tersesystems.blindsight.api.Markers) and @scaladoc[Arguments](com.tersesystems.blindsight.api.Arguments), if you have them defined.

## Markers

You can pass in something that is not a marker, and provided you have a @scaladoc[ToMarkers](com.tersesystems.blindsight.api.ToMarkers) in implicit scope, you can get it auto-converted.

There is an automatic implicit conversion from `org.slf4j.Marker` to @scaladoc[Markers](com.tersesystems.blindsight.api.Markers):

```scala
val marker: org.slf4j.Marker = MarkerFactory.getDetachedMarker("SOME_MARKER")
logger.info(markers, "message with marker")
```

You can also combine markers:

```scala
val markersOnePlusTwo: Markers = markers1 + markers2
```

@@@ note

You should **not** try to manipulate the marker through `marker.add` or `marker.remove`.  It's better to treat a `org.slf4j.Marker` instance as completely immutable, and manage any composition through @scaladoc[Markers](com.tersesystems.blindsight.api.Markers).

@@@

Getting at the underlying marker is a `lazy val` that is computed once:

```scala
val marker: org.slf4j.Marker = markersOnePlusTwo.marker
```

You can also convert your own objects into appropriate markers.

```scala
import com.tersesystems.blindsight._
import com.tersesystems.blindsight.api.{Argument, Markers, ToMarkers}
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

import com.tersesystems.blindsight.logstash.Implicits._
logger.info("markerKey" -> "markerValue", "marker and argument")
```

## Arguments 

Arguments in Blindsight occupy one variable.  In constrast to the SLF4J API, which takes an `Any`.  There **must** be a type class instance of @scaladoc[ToArguments](com.tersesystems.blindsight.api.ToArguments) in scope.  This is to prevent awkward `toString` matches on object instances, and ensure that structured logging is taking place. 

```scala
// Will not compile, because no ToArguments[SomeRandomObject] is found in implicit scope!
logger.info("one argument {}", new SomeRandomObject()) 
```

Default @scaladoc[ToArguments](com.tersesystems.blindsight.api.ToArguments) are determined for the primitives (`String`, `Int`, etc):

```scala
logger.info("one argument {}", 42) // works, because default
```

If you have several arguments, you will need to wrap them so they are provided as a single @scaladoc[Arguments](com.tersesystems.blindsight.api.Arguments) instance:

```scala
logger.info("arg {}, arg {}, arg {}", Arguments(1, "2", false))
```

comes out as:

```
FgEddUhGnXE6O0Qbm7EAAA 17:36:55.142 [INFO ] e.s.Slf4jMain$ -  arg 1, arg 2, arg false
```

Logstash `StructuredArguments` are covered if you import the `blindsight-logstash` library.  Using Logstash markers means that key value pairs can be created from tuples. 

```scala
import com.tersesystems.blindsight.logstash.Implicits._

logger.info("marker and argument {}", "argumentKey" -> "argumentValue")

// Maps are autoconverted
logger.info(
  "first is [{}], second is [{}]",
  Map("a" -> "b", "c" -> "d")
)

// Otherwise, wrapping in Arguments() is the way to go
logger.info(
  "first is [{}], second is [{}]",
  Arguments("a" -> "b", "c" -> "d")
)
```

Not everything is defined as an implicit out of the box, but it's easy to define defaults.  For example, for dates you may want to define some defaults:

```scala
import java.time.format.DateTimeFormatter

implicit val dateToArgument: ToArguments[Date] = ToArguments[java.util.Date] { date =>
  new Arguments(Seq(DateTimeFormatter.ISO_INSTANT.format(date.toInstant)))
}

implicit val instantToArgument: ToArguments[java.time.Instant] = ToArguments[java.time.Instant] { instant =>
  new Arguments(Seq(DateTimeFormatter.ISO_INSTANT.format(instant)))
}

logger.info("date is {}", new java.util.Date())
logger.info("instant is {}", Instant.now())
```

Exceptions must be the last argument in the @scaladoc[Arguments](com.tersesystems.blindsight.api.Arguments).  For example:

```scala
val e = new Exception("something is horribly wrong")
logger.error("this is an error with argument {}", Arguments("a" -> "b", e))
```

Note that @scaladoc[ToArguments](com.tersesystems.blindsight.api.ToArguments) is a contravariant type class.  This means that the *lowest* type class match will take priority.  This is useful when it comes time to passing things like exceptions:

```scala
val exception: IllegalStateException = new IllegalStateException("Cannot divide by zero")
logger.error("Some error", exception) // does not compile under invariant type class
```

but this does lead to some [unintuitive behavior](https://groups.google.com/forum/#!topic/scala-language/ZE83TvSWpT4) in type class resolving that [even the Scala team question](https://www.scala-lang.org/old/node/4626).

Getting at the underlying array can be done with `asArray`:

```scala
val argArray: Array[_] = arguments.asArray
```

But this is really for the underlying SLF4J logger and shouldn't need to be touched directly.

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

In the unchecked API, you can set `-Dblindsight.anywarn=true` as a system property, and output will be written to `System.out.error` when calls to `Any` are made.