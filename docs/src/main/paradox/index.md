@@@ index

* [Setup](setup/index.md)
* [Usage](usage/index.md)
* [Extending](extending/index.md)
* [Performance](performance/index.md)
* [Principles](principles.md)

@@@

# Blindsight

> Suffering in silence, you check the logs for fresh telemetry.
>
> You think: *That can't be right*.
>
> -- [Blindsight](https://www.rifters.com/real/Blindsight.htm#Prologue), Peter Watts

Blindsight is a logging library written in Scala that wraps SLF4J.  The name is taken from Peter Watts' excellent first contact novel, [Blindsight](https://en.wikipedia.org/wiki/Blindsight_\(Watts_novel\)).

The core feature of Blindsight is that it is "type safe" -- rather than passing in arguments of type `java.lang.Object`, the API accepts only objects that can be converted into an `Argument` through the `ToArgument` @ref:[type class](usage/typeclasses.html).

```
val str: String = "string arg"
val number: Int = 1
val arg: Person = Person(name, age) // has a ToArgument[Person] type class instance
logger.info("{} {} {}", bool, number, person) // compiles fine

logger.info("will not compile", new Object()) // WILL NOT COMPILE
```

By adding type safety, Blindsight gives the application more control over how data is logged, rather than implicitly relying on the `toString` method to render data for logging purposes.

Blindsight adds @ref:[useful features](usage/overview.md) that solve several outstanding problems with logging:

* Rendering structured logs in multiple formats through an AST, along with an optional format-independent @ref:[DSL](usage/dsl.md).
* Providing thread-safe context to logs through @ref:[context aware logging](usage/context.md).
* Time-based and targeted logging through @ref:[conditional logging](usage/conditional.html).
* Dynamic targeted logging through @ref:[scripting](usage/scripting.html).
* Easier "printf debugging" through macro based @ref:[inspections](usage/inspections.html).

Using Scala to break apart the SLF4J API also makes constructing new logging APIs much easier.  You have the option of creating your own, depending on your use case:

* Building up complex logging statements through @ref:[fluent logging](usage/fluent.html).
* Enforcing user supplied type constraints through @ref:[semantic logging](usage/semantic.html).
* Minimal-overhead tracing and causality tracking through @ref:[flow logging](usage/flow.html).
* Managing complex relationships and schema through @ref:[JSON-LD](usage/jsonld.md).

Finally, there's also more advanced functionality to transform arguments and statements before entering SLF4J:

* Resolving operation-specific loggers through @ref:[logger resolvers](usage/resolvers.md).
* Hooks into logging entries through @ref:[entry transformation](usage/transform.html)
* Application accessible debug and trace logs through @ref:[event buffers](usage/buffer.html)

## Blindsight and Echopraxia

If you are looking for a strict structured logging solution in Scala, please checkout [Echopraxia](https://github.com/tersesystems/echopraxia-plusscala).  Structured logging is optional in Blindsight, and it's possible to mix structured and "flat" arguments and markers into a logging statement.  In contrast, [Echopraxia](https://github.com/tersesystems/echopraxia-plusscala) **requires** structured logging in its API and does not allow unstructured data as input.

@@toc { depth=1 }