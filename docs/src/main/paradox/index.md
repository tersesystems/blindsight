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

Blindsight is "observability through logging" where observability is defined as [baked in high cardinality structured data with field types](https://www.honeycomb.io/blog/observability-a-manifesto/).  The name is taken from Peter Watts' excellent first contact novel, [Blindsight](https://en.wikipedia.org/wiki/Blindsight_\(Watts_novel\)).

Blindsight is a logging library written in Scala that wraps SLF4J to add @ref:[useful features](usage/overview.md) that solve several outstanding problems with logging:

* Expressing domain specific objects as arguments through @ref:[type classes](usage/typeclasses.md).
* Rendering structured logs in multiple formats through a format-independent @ref:[AST and DSL](usage/dsl.md).
* Enabling rich structured data through @ref:[JSON-LD](usage/jsonld.md).
* Resolving operation-specific loggers through @ref:[logger resolvers](usage/resolvers.md).
* Building up complex logging statements through @ref:[fluent logging](usage/fluent.md).
* Enforcing user supplied type constraints through @ref:[semantic logging](usage/semantic.md).
* Minimal-overhead tracing and causality tracking through @ref:[flow logging](usage/flow.md).
* Providing thread-safe context to logs through @ref:[context aware logging](usage/context.md).
* Time-based and targeted diagnostic logging through @ref:[conditional logging](usage/conditional.md).
* Runtime method or line based logging overrides through @ref:[scripting](usage/scripting.md).

The only hard dependency is the SLF4J API, but the DSL functionality is only implemented for Logback with [logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder).

Please see the @ref:[Setup](setup/index.md) page for artifacts and installation.

Blindsight is a pure SLF4J wrapper: it delegates all logging through to the SLF4J API and does not configure or manage the SLF4J implementation at all.

Versions are published for Scala 2.11, 2.12, and 2.13.

It is heavily informed by the [blog posts at tersesystems.com](https://tersesystems.com/category/logging/) and the [diagnostic logging showcase](https://github.com/tersesystems/terse-logback-showcase).

@@toc { depth=1 }