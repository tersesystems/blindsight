# Logger Resolvers


The easiest way to get at a logger is to use @scaladoc[LoggerFactory](com.tersesystems.blindsight.LoggerFactory$).  This looks like the SLF4J version, but is more flexible because it uses a @scaladoc[LoggerResolver](com.tersesystems.blindsight.LoggerResolver) type class under the hood.

```scala
import com.tersesystems.blindsight.LoggerFactory

val loggerFromName = LoggerFactory.getLogger("some.Logger")
val loggerFromClass = LoggerFactory.getLogger(getClass)
```

There is also a macro based version which finds the enclosing class name and hands it to you:

```scala
val loggerFromEnclosing = LoggerFactory.getLogger
```

Finally, you also have the option of creating your own logger resolver.  This is useful when you want to get away from class based logging, and use a naming strategy based on a correlation id.

```scala
trait LoggerResolver[T] {
  def resolveLogger(instance: T): org.slf4j.Logger
}
```

means you can resolve a logger directly from a request:

```scala
implicit val requestToResolver: LoggerResolver[Request] = (instance: Request) => {
    loggerFactory.getLogger("requests." + instance.requestId())
}
```

And from then on, you can do:

```scala
val myRequest: Request = ...
val logger = LoggerFactory.getLogger(myRequest)
```
