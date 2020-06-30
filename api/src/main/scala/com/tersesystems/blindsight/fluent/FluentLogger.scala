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

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.mixins._
import com.tersesystems.blindsight.slf4j._
import org.slf4j.event.Level
import org.slf4j.event.Level._

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
    with UnderlyingMixin
    with TransformLogEntryMixin
    with OnConditionMixin {
  override type Self      = FluentLogger
  override type Method    = FluentMethod
  override type Predicate = SimplePredicate
}

object FluentLogger {

  class Impl(core: CoreLogger) extends FluentLogger {

    override def markers: Markers = core.markers

    override def underlying: org.slf4j.Logger = core.underlying

    override val isTraceEnabled: Predicate = core.predicate(TRACE)
    override val trace: Method             = new FluentMethod.Impl(TRACE, core)

    override val isDebugEnabled: Predicate = core.predicate(DEBUG)
    override val debug: Method             = new FluentMethod.Impl(DEBUG, core)

    override val isInfoEnabled: Predicate = core.predicate(INFO)
    override val info: Method             = new FluentMethod.Impl(INFO, core)

    override val isWarnEnabled: Predicate = core.predicate(WARN)
    override val warn: Method             = new FluentMethod.Impl(WARN, core)

    override val isErrorEnabled: Predicate = core.predicate(ERROR)
    override val error: Method             = new FluentMethod.Impl(ERROR, core)

    override def withMarker[T: ToMarkers](markerInstance: T): Self = {
      new Impl(core.withMarker(markerInstance))
    }

    override def onCondition(condition: Condition): FluentLogger = {
      new Impl(core.onCondition(condition))
    }

    override def withTransform(
        level: Level,
        f: LogEntry => LogEntry
    ): FluentLogger = {
      new Impl(core.withTransform(level, f))
    }
  }
}
