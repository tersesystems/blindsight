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

import com.tersesystems.blindsight.fluent.FluentMethod
import com.tersesystems.blindsight.mixins._
import com.tersesystems.blindsight.{LoggerState, Markers, ParameterList, ToMarkers}
import org.slf4j.{Logger, Marker}
import org.slf4j.event.Level
import org.slf4j.event.Level.{DEBUG, ERROR, INFO, TRACE, WARN}
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
    with PredicateMixin[SLF4JPredicate]

object SLF4JLogger {

  /**
   * A convenient abstract base class implementation.
   *
   * @tparam M the type of method.
   */
  abstract class Base[M: ClassTag](loggerState: LoggerState) extends ExtendedSLF4JLogger[M] {
    override type Self      = SLF4JLogger[M]
    override type Method    = M
    override type Predicate = SLF4JPredicate

    protected val parameterLists: Seq[ParameterList] = ParameterList.lists(this.underlying)

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
  }

  /**
   * A logger that provides "strict" logging that only takes type class aware arguments.
   */
  class Strict(loggerState: LoggerState) extends SLF4JLogger.Base[StrictSLF4JMethod](loggerState) {
    override protected def newInstance(
        underlying: org.slf4j.Logger,
        markerState: Markers
    ): Self = new Strict(loggerState)

    override protected def newMethod(level: Level): StrictSLF4JMethod =
      new StrictSLF4JMethod.Impl(level, this)

    override def onCondition(test: => Boolean): SLF4JLogger[StrictSLF4JMethod] =
      new Strict.Conditional(test, this)

    /**
     * Returns the accumulated markers of this logger.
     *
     * @return the accumulated markers, may be Markers.empty.
     */
    override def markers: Markers = loggerState.markers

    override def underlying: Logger = loggerState.underlying
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

      override def parameterList(level: Level): ParameterList =
        new ParameterList.Conditional(test, logger.parameterList(level))

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

  /**
   * A logger that provides "unchecked" logging that only takes type class aware arguments.
   *
   */
  class Unchecked(loggerState: LoggerState) extends ExtendedSLF4JLogger[UncheckedSLF4JMethod] {
    override type Self      = SLF4JLogger[UncheckedSLF4JMethod]
    override type Method    = UncheckedSLF4JMethod
    override type Predicate = SLF4JPredicate

    /**
     * Returns a logger which will always render with the given marker.
     *
     * @param instance a type class instance of [[ToMarkers]]
     * @tparam T the instance type.
     * @return a new instance of the logger that has this marker.
     */
    override def withMarker[T: ToMarkers](instance: T): SLF4JLogger[UncheckedSLF4JMethod] = {
      new Unchecked(loggerState.withMarker(instance))
    }

    override def isTraceEnabled: Predicate = predicate(TRACE)
    override def trace: Method             = new UncheckedSLF4JMethod.Impl(TRACE, this)

    override def isDebugEnabled: Predicate = predicate(DEBUG)
    override def debug: Method             = new UncheckedSLF4JMethod.Impl(DEBUG, this)

    override def isInfoEnabled: Predicate = predicate(INFO)
    override def info: Method             = new UncheckedSLF4JMethod.Impl(INFO, this)

    override def isWarnEnabled: Predicate = predicate(WARN)
    override def warn: Method             = new UncheckedSLF4JMethod.Impl(WARN, this)

    override def isErrorEnabled: Predicate = predicate(ERROR)
    override def error: Method             = new UncheckedSLF4JMethod.Impl(ERROR, this)

    def parameterList(level: Level): ParameterList = logger.parameterList(level)

    override def predicate(level: Level): Predicate = logger.predicate(level)

    override def markers: Markers = logger.markers

    override def sourceInfoMarker(
        level: Level,
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Markers = logger.sourceInfoMarker(level, line, file, enclosing)

    override def underlying: org.slf4j.Logger = logger.underlying

    override def onCondition(test: => Boolean): SLF4JLogger[UncheckedSLF4JMethod] =
      new Unchecked.Conditional(test, this)
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

      override def parameterList(level: Level): ParameterList =
        new ParameterList.Conditional(test, logger.parameterList(level))

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

}
