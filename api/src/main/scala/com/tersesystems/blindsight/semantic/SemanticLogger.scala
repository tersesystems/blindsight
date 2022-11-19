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

package com.tersesystems.blindsight.semantic

import com.tersesystems.blindsight.core.{CoreLogger, CorePredicate}
import com.tersesystems.blindsight.mixins._
import com.tersesystems.blindsight.{EventBuffer, _}
import org.slf4j.event.Level
import org.slf4j.event.Level._

/**
 * The semantic logger trait takes a statement as a type.  Types can be refined from the
 * general to the specific.
 *
 * {{{
 * val userEventLogger: SemanticLogger[UserEvent] = LoggerFactory.getLogger.semantic[UserEvent]
 * userEventLogger.info(UserLoggedInEvent("steve", "127.0.0.1"))
 * userEventLogger.info(UserLoggedOutEvent("steve", "timeout"))
 * }}}
 *
 * @tparam StatementType the type class instance of [[com.tersesystems.blindsight.ToStatement]].
 */
trait SemanticLogger[StatementType]
    extends SemanticLoggerAPI[StatementType, CorePredicate, SemanticMethod]
    with UnderlyingMixin
    with SemanticEventBufferMixin[StatementType]
    with SemanticEntryTransformMixin[StatementType]
    with SemanticMarkerMixin[StatementType]
    with SemanticRefineMixin[StatementType] {
  type Self[T] = SemanticLogger[T]

  def withCondition(condition: Condition): Self[StatementType]

  @deprecated("use withCondition", "1.4.0")
  def onCondition(condition: Condition): Self[StatementType] = withCondition(condition)
}

object SemanticLogger {

  class Impl[StatementType](protected val core: CoreLogger) extends SemanticLogger[StatementType] {

    override def underlying: org.slf4j.Logger = core.underlying

    override def markers: Markers = core.markers

    override def withMarker[T: ToMarkers](markerInst: => T): Self[StatementType] = {
      val markers = implicitly[ToMarkers[T]].toMarkers(markerInst)
      self(core.withMarker(markers))
    }

    override def refine[T <: StatementType: ToStatement: NotNothing]: Self[T] = self[T](core)

    override def withCondition(condition: Condition): Self[StatementType] =
      self(core.withCondition(condition))

    override def withEntryTransform(
        level: Level,
        f: Entry => Entry
    ): Self[StatementType] = self(core.withEntryTransform(level, f))

    override def withEventBuffer(buffer: EventBuffer): Self[StatementType] =
      self(core.withEventBuffer(buffer))

    override val isTraceEnabled: Predicate            = predicate(TRACE)
    override val trace: SemanticMethod[StatementType] = method(TRACE)

    override val isDebugEnabled: Predicate            = predicate(DEBUG)
    override val debug: SemanticMethod[StatementType] = method(DEBUG)

    override val isInfoEnabled: Predicate            = predicate(INFO)
    override val info: SemanticMethod[StatementType] = method(INFO)

    override val isWarnEnabled: Predicate            = predicate(WARN)
    override val warn: SemanticMethod[StatementType] = method(WARN)

    override val isErrorEnabled: Predicate            = predicate(ERROR)
    override val error: SemanticMethod[StatementType] = method(ERROR)

    protected def self[T: NotNothing](core: CoreLogger): Self[T] = new Impl(core)
    protected def predicate(level: Level): Predicate             = core.predicate(level)
    protected def method(level: Level): Method[StatementType] = {
      new SemanticMethod.Impl[StatementType](level, core)
    }

  }

}
