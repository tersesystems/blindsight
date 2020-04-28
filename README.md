# Blindsight

[![Build Status](https://travis-ci.org/tersesystems/blindsight.svg?branch=master)](https://travis-ci.org/tersesystems/blindsight) ![Bintray](https://img.shields.io/bintray/v/tersesystems/maven/blindsight)

Blindsight is a Scala logging API that allows for [structured logging](https://tersesystems.github.io/blindsight/usage/structured.html), [fluent logging](https://tersesystems.github.io/blindsight/usage/fluent.html), [semantic logging](https://tersesystems.github.io/blindsight/usage/semantic.html), [flow logging](https://tersesystems.github.io/blindsight/usage/flow.html), [context aware logging](https://tersesystems.github.io/blindsight/usage/context.html), [conditional logging](https://tersesystems.github.io/blindsight/usage/conditional.html), and [other useful things](https://tersesystems.github.io/blindsight/usage/overview.html).
 
The name is taken from Peter Watts's excellent first contact novel, [Blindsight](https://en.wikipedia.org/wiki/Blindsight_\(Watts_novel\)).

## TL;DR

To use a Blindsight @scaladoc[Logger](com.tersesystems.blindsight.Logger):

```scala
val logger = com.tersesystems.blindsight.LoggerFactory.getLogger
logger.info("I am an SLF4J-like logger")
```

But there's a lot more, of course.  You can do structured logging:

```scala
val markers = Markers("array" -> Seq("one", "two", "three"))
logger.info(markers, "Logs with an array as marker")
```

There's fluent mode:

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

logger.info.when(booleanCondition) { info("when true") }
```

And contextual logging:

```scala
logger.withMarker("userId" -> userId).info("Logging with user id added as a context marker!")
```

## Documentation 

See [the documentation](https://tersesystems.github.io/blindsight/) for more details.

## License

Blindsight is released under the "Apache 2" license. See [LICENSE](LICENSE) for specifics and copyright declaration.