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

package com.tersesystems.blindsight.semantic

import com.tersesystems.blindsight.{EntryBuffer, _}
import com.tersesystems.blindsight.mixins._
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
    extends SemanticLoggerAPI[StatementType, SimplePredicate, SemanticMethod]
    with UnderlyingMixin
    with SemanticEntryBufferMixin[StatementType]
    with SemanticTransformStatementMixin[StatementType]
    with SemanticMarkerMixin[StatementType]
    with SemanticRefineMixin[StatementType] {
  type Self[T] = SemanticLogger[T]

  def onCondition(condition: Condition): Self[StatementType]
}

object SemanticLogger {

  class Impl[StatementType](protected val core: CoreLogger) extends SemanticLogger[StatementType] {

    override def underlying: org.slf4j.Logger = core.underlying

    override def markers: Markers = core.markers

    override def entries: Option[EntryBuffer] = core.entries

    override def withMarker[T: ToMarkers](markerInst: => T): Self[StatementType] = {
      val markers = implicitly[ToMarkers[T]].toMarkers(markerInst)
      new Impl[StatementType](core.withMarker(markers))
    }

    override def refine[T <: StatementType: ToStatement: NotNothing]: Self[T] = new Impl[T](core)

    override def onCondition(condition: Condition): Self[StatementType] = {
      new Impl[StatementType](core.onCondition(condition))
    }

    override def withTransform(
        level: Level,
        f: Entry => Entry
    ): Self[StatementType] = {
      new Impl[StatementType](core.withTransform(level, f))
    }

    override def withEntryBuffer(buffer: EntryBuffer): Self[StatementType] =
      new Impl[StatementType](core.withEntryBuffer(buffer))

    override val isTraceEnabled: Predicate = core.predicate(TRACE)
    override val trace: SemanticMethod[StatementType] =
      new SemanticMethod.Impl[StatementType](TRACE, core)

    override val isDebugEnabled: Predicate = core.predicate(DEBUG)
    override val debug: SemanticMethod[StatementType] =
      new SemanticMethod.Impl[StatementType](DEBUG, core)

    override val isInfoEnabled: Predicate = core.predicate(INFO)
    override val info: SemanticMethod[StatementType] =
      new SemanticMethod.Impl[StatementType](INFO, core)

    override val isWarnEnabled: Predicate = core.predicate(WARN)
    override val warn: SemanticMethod[StatementType] =
      new SemanticMethod.Impl[StatementType](WARN, core)

    override val isErrorEnabled: Predicate = core.predicate(ERROR)
    override val error: SemanticMethod[StatementType] =
      new SemanticMethod.Impl[StatementType](ERROR, core)

  }

}
