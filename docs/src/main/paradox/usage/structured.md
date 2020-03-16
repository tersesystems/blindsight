# Structured Logging

Blindsight provides a `logstash` type class mapping which maps between tuples, arrays, and JSON nodes to [Markers and StructuredArguments](https://github.com/logstash/logstash-logback-encoder#event-specific-custom-fields).

```scala
trait ToMarkers[T] {
  def toMarkers(instance: T): Markers
}
trait ToMessage[T] {
  def toMessage(instance: => T): Message
}
trait ToArguments[T] {
  def toArguments(instance: => T): Arguments
}
trait ToStatement[T] {
  def toStatement(instance: => T): Statement
}
```

You can set these up for yourself, but you don't have to.   Blindsight comes with bindings of `ToMarkers` and `ToArguments` to [Logstash Markers and StructuredArguments](https://github.com/logstash/logstash-logback-encoder#event-specific-custom-fields).  This lets you be far more expressive in constructing markers and arguments, and lets you pack far more information in.

For example, `LogstashMarkers` has the concept of a "key=value" pair that gets written out to JSON.  We can create a type class to represent that as a tuple:

```scala
trait LowPriorityMarkers {
  implicit val tupleStringToMarkers: ToMarkers[(String, String)] = ToMarkers {
    case (k, v) =>
      Markers(LogstashMarkers.append(k, v))
  }
}
object LowPriorityMarkers extends LowPriorityMarkers
```

And then we can do the following in the fluent API:

```scala
import LowPriorityMarkers._
logger.info.markers("userId" -> userId).log()
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