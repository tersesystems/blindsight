# Contextual Logging

Blindsight can handle context cleanly over multiple threads.  Context is handled through an immutable API that returns new instances on modification.

This is very useful when you are logging complex operations because a complete picture is often not available at the beginning of the unit of work.   The more context you have in your logs, the better.

Blindsight does not use MDC for managing context, and does not touch thread local storage.  Instead, it uses markers and builds up those markers for use in structured logging, returning a logger that will automatically apply those markers as context.

The assumption is that you will be using the @ref:[structured logging](dsl.md) and [Terse Logback](https://tersesystems.github.io/terse-logback).  You should also read through how to create your own [markers](https://tersesystems.com/blog/2019/05/18/application-logging-in-java-part-4/) for use in logging.

## General Principles

Mark McBride has a blog post on [adding context to events using the Five Ws](https://www.honeycomb.io/blog/event-foo-moar-context-better-events/).

## Marker Context

Blindsight allows state to be built up through a set of SLF4J markers, wrapped by @scaladoc[Markers](com.tersesystems.blindsight.api.Markers).

```scala
val entryLogger = logger.withMarker(MarkerFactory.getMarker("ENTRY"))
entryLogger.trace("entry: entering method")
```

You can accumulate several markers at once.

```scala
import net.logstash.logback.marker.{Markers => LogstashMarkers}
val loggerWithTwoMarkers = entryLogger.withMarker(LogstashMarkers.append("user", "will"))
``` 

The markers are not added to each other -- in fact, there is no absolutely use of SLF4J marker parent/child relationships or SLF4J iterators in the code.  Instead, they're handled by a Scala immutable set, and a custom parent marker is created lazily when you call `markers.marker`.

@@@ note

You should not call `marker.add(childMarker)` or otherwise use the SLF4J Marker API, as it directly mutates marker internal state.

@@@

The markers are available from the logger using `markers`:

```scala
val markers: Markers = entryLogger.markers
val parentMarker: Marker = markers.marker
```

You can log with markers as usual, and they will be added on top of the logger's markers:

```scala
val anotherMarker = ... 
entryLogger.info(anotherMarker, "a message")
```

Managing context is from this point a question of whether you want to pass around @scaladoc[Markers](com.tersesystems.blindsight.Markers), or pass around a @scaladoc[Logger](com.tersesystems.blindsight.Logger), useful for [constructing events](https://tersesystems.com/blog/2020/03/10/a-taxonomy-of-logging/). 

## Mapped Diagnostic Context

Blindsight does not use [MDC](http://logback.qos.ch/manual/mdc.html), and does not recommend its use.

There are many reasons to not use MDC.  It is inherently limiting as a `Map[String,String]`.  It must be managed carefully in asynchronous programming to resolve the Map.  It is mutable -- it will only contain the last written values in the map at the time of logging, and no record is kept of prior values.  It is static -- it cannot be swapped out, enhanced, placed behind a proxy, or otherwise managed.  And finally, MDC fails silently.  When something goes wrong in MDC, it's anyone's guess what cleared it.

MDC is still useful in some circumstances.  When you have third party code that already has logging, then setting variables in the MDC may be the only way of passing context to that library code: if this is the case, then you will have to manage the mapping of complex non-string data from markers to MDC by hand.

If there are properties in MDC that you want to use in Blindsight across an async boundary, you should pull them from MDC using `getCopyOfContextMap` and add them as structured logging properties before kicking things off:

```scala
val mdcMap = MDC.getCopyOfContextMap.asJava
val loggerWithMap = logger.withMarker(bobj(mdcMap))
val resultFuture = Future {
  loggerWithMap.trace("This can be executed in different thread")
  computeThing()
}
```

Note that this is rendering markers, not revaluing MDC with a custom execution context -- converters that display MDC values like `%X{foo}` won't work.