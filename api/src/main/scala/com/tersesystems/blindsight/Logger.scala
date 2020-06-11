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

  final class Impl(core: CoreLogger) extends Logger {
    override type Predicate = SimplePredicate
    override type Method    = StrictSLF4JMethod

    override val strict: SLF4JLogger[StrictSLF4JMethod] = new SLF4JLogger.Strict(core)

    override lazy val unchecked: SLF4JLogger[UncheckedSLF4JMethod] = new SLF4JLogger.Unchecked(core)

    override lazy val flow: FlowLogger = new FlowLogger.Impl(core)

    override lazy val fluent: FluentLogger = new FluentLogger.Impl(core)

    override def semantic[StatementType: NotNothing]: SemanticLogger[StatementType] =
      new SemanticLogger.Impl[StatementType](core)

    override def onCondition(condition: Condition): Self =
      new Impl(core.onCondition(condition))

    override def withMarker[T: ToMarkers](markerInstance: T): Self =
      new Impl(core.withMarker(markerInstance))

    override def markers: Markers = core.markers

    override def underlying: org.slf4j.Logger = core.underlying

    override def isTraceEnabled: Predicate = strict.isTraceEnabled
    override def trace: Method             = strict.trace

    override def isDebugEnabled: Predicate = strict.isDebugEnabled
    override def debug: Method             = strict.debug

    override def isInfoEnabled: Predicate = strict.isInfoEnabled
    override def info: Method             = strict.info

    override def isWarnEnabled: Predicate = strict.isWarnEnabled
    override def warn: Method             = strict.warn

    override def isErrorEnabled: Predicate = strict.isErrorEnabled
    override def error: Method             = strict.error
  }

}
