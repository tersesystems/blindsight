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

import com.tersesystems.blindsight.api._
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

/**
 * A strict logger method that only takes type class instances for statements.
 */
trait StrictSLF4JMethod {
  def level: Level

  /**
   * Runs with a block function that is only executioned when condition is true.
   *
   * @param condition the call by name boolean that must return true
   * @param block the block executed when condition is true.
   */
  def when(condition: => Boolean)(block: StrictSLF4JMethod => Unit): Unit

  def apply(
      message: => Message
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[A: ToArguments](
      message: => Message,
      args: A
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[M: ToMarkers](
      markers: M,
      message: => Message
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[M: ToMarkers, A: ToArguments](
      markers: M,
      message: => Message,
      args: A
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit
}

object StrictSLF4JMethod {

  /**
   * Strict method implementation.
   */
  class Impl(val level: Level, logger: ExtendedSLF4JLogger[StrictSLF4JMethod])
      extends StrictSLF4JMethod {

    @inline
    protected def markers: Markers = logger.markers

    protected val parameterList: ParameterList = logger.parameterList(level)

    import parameterList._

    override def apply(
        msg: => Message
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m = collateMarkers
      if (m.nonEmpty) {
        if (executePredicate(m.marker)) {
          markerMessage(m.marker, msg.toString)
        }
      } else {
        if (executePredicate()) {
          message(msg.toString)
        }
      }
    }

    override def apply[A: ToArguments](
        message1: => Message,
        args: A
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m: Markers = collateMarkers
      if (m.nonEmpty) {
        if (executePredicate(m.marker)) {
          val arguments = implicitly[ToArguments[A]].toArguments(args)
          markerMessageArgs(m.marker, message1.toString, arguments.asArray)
        }
      } else {
        if (executePredicate()) {
          val arguments = implicitly[ToArguments[A]].toArguments(args)
          messageArgs(message1.toString, arguments.asArray)
        }
      }
    }

    override def apply[M: ToMarkers](
        marker: M,
        message1: => Message
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m = collateMarkers(marker)
      if (executePredicate(m.marker)) {
        markerMessage(m.marker, message1.toString)
      }
    }

    override def apply[M: ToMarkers, A: ToArguments](
        marker: M,
        message1: => Message,
        args: A
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m = collateMarkers(marker)
      if (executePredicate(m.marker)) {
        val arguments = implicitly[ToArguments[A]].toArguments(args)
        markerMessageArgs(m.marker, message1.toString, arguments.asArray)
      }
    }

    private def collateMarkers(implicit line: Line, file: File, enclosing: Enclosing): Markers = {
      val sourceMarker: Markers = logger.sourceInfoMarker(level, line, file, enclosing)
      sourceMarker + markers
    }

    private def collateMarkers[MR: ToMarkers](
        marker: MR
    )(implicit line: Line, file: File, enclosing: Enclosing): Markers = {
      collateMarkers + implicitly[ToMarkers[MR]].toMarkers(marker)
    }

    override def when(condition: => Boolean)(block: StrictSLF4JMethod => Unit): Unit = {
      if (condition && executePredicate(collateMarkers.marker)) {
        block(this)
      }
    }

    override def toString: String = {
      s"${getClass.getName}(logger=${logger})"
    }
  }

  /**
   * Conditional method implementation.  Only calls when test evaluates to true.
   *
   * @param level the method's level
   * @param test the call by name boolean that must be true
   * @param logger the logger that this method belongs to.
   */
  class Conditional(
      val level: Level,
      test: => Boolean,
      logger: ExtendedSLF4JLogger[StrictSLF4JMethod]
  ) extends StrictSLF4JMethod {

    private def method = logger.method(level)

    override def when(condition: => Boolean)(block: StrictSLF4JMethod => Unit): Unit = {
      if (test && condition) {
        block(this)
      }
    }

    override def apply(
        message: => Message
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (test) method.apply(message)
    }

    override def apply[A: ToArguments](
        message: => Message,
        args: A
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (test) method.apply(message, args)
    }

    override def apply[M: ToMarkers](
        markers: M,
        message: => Message
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (test) method.apply(markers, message)
    }

    override def apply[M: ToMarkers, A: ToArguments](
        markers: M,
        message: => Message,
        args: A
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (test) method.apply(markers, message, args)
    }

    override def toString: String = {
      s"${getClass.getName}(logger=${logger})"
    }
  }
}
