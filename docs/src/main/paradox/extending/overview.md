# Overview

In SLF4J, you have a logging API that looks like this: 

```java
public interface Logger {
  boolean isInfoEnabled();
  void info(String message);
}
```

## LoggerAPI

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

## Logger

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

    override def withCondition(condition: Condition): SLF4JLogger[StrictSLF4JMethod] =
      new Strict(core.withCondition(condition))
  }
}
```

## Methods

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

And from there, the method implementation does the work of resolving type class instances and calling through to SLF4J with the appropriate arguments.

Breaking down the API means that you can pass through only the `LoggerMethod`, or assemble your custom logging levels.  You have the option of extending `LoggerMethod` and `LoggerPredicate` with your own application specific logging API.   Blindsight is designed to work with you so that adding new functionality is easy.

### Complete Implementation

If you want to define your own logger from scratch, with a completely different API, that's easy enough too.  Blindsight's API is small enough that you can swap out parts in a modular way.

For example, here's a custom logger that just has `is*` instead of `is*Enabled`:

```scala
import com.tersesystems.blindsight.{Condition, Entry, EventBuffer, Markers, ToMarkers}
import com.tersesystems.blindsight.core.{CoreLogger, CorePredicate}
import com.tersesystems.blindsight.slf4j.{SLF4JLogger, StrictSLF4JMethod}
import org.slf4j.Logger
import org.slf4j.event.Level
import org.slf4j.event.Level.{DEBUG, ERROR, INFO, TRACE, WARN}

class CustomLogger(private val core: CoreLogger = CustomLogger.coreLogger) extends SLF4JLogger[StrictSLF4JMethod] {
  override type Self = CustomLogger
  override type Method = StrictSLF4JMethod
  override type Predicate = CorePredicate

  override val underlying: Logger = core.underlying

  override val markers: Markers = core.markers

  override def withEntryTransform(level: Level, f: Entry => Entry): Self =
    new CustomLogger(core.withEntryTransform(level, f))

  override def withEntryTransform(f: Entry => Entry): Self =
    new CustomLogger(core.withEntryTransform(f))

  override def withEventBuffer(buffer: EventBuffer): Self =
    new CustomLogger(core.withEventBuffer(buffer))

  override def withEventBuffer(level: Level, buffer: EventBuffer): Self =
    new CustomLogger(core.withEventBuffer(level, buffer))

  override def withCondition(condition: Condition): Self =
    new CustomLogger(core.withCondition(condition))

  override def withMarker[T: ToMarkers](instance: T): Self =
    new CustomLogger(core.withMarker(instance))

  // note different "method names"
  override val isTrace: Predicate = core.predicate(TRACE)
  override val isDebug: Predicate = core.predicate(DEBUG)
  override val isInfo: Predicate = core.predicate(INFO)
  override val isWarn: Predicate = core.predicate(WARN)
  override val isError: Predicate = core.predicate(ERROR)

  override val trace: Method = new StrictSLF4JMethod.Impl(TRACE, core)
  override val debug: Method = new StrictSLF4JMethod.Impl(DEBUG, core)
  override val info: Method = new StrictSLF4JMethod.Impl(INFO, core)
  override val warn: Method = new StrictSLF4JMethod.Impl(WARN, core)
  override val error: Method = new StrictSLF4JMethod.Impl(ERROR, core)
}
```