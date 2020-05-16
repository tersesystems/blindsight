# Conditional Logging

No matter how fast your logging is, it's always faster not to log a statement at all.  Blindsight does its best to allow the system to **not** log as much as it makes it possible to log.

Blindsight has conditional logging on two levels; on the logger itself, and on the method.  Conditional logging does not take into account any internal state of the logger, i.e. marker state, logger names, etc.  It takes a boolean call by name, and that's it.

Conditional logging has the obvious use case of "always" and "never" logging and logging based on given feature flags.  However, it also allows for less intuitive use cases, such as time-limited logging conditions or JVM conditions that limit logging in response to high CPU or memory pressure.

## On Condition

All loggers have an `onCondition` method that returns a conditional logger of the same type.

This logger will only log if the condition is true.

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #simple-conditional }

Conditions will be stacked with a boolean AND.

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #composed-conditional }

The conditions are not exposed from the logger, and clients should not peek under the hood.

## When

By contrast, `when` is used on methods, rather than the logger, and provides a block that is executed only when the condition is true:

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #when-conditional }

This is useful when constructing and executing a logging statement is expensive in itself, and allows for finer grained control.

You can, of course, partially apply `when` to return a function:

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #function-when-conditional }

It is generally easier to pass a conditional logger around rather than a logging function.

## Conditional on Circuit Breaker

You can rate limit your logging, or manage logging with a circuit breaker, so that error messages are suppressed when the circuit breaker is open.

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #circuitbreaker-conditional }

## Conditional on Memory Pressure

Using conditional logging is preferable to using call-by-name semantics in expensive logging statements.  Call-by-name arguments still create short lived objects that take up memory in a [thread local allocation buffer](https://alidg.me/blog/2019/6/21/tlab-jvm) and must be cleaned up by [garbage collection](https://www.infoq.com/presentations/jvm-60-memory/).  Using `when` will at least create only one function block, rather than many of them.

If you are concerned about the costs of logging overall and are using JDK 11, you can create a condition that returns false in cases of high JVM memory pressure, ideally through a [JEP 331](http://openjdk.java.net/jeps/331) enabled sampler like [heapsampler](https://github.com/odnoklassniki/jvmti-tools/#heapsampler) -- if that's not available, you can use [JFR event streaming](https://blogs.oracle.com/javamagazine/java-flight-recorder-and-jfr-event-streaming-in-java-14) as a feedback mechanism, so you can check the [TLAB allocation rates](https://shipilev.net/jvm/anatomy-quarks/4-tlab-allocation/).

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #low-pressure-conditional }

If you are on a pre-11 JVM, you can still provide a feedback mechanism to reduce memory pressure.  For example, you can run Yourkit as a [Java Agent](https://www.yourkit.com/docs/java/help/agent.jsp) and enable [object counting](https://www.yourkit.com/docs/java/help/allocations.jsp).  This is low-overhead and can be run in production, but requires some extra work to close the loop. 

## Conditional on Feature Flag

[Feature Flag](https://martinfowler.com/articles/feature-toggles.html) systems allow you to turn on and off features in the application at runtime, in response to complex inputs.  Commercial feature flag systems like [Launch Darkly](https://docs.launchdarkly.com/home/managing-flags/targeting-users#section-assigning-users-to-a-variation) allow you to target feature flags to specific users.

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #featureflag-conditional }

By combining conditionals with feature flags, you can use [targeted diagnostic logging](https://tersesystems.com/blog/2019/07/22/targeted-diagnostic-logging-in-production/) to show debug statements in production only for a specific user, without impacting logging for the entire application as a whole.

## Conditional on Time

Conditional logging can also be used for time and date limited logging statements.  This can be a more flexible way of dealing and rollouts, where we can say "log verbose debugging statements for the next ten minutes" or "suppress logging completely from 10 pm to 8 am."

This works best with a @scaladoc[Deadline](scala.concurrent.duration.Deadline):

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #deadline-conditional }

For periodic scheduling, you can use [CronScheduler](https://github.com/TimeAndSpaceIO/CronScheduler) in conjunction with an @javadoc[AtomicBoolean](java.util.concurrent.atomic.AtomicBoolean).  You can use @javadoc[ScheduledExecutorService](java.util.concurrent.ScheduledExecutorService), but be aware that @javadoc[ScheduledExecutorService](java.util.concurrent.ScheduledExecutorService) is not good at handling a periodic schedule as it can suffer from [extended drift, especially when system time is corrected](https://medium.com/@leventov/cronscheduler-a-reliable-java-scheduler-for-external-interactions-cb7ce4a4f2cd).

@@snip [ConditionalExample.scala](../../../test/scala/example/conditional/ConditionalExample.scala) { #periodic-conditional }

The key to using logging in conjunction with a periodic conditional schedule is that you always log on an operation, and can alter the periodic schedule at runtime, without restarting the service.  By doing this, you are turning up the observability of an operation for a specific period, as opposed to simply running a logging statement on a periodic basis.