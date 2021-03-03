# Type Classes

The important types in Blindsight are @scaladoc[Markers](com.tersesystems.blindsight.Markers), @scaladoc[Argument](com.tersesystems.blindsight.Argument), and  @scaladoc[Message](com.tersesystems.blindsight.Message).

 Where possible, the APIs map automatically, using the @scaladoc[ToMarkers](com.tersesystems.blindsight.ToMarkers), @scaladoc[ToMessage](com.tersesystems.blindsight.ToMessage) and @scaladoc[ToArgument](com.tersesystems.blindsight.ToArgument) type classes, respectively.

## General Principles

Type classes let you represent your domain objects as structured logging data. 

Although Blindsight does provide mappings of the basic primitive types, you may want to provide some more semantic detail about what the value represents, and use the DSL with a specific field name and type -- for example, rather than representing an age as an integer, `logger.info("person age = {}", persion.age)` is easier if you use a specific class `Age` and have a type class instance that represents that `Age` as `bobj("age_year" -> ageInYear)`

You can of course use type classes to render any given type in logging.  For example, to render a `Future` as an argument:

```scala
implicit val futureToArgument: ToArgument[Future[_]] = ToArgument[Future[_]] { future =>
   new Argument(future.toString)
}

logger.info("future is {}", Future.successful(()))
```

@@@ note

You may find it helpful to use [Refined](https://github.com/fthomas/refined) and [Coulomb](https://github.com/erikerlandson/coulomb#documentation) to provide type-safe validation and unit representation of data to the DSL.

@@@

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

## Argument and Arguments

Arguments must be convertible to @scaladoc[Argument](com.tersesystems.blindsight.Argument).  This is usually done with type class instances.

Default @scaladoc[ToArgument](com.tersesystems.blindsight.ToArgument) are determined for the primitives (`String`, `Int`, etc):

@@snip [TypeClassExample.scala](../../../test/scala/example/typeclasses/TypeClassExample.scala) { #argument-int }

You can define your own argument type class instances:

@@snip [TypeClassExample.scala](../../../test/scala/example/typeclasses/TypeClassExample.scala) { #argument-chronounit }

Although it's usually better to use the @ref:[DSL](dsl.md) and map to a @scaladoc[BObject](com.tersesystems.blindsight.AST.BObject):

@@snip [TypeClassExample.scala](../../../test/scala/example/typeclasses/TypeClassExample.scala) { #argument-person }

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

