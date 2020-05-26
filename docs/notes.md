## Logger Resolvers

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

## Structured DSL

https://github.com/serilog/serilog/wiki/Structured-Data

## Metrics

- Handling metrics through schema?

Do it on the backend.  Handle events through means of several metrics appender.  When you post an event, there's a metrics appender than handles the aggregation.  This is actually much better than handling metrics inline with the code, because there are locks around histograms etc.  This makes it async and offline from the processing thread, and lets you replace your metrics code later.
