# Type Classes

The important types in Blindsight are @scaladoc[Markers](com.tersesystems.blindsight.Markers), @scaladoc[Argument](com.tersesystems.blindsight.Argument), and  @scaladoc[Message](com.tersesystems.blindsight.Message).

 Where possible, the APIs map automatically, using the @scaladoc[ToMarkers](com.tersesystems.blindsight.ToMarkers), @scaladoc[ToMessage](com.tersesystems.blindsight.ToMessage) and @scaladoc[ToArgument](com.tersesystems.blindsight.ToArgument) type classes, respectively.

## Argument and Arguments

Arguments must be convertible to @scaladoc[Argument](com.tersesystems.blindsight.Argument).  This is usually done with type class instances.

Default @scaladoc[ToArgument](com.tersesystems.blindsight.ToArgument) are determined for the primitives (`String`, `Int`, etc):

@@snip [TypeClassExample.scala](../../../test/scala/example/typeclasses/TypeClassExample.scala) { #argument-int }

You can define your own argument type class instances:

@@snip [TypeClassExample.scala](../../../test/scala/example/typeclasses/TypeClassExample.scala) { #argument-chronounit }

Although it's usually better to use the @ref:[DSL](dsl.md) and map to a @scaladoc[BObject](com.tersesystems.blindsight.AST.BObject), which is an "object" value:

@@snip [TypeClassExample.scala](../../../test/scala/example/typeclasses/TypeClassExample.scala) { #argument-person }

There is a plural of @scaladoc[Argument](com.tersesystems.blindsight.Argument), @scaladoc[Arguments](com.tersesystems.blindsight.Arguments) that is used when you have large numbers of Arguments.

```scala
val arguments: Arguments = Arguments.fromSeq(veryLargeList)
logger.info(template, arguments)
```

## Markers

You can pass in something that is not a marker, and provided you have a @scaladoc[ToMarkers](com.tersesystems.blindsight.ToMarkers) in implicit scope, you can get it auto-converted through type annotation.  The various logging statement will only take a @scaladoc[Markers](com.tersesystems.blindsight.Markers):

@@snip [TypeClassExample.scala](../../../test/scala/example/typeclasses/TypeClassExample.scala) { #markers-example }

You can also combine markers:

@@snip [TypeClassExample.scala](../../../test/scala/example/typeclasses/TypeClassExample.scala) { #combine-markers }

@@@ note

You should **not** try to manipulate the marker through `marker.add` or `marker.remove`.  It's better to treat a `org.slf4j.Marker` instance as completely immutable, and manage any composition through @scaladoc[Markers](com.tersesystems.blindsight.Markers).

@@@

Getting at the underlying marker is a `lazy val` that is computed once:

@@snip [TypeClassExample.scala](../../../test/scala/example/typeclasses/TypeClassExample.scala) { #combined-slf4j-marker }

You can also convert your own objects into appropriate markers.

@@snip [TypeClassExample.scala](../../../test/scala/example/typeclasses/TypeClassExample.scala) { #weekday-to-marker }

Or you can use the @scaladoc[MarkersEnrichment](com.tersesystems.blindsight.MarkersEnrichment) that adds an `asMarkers` method to `org.slf4j.Marker` through type enrichment:

@@snip [TypeClassExample.scala](../../../test/scala/example/typeclasses/TypeClassExample.scala)  { #marker-enrichment }

The SLF4J API is awkward to use with markers, because there are several possible variations that can confuse the compiler and stop the type class from being used directly.  To avoid using the `Markers(marker)` wrapper, you can use the @ref:[fluent API](fluent.md) or use @ref:[contextual logging](context.md).

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

