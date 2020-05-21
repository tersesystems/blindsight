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
import com.tersesystems.blindsight.slf4j.{ExtendedSLF4JLogger, SLF4JLoggerAPI, SLF4JPredicate}
import com.tersesystems.blindsight.{Markers, ParameterList, ToMarkers}
import org.slf4j.Logger
import org.slf4j.event.Level
import org.slf4j.event.Level._
import sourcecode.{Enclosing, File, Line}

/**
 * The fluent logger trait.
 */
trait FluentLogger
    extends SLF4JLoggerAPI[SLF4JPredicate, FluentMethod]
    with MarkerMixin
    with OnConditionMixin {
  override type Self      = FluentLogger
  override type Method    = FluentMethod
  override type Predicate = SLF4JPredicate
}

/**
 * The implementation trait.
 */
trait ExtendedFluentLogger
    extends FluentLogger
    with PredicateMixin[SLF4JPredicate]
    with ParameterListMixin
    with UnderlyingMixin
    with SourceInfoMixin

object FluentLogger {

  class Impl(logger: ExtendedSLF4JLogger[_]) extends ExtendedFluentLogger {
    override def withMarker[T: ToMarkers](markerInstance: T): Self = {
      new Impl(logger.withMarker(markerInstance).asInstanceOf[ExtendedSLF4JLogger[_]])
    }

    override def onCondition(test: => Boolean): FluentLogger = {
      new Conditional(test, this)
    }

    override def isTraceEnabled: Predicate = logger.predicate(TRACE)
    override def trace: Method             = new FluentMethod.Impl(TRACE, this)

    override def isDebugEnabled: Predicate = logger.predicate(DEBUG)
    override def debug: Method             = new FluentMethod.Impl(DEBUG, this)

    override def isInfoEnabled: Predicate = logger.predicate(INFO)
    override def info: Method             = new FluentMethod.Impl(INFO, this)

    override def isWarnEnabled: Predicate = logger.predicate(WARN)
    override def warn: Method             = new FluentMethod.Impl(WARN, this)

    override def isErrorEnabled: Predicate = logger.predicate(ERROR)
    override def error: Method             = new FluentMethod.Impl(ERROR, this)

    override def parameterList(level: Level): ParameterList = logger.parameterList(level)

    override def predicate(level: Level): Predicate = logger.predicate(level)

    override def markers: Markers = logger.markers

    override def sourceInfoMarker(
        level: Level,
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Markers = logger.sourceInfoMarker(level, line, file, enclosing)

    override def underlying: org.slf4j.Logger = logger.underlying

  }

  class Conditional(test: => Boolean, logger: ExtendedFluentLogger) extends ExtendedFluentLogger {
    override type Self      = FluentLogger
    override type Method    = FluentMethod
    override type Predicate = SLF4JPredicate

    override def withMarker[T: ToMarkers](markerInstance: T): Self = {
      new Conditional(test, logger.withMarker(markerInstance).asInstanceOf[ExtendedFluentLogger])
    }

    override def onCondition(test2: => Boolean): Self = {
      new Conditional(test && test2, logger)
    }

    override def isTraceEnabled: Predicate = logger.isTraceEnabled
    override def trace: Method             = new FluentMethod.Conditional(Level.TRACE, test, logger)

    override def isDebugEnabled: Predicate = logger.isDebugEnabled
    override def debug: Method             = new FluentMethod.Conditional(Level.DEBUG, test, logger)

    override def isInfoEnabled: Predicate = logger.isInfoEnabled
    override def info: Method             = new FluentMethod.Conditional(Level.INFO, test, logger)

    override def isWarnEnabled: Predicate = logger.isWarnEnabled
    override def warn: Method             = new FluentMethod.Conditional(Level.WARN, test, logger)

    override def isErrorEnabled: Predicate = logger.isErrorEnabled
    override def error: Method             = new FluentMethod.Conditional(Level.ERROR, test, logger)

    override def markers: Markers = logger.markers

    override def sourceInfoMarker(
        level: Level,
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Markers = logger.sourceInfoMarker(level, line, file, enclosing)

    override def parameterList(level: Level): ParameterList = logger.parameterList(level)

    override def predicate(level: Level): SLF4JPredicate = logger.predicate(level)

    override def underlying: Logger = logger.underlying
  }
}
