/*
 * Copyright 2020 Terse Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tersesystems.blindsight.slf4j

import com.tersesystems.blindsight.mixins._
import com.tersesystems.blindsight.{Markers, ParameterList, ToMarkers}
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

import scala.reflect.ClassTag

/**
 * Public SLF4J Logger interface.  This is intended for the end user.
 *
 * {{{
 * val markers = Markers(bobj("key" -> "value"))
 * val message = "message arg1={} arg2={} arg3={}"
 * val arguments: Arguments = Arguments("arg1", 42, true)
 * val e = new RuntimeException("whoops")
 * logger.info(markers, message, arguments, e);
 * }}}
 *
 * @tparam M the type of method.
 */
trait SLF4JLogger[M]
    extends SLF4JLoggerAPI[SLF4JPredicate, M]
    with MarkerMixin
    with UnderlyingMixin
    with OnConditionMixin {
  override type Self <: SLF4JLogger[M]
}

/**
 * Service level interface, contains additional methods to make
 * extending the API easier.  Not for public consumption.
 *
 * @tparam M the type of method.
 */
trait ExtendedSLF4JLogger[M]
    extends SLF4JLogger[M]
    with SourceInfoMixin
    with ParameterListMixin
    with PredicateMixin[SLF4JPredicate] {
  def method(level: Level): M
}

object SLF4JLogger {

  /**
   * A convenient abstract base class implementation.
   *
   * @param underlying the underlying logger.
   * @param markers the marker state to be applied to every method.
   * @tparam M the type of method.
   */
  abstract class Base[M: ClassTag](val underlying: org.slf4j.Logger, val markers: Markers)
      extends ExtendedSLF4JLogger[M] {
    override type Self      = SLF4JLogger[M]
    override type Method    = M
    override type Predicate = SLF4JPredicate

    protected val parameterLists: Seq[ParameterList] = ParameterList.lists(this.underlying)

    private val methods: Array[M] = Array(
      error,
      warn,
      info,
      debug,
      trace
    )

    private val predicates = Array(
      isErrorEnabled,
      isWarnEnabled,
      isInfoEnabled,
      isDebugEnabled,
      isTraceEnabled
    )

    override def sourceInfoMarker(
        level: Level,
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Markers = Markers.empty

    override def method(level: Level): Method = methods(level.ordinal())

    override def predicate(level: Level): Predicate = predicates(level.ordinal())

    @inline
    def parameterList(level: Level): ParameterList = parameterLists(level.ordinal)

    override def isTraceEnabled: Predicate = newPredicate(Level.TRACE)
    override def trace: Method             = newMethod(Level.TRACE)

    override def isDebugEnabled: Predicate = newPredicate(Level.DEBUG)
    override def debug: Method             = newMethod(Level.DEBUG)

    override def isInfoEnabled: Predicate = newPredicate(Level.INFO)
    override def info: Method             = newMethod(Level.INFO)

    override def isWarnEnabled: Predicate = newPredicate(Level.WARN)
    override def warn: Method             = newMethod(Level.WARN)

    override def isErrorEnabled: Predicate = newPredicate(Level.ERROR)
    override def error: Method             = newMethod(Level.ERROR)

    override def withMarker[T: ToMarkers](markerInst: T): Self = {
      val markers = implicitly[ToMarkers[T]].toMarkers(markerInst)
      newInstance(underlying, markers + markers)
    }

    override def onCondition(test: => Boolean): Self

    protected def newInstance(underlying: org.slf4j.Logger, markerState: Markers): Self
    protected def newMethod(level: Level): Method
    protected def newPredicate(level: Level): Predicate = new SLF4JPredicate.Impl(level, this)
  }

  /**
   * A logger that provides its own methods to wrap the original logger.
   * Can be used as a base for conditional logging.
   *
   * @param logger the logger to delegate calls to.
   * @tparam M the type of method.
   */
  abstract class Delegate[M](protected val logger: ExtendedSLF4JLogger[M])
      extends ExtendedSLF4JLogger[M] {
    override type Self      = SLF4JLogger[M]
    override type Method    = M
    override type Predicate = logger.Predicate

    override def onCondition(test2: => Boolean): Self

    override def withMarker[T: ToMarkers](markerInstance: T): Self

    protected def newMethod(level: Level): M

    override def isTraceEnabled: Predicate = logger.isTraceEnabled
    override def trace: Method             = newMethod(Level.TRACE)

    override def isDebugEnabled: Predicate = logger.isDebugEnabled
    override def debug: Method             = newMethod(Level.DEBUG)

    override def isInfoEnabled: Predicate = logger.isInfoEnabled
    override def info: Method             = newMethod(Level.INFO)

    override def isWarnEnabled: Predicate = logger.isWarnEnabled
    override def warn: Method             = newMethod(Level.WARN)

    override def isErrorEnabled: Predicate = logger.isErrorEnabled
    override def error: Method             = newMethod(Level.ERROR)

    override def markers: Markers = logger.markers

    override def sourceInfoMarker(
        level: Level,
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Markers = {
      logger.sourceInfoMarker(level, line, file, enclosing)
    }

    override def parameterList(level: Level): ParameterList = logger.parameterList(level)

    override def predicate(level: Level): SLF4JPredicate = logger.predicate(level)

    override def underlying: org.slf4j.Logger = logger.underlying

    override def method(level: Level): M = logger.method(level)
  }

  /**
   * A logger that provides "unchecked" methods that use `Any` as arguments.
   *
   * @param underlying the underlying logger.
   * @param markers the marker state to be applied to every method.
   */
  class Unchecked(underlying: org.slf4j.Logger, markers: Markers)
      extends SLF4JLogger.Base[UncheckedSLF4JMethod](underlying, markers) {

    override protected def newInstance(
        underlying: org.slf4j.Logger,
        markerState: Markers
    ): Self = new Unchecked(underlying, markerState)

    override protected def newMethod(level: Level) = new UncheckedSLF4JMethod.Impl(level, this)

    override def onCondition(test: => Boolean): SLF4JLogger[UncheckedSLF4JMethod] =
      new Unchecked.Conditional(test, this)
  }

  /**
   * A logger that provides "strict" logging that only takes type class aware arguments.
   *
   * @param underlying the underlying logger.
   * @param markers the marker state to be applied to every method.
   */
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

  object Unchecked {

    /**
     * A conditional logger that only calls the method if test returns true.
     *
     * @param test a call by name boolean which must be true for calls to happen.
     * @param logger the logger to delegate calls to.
     */
    class Conditional(test: => Boolean, logger: ExtendedSLF4JLogger[UncheckedSLF4JMethod])
        extends SLF4JLogger.Delegate[UncheckedSLF4JMethod](logger) {
      override def onCondition(test2: => Boolean): SLF4JLogger[UncheckedSLF4JMethod] =
        new Conditional(test && test2, logger)

      override def withMarker[T: ToMarkers](markerInstance: T): SLF4JLogger[UncheckedSLF4JMethod] =
        new Conditional(
          test,
          logger.withMarker(markerInstance).asInstanceOf[ExtendedSLF4JLogger[Method]]
        )

      override protected def newMethod(level: Level): Method =
        new UncheckedSLF4JMethod.Conditional(level, test, logger)
    }
  }

  object Strict {

    /**
     * A conditional logger that only calls the method if test returns true.
     *
     * @param test a call by name boolean which must be true for calls to happen.
     * @param logger the logger to delegate calls to.
     */
    class Conditional(test: => Boolean, logger: ExtendedSLF4JLogger[StrictSLF4JMethod])
        extends SLF4JLogger.Delegate[StrictSLF4JMethod](logger) {
      override def onCondition(test2: => Boolean): SLF4JLogger[StrictSLF4JMethod] =
        new Conditional(test && test2, logger)

      override def withMarker[T: ToMarkers](markerInstance: T): SLF4JLogger[StrictSLF4JMethod] =
        new Conditional(
          test,
          logger.withMarker(markerInstance).asInstanceOf[ExtendedSLF4JLogger[Method]]
        )

      override protected def newMethod(level: Level): Method =
        new StrictSLF4JMethod.Conditional(level, test, logger)
    }
  }

}
