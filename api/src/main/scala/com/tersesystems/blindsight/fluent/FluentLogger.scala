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

package com.tersesystems.blindsight.fluent

import com.tersesystems.blindsight.mixins._
import com.tersesystems.blindsight.slf4j._
import com.tersesystems.blindsight._
import org.slf4j.Logger
import org.slf4j.event.Level
import org.slf4j.event.Level._
import sourcecode.{Enclosing, File, Line}

/**
 * The fluent logger trait.
 *
 * {{{
 * val fluentLogger: FluentLogger = LoggerFactory.getLogger.fluent
 * fluentLogger.info.message("I am a fluent logger").log()
 * }}}
 */
trait FluentLogger
    extends SLF4JLoggerAPI[SimplePredicate, FluentMethod]
    with MarkerMixin
    with OnConditionMixin {
  override type Self      = FluentLogger
  override type Method    = FluentMethod
  override type Predicate = SimplePredicate
}

/**
 * The implementation trait.
 */
trait ExtendedFluentLogger
    extends FluentLogger
    with PredicateMixin[SimplePredicate]
    with UnderlyingMixin
    with SourceInfoMixin

object FluentLogger {

  class Impl(logger: LoggerState) extends ExtendedFluentLogger with SourceInfoMixin {
    override def withMarker[T: ToMarkers](markerInstance: T): Self = {
      new Impl(logger.withMarker(markerInstance))
    }

    override def onCondition(test: => Boolean): FluentLogger = {
      new Conditional(logger.onCondition(test _))
    }

    override val isTraceEnabled: Predicate = predicate(TRACE)
    override val trace: Method             = new FluentMethod.Impl(TRACE, logger)

    override val isDebugEnabled: Predicate = predicate(DEBUG)
    override val debug: Method             = new FluentMethod.Impl(DEBUG, logger)

    override val isInfoEnabled: Predicate = predicate(INFO)
    override val info: Method             = new FluentMethod.Impl(INFO, logger)

    override val isWarnEnabled: Predicate = predicate(WARN)
    override val warn: Method             = new FluentMethod.Impl(WARN, logger)

    override val isErrorEnabled: Predicate = predicate(ERROR)
    override val error: Method             = new FluentMethod.Impl(ERROR, logger)

    override def predicate(level: Level): Predicate = logger.predicate(level)

    override def markers: Markers = logger.markers

    override def underlying: org.slf4j.Logger = logger.underlying

  }

  class Conditional(logger: LoggerState) extends FluentLogger {
    override type Self      = FluentLogger
    override type Method    = FluentMethod
    override type Predicate = SimplePredicate

    override def withMarker[T: ToMarkers](markerInstance: T): Self = {
      new Conditional(logger.withMarker(markerInstance))
    }

    override def onCondition(test2: => Boolean): Self = {
      new Conditional(logger.onCondition(test2 _))
    }

    override def isTraceEnabled: Predicate = logger.predicate(TRACE)
    override def trace: Method             = new FluentMethod.Conditional(TRACE, logger)

    override def isDebugEnabled: Predicate = logger.predicate(DEBUG)
    override def debug: Method             = new FluentMethod.Conditional(DEBUG, logger)

    override def isInfoEnabled: Predicate = logger.predicate(INFO)
    override def info: Method             = new FluentMethod.Conditional(INFO, logger)

    override def isWarnEnabled: Predicate = logger.predicate(WARN)
    override def warn: Method             = new FluentMethod.Conditional(WARN, logger)

    override def isErrorEnabled: Predicate = logger.predicate(ERROR)
    override def error: Method             = new FluentMethod.Conditional(ERROR, logger)

    override def markers: Markers = logger.markers
  }
}
