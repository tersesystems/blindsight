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

import com.tersesystems.blindsight._
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

trait SemanticMethod[StatementType] {
  def level: Level

  def when(condition: Condition)(block: SemanticMethod[StatementType] => Unit): Unit

  def apply[T <: StatementType: ToStatement](
      instance: T
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[T <: StatementType: ToStatement](
      instance: T,
      t: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

}

object SemanticMethod {

  class Impl[StatementType](
      val level: Level,
      core: CoreLogger
  ) extends SemanticMethod[StatementType] {

    import core.{markers => markerState}

    override def apply[T <: StatementType: ToStatement](
        instance: T,
        t: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val statement = implicitly[ToStatement[T]].toStatement(instance)
      val markers   = collateMarkers(statement.markers)
      if (isEnabled(markers)) {
        core
          .parameterList(level)
          .executeStatement(statement.withMarkers(markers).withThrowable(t))
      }
    }

    def isEnabled(markers: Markers): Boolean = {
      if (markers.nonEmpty) {
        core.parameterList(level).executePredicate(markers.marker)
      } else {
        core.parameterList(level).executePredicate()
      }
    }

    protected def collateMarkers(
        markers: Markers
    )(implicit line: Line, file: File, enclosing: Enclosing): Markers = {
      val sourceMarkers = core.sourceInfoBehavior(level, line, file, enclosing)
      sourceMarkers + markerState + markers
    }

    override def apply[T <: StatementType: ToStatement](
        instance: T
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val statement: Statement =
        implicitly[ToStatement[T]].toStatement(instance)
      val markers = collateMarkers(statement.markers)
      if (isEnabled(markers)) {
        core.parameterList(level).executeStatement(statement.withMarkers(markers))
      }
    }

    override def when(condition: Condition)(block: SemanticMethod[StatementType] => Unit): Unit = {
      if (core.when(level, condition)) {
        block(this)
      }
    }

  }
}
