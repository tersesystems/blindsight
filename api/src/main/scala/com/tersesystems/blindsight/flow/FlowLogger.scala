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

import com.tersesystems.blindsight.mixins.{EntryBufferMixin, _}
import com.tersesystems.blindsight.slf4j._
import com.tersesystems.blindsight._
import org.slf4j
import org.slf4j.event.Level
import org.slf4j.event.Level._

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
    extends SLF4JLoggerAPI[SimplePredicate, FlowMethod]
    with UnderlyingMixin
    with MarkerMixin
    with EntryTransformMixin
    with EntryBufferMixin
    with OnConditionMixin {
  override type Self      = FlowLogger
  override type Method    = FlowMethod
  override type Predicate = SimplePredicate
}

object FlowLogger {

  class Impl(core: CoreLogger) extends FlowLogger {
    override val isTraceEnabled: Predicate = core.predicate(TRACE)
    override val trace: Method             = new FlowMethod.Impl(TRACE, core)

    override val isDebugEnabled: Predicate = core.predicate(DEBUG)
    override val debug: Method             = new FlowMethod.Impl(DEBUG, core)

    override val isInfoEnabled: Predicate = core.predicate(INFO)
    override val info: Method             = new FlowMethod.Impl(INFO, core)

    override val isWarnEnabled: Predicate = core.predicate(WARN)
    override val warn: Method             = new FlowMethod.Impl(WARN, core)

    override val isErrorEnabled: Predicate = core.predicate(ERROR)
    override val error: Method             = new FlowMethod.Impl(ERROR, core)

    override def markers: Markers = core.markers

    override def entries: Option[EntryBuffer] = core.entries

    override def underlying: org.slf4j.Logger = core.underlying

    /**
     * Returns a new instance of the logger that will only log if the
     * condition is met.
     *
     * @return the new conditional logger instance.
     */
    override def onCondition(condition: Condition): FlowLogger = {
      if (condition == Condition.never) {
        new Noop(core)
      } else {
        new Impl(core.onCondition(condition))
      }
    }

    override def withMarker[T: ToMarkers](markerInstance: T): Self = {
      new Impl(core.withMarker(markerInstance))
    }

    override def withEntryTransform(
        level: Level,
        f: Entry => Entry
    ): Self = {
      new Impl(core.withEntryTransform(level, f))
    }

    override def withEntryTransform(f: Entry => Entry): Self = new Impl(core.withEntryTransform(f))

    override def withEntryBuffer(buffer: EntryBuffer): Self = new Impl(core.withEntryBuffer(buffer))
  }

  final class Noop(core: CoreLogger) extends FlowLogger {
    override val isTraceEnabled: Predicate = core.predicate(TRACE)
    override val trace: Method             = FlowMethod.Noop

    override val isDebugEnabled: Predicate = core.predicate(DEBUG)
    override val debug: Method             = FlowMethod.Noop

    override val isInfoEnabled: Predicate = core.predicate(INFO)
    override val info: Method             = FlowMethod.Noop

    override val isWarnEnabled: Predicate = core.predicate(WARN)
    override val warn: Method             = FlowMethod.Noop

    override val isErrorEnabled: Predicate = core.predicate(ERROR)
    override val error: Method             = FlowMethod.Noop

    override def markers: Markers = core.markers

    override def underlying: slf4j.Logger = core.underlying

    override def entries: Option[EntryBuffer] = core.entries

    override def onCondition(condition: Condition): FlowLogger = this // XXX is this right?

    override def withEntryBuffer(buffer: EntryBuffer): FlowLogger = this // XXX is this right?

    override def withMarker[T: ToMarkers](instance: T): FlowLogger = this // XXX is this right?

    override def withEntryTransform(level: Level, f: Entry => Entry): Self =
      this // XXX is this right?

    override def withEntryTransform(f: Entry => Entry): FlowLogger = this
  }

}
