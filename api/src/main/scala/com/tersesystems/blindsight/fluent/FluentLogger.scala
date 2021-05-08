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

import com.tersesystems.blindsight.core.{CoreLogger, CorePredicate, CoreLoggerDefaults}
import com.tersesystems.blindsight.mixins.{EventBufferMixin, _}
import com.tersesystems.blindsight.slf4j._
import org.slf4j.event.Level

/**
 * The fluent logger trait.
 *
 * {{{
 * val fluentLogger: FluentLogger = LoggerFactory.getLogger.fluent
 * fluentLogger.info.message("I am a fluent logger").log()
 * }}}
 */
trait FluentLogger
    extends SLF4JLoggerAPI[CorePredicate, FluentMethod]
    with MarkerMixin
    with UnderlyingMixin
    with EntryTransformMixin
    with EventBufferMixin
    with OnConditionMixin {
  override type Self      = FluentLogger
  override type Method    = FluentMethod
  override type Predicate = CorePredicate
}

object FluentLogger {

  abstract class Base(protected val core: CoreLogger) extends FluentLogger
    with CoreLoggerDefaults
    with LoggerMethodDefaults[FluentMethod] {
    override protected def predicate(level: Level): Predicate = core.predicate(level)
  }

  class Impl(core: CoreLogger) extends Base(core)  {
    override protected def method(level: Level): Method = new FluentMethod.Impl(level, core)
    override protected def self(core: CoreLogger): Self = new Impl(core)
  }
}
