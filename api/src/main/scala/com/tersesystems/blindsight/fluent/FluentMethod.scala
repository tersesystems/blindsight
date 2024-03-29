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

package com.tersesystems.blindsight.fluent

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.core.{CoreLogger, ParameterList}
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

import scala.annotation.nowarn

/**
 * The fluent method.
 */
trait FluentMethod extends FluentAPI {

  def when(condition: Condition)(block: FluentMethod => Unit): Unit
}

object FluentMethod {

  trait Builder extends FluentAPI {
    def log(): Unit
    def logWithPlaceholders(): Unit
  }

  class Impl(val level: Level, core: CoreLogger) extends FluentMethod {

    private val parameterList: ParameterList = core.parameterList(level)

    override def when(condition: Condition)(block: FluentMethod => Unit): Unit = {
      if (core.when(level, condition)) {
        block(this)
      }
    }

    override def statement(statement: => Statement): FluentMethod.Builder = {
      BuilderImpl(
        mkrs = () => statement.markers,
        m = () => statement.message,
        args = () => statement.arguments,
        e = statement.throwable
      )
    }

    override def argument[T: ToArgument](instance: => T): FluentMethod.Builder = {
      BuilderImpl(
        () => Markers.empty,
        () => Message.empty,
        () => Arguments.fromInstance(instance),
        None
      )
    }

    override def cause(e: Throwable): FluentMethod.Builder = {
      BuilderImpl(() => Markers.empty, () => Message.empty, () => Arguments.empty, Some(e))
    }

    override def message[T: ToMessage](instance: => T): FluentMethod.Builder = {
      BuilderImpl(() => Markers.empty, () => Message(instance), () => Arguments.empty, None)
    }

    override def marker[T: ToMarkers](instance: => T): FluentMethod.Builder =
      BuilderImpl(() => Markers(instance), () => Message.empty, () => Arguments.empty, None)

    @nowarn
    @inline
    protected def enabled(
        markers: Markers
    )(implicit line: Line, file: File, enclosing: Enclosing): Boolean = {
      if (markers.nonEmpty) {
        parameterList.executePredicate(markers.marker)
      } else {
        parameterList.executePredicate()
      }
    }

    @inline
    protected def executeStatement(
        statement: Statement
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      parameterList.executeStatement(statement)
    }

    case class BuilderImpl(
        mkrs: () => Markers,
        m: () => Message,
        args: () => Arguments,
        e: Option[Throwable]
    ) extends FluentMethod.Builder {

      override def statement(statement: => Statement): FluentMethod.Builder = {
        copy(
          mkrs = () => statement.markers,
          m = () => statement.message,
          args = () => statement.arguments,
          e = statement.throwable
        )
      }

      override def marker[T: ToMarkers](instance: => T): FluentMethod.Builder = {
        copy(mkrs = () => mkrs() + Markers(instance))
      }

      override def message[T: ToMessage](instance: => T): FluentMethod.Builder = {
        copy(m = () => m() + Message(instance))
      }

      override def argument[T: ToArgument](instance: => T): FluentMethod.Builder = {
        copy(args = () => args() + Argument(instance))
      }

      override def cause(e: Throwable): FluentMethod.Builder = copy(e = Some(e))

      override def log(): Unit = {
        val markers = mkrs()
        if (enabled(markers)) {
          val statement = e
            .map(ee => Statement(markers, m(), args(), ee))
            .getOrElse(Statement(markers, m(), args()))
          executeStatement(statement)
        }
      }

      override def logWithPlaceholders(): Unit = {
        val markers = mkrs()
        if (enabled(markers)) {
          val message = m().withPlaceHolders(args())
          val statement = e
            .map(ee => Statement(markers, message, args(), ee))
            .getOrElse(Statement(markers, message, args()))
          executeStatement(statement)
        }
      }
    }

    object BuilderImpl {
      val empty: BuilderImpl =
        BuilderImpl(() => Markers.empty, () => Message.empty, () => Arguments.empty, None)
    }
  }
}
