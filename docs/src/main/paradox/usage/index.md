@@@ index

* [Principles](principles.md)
* [Overview](overview.md)
* [SLF4J API](slf4j.md)
* [Fluent API](fluent.md)
* [Semantic API](semantic.md)
* [Flow API](flow.md)
* [Structured Logging](structured.md)
* [Contextual Logging](context.md)
* [Conditional Logging](conditional.md)
* [Logger Resolvers](resolvers.md)
* [Source Code](sourcecode.md)

@@@

# Usage

To use a Blindsight @scaladoc[Logger](com.tersesystems.blindsight.Logger):

```scala
val logger = com.tersesystems.blindsight.LoggerFactory.getLogger
logger.info("I am an SLF4J-like logger")
```

For more details, see the links below:

@@toc { depth=1 }