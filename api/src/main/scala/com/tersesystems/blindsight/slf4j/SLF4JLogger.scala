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

package com.tersesystems.blindsight.slf4j

import com.tersesystems.blindsight.mixins.{EntryBufferMixin, _}
import com.tersesystems.blindsight._
import org.slf4j.Logger
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
 *
 * @tparam M the type of method.
 */
trait SLF4JLogger[M]
    extends SLF4JLoggerAPI[SimplePredicate, M]
    with MarkerMixin
    with UnderlyingMixin
    with EntryTransformMixin
    with EntryBufferMixin
    with OnConditionMixin {
  override type Self <: SLF4JLogger[M]
}

object SLF4JLogger {

  /**
   * A convenient abstract base class implementation.
   *
   * @tparam M the type of method.
   */
  abstract class Base[M](core: CoreLogger) extends SLF4JLogger[M] {
    override type Self      = SLF4JLogger[M]
    override type Method    = M
    override type Predicate = SimplePredicate

    override val underlying: Logger = core.underlying

    /**
     * Returns the accumulated markers of this logger.
     *
     * @return the accumulated markers, may be Markers.empty.
     */
    override val markers: Markers = core.markers

    override def entries: Option[EntryBuffer] = core.entries

    override val isTraceEnabled: Predicate = core.predicate(TRACE)
    override val isDebugEnabled: Predicate = core.predicate(DEBUG)
    override val isInfoEnabled: Predicate  = core.predicate(INFO)
    override val isWarnEnabled: Predicate  = core.predicate(WARN)
    override val isErrorEnabled: Predicate = core.predicate(ERROR)
  }

  /**
   * A logger that provides "strict" logging that only takes type class aware arguments.
   */
  class Strict(core: CoreLogger) extends SLF4JLogger.Base[StrictSLF4JMethod](core) {
    override val trace: Method = new StrictSLF4JMethod.Impl(TRACE, core)
    override val debug: Method = new StrictSLF4JMethod.Impl(DEBUG, core)
    override val info: Method  = new StrictSLF4JMethod.Impl(INFO, core)
    override val warn: Method  = new StrictSLF4JMethod.Impl(WARN, core)
    override val error: Method = new StrictSLF4JMethod.Impl(ERROR, core)

    override def withMarker[T: ToMarkers](markerInst: T): Self =
      new Strict(core.withMarker(markerInst))

    override def onCondition(condition: Condition): Self =
      new Strict(core.onCondition(condition))

    override def withTransform(level: Level, f: Entry => Entry): Self =
      new Strict(core.withTransform(level, f))

    override def withTransform(f: Entry => Entry): Self =
      new Strict(core.withTransform(f))

    override def withEntryBuffer(buffer: EntryBuffer): Self =
      new Strict(core.withEntryBuffer(buffer))
  }

  /**
   * A logger that provides "unchecked" logging that only takes type class aware arguments.
   */
  class Unchecked(core: CoreLogger) extends SLF4JLogger.Base[UncheckedSLF4JMethod](core) {
    override val trace: Method = new UncheckedSLF4JMethod.Impl(TRACE, core)
    override val debug: Method = new UncheckedSLF4JMethod.Impl(DEBUG, core)
    override val info: Method  = new UncheckedSLF4JMethod.Impl(INFO, core)
    override val warn: Method  = new UncheckedSLF4JMethod.Impl(WARN, core)
    override val error: Method = new UncheckedSLF4JMethod.Impl(ERROR, core)

    /**
     * Returns a logger which will always render with the given marker.
     *
     * @param instance a type class instance of [[ToMarkers]]
     * @tparam T the instance type.
     * @return a new instance of the logger that has this marker.
     */
    override def withMarker[T: ToMarkers](instance: T): Self =
      new Unchecked(core.withMarker(instance))

    override def onCondition(condition: Condition): Self =
      new Unchecked(core.onCondition(condition))

    override def withTransform(
        level: Level,
        transform: Entry => Entry
    ): SLF4JLogger[UncheckedSLF4JMethod] =
      new Unchecked(core.withTransform(level, transform))

    override def withTransform(f: Entry => Entry): Self =
      new Unchecked(core.withTransform(f))

    override def withEntryBuffer(buffer: EntryBuffer): Self =
      new Unchecked(core.withEntryBuffer(buffer))
  }

}
