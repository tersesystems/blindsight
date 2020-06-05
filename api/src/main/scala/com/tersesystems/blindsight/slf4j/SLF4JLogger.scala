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

import com.tersesystems.blindsight
import com.tersesystems.blindsight.fluent.FluentMethod
import com.tersesystems.blindsight.mixins._
import com.tersesystems.blindsight.{LoggerState, Markers, ParameterList, SimplePredicate, ToMarkers}
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
    extends SLF4JLoggerAPI[SimplePredicate, M]
    with MarkerMixin
    with UnderlyingMixin
    with OnConditionMixin {
  override type Self <: SLF4JLogger[M]
}

object SLF4JLogger {

  /**
   * A convenient abstract base class implementation.
   *
   * @tparam M the type of method.
   */
  abstract class Base[M: ClassTag](loggerState: LoggerState) extends SLF4JLogger[M] {
    override type Self      = SLF4JLogger[M]
    override type Method    = M
    override type Predicate = SimplePredicate

    override val underlying: Logger = loggerState.underlying

    /**
     * Returns the accumulated markers of this logger.
     *
     * @return the accumulated markers, may be Markers.empty.
     */
    override val markers: Markers = loggerState.markers

    override val isTraceEnabled: Predicate = newPredicate(Level.TRACE)
    override val trace: Method             = newMethod(Level.TRACE)

    override val isDebugEnabled: Predicate = newPredicate(Level.DEBUG)
    override val debug: Method             = newMethod(Level.DEBUG)

    override val isInfoEnabled: Predicate = newPredicate(Level.INFO)
    override val info: Method             = newMethod(Level.INFO)

    override val isWarnEnabled: Predicate = newPredicate(Level.WARN)
    override val warn: Method             = newMethod(Level.WARN)

    override val isErrorEnabled: Predicate = newPredicate(Level.ERROR)
    override val error: Method             = newMethod(Level.ERROR)

    private val predicates = Array(
      isErrorEnabled,
      isWarnEnabled,
      isInfoEnabled,
      isDebugEnabled,
      isTraceEnabled
    )

    override def withMarker[T: ToMarkers](markerInst: T): Self = {
      newInstance(loggerState.withMarker(markerInst))
    }

    override def onCondition(test: => Boolean): Self

    protected def newInstance(loggerState: LoggerState): Self
    protected def newMethod(level: Level): Method
    protected def newPredicate(level: Level): Predicate =
      new blindsight.SimplePredicate.Impl(level, loggerState)
  }

  /**
   * A logger that provides "strict" logging that only takes type class aware arguments.
   */
  class Strict(loggerState: LoggerState) extends SLF4JLogger.Base[StrictSLF4JMethod](loggerState) {
    override protected def newInstance(loggerState: LoggerState): Self = new Strict(loggerState)

    override protected def newMethod(level: Level): StrictSLF4JMethod =
      new StrictSLF4JMethod.Impl(level, loggerState)

    override def onCondition(test: => Boolean): SLF4JLogger[StrictSLF4JMethod] =
      new Strict.Conditional(loggerState.onCondition(test _))
  }

  object Strict {

    /**
     * A conditional logger that only calls the method if test returns true.
     */
    class Conditional(logger: LoggerState) extends SLF4JLogger.Base[StrictSLF4JMethod](logger) {
      override val isTraceEnabled: Predicate = newPredicate(Level.TRACE)
      override val trace: Method             = newMethod(Level.TRACE)

      override val isDebugEnabled: Predicate = newPredicate(Level.DEBUG)
      override val debug: Method             = newMethod(Level.DEBUG)

      override val isInfoEnabled: Predicate = newPredicate(Level.INFO)
      override val info: Method             = newMethod(Level.INFO)

      override val isWarnEnabled: Predicate = newPredicate(Level.WARN)
      override val warn: Method             = newMethod(Level.WARN)

      override val isErrorEnabled: Predicate = newPredicate(Level.ERROR)
      override val error: Method             = newMethod(Level.ERROR)

      override def onCondition(test2: => Boolean): SLF4JLogger[StrictSLF4JMethod] =
        new Conditional(logger.onCondition(test2 _))

      override def withMarker[T: ToMarkers](markerInstance: T): SLF4JLogger[StrictSLF4JMethod] =
        new Conditional(logger.withMarker(markerInstance))

      override protected def newMethod(level: Level): Method =
        new StrictSLF4JMethod.Conditional(level, logger)

      override protected def newInstance(
          loggerState: LoggerState
      ): SLF4JLogger[StrictSLF4JMethod] = {
        new Conditional(loggerState)
      }
    }
  }

  /**
   * A logger that provides "unchecked" logging that only takes type class aware arguments.
   *
   */
  class Unchecked(loggerState: LoggerState) extends SLF4JLogger[UncheckedSLF4JMethod] {
    override type Self      = SLF4JLogger[UncheckedSLF4JMethod]
    override type Method    = UncheckedSLF4JMethod
    override type Predicate = SimplePredicate

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

    override val isTraceEnabled: Predicate = predicate(TRACE)
    override val trace: Method             = newMethod(TRACE)

    override val isDebugEnabled: Predicate = predicate(DEBUG)
    override val debug: Method             = newMethod(DEBUG)

    override val isInfoEnabled: Predicate = predicate(INFO)
    override val info: Method             = newMethod(INFO)

    override val isWarnEnabled: Predicate = predicate(WARN)
    override val warn: Method             = newMethod(WARN)

    override val isErrorEnabled: Predicate = predicate(ERROR)
    override val error: Method             = newMethod(ERROR)

    protected def newMethod(level: Level): Method =
      new UncheckedSLF4JMethod.Impl(level, loggerState)

    private def predicate(level: Level): Predicate = loggerState.predicate(level)

    override def markers: Markers = loggerState.markers

    override def underlying: org.slf4j.Logger = loggerState.underlying

    override def onCondition(test: => Boolean): SLF4JLogger[UncheckedSLF4JMethod] =
      new Unchecked.Conditional(loggerState.onCondition(test _))

  }

  object Unchecked {

    /**
     * A conditional logger that only calls the method if test returns true.
     */
    class Conditional(logger: LoggerState) extends SLF4JLogger.Unchecked(logger) {

      override def onCondition(test2: => Boolean): SLF4JLogger[UncheckedSLF4JMethod] =
        new Conditional(logger.onCondition(test2 _))

      override def withMarker[T: ToMarkers](markerInstance: T): SLF4JLogger[UncheckedSLF4JMethod] =
        new Conditional(logger.withMarker(markerInstance))

      override protected def newMethod(level: Level): Method =
        new UncheckedSLF4JMethod.Conditional(level, logger)
    }
  }

}
