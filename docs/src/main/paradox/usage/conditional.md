# Conditional Logging

No matter how fast your logging is, it's always faster not to log a statement at all.  Blindsight does its best to allow the system to **not** log as much as it makes it possible to log.

Blindsight has conditional logging on two levels; on the logger itself, and on the method.  Conditional logging does not take into account any internal state of the logger, i.e. marker state, logger names, etc.  It takes a boolean call by name, and that's it.

## On Condition

All loggers have an `onCondition` method that returns a conditional logger of the same type.

This logger will only log if the condition is true.

```scala
def booleanCondition: Boolean = ...
val conditionalLogger = logger.onCondition(booleanCondition)
conditionalLogger.info("Only logs when condition is true")
```

Conditions will be stacked with a boolean AND.

```scala
def booleanCondition: Boolean = ...
val bothConditionsLogger = conditionalLogger.onCondition(anotherCondition)
bothConditionsLogger.info("Only logs when both conditions are true")
```

The conditions are not exposed from the logger, and clients should not peek under the hood.

Conditional loggers are good with [feature flags](https://tersesystems.com/blog/2019/07/22/targeted-diagnostic-logging-in-production/), circuit breakers, sampling, and other systems that throttle logging.  

They are also good with [capabilities](https://tersesystems.com/blog/2018/06/24/security-in-scala/), because loggers can be revoked along with the capability.  For example, a logger reporting a failed network stream can be silenced if the reason why the stream failed is because of an intentional security cutoff.

## When

By contrast, `when` is used on methods, rather than the logger, and provides a block that is executed only when the condition is true:

```scala
logger.info.when(booleanCondition) { info =>
  info("log")
}
```

This is useful when constructing and executing a logging statement is expensive in itself, and allows for finer grained control.

@@@ note

Using `when` is preferable to using call-by-name semantics in expensive logging statements.  Call-by-name arguments still create short lived objects that take up memory and must be cleaned up by [garbage collection](https://www.infoq.com/presentations/jvm-60-memory/).  Using `when` will at least create only one function block, rather than many of them.

If you are concerned about the costs of logging overall, you can create a condition that returns false in cases of high JVM memory pressure, ideally through [JEP 331](http://openjdk.java.net/jeps/331) or [JFR event streaming](https://blogs.oracle.com/javamagazine/java-flight-recorder-and-jfr-event-streaming-in-java-14), or use Log4J2's [garbage-free logging](https://logging.apache.org/log4j/2.x/manual/garbagefree.html).

@@@

You can, of course, partially apply `when` to return a function:

```scala
val infoFunction = logger.info.when(1 == 1)(_)
infoFunction(info => info("when true"))
```

It is generally easier to pass a conditional logger around rather than a logging function.