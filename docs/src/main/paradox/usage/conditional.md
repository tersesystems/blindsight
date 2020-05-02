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

You can, of course, partially apply `when` to return a function:

```scala
val infoFunction = logger.info.when(1 == 1)(_)
infoFunction(info => info("when true"))
```

But it is generally easier to pass a conditional logger around rather than a logging function.