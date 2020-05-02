# Fluent API

A [fluent builder interface](https://www.martinfowler.com/bliki/FluentInterface.html) is an API that relies heavily on method chaining to build up an expression.  The Blindsight fluent API works with @scaladoc[Markers](com.tersesystems.blindsight.api.Markers), @scaladoc[Message](com.tersesystems.blindsight.api.Message), and @scaladoc[Arguments](com.tersesystems.blindsight.api.Arguments), and uses type classes to map appropriately, using the @scaladoc[ToMarkers](com.tersesystems.blindsight.api.ToMarkers), @scaladoc[ToMessage](com.tersesystems.blindsight.api.ToMessage) and @scaladoc[ToArguments](com.tersesystems.blindsight.api.ToArguments) type classes, respectively. 

The fluent API has an immediate advantage in that there's less overloading in the API, and there's more room to chain.  With type classes, it's possible to set up much richer [structured logging](https://tersesystems.com/blog/2020/03/10/a-taxonomy-of-logging/).

## Accessing Fluent Logger

The easiest way of getting a fluent logger is to use the @scaladoc[LoggerFactory](com.tersesystems.blindsight.LoggerFactory) and then call the `fluent` method:
 
@@snip [FluentMain.scala](../../../test/scala/example/fluent/FluentMain.scala) { #fluent-logger }
 
The fluent logger builds up the logging statement by calling `marker`, `argument`, `message`, `cause`, and then finally `log()` or `logWithPlaceholders()` to execute the statement.  You may call each of these methods repeatedly, and they build up markers, arguments, or a concatenated message (note that exceptions are, well, an exception).  You may call them in any order.  If you call `log()` then the statement is executed exactly as written.  If you call `logWithPlaceholders()` then the number of arguments is counted and "{}" format placeholders are appended to the statement's message so that all arguments are visible.

@@snip [FluentMain.scala](../../../test/scala/example/fluent/FluentMain.scala) { #fluent-statement }

## Markers

You can log with a marker alone and then log:

@@snip [FluentMain.scala](../../../test/scala/example/fluent/FluentMain.scala) { #fluent-markers }

This will write out an empty string as the message, and a logstash marker.

You can also write out anything that has a  @scaladoc[ToMarkers](com.tersesystems.blindsight.api.ToMarkers) implementation, i.e. the Logstash implicits lets you do this:

```scala
import com.tersesystems.blindsight.logstash.Implicits._
import com.tersesystems.blindsight.api._

// Shown explicitly here but this is all in Implicits._
implicit def arrayToMarkers[T]: ToMarkers[(String, Seq[T])] = ToMarkers {
  case (k, v) => Markers(LogstashMarkers.appendArray(k, v: _*))
}

fluentLogger.marker("array" -> Seq("one", "two", "three")).log()
```

All markers are in a @scaladoc[Markers](com.tersesystems.blindsight.api.Markers) instance internally, but are not accessible from the builder.

## Message

A message is the part of the statement that is written out as a string.  

```scala
fluentLogger.message("some message").log()
```

Messages can be concatenated together by calling `message` repeatedly:

```scala
fluentLogger.message("hello").message("world").log()
```

You can pass in your own custom instances, of course.

```scala
case class Person(name: String)

implicit val personToMessage: ToMessage[Person] = ToMessage { person =>
  Message(s"My name is ${person.name}")
}
fluentLogger.message(person).log()
```

## Arguments

Arguments can be added with `argument`.  You do not need to define a message, but you should call `logWithPlaceholders` in that case so the argument is visible.

```scala
fluentLogger.argument(someArgument).logWithPlaceholders()
```

Custom formatting is done with @scaladoc[ToArguments](com.tersesystems.blindsight.api.ToArguments).

```scala
case class Person(name: String)

implicit val personToArguments: ToArguments[Person] = ToArguments { person =>
  Arguments(s"${person.name}")
}

val person = Person("Felix")
fluentLogger.message("I was talking to {}").argument(person).log()
```

## Exceptions

Exceptions are added using `cause`.  Exceptions are of type `Throwable`, just like the SLF4J API.

```scala
fluentLogger.message("the exception was {}").cause(someException).log()
```

Technically, an exception is the last argument in a logging statement.  Exception does **not** compose, and if you call `exception` repeatedly, the last exception will be set.  