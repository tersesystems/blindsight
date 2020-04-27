# Structured Logging

Structured logging is the basis for logging rich events.  `logstash-logback-encoder` has [Markers and StructuredArguments](https://github.com/logstash/logstash-logback-encoder#event-specific-custom-fields) which will write out markers and arguments to a [JSON encoder](https://github.com/logstash/logstash-logback-encoder#encoders--layouts) while still enabling line oriented output to console and file appenders.

@@@ note

See [Terse Logback](https://tersesystems.github.io/terse-logback/) and the [Terse Logback Showcase](https://github.com/tersesystems/terse-logback-showcase) for examples of how to configure logstash-logback-encoder for JSON.

@@@

You can set up structured logging using Logstash Markers and StructuredArguments, but you don't have to.   Blindsight comes with bindings of @scaladoc[ToMarkers](com.tersesystems.blindsight.api.ToMarkers) and @scaladoc[ToArguments](com.tersesystems.blindsight.api.ToArguments) to [Logstash Markers and StructuredArguments](https://github.com/logstash/logstash-logback-encoder#event-specific-custom-fields).  This lets you be far more expressive in constructing markers and arguments, and lets you pack far more information in.

For example, Logstash Markers has the concept of a "key=value" pair that gets written out to JSON using `append`.  We can create a type class to represent that as a tuple:

```scala
trait LowPriorityMarkers {
  implicit val tupleStringToMarkers: ToMarkers[(String, String)] = ToMarkers {
    case (k, v) =>
      Markers(LogstashMarkers.append(k, v))
  }
}
object Implicits extends LowPriorityMarkers
```

And then we can do the following in the fluent API:

```scala
import com.tersesystems.blindsight.logstash.Implicits._
logger.info("userId" -> userId, "Logging with user id added as a marker!")
```

From there, you can build up structured logging statements and rely on the type class mappings, for example:

```scala
import com.tersesystems.blindsight.logstash.Implicits._

val tupleStringMarker = Markers("string" -> "steve")
val tupleNumberMarker = Markers("number" -> 42)
val tupleBooleanNumber = Marker("boolean" -> true)
val arrayMarker = Markers("array" -> Seq("one", "two", "three"))

val tupleStringArguments = Arguments("arg1" -> "value1")
val tupleNumberArguments = Arguments("numericArg" -> 42)
val tupleBooleanArguments = Arguments("booleanArg" -> false)
val mapArguments = Arguments(Map("a" -> "b"))
val arrayArguments = Arguments("sequenceArg" -> Seq("a", "b", "c"))
```

You can also extend arguments and maarkers using your own type class instances.  This is especially useful when you already have structured logging, and want to pass it through without changes, since logstash-logback-encoder supports JsonNode transparently.

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
implicit def jsonToArguments[T: ToJsonNode]: ToArguments[(String, T)] = ToArguments {
  case (k, instance) =>
    val node = implicitly[ToJsonNode[T]].jsonNode(instance)
    Arguments(StructuredArguments.keyValue(k, node)) // or raw(k, node.toPrettyString)
}

import org.json4s._
import org.json4s.jackson.JsonMethods._

val arguments = Arguments("markerJson" -> parse(""" { "numbers" : [1, 2, 3, 4] } """))
logger.info("This message has json {}", arguments)
```