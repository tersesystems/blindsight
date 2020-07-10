# Conditional Logging

No matter how fast your logging is, it's always faster not to log a statement at all.  Blindsight does its best to allow the system to **not** log as much as it makes it possible to log.

Blindsight has conditional logging on two levels; on the logger itself, and on the method.  Conditional logging has access to the level and to the @scaladoc[LoggerState](com.tersesystems.blindsight.LoggerState) available to the logger, but does not have access to individual statements.

Conditional logging is just as fast as adding a guard conditional wrapper around SLF4J statements and far more flexible.   It provides the obvious use cases of "never" logging and logging based on given feature flags, and also allows for less intuitive use cases, such as time-limited logging conditions or JVM conditions that limit logging in response to high CPU or memory pressure.

@@@ note

You should use `Condition.never` when disabling logging, as it will allow Blindsight to do some additional optimization, particularly with @ref:[flow logging](flow.md).  Because conditions run through a logical AND, using `Condition.never` will return a "no-op" logger which will always return false and produce no output, and adding additional conditions afterwards has no effect.

@@@

## On Condition

All loggers have an `withCondition` method that takes a @scaladoc[Condition](com.tersesystems.blindsight.Condition).  You can look at a marker from the logger state:

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #marker-conditional }

And look at the level as well:

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #level-marker-conditional }

And you can also pass through anything that returns a boolean and it will take it as a call by name.

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #simple-conditional }

Conditions will be stacked with a boolean AND:

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #composed-conditional }

The conditions are not exposed from the logger, and clients should not peek under the hood.

## When

By contrast, `when` is used on methods, rather than the logger, and provides a block that is executed only when the condition is true:

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #when-conditional }

This is useful when constructing and executing a logging statement is expensive in itself, and allows for finer grained control.

You can, of course, partially apply `when` to return a function:

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #function-when-conditional }

It is generally easier to pass a conditional logger around rather than a logging function.

## Conditional with Tracer Bullet

Conditional logging is very useful in conjunction with [tracer-bullet logging](https://gist.github.com/wsargent/36e6c3a56b6aedc8db77687ee5ab8c69), where you set a marker that is using a [turbofilter](http://logback.qos.ch/manual/filters.html#TurboFilter) with `OnMatch=ACCEPT`: 
 
```xml
<turboFilter class="ch.qos.logback.classic.turbo.MarkerFilter">
  <Name>TRACER_FILTER</Name>
  <Marker>TRACER</Marker>
  <OnMatch>ACCEPT</OnMatch>
</turboFilter> 
```

This means that you can bypass the logging system's levels, and be sure that logging at a TRACE level will cause a logging event to be generated, even if the logger level is set to INFO.

```scala
val tracerMarker = org.slf4j.MarkerFactory.getMarker("TRACER")
val traceCondition = Condition(request.getQueryString("trace").nonEmpty)
val traceBulletLogger = logger.withCondition(traceCondition).withMarker(tracerMarker)
traceBulletLogger.trace("trace statement written even if loglevel is INFO!")
```

## Conditional on Circuit Breaker

You can rate limit your logging, or manage logging with a circuit breaker, so that error messages are suppressed when the circuit breaker is open.

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #circuitbreaker-conditional }

## Conditional on Feature Flag

[Feature Flag](https://martinfowler.com/articles/feature-toggles.html) systems allow you to turn on and off features in the application at runtime, in response to complex inputs.  Commercial feature flag systems like [Launch Darkly](https://docs.launchdarkly.com/home/managing-flags/targeting-users#section-assigning-users-to-a-variation) allow you to target feature flags to specific users.

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #featureflag-conditional }

Note that this is just an example -- in production, you should organize your feature flags so that they're [only exposed from one place](https://andydote.co.uk/2019/06/11/feature-toggles-reducing-coupling/) and the call out to the flag system is under the hood.

By combining conditionals with feature flags, you can use [targeted diagnostic logging](https://tersesystems.com/blog/2019/07/22/targeted-diagnostic-logging-in-production/) to show debug statements in production only for a specific user, without impacting logging for the entire application as a whole.

## Conditional on Time

Conditional logging can also be used for time and date limited logging statements.  This can be a more flexible way of dealing and rollouts, where we can say "log verbose debugging statements for the next ten minutes" or "suppress logging completely from 10 pm to 8 am."

This works best with a @scaladoc[Deadline](scala.concurrent.duration.Deadline):

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #deadline-conditional }

For periodic scheduling, you can use [CronScheduler](https://github.com/TimeAndSpaceIO/CronScheduler) in conjunction with an @javadoc[AtomicBoolean](java.util.concurrent.atomic.AtomicBoolean).  You can use @javadoc[ScheduledExecutorService](java.util.concurrent.ScheduledExecutorService), but be aware that @javadoc[ScheduledExecutorService](java.util.concurrent.ScheduledExecutorService) is not good at handling a periodic schedule as it can suffer from [extended drift, especially when system time is corrected](https://medium.com/@leventov/cronscheduler-a-reliable-java-scheduler-for-external-interactions-cb7ce4a4f2cd).

To use CronScheduler, first define a periodic scheduler in Java:

@@snip [Periodic.java](../../../test/scala/example/conditional/Periodic.java) { #periodic-cron }

Then access it from Scala:

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #periodic-conditional }

The key to using logging in conjunction with a periodic conditional schedule is that you always log on an operation, and can alter the periodic schedule at runtime, without restarting the service.  By doing this, you are turning up the observability of an operation for a specific period, as opposed to simply running a logging statement on a periodic basis.

## Conditional on Memory Pressure

Using conditional logging can reduce @ref:[memory churn](../performance/memory.md).  If you are concerned about the costs of logging, you can create a condition that returns false in cases of [high JVM memory pressure](https://github.com/clojure-goes-fast/jvm-alloc-rate-meter).

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #low-pressure-conditional }

