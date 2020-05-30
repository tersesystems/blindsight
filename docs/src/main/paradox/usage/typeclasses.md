# Type Classes

The important types in Blindsight are @scaladoc[Markers](com.tersesystems.blindsight.Markers), @scaladoc[Argument](com.tersesystems.blindsight.Argument), and  @scaladoc[Message](com.tersesystems.blindsight.Message).

 Where possible, the APIs map automatically, using the @scaladoc[ToMarkers](com.tersesystems.blindsight.ToMarkers), @scaladoc[ToMessage](com.tersesystems.blindsight.ToMessage) and @scaladoc[ToArgument](com.tersesystems.blindsight.ToArgument) type classes, respectively.

## General Principles

Type classes let you represent your domain objects as structured logging data. 

Although Blindsight does provide mappings of the basic primitive types, you may want to provide some more semantic detail about what the value represents, and use the DSL with a specific field name and type -- for example, rather than representing an age as an integer, `logger.info("person age = {}", persion.age)` is easier if you use a specific class `Age` and have a type class instance that represents that `Age` as `bobj("age_year" -> ageInYear)`

You will want to be consistent and organized about how you represent your field names, and you will typically want to include a representation of the unit used a scalar quantity, particularly time-based fields.  [Honeycomb suggests](https://www.honeycomb.io/blog/event-foo-building-better-events/) a suffix with unit quantity -- `_ms`, `_sec`, `_ns`, `_µs`, etc.

This also follows for specific points in time.  If you represent an instant as a time since epoch, use `_tse` along with the unit, i.e. milliseconds since epoch is `created_tse_ms`:
  
@@snip [TimeExample.scala](../../../test/scala/example/dsl/TimeExample.scala) { #created_tse_ms }

If you represent an instant in [RFC 3339](https://tools.ietf.org/html/rfc3339#section-5.7) / ISO 8601 format (ideally in UTC), use "_ts", i.e. `created_ts`:

@@snip [TimeExample.scala](../../../test/scala/example/dsl/TimeExample.scala) { #created_ts }

If you are representing a duration, then specify `_dur` and the unit, i.e. a backoff duration between retries may be `backoff_dur_ms=150`.  

@@snip [TimeExample.scala](../../../test/scala/example/dsl/TimeExample.scala) { #backoff_dur_ms }

If you are using Java durations, then use `dur_iso` and the ISO-8601 duration format `PnDTnHnMn.nS`, i.e. the duration of someone's bike ride may be `ride_dur_iso="PT2H15M"` 

@@snip [TimeExample.scala](../../../test/scala/example/dsl/TimeExample.scala) { #ride_dur_iso }

This is because both JSON and logfmt do not come with any understanding of dates themselves, and logs are not always kept under tight control under a schema.  Keeping the units explicit lets the logs be self-documenting.

You may find it helpful to use [Refined](https://github.com/fthomas/refined) and [Coulomb](https://github.com/erikerlandson/coulomb#documentation) to provide type-safe validation and unit representation of data to the [DSL](usage/dsl.md).

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

## Argument and Arguments

Arguments must be convertable to @scaladoc[Argument](com.tersesystems.blindsight.Argument).  This is usually done with type class instances.

Default @scaladoc[ToArgument](com.tersesystems.blindsight.ToArgument) are determined for the primitives (`String`, `Int`, etc):

```scala
logger.info("one argument {}", 42) // works, because default
```

You can define your own argument type class instances:

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

Although it's usually better to use the @ref:[DSL](dsl.md) and map to a @scaladoc[BObject](com.tersesystems.blindsight.AST.BObject):

```scala
implicit val instantToArgument: ToArgument[java.time.Instant] = ToArgument[java.time.Instant] { instant =>
  Argument(bobj("instant_utc" -> DateTimeFormatter.ISO_INSTANT.format(instant)))
}
```

There is a plural of @scaladoc[Argument](com.tersesystems.blindsight.Argument), @scaladoc[Arguments](com.tersesystems.blindsight.Arguments) that is used in place of varadic arguments.  If you have more than two arguments, you will need to wrap them so they are provided as a single @scaladoc[Arguments](com.tersesystems.blindsight.Arguments) instance:

```scala
logger.info("arg {}, arg {}, arg {}", Arguments(1, "2", false))
```

## Message

The @scaladoc[Message](com.tersesystems.blindsight.Message) is usually a String, but it doesn't have to be.  Using the @scaladoc[ToMessage](com.tersesystems.blindsight.ToMessage) type class, you can convert any class into a message.  This is probably most appropriate with string-like classes like `CharSequence` and `akka.util.ByteString`.

```scala
implicit val toMessage: ToMessage[CharSequence] = ToMessage[CharSequence] { charSeq =>
  new Message(charSeq.toString)
}
val charSeq: CharSequence = new ArrayCharSequence(Array('1', '2', '3'))
logger.fluent.info.message(charSeq).log()
```

## Custom JSON Mappings

You can pass through JSON directly if you already have it and recreating it through the DSL would be a waste.

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