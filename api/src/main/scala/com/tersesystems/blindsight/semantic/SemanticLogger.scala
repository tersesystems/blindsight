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

import com.tersesystems.blindsight.mixins._
import com.tersesystems.blindsight.slf4j._
import com.tersesystems.blindsight._
import org.slf4j
import org.slf4j.event.Level
import org.slf4j.event.Level._
import sourcecode.{Enclosing, File, Line}

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
    with SemanticMarkerMixin[StatementType]
    with SemanticRefineMixin[StatementType] {
  type Self[T] = SemanticLogger[T]

  def onCondition(condition: Condition): Self[StatementType]
}

object SemanticLogger {

  class Impl[StatementType](protected val logger: CoreLogger)
      extends SemanticLogger[StatementType] {

    override def underlying: slf4j.Logger = logger.underlying

    override def markers: Markers = logger.markers
    override def withMarker[T: ToMarkers](markerInst: => T): Self[StatementType] = {
      val markers = implicitly[ToMarkers[T]].toMarkers(markerInst)
      new Impl[StatementType](logger.withMarker(markers))
    }

    override def refine[T <: StatementType: ToStatement: NotNothing]: Self[T] = new Impl[T](logger)

    override def onCondition(condition: Condition): Self[StatementType] = {
      new Conditional[StatementType](logger.onCondition(condition))
    }

    override val isTraceEnabled: Predicate = logger.predicate(TRACE)
    override val trace: SemanticMethod[StatementType] =
      new SemanticMethod.Impl[StatementType](TRACE, logger)

    override val isDebugEnabled: Predicate = logger.predicate(DEBUG)
    override val debug: SemanticMethod[StatementType] =
      new SemanticMethod.Impl[StatementType](DEBUG, logger)

    override val isInfoEnabled: Predicate = logger.predicate(INFO)
    override val info: SemanticMethod[StatementType] =
      new SemanticMethod.Impl[StatementType](INFO, logger)

    override val isWarnEnabled: Predicate = logger.predicate(WARN)
    override val warn: SemanticMethod[StatementType] =
      new SemanticMethod.Impl[StatementType](WARN, logger)

    override val isErrorEnabled: Predicate = logger.predicate(ERROR)
    override val error: SemanticMethod[StatementType] =
      new SemanticMethod.Impl[StatementType](ERROR, logger)

  }

  class Conditional[StatementType](logger: CoreLogger) extends SemanticLogger[StatementType] {
    override type Self[T]   = SemanticLogger[T]
    override type Method[T] = SemanticMethod[T]
    override type Predicate = SimplePredicate

    override def underlying: slf4j.Logger = logger.underlying

    override def markers: Markers = logger.markers
    override def withMarker[T: ToMarkers](markerInstance: => T): Self[StatementType] = {
      new Conditional(logger.withMarker(markerInstance))
    }

    override def refine[T <: StatementType: ToStatement: NotNothing]: Self[T] = {
      new Conditional[T](logger)
    }

    override def onCondition(condition: Condition): Self[StatementType] = {
      new Conditional(logger.onCondition(condition))
    }

    override def isTraceEnabled: Predicate = logger.predicate(TRACE)
    override def trace: SemanticMethod[StatementType] =
      new SemanticMethod.Conditional[StatementType](TRACE, logger)

    override def isDebugEnabled: Predicate = logger.predicate(DEBUG)
    override def debug: SemanticMethod[StatementType] =
      new SemanticMethod.Conditional[StatementType](DEBUG, logger)

    override def isInfoEnabled: Predicate = logger.predicate(INFO)
    override def info: SemanticMethod[StatementType] =
      new SemanticMethod.Conditional[StatementType](INFO, logger)

    override def isWarnEnabled: Predicate = logger.predicate(WARN)
    override def warn: SemanticMethod[StatementType] =
      new SemanticMethod.Conditional[StatementType](WARN, logger)

    override def isErrorEnabled: Predicate = logger.predicate(ERROR)
    override def error: SemanticMethod[StatementType] =
      new SemanticMethod.Conditional[StatementType](ERROR, logger)
  }
}
