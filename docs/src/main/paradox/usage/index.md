@@@ index

* [Overview](overview.md)
* [SLF4J API](slf4j.md)
* [Fluent API](fluent.md)
* [Semantic API](semantic.md)
* [Flow API](flow.md)
* [Structured Logging](structured.md)
* [Conditional Logging](conditional.md)
* [Contextual Logging](context.md)
* [Logger Resolvers](resolvers.md)
* [Source Code](sourcecode.md)

@@@

# Usage

To use a Blindsight @scaladoc[Logger](com.tersesystems.blindsight.Logger):

```scala
val logger = com.tersesystems.blindsight.LoggerFactory.getLogger
logger.info("I am an SLF4J-like logger")
```

But there's a lot more, of course.  

## Examples

You can do structured logging:

```scala
import com.tersesystems.blindsight.api._
import com.tersesystems.blindsight.logstash.Implicits._

val markers = Markers("array" -> Seq("one", "two", "three"))
logger.info(markers, "Logs with an array as marker")
```

Fluent mode:

```scala
logger.fluent.info.message("I am a fluent logger").log()
```

Semantic mode:

```scala
logger.semantic[UserEvent].info(userEvent)
```

Flow mode:

```scala
val result = logger.flow.trace(arg1 + arg2)
```

Conditional logging:

```scala
logger.onCondition(booleanCondition).info("Only logs when condition is true")

logger.info.when(booleanCondition) { info => info("when true") }
```

And contextual logging:

```scala
logger.withMarker("userId" -> userId).info("Logging with user id added as a context marker!")
```

## Principles

Blindsight has some organizing principles that inform the design.

* Loggers depend directly and solely on the SLF4J API, which can always be accessed directly.
* APIs can be extended or replaced for domain specific logging.
* Knowing **when** and **when not** to log is more important than "how fast" you log.
* Loggers can be resolved from user defined context, not simply by name or by class.
* Structured logging is baked in, uses standard Scala idioms, and can be overridden.

Likewise, there are things that Blindsight eschews:

* No effects; logging is always a side effect.
* No constraints or configuration on SLF4J implementation.
* No FP library requirements; no need for scalaz, cats, zio etc.
* No formatting on the front end; messages should not contain JSON/XML.

For more details, see the links below:

@@toc { depth=1 }