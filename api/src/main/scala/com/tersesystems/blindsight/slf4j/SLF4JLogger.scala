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
import com.tersesystems.blindsight.{LoggerState, Markers, SimplePredicate, ToMarkers}
import org.slf4j.Logger
import org.slf4j.event.Level._

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

    override def onCondition(test: => Boolean): Self
  }

  object Strict {

    /**
     * A logger that provides "strict" logging that only takes type class aware arguments.
     */
    class Impl(loggerState: LoggerState) extends SLF4JLogger.Base[StrictSLF4JMethod](loggerState) {
      override val isTraceEnabled: Predicate = loggerState.predicate(TRACE)
      override val trace: Method =
        new StrictSLF4JMethod.Impl(TRACE, loggerState)

      override val isDebugEnabled: Predicate = loggerState.predicate(DEBUG)
      override val debug: Method =
        new StrictSLF4JMethod.Impl(DEBUG, loggerState)

      override val isInfoEnabled: Predicate = loggerState.predicate(INFO)
      override val info: Method             = new StrictSLF4JMethod.Impl(INFO, loggerState)

      override val isWarnEnabled: Predicate = loggerState.predicate(WARN)
      override val warn: Method             = new StrictSLF4JMethod.Impl(WARN, loggerState)

      override val isErrorEnabled: Predicate = loggerState.predicate(ERROR)
      override val error: Method =
        new StrictSLF4JMethod.Impl(ERROR, loggerState)

      override def withMarker[T: ToMarkers](markerInst: T): Self =
        new Impl(loggerState.withMarker(markerInst))

      override def onCondition(test: => Boolean): SLF4JLogger[StrictSLF4JMethod] =
        new Conditional(loggerState.onCondition(test _))
    }

    /**
     * A conditional logger that only calls the method if test returns true.
     */
    class Conditional(logger: LoggerState) extends SLF4JLogger.Base[StrictSLF4JMethod](logger) {
      override val isTraceEnabled: Predicate = logger.predicate(TRACE)
      override val trace: Method =
        new StrictSLF4JMethod.Conditional(TRACE, logger)

      override val isDebugEnabled: Predicate = logger.predicate(DEBUG)
      override val debug: Method =
        new StrictSLF4JMethod.Conditional(DEBUG, logger)

      override val isInfoEnabled: Predicate = logger.predicate(INFO)
      override val info: Method =
        new StrictSLF4JMethod.Conditional(INFO, logger)

      override val isWarnEnabled: Predicate = logger.predicate(WARN)
      override val warn: Method =
        new StrictSLF4JMethod.Conditional(WARN, logger)

      override val isErrorEnabled: Predicate = logger.predicate(ERROR)
      override val error: Method =
        new StrictSLF4JMethod.Conditional(ERROR, logger)

      override def onCondition(test2: => Boolean): SLF4JLogger[StrictSLF4JMethod] =
        new Conditional(logger.onCondition(test2 _))

      override def withMarker[T: ToMarkers](markerInstance: T): SLF4JLogger[StrictSLF4JMethod] =
        new Conditional(logger.withMarker(markerInstance))
    }
  }

  object Unchecked {

    /**
     * A logger that provides "unchecked" logging that only takes type class aware arguments.
     *
      */
    class Impl(loggerState: LoggerState) extends SLF4JLogger[UncheckedSLF4JMethod] {
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
        new Impl(loggerState.withMarker(instance))
      }

      override val isTraceEnabled: Predicate = loggerState.predicate(TRACE)
      override val trace: Method =
        new UncheckedSLF4JMethod.Impl(TRACE, loggerState)

      override val isDebugEnabled: Predicate = loggerState.predicate(DEBUG)
      override val debug: Method =
        new UncheckedSLF4JMethod.Impl(DEBUG, loggerState)

      override val isInfoEnabled: Predicate = loggerState.predicate(INFO)
      override val info: Method =
        new UncheckedSLF4JMethod.Impl(INFO, loggerState)

      override val isWarnEnabled: Predicate = loggerState.predicate(WARN)
      override val warn: Method =
        new UncheckedSLF4JMethod.Impl(WARN, loggerState)

      override val isErrorEnabled: Predicate = loggerState.predicate(ERROR)
      override val error: Method =
        new UncheckedSLF4JMethod.Impl(ERROR, loggerState)

      override def markers: Markers = loggerState.markers

      override def underlying: org.slf4j.Logger = loggerState.underlying

      override def onCondition(test: => Boolean): SLF4JLogger[UncheckedSLF4JMethod] =
        new Conditional(loggerState.onCondition(test _))

    }

    /**
     * A conditional logger that only calls the method if test returns true.
     */
    class Conditional(loggerState: LoggerState) extends Impl(loggerState) {

      override def onCondition(test2: => Boolean): SLF4JLogger[UncheckedSLF4JMethod] =
        new Conditional(loggerState.onCondition(test2 _))

      override def withMarker[T: ToMarkers](markerInstance: T): SLF4JLogger[UncheckedSLF4JMethod] =
        new Conditional(loggerState.withMarker(markerInstance))

      override val trace: Method =
        new UncheckedSLF4JMethod.Conditional(TRACE, loggerState)
      override val debug: Method =
        new UncheckedSLF4JMethod.Conditional(DEBUG, loggerState)
      override val info: Method =
        new UncheckedSLF4JMethod.Conditional(INFO, loggerState)
      override val warn: Method =
        new UncheckedSLF4JMethod.Conditional(WARN, loggerState)
      override val error: Method =
        new UncheckedSLF4JMethod.Conditional(ERROR, loggerState)
    }
  }

}
