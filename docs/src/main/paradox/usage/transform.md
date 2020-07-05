# Entry Transformation

An @scaladoc[Entry](com.tersesystems.blindsight.Entry) is a "raw" statement that is a representation of what will be sent to SLF4J.  It consists of an `Option[org.slf4j.Marker]`, a message of type `String`, and arguments of type `Option[Array[Any]]`.

Blindsight can add transformation steps to modify or replace the @scaladoc[Entry](com.tersesystems.blindsight.Entry) just before it is logged.  

You can apply multiple transformations, and they will be processed in order.

## Usage

Entry transformation applies a function to the entry before it is logged.  This gives you the opportunity to modify the behavior of logging from a central location, as opposed to editing every single statement. 

For example, you can [fortune cookie](https://en.wikipedia.org/wiki/Fortune_cookie#In_popular_culture) every logging message:

```scala
val logger = LoggerFactory.getLogger
                .withEntryTransform(e =>
                   e.copy(message = e.message + " IN BED"))

logger.info("You will discover your hidden talents")
```

You can also specify an entry transform for a particular level, which can be useful for integrating into metrics and error reporting systems:

```scala
val errorTransform: (Entry => Entry) = { entry =>
  errors.count.increment()
  entry
}

val logger = LoggerFactory.getLogger
                .withEntryTransform(Level.ERROR, errorTransform)

logger.error("someone is wrong on the internet")
```

You can specify multiple transformations, and they will be applied in the order in which they were declared:

```scala
val logger = createLogger
        .withEntryTransform(e => e.copy(message = e.message + " ONE"))
        .withEntryTransform(e => e.copy(message = e.message + " TWO"))

logger.info("MESSAGE")

// get list appender from logback...
val event = listAppender.list.get(0)
event.getMessage must equal("MESSAGE ONE TWO")
```

## Transforms with Event Buffers

You can use entry transformations in conjunction with @ref:[event buffering](buffer.md). 
 
Because event buffering is itself a case of entry transformation, the same rules apply.  This means that if you want the transformed entry, you must add the event buffer **after** the event transformation:

```scala
val logger = createLogger
        .withEntryTransform(e => e.copy(message = e.message + " ONE"))
        .withEventBuffer(eventBuffer)

logger.info("MESSAGE")
// event buffer will contain event with entry.message == "MESSAGE ONE"
```

