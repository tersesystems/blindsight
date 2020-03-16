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

import com.tersesystems.blindsight.api._
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

trait FluentMethod extends FluentAPI {

  def when(condition: => Boolean)(block: FluentMethod => Unit): Unit

  def apply[T: ToStatement](
      instance: => T
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit
}

object FluentMethod {

  trait Builder extends FluentAPI {
    def log(): Unit
    def logWithPlaceholders(): Unit
  }

  class Impl(val level: Level, logger: ExtendedFluentLogger) extends FluentMethod {

    protected val parameterList: ParameterList = logger.parameterList(level)

    def markerState: Markers = logger.markers

    def when(condition: => Boolean)(block: FluentMethod => Unit): Unit = {
      if (condition && isEnabled(collateMarkers(logger.markers))) {
        block(this)
      }
    }

    final case class BuilderImpl(
        mkrs: Markers,
        m: Message,
        args: Arguments,
        e: Option[Throwable]
    ) extends FluentMethod.Builder {

      override def marker[T: ToMarkers](instance: => T): FluentMethod.Builder = {
        val moreMarkers = implicitly[ToMarkers[T]].toMarkers(instance)
        copy(mkrs = mkrs + moreMarkers)
      }

      override def message[T: ToMessage](instance: => T): FluentMethod.Builder = {
        val message = implicitly[ToMessage[T]].toMessage(instance)
        copy(m = m + message)
      }

      override def argument[T: ToArguments](instance: => T): FluentMethod.Builder = {
        val arguments = implicitly[ToArguments[T]].toArguments(instance)
        copy(args = args + arguments)
      }

      override def cause(e: Throwable): FluentMethod.Builder = copy(e = Some(e))

      override def log(): Unit = {
        val statement = Statement(markers = mkrs, message = m, arguments = args, e)
        apply(statement)
      }

      override def logWithPlaceholders(): Unit = {
        val statement =
          Statement(markers = mkrs, message = m.withPlaceHolders(args), arguments = args, e)
        apply(statement)
      }
    }

    object BuilderImpl {
      def empty: BuilderImpl = BuilderImpl(Markers.empty, Message.empty, Arguments.empty, None)
    }

    override def argument[T: ToArguments](instance: => T): FluentMethod.Builder = {
      BuilderImpl.empty.argument(instance)
    }

    override def cause(e: Throwable): FluentMethod.Builder = BuilderImpl.empty.cause(e)

    override def message[T: ToMessage](instance: => T): FluentMethod.Builder = {
      BuilderImpl.empty.message(instance)
    }

    override def marker[T: ToMarkers](instance: => T): FluentMethod.Builder =
      BuilderImpl.empty.marker(instance)

    override def apply[T: ToStatement](
        instance: => T
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val statement = implicitly[ToStatement[T]].toStatement(instance)
      val markers   = collateMarkers(statement.markers)
      if (isEnabled(markers)) {
        parameterList.executeStatement(statement.withMarkers(markers))
      }
    }

    protected def collateMarkers(
        markers: Markers
    )(implicit line: Line, file: File, enclosing: Enclosing): Markers = {
      val sourceMarkers = logger.sourceInfoMarker(level, line, file, enclosing)
      sourceMarkers + markerState + markers
    }

    protected def isEnabled(markers: Markers): Boolean = {
      if (markers.nonEmpty) {
        parameterList.executePredicate(markers.marker)
      } else {
        parameterList.executePredicate()
      }
    }
  }

  class Conditional(level: Level, test: => Boolean, logger: ExtendedFluentLogger)
      extends FluentMethod.Impl(level, logger) {

    override def when(condition: => Boolean)(block: FluentMethod => Unit): Unit = {
      if (test && condition && isEnabled(collateMarkers(logger.markers))) {
        block(this)
      }
    }

    override def apply[T: ToStatement](
        instance: => T
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (test) {
        val statement = implicitly[ToStatement[T]].toStatement(instance)
        logger.parameterList(level).executeStatement(statement)
      }
    }
  }

}
