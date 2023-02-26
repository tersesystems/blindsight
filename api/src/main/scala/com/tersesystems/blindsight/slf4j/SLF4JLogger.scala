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

package com.tersesystems.blindsight.slf4j

import com.tersesystems.blindsight.core._
import com.tersesystems.blindsight.mixins._
import org.slf4j.event.Level
import org.slf4j.event.Level._

/**
 * Public SLF4J Logger interface.  This is intended for the end user.
 *
 * {{{
 * val markers = Markers(bobj("key" -> "value"))
 * val message = "message arg1={} arg2={} arg3={}"
 * val arguments: Arguments = Arguments("arg1", 42, true)
 * val e = new RuntimeException("whoops")
 * logger.info(markers, message, arguments, e);
 * }}}
 */
trait SLF4JLogger
    extends SLF4JLoggerAPI[CorePredicate, StrictSLF4JMethod]
    with MarkerMixin
    with UnderlyingMixin
    with EntryTransformMixin
    with EventBufferMixin
    with ConditionMixin
    with OnConditionMixin {
  override type Self <: SLF4JLogger
}

/**
 * Provides a "Logger API" experience
 */
trait LoggerMethodDefaults[M] extends SLF4JLoggerAPI[CorePredicate, M] {
  val isTraceEnabled: Predicate = predicate(TRACE)
  val trace: Method             = method(TRACE)

  val isDebugEnabled: Predicate = predicate(DEBUG)
  val debug: Method             = method(DEBUG)

  val isInfoEnabled: Predicate = predicate(INFO)
  val info: Method             = method(INFO)

  val isWarnEnabled: Predicate = predicate(WARN)
  val warn: Method             = method(WARN)

  val isErrorEnabled: Predicate = predicate(ERROR)
  val error: Method             = method(ERROR)

  protected def predicate(level: Level): Predicate
  protected def method(level: Level): Method
}

object SLF4JLogger {

  /**
   * A logger that provides "strict" logging that only takes type class aware arguments.
   */
  class Strict(val core: CoreLogger)
      extends SLF4JLogger
      with CoreLoggerDefaults
      with LoggerMethodDefaults[StrictSLF4JMethod] {
    override type Self      = SLF4JLogger
    override type Method    = StrictSLF4JMethod
    override type Predicate = CorePredicate

    override protected def predicate(level: Level): Predicate = core.predicate(level)
    override def method(level: Level): Method           = new StrictSLF4JMethod.Impl(level, core)
    override protected def self(core: CoreLogger): Self = new Strict(core)
  }

}
