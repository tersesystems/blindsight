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

import com.tersesystems.blindsight.api.{Markers, ParameterList, ToMarkers}
import com.tersesystems.blindsight.api.mixins.{
  MarkerMixin,
  ParameterListMixin,
  PredicateMixin,
  SourceInfoMixin,
  UnderlyingMixin
}
import com.tersesystems.blindsight.slf4j.{ExtendedSLF4JLogger, SLF4JLoggerAPI, SLF4JPredicate}
import org.slf4j.event.Level
import org.slf4j.event.Level.{DEBUG, ERROR, INFO, TRACE, WARN}
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
 *
 * Because this logger executes blocks of computation and may optionally decorate it with logging,
 * it does <b>not</b> implement the `OnConditionMixin` and should not be used with conditional
 * logging logic.  If conditional logging is required, it is generally safer to do it in the logging
 * framework by using a deny filter with a marker defined in the FlowBehavior.
 */
trait FlowLogger extends SLF4JLoggerAPI[SLF4JPredicate, FlowMethod] with MarkerMixin {
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
  }

}
