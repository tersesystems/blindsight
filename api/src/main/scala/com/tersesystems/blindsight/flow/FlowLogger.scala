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

package com.tersesystems.blindsight.flow

import com.tersesystems.blindsight.mixins._
import com.tersesystems.blindsight.slf4j.{ExtendedSLF4JLogger, SLF4JLoggerAPI, SLF4JPredicate}
import com.tersesystems.blindsight.{Markers, ParameterList, ToMarkers}
import org.slf4j.event.Level
import org.slf4j.event.Level._
import sourcecode.{Enclosing, File, Line}

/**
 * This trait implements a logger that is used for rendering entry/exit logging wrappers.
 *
 * The `FlowLoggerMethod` implements the bulk of the logic here, and is intended to be used with a
 * user-provided `FlowBehavior` that determines what statements and side effects happen on entry
 * and exit.
 *
 * If logging is enabled, then the execution is wrapped to capture the result or execution, and then
 * the result is returned or execution rethrown.  If the logging level is not enabled or logging
 * execution is denied by a filter, then execution of the block still proceeds but is not wrapped by a
 * `Try` block.
 */
trait FlowLogger
    extends SLF4JLoggerAPI[SLF4JPredicate, FlowMethod]
    with MarkerMixin
    with OnConditionMixin {
  override type Self      = FlowLogger
  override type Method    = FlowMethod
  override type Predicate = SLF4JPredicate
}

trait ExtendedFlowLogger
    extends FlowLogger
    with PredicateMixin[SLF4JPredicate]
    with ParameterListMixin
    with UnderlyingMixin
    with SourceInfoMixin

object FlowLogger {

  class Impl(logger: ExtendedSLF4JLogger[_]) extends ExtendedFlowLogger {
    override def withMarker[T: ToMarkers](markerInstance: T): Self = {
      new Impl(logger.withMarker(markerInstance).asInstanceOf[ExtendedSLF4JLogger[_]])
    }

    override def isTraceEnabled: Predicate = logger.predicate(TRACE)
    override def trace: Method             = new FlowMethod.Impl(TRACE, this)

    override def isDebugEnabled: Predicate = logger.predicate(DEBUG)
    override def debug: Method             = new FlowMethod.Impl(DEBUG, this)

    override def isInfoEnabled: Predicate = logger.predicate(INFO)
    override def info: Method             = new FlowMethod.Impl(INFO, this)

    override def isWarnEnabled: Predicate = logger.predicate(WARN)
    override def warn: Method             = new FlowMethod.Impl(WARN, this)

    override def isErrorEnabled: Predicate = logger.predicate(ERROR)
    override def error: Method             = new FlowMethod.Impl(ERROR, this)

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

    /**
     * Returns a new instance of the logger that will only log if the
     * condition is met.
     *
     * @param test the call by name boolean that is a prerequisite for logging.
     * @return the new conditional logger instance.
     */
    override def onCondition(test: => Boolean): FlowLogger = new Conditional(test, logger)
  }

  /**
   * Runs the conditional block with logging if test is true, otherwise runs the block.
   *
   * @param test the test to be run for logging.
   * @param logger the extended slf4j logger.
   */
  class Conditional(test: => Boolean, logger: ExtendedSLF4JLogger[_]) extends ExtendedFlowLogger {
    override def withMarker[T: ToMarkers](markerInstance: T): Self = {
      new Conditional(test, logger.withMarker(markerInstance).asInstanceOf[ExtendedSLF4JLogger[_]])
    }

    override def isTraceEnabled: Predicate = logger.predicate(TRACE)
    override def trace: Method             = new FlowMethod.Conditional(test, TRACE, this)

    override def isDebugEnabled: Predicate = logger.predicate(DEBUG)
    override def debug: Method             = new FlowMethod.Conditional(test, DEBUG, this)

    override def isInfoEnabled: Predicate = logger.predicate(INFO)
    override def info: Method             = new FlowMethod.Conditional(test, INFO, this)

    override def isWarnEnabled: Predicate = logger.predicate(WARN)
    override def warn: Method             = new FlowMethod.Conditional(test, WARN, this)

    override def isErrorEnabled: Predicate = logger.predicate(ERROR)
    override def error: Method             = new FlowMethod.Conditional(test, ERROR, this)

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

    /**
     * Returns a new instance of the logger that will only log if the
     * condition is met.
     *
     * @param test2 the call by name boolean that is a prerequisite for logging.
     * @return the new conditional logger instance.
     */
    override def onCondition(test2: => Boolean): FlowLogger = new Conditional(test && test2, logger)
  }

}
