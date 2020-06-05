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

package com.tersesystems.blindsight

import com.tersesystems.blindsight.flow.FlowLogger
import com.tersesystems.blindsight.fluent.FluentLogger
import com.tersesystems.blindsight.semantic.SemanticLogger
import com.tersesystems.blindsight.slf4j._

/**
 * The Blindsight logger trait.
 */
trait Logger extends SLF4JLogger[StrictSLF4JMethod] {
  override type Self = Logger

  def fluent: FluentLogger

  def flow: FlowLogger

  def strict: SLF4JLogger[StrictSLF4JMethod]

  def unchecked: SLF4JLogger[UncheckedSLF4JMethod]

  def semantic[MessageType: NotNothing]: SemanticLogger[MessageType]
}

object Logger {

  class Impl(loggerState: LoggerState)
      extends Logger
      with SLF4JLoggerAPI.Proxy[SLF4JPredicate, StrictSLF4JMethod] {

    override type Parent = ExtendedSLF4JLogger[StrictSLF4JMethod]
    override type Self   = Logger

    override protected val logger = new SLF4JLogger.Strict(loggerState)

    override def strict: SLF4JLogger[StrictSLF4JMethod] = logger

    override lazy val unchecked: SLF4JLogger[UncheckedSLF4JMethod] = {
      new SLF4JLogger.Unchecked(loggerState)
    }

    override lazy val flow: FlowLogger = {
      new FlowLogger.Impl(loggerState)
    }

    override lazy val fluent: FluentLogger = {
      new FluentLogger.Impl(loggerState)
    }

    override def semantic[StatementType: NotNothing]: SemanticLogger[StatementType] = {
      new SemanticLogger.Impl[StatementType](loggerState)
    }

    override def onCondition(test: => Boolean): Self = {
      new Impl(loggerState.onCondition(test _))
    }

    override def withMarker[T: ToMarkers](markerInstance: T): Self = {
      new Impl(loggerState.withMarker(markers))
    }

    override def markers: Markers = loggerState.markers

    override def underlying: org.slf4j.Logger = loggerState.underlying
  }

}
