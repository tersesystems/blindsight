/*
 * Copyright 2020 com.tersesystems.blindsight
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

import com.tersesystems.blindsight.core.{CoreLogger, CorePredicate, CoreLoggerDefaults}
import com.tersesystems.blindsight.flow.FlowLogger
import com.tersesystems.blindsight.fluent.FluentLogger
import com.tersesystems.blindsight.semantic.SemanticLogger
import com.tersesystems.blindsight.slf4j._

/**
 * The Blindsight logger trait.
 */
trait Logger extends SLF4JLogger {
  override type Self = Logger

  def fluent: FluentLogger

  def flow: FlowLogger

  def strict: SLF4JLogger

  def semantic[MessageType: NotNothing]: SemanticLogger[MessageType]
}

object Logger {

  class Impl(protected val core: CoreLogger)
      extends Logger
      with SLF4JLoggerAPI.Proxy[CorePredicate, StrictSLF4JMethod]
      with CoreLoggerDefaults {

    override type Parent = SLF4JLogger
    override type Self   = Logger

    // from Proxy API
    override protected val logger = new SLF4JLogger.Strict(core)

    override def strict: SLF4JLogger = logger

    override def markers: Markers = core.markers

    override def underlying: org.slf4j.Logger = core.underlying

    override lazy val flow: FlowLogger = {
      new FlowLogger.Impl(core)
    }

    override lazy val fluent: FluentLogger = {
      new FluentLogger.Impl(core)
    }

    override def semantic[StatementType: NotNothing]: SemanticLogger[StatementType] = {
      new SemanticLogger.Impl[StatementType](core)
    }

    override protected def self(core: CoreLogger): Self = new Impl(core)
  }
}
