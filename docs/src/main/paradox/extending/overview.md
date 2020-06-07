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

trait SLF4JLoggerComponent[P, M] {
  type Predicate <: P
  type Method <: M
  type Self
}

object SLF4JLoggerAPI {
  trait Info[P, M] extends SLF4JLoggerComponent[P, M] {
    def isInfoEnabled: Predicate
    def info: Method
  }
}

trait SLF4JLoggerAPI[P, M]
    extends SLF4JLoggerComponent[P, M]
    with SLF4JLoggerAPI.Trace[P, M]
    with SLF4JLoggerAPI.Debug[P, M]
    with SLF4JLoggerAPI.Info[P, M]
    with SLF4JLoggerAPI.Warn[P, M]
    with SLF4JLoggerAPI.Error[P, M]
```

The logger itself just puts together the appropriate predicates and methods.

```scala
object SLF4JLogger {

  abstract class Base[M: ClassTag](core: CoreLogger) extends SLF4JLogger[M] {
    override type Self      = SLF4JLogger[M]
    override type Method    = M
    override type Predicate = SimplePredicate

    override val underlying: Logger = core.underlying

    override val markers: Markers = core.markers

    override val isTraceEnabled: Predicate = core.predicate(TRACE)
    override val isDebugEnabled: Predicate = core.predicate(DEBUG)
    override val isInfoEnabled: Predicate  = core.predicate(INFO)
    override val isWarnEnabled: Predicate  = core.predicate(WARN)
    override val isErrorEnabled: Predicate = core.predicate(ERROR)
  }

  /**
   * A logger that provides "strict" logging that only takes type class aware arguments.
   */
  class Strict(core: CoreLogger) extends SLF4JLogger.Base[StrictSLF4JMethod](core) {
    override val trace: Method = new StrictSLF4JMethod.Impl(TRACE, core)
    override val debug: Method = new StrictSLF4JMethod.Impl(DEBUG, core)
    override val info: Method  = new StrictSLF4JMethod.Impl(INFO, core)
    override val warn: Method  = new StrictSLF4JMethod.Impl(WARN, core)
    override val error: Method = new StrictSLF4JMethod.Impl(ERROR, core)

    override def withMarker[T: ToMarkers](markerInst: T): Self =
      new Strict(core.withMarker(markerInst))

    override def onCondition(condition: Condition): SLF4JLogger[StrictSLF4JMethod] =
      new Strict(core.onCondition(condition))
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

  class Impl(val level: Level, core: CoreLogger)
      extends StrictSLF4JMethod {

    @inline
    protected def markers: Markers = core.markers

    protected val parameterList: ParameterList = core.parameterList(level)

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

The @scaladoc[CoreLogger](com.tersesystems.blindsight.CoreLogger) contains the API independent code for logging.  This exposes the state of the logger (markers, condition and underlying SLF4J logger), and exposes methods for building up additional state without tying that state to an end-user API.

```scala
trait CoreLogger extends UnderlyingMixin with MarkerMixin with OnConditionMixin {
  type Self = CoreLogger

  def condition: Condition

  def sourceInfoBehavior: SourceInfoBehavior

  def predicate(level: Level): SimplePredicate

  def parameterList(level: Level): ParameterList
}
```

The @scaladoc[CoreLogger](com.tersesystems.blindsight.CoreLogger) mediates between the user level loggers and the @scaladoc[ParameterList](com.tersesystems.blindsight.ParameterList), which organizes the SLF4J logger by level.  

```scala
trait ParameterList {

  def executePredicate(): Boolean
  def executePredicate(marker: Marker): Boolean

  def message(msg: String): Unit
  def messageArg1(msg: String, arg: Any): Unit
  def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit
  def messageArgs(msg: String, args: Seq[_]): Unit
  def markerMessage(marker: Marker, msg: String): Unit
  def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit
  def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit
  def markerMessageArgs(marker: Marker, msg: String, args: Seq[_]): Unit

  def executeStatement(statement: Statement): Unit
}
```

There is a @scaladoc[Conditional](com.tersesystems.blindsight.ParameterList.Conditional) which will only call out if the condition is met:

```scala
object ParameterList {
  class Conditional(level: Level, core: CoreLogger) extends ParameterList {
    override def executePredicate(): Boolean = {
      core.condition(level) && core.parameterList(level).executePredicate()
    }
    override def executePredicate(marker: Marker): Boolean = {
      core.condition(level) && core.parameterList(level).executePredicate(marker)
    }

    override def message(msg: String): Unit =
      if (core.condition(level)) core.parameterList(level).message(msg)
    override def messageArg1(msg: String, arg: Any): Unit =
      if (core.condition(level)) core.parameterList(level).messageArg1(msg, arg)
    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit =
      if (core.condition(level)) core.parameterList(level).messageArg1Arg2(msg, arg1, arg2)
    override def messageArgs(msg: String, args: Seq[_]): Unit =
      if (core.condition(level)) core.parameterList(level).messageArgs(msg, args)
    override def markerMessage(marker: Marker, msg: String): Unit =
      if (core.condition(level)) core.parameterList(level).markerMessage(marker, msg)
    override def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit =
      if (core.condition(level)) core.parameterList(level).markerMessageArg1(marker, msg, arg)
    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit =
      if (core.condition(level))
        core.parameterList(level).markerMessageArg1Arg2(marker, msg, arg1, arg2)
    override def markerMessageArgs(marker: Marker, msg: String, args: Seq[_]): Unit =
      if (core.condition(level)) core.parameterList(level).markerMessageArgs(marker, msg, args)

    override def executeStatement(statement: Statement): Unit =
      if (core.condition(level)) core.parameterList(level).executeStatement(statement)
  }
}
```

Finally, the parameter lists are organized by level, so `executePredicate()` resolves to `underlying.isLoggingDebug()` if the level is `INFO`.

```scala
object ParameterList {
  class Info(logger: org.slf4j.Logger) extends Impl(Level.INFO, logger) {
    override def executePredicate(): Boolean               = logger.isInfoEnabled
    override def executePredicate(marker: Marker): Boolean = logger.isInfoEnabled(marker)

    override def message(msg: String): Unit               = logger.info(msg)
    override def messageArg1(msg: String, arg: Any): Unit = logger.info(msg, arg)
    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit =
      logger.info(msg, arg1, arg2)
    override def messageArgs(msg: String, args: Seq[_]): Unit =
      logger.info(msg, args.asJava.toArray: _*)
    override def markerMessage(marker: Marker, msg: String): Unit = logger.info(marker, msg)
    override def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit =
      logger.info(marker, msg, arg)
    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit =
      logger.info(marker, msg, arg1, arg2)
    override def markerMessageArgs(marker: Marker, msg: String, args: Seq[_]): Unit =
      logger.info(marker, msg, args.asJava.toArray: _*)
  }
}
```

This is how Blindsight works.