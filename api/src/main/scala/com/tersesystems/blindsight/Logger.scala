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

import com.tersesystems.blindsight.core.{CoreLogger, CorePredicate}
import com.tersesystems.blindsight.flow.FlowLogger
import com.tersesystems.blindsight.fluent.FluentLogger
import com.tersesystems.blindsight.semantic.SemanticLogger
import com.tersesystems.blindsight.slf4j._
import org.slf4j.event.Level

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

  class Impl(core: CoreLogger)
      extends Logger
      with SLF4JLoggerAPI.Proxy[CorePredicate, StrictSLF4JMethod] {

    override type Parent = SLF4JLogger[StrictSLF4JMethod]
    override type Self   = Logger

    override protected val logger = new SLF4JLogger.Strict(core)

    override def strict: SLF4JLogger[StrictSLF4JMethod] = logger

    override def markers: Markers = core.markers

    override def underlying: org.slf4j.Logger = core.underlying

    override def entries: Option[EntryBuffer] = core.entries

    override lazy val unchecked: SLF4JLogger[UncheckedSLF4JMethod] = {
      new SLF4JLogger.Unchecked(core)
    }

    override lazy val flow: FlowLogger = {
      new FlowLogger.Impl(core)
    }

    override lazy val fluent: FluentLogger = {
      new FluentLogger.Impl(core)
    }

    override def semantic[StatementType: NotNothing]: SemanticLogger[StatementType] = {
      new SemanticLogger.Impl[StatementType](core)
    }

    override def onCondition(condition: Condition): Self = {
      new Impl(core.onCondition(condition))
    }

    override def withMarker[T: ToMarkers](markerInstance: T): Self = {
      new Impl(core.withMarker(markerInstance))
    }

    override def withEntryTransform(
        level: Level,
        f: Entry => Entry
    ): Logger = {
      new Impl(core.withEntryTransform(level, f))
    }

    override def withEntryTransform(f: Entry => Entry): Logger =
      new Impl(core.withEntryTransform(f))

    override def withEntryBuffer(buffer: EntryBuffer): Logger =
      new Impl(core.withEntryBuffer(buffer))
  }

}
