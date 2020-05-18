# Structured Logging

Structured logging is the basis for logging rich events.  

@@@ note

`logstash-logback-encoder` has [Markers and StructuredArguments](https://github.com/logstash/logstash-logback-encoder#event-specific-custom-fields) which will write out markers and arguments to a [JSON encoder](https://github.com/logstash/logstash-logback-encoder#encoders--layouts) while still enabling line oriented output to console and file appenders.

See [Terse Logback](https://tersesystems.github.io/terse-logback/) and the [Terse Logback Showcase](https://github.com/tersesystems/terse-logback-showcase) for examples of how to configure logstash-logback-encoder for JSON.

@@@

## DSL

TODO

## Custom Mappings

You can also extend arguments and markers using your own type class instances.  This is especially useful when you already have structured logging, and want to pass it through without changes.

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