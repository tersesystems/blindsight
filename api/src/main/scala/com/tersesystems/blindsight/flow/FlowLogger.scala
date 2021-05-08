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

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.core.{CoreLogger, CorePredicate, CoreLoggerDefaults}
import com.tersesystems.blindsight.mixins._
import com.tersesystems.blindsight.slf4j._
import org.slf4j.event.Level

/**
 * This trait implements a logger that is used for rendering entry/exit logging wrappers.
 *
 * The [[FlowMethod]] implements the bulk of the logic here, and is intended to be used with a
 * user-provided [[FlowBehavior]] that determines what statements and side effects happen on entry
 * and exit.
 *
 * If logging is enabled, then the execution is wrapped to capture the result or execution, and then
 * the result is returned or execution rethrown.  If the logging level is not enabled or logging
 * execution is denied by a filter, then execution of the block still proceeds but is not wrapped by a
 * `Try` block.
 *
 * You should use `Condition.never` explicitly here to disable logging, as it will shortcut to a Noop
 * implementation.  Benchmarks show a noop flow takes 42ns to execute, 4.5ns if you remove sourcecode.Args
 * from the method signature.
 *
 * {{{
 * import com.tersesystems.blindsight._
 * import com.tersesystems.blindsight.flow._
 *
 * implicit def flowBehavior[B: ToArgument]: FlowBehavior[B] = new SimpleFlowBehavior
 * val logger = LoggerFactory.getLogger
 * val flowLogger: FlowLogger = logger.flow
 * val resultIsThree: Int = flowLogger.trace(1 + 2)
 * }}}
 */
trait FlowLogger
    extends SLF4JLoggerAPI[CorePredicate, FlowMethod]
    with UnderlyingMixin
    with MarkerMixin
    with EntryTransformMixin
    with EventBufferMixin
    with OnConditionMixin {
  override type Self      = FlowLogger
  override type Method    = FlowMethod
  override type Predicate = CorePredicate
}

object FlowLogger {

  abstract class Base(protected val core: CoreLogger)
      extends FlowLogger
      with CoreLoggerDefaults
      with LoggerMethodDefaults[FlowMethod] {
    override protected def predicate(level: Level): Predicate = core.predicate(level)
  }

  class Impl(core: CoreLogger) extends Base(core) {
    override def withCondition(condition: Condition): Self = {
      if (condition == Condition.never) {
        new Noop(core)
      } else {
        self(core.withCondition(condition))
      }
    }

    override protected def self(core: CoreLogger): Self = new Impl(core)
    override protected def method(level: Level): Method = new FlowMethod.Impl(level, core)
  }

  final class Noop(core: CoreLogger) extends Base(core) {
    override protected def self(core: CoreLogger): Self = new Noop(core)
    override protected def method(level: Level): Method = FlowMethod.Noop
  }

}
