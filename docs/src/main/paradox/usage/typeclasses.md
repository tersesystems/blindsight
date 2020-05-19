# Type Classes



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

## Arguments

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


## Custom JSON Mappings

For example, if you are working with json4s or play-json, you can convert to Jackson JsonNode using a type class:

```scala
import com.fasterxml.jackson.databind.JsonNode

trait ToJsonNode[T] {
  def jsonNode(instance: T): JsonNode
}

object ToJsonNode {
  import org.json4s._

  implicit val json4sToJsonNode: ToJsonNode[JValue] = new ToJsonNode[JValue] {
    import org.json4s.jackson.JsonMethods._
    override def jsonNode(instance: JValue): JsonNode = asJsonNode(instance)
  }
}
```

And then set up your own type mappings as follows:

```scala
implicit def jsonToArgument[T: ToJsonNode]: ToArgument[(String, T)] = ToArgument {
  case (k, instance) =>
    val node = implicitly[ToJsonNode[T]].jsonNode(instance)
    Argument(StructuredArguments.keyValue(k, node)) // or raw(k, node.toPrettyString)
}

import org.json4s._
import org.json4s.jackson.JsonMethods._

logger.info("This message has json {}", parse(""" { "numbers" : [1, 2, 3, 4] } """))
```