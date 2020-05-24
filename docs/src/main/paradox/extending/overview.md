# Overview

In SLF4J, you have a logging API that looks like this: 

```java
public interface Logger {
  boolean isInfoEnabled();
  void info(String message);
}
```

In Blindsight, the API and the logging levels are all traits, and the `LoggerAPI` is a composition of those traits. 

```scala
trait LoggerComponent[P, M] {
  type Predicate <: P
  type Method    <: M
  type Self
}

trait InfoLoggerAPI[P, M] extends LoggerComponent[P, M] {
  def isInfoEnabled: Predicate
  def info: Method
}

trait LoggerAPI[P, M]
    extends LoggerComponent[P, M]
    with TraceLoggerAPI[P, M]
    with DebugLoggerAPI[P, M]
    with InfoLoggerAPI[P, M]
    with WarnLoggerAPI[P, M]
    with ErrorLoggerAPI[P, M]
```

And the logger itself just puts together the appropriate predicates and methods.

```scala

object SLF4JLogger {

  abstract class Base[M: ClassTag](val underlying: org.slf4j.Logger, val markers: Markers)
      extends ExtendedSLF4JLogger[M] {      
    override type Self      = SLF4JLogger[M]
    override type Method    = M
    override type Predicate = SLF4JPredicate

    // ... some infrastructure for parameter lists...

    // implementation for trace, only the level changes
    override def isTraceEnabled: Predicate = newPredicate(Level.TRACE)
    override def trace: Method             = newMethod(Level.TRACE)

    override def withMarker[T: ToMarkers](markerInst: T): Self = {
      val markers = implicitly[ToMarkers[T]].toMarkers(markerInst)
      newInstance(underlying, markers + markers)
    }

    protected def newInstance(underlying: org.slf4j.Logger, markerState: Markers): Self
    protected def newMethod(level: Level): Method
    protected def newPredicate(level: Level): Predicate = new SLF4JPredicate.Impl(level, this)
  }

  class Strict(underlying: org.slf4j.Logger, markers: Markers)
      extends SLF4JLogger.Base[StrictSLF4JMethod](underlying, markers) {
    override protected def newInstance(
        underlying: org.slf4j.Logger,
        markerState: Markers
    ): Self = new Strict(underlying, markerState)

    override protected def newMethod(level: Level): StrictSLF4JMethod =
      new StrictSLF4JMethod.Impl(level, this)

    override def onCondition(test: => Boolean): SLF4JLogger[StrictSLF4JMethod] =
      new Strict.Conditional(test, this)
  }
}
```

You can see there's very little to the logger itself.  The method API itself is as you'd expect:

```scala
trait StrictSLF4JMethod {

  def apply(
      message: Message
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      throwable: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      message: Message,
      throwable: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  // ...
}
```

And from there, the method implementation does the work of resolving type class instances and calling through to SLF4J with the appropriate parameters.

```scala
object StrictSLF4JMethod {

  class Impl(val level: Level, logger: ExtendedSLF4JLogger[StrictSLF4JMethod])
      extends StrictSLF4JMethod {

    @inline
    protected def markers: Markers = logger.markers

    protected val parameterList: ParameterList = logger.parameterList(level)

    override def apply(
        msg: => Message
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m = collateMarkers
      if (m.nonEmpty) {
        if (executePredicate(m.marker)) {
          parameterList.markerMessage(m.marker, msg.toString)
        }
      } else {
        if (executePredicate()) {
          parameterList.message(msg.toString)
        }
      }
    }

    // ... and so on for the rest of the API.
  }
}
```

Breaking down the API means that you can pass through only the `LoggerMethod`, or assemble your custom logging levels.  You have the option of extending `LoggerMethod` and `LoggerPredicate` with your own application specific logging API.   Blindsight is designed to work with you so that adding new functionality is easy.
