# String Interpolation

You may be familiar with Scala's [string interpolation](https://docs.scala-lang.org/overviews/core/string-interpolation.html), using the `s` interpolator.

With Blindsight, you can log statements as strings using the `st` interpolator.  This creates a complete @scaladoc[Statement](com.tersesystems.blindsight.Statement) that can be used in logging.  The `st` interpolator has full support for @scaladoc[Markers](com.tersesystems.blindsight.Markers), @scaladoc[Arguments](com.tersesystems.blindsight.Arguments), and exceptions, and will create an [SLF4J parameterized message](http://slf4j.org/faq.html#logging_performance) as the @scaladoc[Message](com.tersesystems.blindsight.Message).

## Usage

To use the `st` interpolator, you must import the package object, which you can do with a wildcar import:

```text
import com.tersesystems.blindsight._
```

## Messages

Plain messages work just as you'd expect.

```scala
val statement: Statement = st"This is a plain message"
logger.info(statement)
```

## Markers

Using @scaladoc[Markers](com.tersesystems.blindsight.Markers) has a few special cases.  There can only be one @scaladoc[Markers](com.tersesystems.blindsight.Markers) instance in the message, and it must come before any arguments or exceptions.  The marker is not parameterized in the message.

You should use the `${markers}` form when interpolating, as this prevents any leading whitespace in the message.

```scala
val marker1 = org.slf4j.MarkerFactory.getMarker("MARKER1")
val markers = Markers(marker1)
val statement: Statement = st"${markers}This is a message with a marker."
logger.info(statement)
```

If you have more than one marker, you can collate them in situ:

```scala
logger.info(st"${Markers(marker1) + Markers(marker2)}a single Markers will compile")
```

## Arguments

You can have multiple @scaladoc[Argument](com.tersesystems.blindsight.Argument) instances in the message.  The interpolator will swap out the argument for the `{}` placeholder in the parameterized string, and roll them up into an @scaladoc[Arguments](com.tersesystems.blindsight.Arguments) instance.

In practice, you will be passing in either basic types or instances that have a @scaladoc[ToArgument](com.tersesystems.blindsight.ToArgument) @ref:[type class instance](typeclasses.md).

```scala
val dayOfWeek = "Monday"
val temp = 72 
val statement: Statement = st"It is ${dayOfWeek} and the temperature is ${temp} degrees."
logger.info(statement)
```

This works in conjunction with the @ref:[DSL](dsl.md):

```scala
import DSL._
logger.info(st"Time since epoch is ${bobj("instant_tse" -> Instant.now.toEpochMilli)}")
```

## Exceptions

You can pass an exception in anywhere in the statement, and the interpolator will create a `toString` argument and pass the exception as the last argument, as per the [SLF4J parameterized exception](http://www.slf4j.org/faq.html#paramException) guidelines.

```scala
val throwable = new IllegalStateException("illegal state")
logger.info(st"this is an $throwable")
```

You can pass in multiple exceptions.  The last exception seen will be passed in as the designated throwable.

```scala
val ex1 = new IllegalStateException("ex1")
val ex2 = new IllegalStateException("ex2")
val ex3 = new IllegalStateException("ex3")
logger.info(st"$ex1 $ex2 $ex3") // ex3 will be chosen as the exception.
```
