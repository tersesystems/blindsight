# Fluent

- https://github.com/skjolber/json-log-domain 
- https://github.com/ffissore/slf4j-fluent/blob/master/src/main/java/org/fissore/slf4j/LoggerAtLevel.java
- https://github.com/Swrve/rate-limited-logger
- https://github.com/jacek99/structlog4j

# Logtree

- Logtree?

https://github.com/lancewalton/treelog

Need an example here.

https://medium.com/@m.langer798/why-im-not-abandoning-slf4zio-in-favor-of-zio-logging-16d0ae70a1b9

https://olegpy.com/better-logging-monix-1/

## Context Resolution

Ties in to operation / "unit of work" activities.

### Through Scoping

You're in an object that has a context already, and can reference it directly.

Either you're an inner class, or it's provided as a parameter, or there's only one.


### Through Thread Local Storage

Works great if you're always using the same thread.

### Instrumentation

Works great if you have byte code instrumentation for the code base.

logback-bytebuddy.

### Through Lookup

Tie the logger / context to the unit of work / operation id.

Then use a resolver with that operation id to find the best context.

Something in scope?  Use it.  Something in thread-local?  Use that.  If not, pull it directly from lookup.

Downside -- anything can access the context and log with it, given the id.
Also have to cache or explicitly remove context.

Also requires that you have a unique id you can look up for everything, and it's fast enough to do so.

FP heavy code can log perfectly well in this scenario, because all you need is the tag and then you can look up from wherever.  It's the resolver's job to find something that can match it.

Ability to deal with FP heavy code (factories for functions?)  Covering exceptional cases and failures.

- 
https://github.com/twitter/util/blob/develop/util-slf4j-api/README.md

# Semantic Logging

Why strongly typed (semantic) logging is important.
https://github.com/microsoft/perfview/blob/master/documentation/TraceEvent/TraceEventProgrammersGuide.md

https://martinfowler.com/articles/domain-oriented-observability.html

https://github.com/SemanticRecord/talaan

- https://github.com/aQute-os/biz.aQute.semantic-logging
- https://github.com/UnquietCode/LogMachine
- http://talaan.semanticrecord.org/
- https://github.com/SemanticRecord/talaan
- https://looking4q.blogspot.com/2019/01/level-up-logs-and-elk-contract-first.html
- https://looking4q.blogspot.com/2018/11/logging-cutting-edge-practices.html
- https://github.com/skjolber/json-log-domain
- https://github.com/ffissore/slf4j-fluent/blob/master/src/main/java/org/fissore/slf4j/LoggerAtLevel.java
- http://www.erights.org/elib/Tracing.html

* http://tech.opentable.co.uk/blog/2015/01/23/on-strongly-typed-logging/

## Metrics

- Handling metrics through schema?

Do it on the backend.  Handle events through means of several metrics appender.  When you post an event, there's a metrics appender than handles the aggregation.  This is actually much better than handling metrics inline with the code, because there are locks around histograms etc.  This makes it async and offline from the processing thread, and lets you replace your metrics code later.


### Causality / Tree Logging

#### Eliot

https://eliot.readthedocs.io/en/stable/quickstart.html#adding-eliot-logging

https://eliot.readthedocs.io/en/stable/generating/actions.html


## Tracing

https://users.scala-lang.org/t/overriding-implicit-contexts/4696/2

https://crates.io/crates/tracing

http://smallcultfollowing.com/babysteps/blog/2020/02/11/async-interview-6-eliza-weisman/

https://github.com/open-telemetry/opentelemetry-java/blob/master/QUICKSTART.md#create-basic-span

TODO Work with tracing API?
     https://tracing.rs/tracing/
     https://docs.honeycomb.io/getting-data-in/java/beeline/

Should work with timers.

# Source Info

common "source code" context to your program at runtime.   https://github.com/lihaoyi/sourcecode#logging 

```scala
trait SourceInfoSupport {
  def sourceInfoMarker(
      level: Level,
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Option[Marker]
}
```
 
```scala
trait LogstashSourceInfoSupport extends SourceInfoSupport {
  override def sourceInfoMarker(
      level: Level,
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Option[Marker] = {
    import com.tersesystems.blindsight.Implicits._
    val lineMarker      = Markers.append("line", line.value)
    val fileMarker      = Markers.append("file", file.value)
    val enclosingMarker = Markers.append("enclosing", enclosing.value)
    Some(lineMarker :+ fileMarker :+ enclosingMarker)
  }
}
``` 