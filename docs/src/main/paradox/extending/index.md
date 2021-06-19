# Extending Blindsight API

If you want to extend Blindsight with your own behavior, the API is set up to make it easy... or at least easier than SLF4J.  In Blindsight, it's possible to override a particular logger method or predicate, or to add your own methods and implementations.

This is because the Blindsight API is really a bunch of smaller APIs, as opposed to SLF4J having a flat API.  Let's go into this in more detail.  

In SLF4J, you have a logging API that looks like this: 

```java
public interface Logger {
  boolean isInfoEnabled();
  void info(String message);
}
```

But all levels are defined, i.e. there's also `isErrorEnabled` / `error` etc.

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

If you want to define your own logger, you can easily swap out behavior.  For example, here's the `ScriptAwareLogger` that calls out to a script manager.

```scala
class ScriptAwareLogger(core: CoreLogger, scriptManager: ScriptManager) extends Logger.Impl(core) {

  override protected val logger = new ScriptAwareSLF4JLogger(core)

  override def strict: SLF4JLogger[StrictSLF4JMethod] = logger

  override lazy val unchecked: SLF4JLogger[UncheckedSLF4JMethod] =
    new ScriptAwareUncheckedSLF4JLogger(core)

  override lazy val fluent: FluentLogger = new ScriptAwareFluentLogger(core)

  override protected def self(core: CoreLogger): Self = {
    new ScriptAwareLogger(core, scriptManager)
  }

  override def semantic[StatementType: NotNothing]: SemanticLogger[StatementType] = {
    new ScriptAwareSemanticLogger(core)
  }

  class ScriptAwareUncheckedSLF4JLogger(core: CoreLogger) extends SLF4JLogger.Unchecked(core) {
    override def self(core: CoreLogger): SLF4JLogger[UncheckedSLF4JMethod] = {
      new ScriptAwareUncheckedSLF4JLogger(core)
    }

    override def method(level: Level): UncheckedSLF4JMethod =
      new UncheckedSLF4JMethod.Impl(level, core) {
        override protected def enabled(implicit
                                       line: Line,
                                       file: File,
                                       enclosing: Enclosing
                                      ): Boolean = {
          scriptManager.execute(super.enabled, level, enclosing, line, file)
        }

        override def enabled(
                              marker: Marker
                            )(implicit line: Line, file: File, enclosing: Enclosing): Boolean = {
          scriptManager.execute(super.enabled(marker), level, enclosing, line, file)
        }
      }
  }

  class ScriptAwareSemanticLogger[StatementType: NotNothing](core: CoreLogger)
    extends SemanticLogger.Impl[StatementType](core) {
    override protected def self[T: NotNothing](core: CoreLogger): Self[T] = {
      new ScriptAwareSemanticLogger(core)
    }

    override def method(level: Level): Method[StatementType] =
      new SemanticMethod.Impl[StatementType](level, core) {
        override protected def enabled(
                                        markers: Markers
                                      )(implicit line: Line, file: File, enclosing: Enclosing): Boolean = {
          scriptManager.execute(super.enabled(markers), level, enclosing, line, file)
        }
      }
  }

  class ScriptAwareFluentLogger(core: CoreLogger) extends FluentLogger.Impl(core) {
    override protected def self(core: CoreLogger): Self = {
      new ScriptAwareFluentLogger(core)
    }

    override def method(level: Level): FluentMethod = new FluentMethod.Impl(level, core) {
      override def enabled(
                            markers: Markers
                          )(implicit line: Line, file: File, enclosing: Enclosing): Boolean = {
        scriptManager.execute(super.enabled(markers), level, enclosing, line, file)
      }
    }
  }

  class ScriptAwareSLF4JLogger(core: CoreLogger) extends SLF4JLogger.Strict(core) {
    override protected def self(core: CoreLogger): Self = {
      new ScriptAwareSLF4JLogger(core)
    }

    override def method(level: Level): StrictSLF4JMethod = {
      new StrictSLF4JMethod.Impl(level, core) {
        override def enabled(implicit line: Line, file: File, enclosing: Enclosing): Boolean = {
          scriptManager.execute(super.enabled, level, enclosing, line, file)
        }

        override def enabled(
                              marker: Marker
                            )(implicit line: Line, file: File, enclosing: Enclosing): Boolean = {
          scriptManager.execute(super.enabled(marker), level, enclosing, line, file)
        }
      }
    }
  }
}
```
