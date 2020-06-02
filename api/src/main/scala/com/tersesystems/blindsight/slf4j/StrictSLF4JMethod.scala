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

import com.tersesystems.blindsight._
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

/**
 * A strict logger method that only takes type class instances for statements.
 */
trait StrictSLF4JMethod {
  def level: Level

  /**
   * Runs with a block function that is only called when condition is true.
   *
   * @param condition the call by name boolean that must return true
   * @param block the block executed when condition is true.
   */
  def when(condition: => Boolean)(block: StrictSLF4JMethod => Unit): Unit

  def apply(
      message: Message
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      throwable: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      message: Message,
      throwable: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[A: ToArgument](
      message: Message,
      arg: A
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[A: ToArgument](
      message: Message,
      arg: A,
      throwable: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[A1: ToArgument, A2: ToArgument](
      message: Message,
      arg1: A1,
      arg2: A2
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      message: Message,
      args: Arguments
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      message: Message,
      args: Arguments,
      throwable: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      markers: Markers
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      markers: Markers,
      message: Message
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      markers: Markers,
      throwable: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      markers: Markers,
      message: Message,
      throwable: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[A: ToArgument](
      markers: Markers,
      message: Message,
      arg: A
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[A: ToArgument](
      markers: Markers,
      message: Message,
      arg: A,
      throwable: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[A1: ToArgument, A2: ToArgument](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      markers: Markers,
      message: Message,
      args: Arguments
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      markers: Markers,
      message: Message,
      args: Arguments,
      throwable: Throwable
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

    val parameterList: ParameterList = logger.parameterList(level)

    import parameterList._

    override def apply(
        msg: Message
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

    override def apply(
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m: Markers = collateMarkers
      if (m.nonEmpty) {
        if (executePredicate(m.marker)) {
          markerMessageArg1(m.marker, "", throwable)
        }
      } else {
        if (executePredicate()) {
          messageArg1("", throwable)
        }
      }
    }

    override def apply[A: ToArgument](
        message: Message,
        arg: A
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m: Markers = collateMarkers
      if (m.nonEmpty) {
        if (executePredicate(m.marker)) {
          markerMessageArg1(m.marker, message.toString, Argument(arg).value)
        }
      } else {
        if (executePredicate()) {
          messageArg1(message.toString, Argument(arg).value)
        }
      }
    }

    override def apply(
        message: Message,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m: Markers = collateMarkers
      if (m.nonEmpty) {
        if (executePredicate(m.marker)) {
          markerMessageArg1(m.marker, message.toString, throwable)
        }
      } else {
        if (executePredicate()) {
          messageArg1(message.toString, throwable)
        }
      }
    }

    override def apply[A: ToArgument](
        message: Message,
        arg: A,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m: Markers = collateMarkers
      if (m.nonEmpty) {
        if (executePredicate(m.marker)) {
          markerMessageArg1Arg2(m.marker, message.toString, Argument(arg).value, throwable)
        }
      } else {
        if (executePredicate()) {
          messageArg1Arg2(message.toString, Argument(arg).value, throwable)
        }
      }
    }

    override def apply[A1: ToArgument, A2: ToArgument](
        message: Message,
        arg1: A1,
        arg2: A2
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m: Markers = collateMarkers
      if (m.nonEmpty) {
        if (executePredicate(m.marker)) {
          markerMessageArg1Arg2(
            m.marker,
            message.toString,
            Argument(arg1).value,
            Argument(arg2).value
          )
        }
      } else {
        if (executePredicate()) {
          messageArg1Arg2(message.toString, Argument(arg1).value, Argument(arg2).value)
        }
      }
    }

    override def apply(
        message: Message,
        args: Arguments
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m: Markers = collateMarkers
      if (m.nonEmpty) {
        if (executePredicate(m.marker)) {
          markerMessageArgs(m.marker, message.toString, args.toArray)
        }
      } else {
        if (executePredicate()) {
          messageArgs(message.toString, args.toArray)
        }
      }
    }

    override def apply(
        message: Message,
        args: Arguments,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m: Markers = collateMarkers
      if (m.nonEmpty) {
        if (executePredicate(m.marker)) {
          markerMessageArgs(m.marker, message.toString, args.toArray :+ throwable)
        }
      } else {
        if (executePredicate()) {
          messageArgs(message.toString, args.toArray :+ throwable)
        }
      }
    }

    override def apply(
        markers: Markers
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m = collateMarkers(markers)
      if (executePredicate(m.marker)) {
        markerMessage(m.marker, "")
      }
    }

    override def apply(
        markers: Markers,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m = collateMarkers(markers)
      if (executePredicate(m.marker)) {
        markerMessageArg1(m.marker, "", throwable)
      }
    }

    override def apply(
        marker: Markers,
        message1: Message
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m = collateMarkers(marker)
      if (executePredicate(m.marker)) {
        markerMessage(m.marker, message1.toString)
      }
    }

    override def apply[A: ToArgument](
        markers: Markers,
        message1: Message,
        arg: A
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m = collateMarkers(markers)
      if (executePredicate(m.marker)) {
        markerMessageArg1(m.marker, message1.toString, Argument(arg).value)
      }
    }

    override def apply(
        markers: Markers,
        message: Message,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m = collateMarkers(markers)
      if (executePredicate(m.marker)) {
        markerMessageArg1(m.marker, message.toString, throwable)
      }
    }

    override def apply[A1: ToArgument, A2: ToArgument](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m = collateMarkers(markers)
      if (executePredicate(m.marker)) {
        markerMessageArg1Arg2(
          m.marker,
          message.toString,
          Argument(arg1).value,
          Argument(arg2).value
        )
      }
    }

    override def apply[A: ToArgument](
        markers: Markers,
        message: Message,
        arg: A,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m = collateMarkers(markers)
      if (executePredicate(m.marker)) {
        markerMessageArg1Arg2(
          m.marker,
          message.toString,
          Argument(arg).value,
          throwable
        )
      }
    }

    override def apply(
        markers: Markers,
        message: Message,
        args: Arguments
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m = collateMarkers(markers)
      if (executePredicate(m.marker)) {
        markerMessageArgs(m.marker, message.toString, args.toArray)
      }
    }

    override def apply(
        markers: Markers,
        message: Message,
        args: Arguments,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m = collateMarkers(markers)
      if (executePredicate(m.marker)) {
        markerMessageArgs(m.marker, message.toString, args.toArray :+ throwable)
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
      s"${getClass.getName}(logger=$logger)"
    }
  }

  /**
   * Conditional method implementation.  Only calls when test evaluates to true.
   *
   * @param level the method's level
   * @param test the call by name boolean that must be true
   * @param logger the logger that this method belongs to.
   */
  class Conditional(level: Level, test: => Boolean, logger: ExtendedSLF4JLogger[StrictSLF4JMethod])
      extends StrictSLF4JMethod.Impl(level, logger) {

    override val parameterList: ParameterList =
      new ParameterList.Conditional(test, logger.parameterList(level))

    override def when(condition: => Boolean)(block: StrictSLF4JMethod => Unit): Unit = {
      if (test && condition) {
        block(this)
      }
    }

    override def toString: String = {
      s"${getClass.getName}(logger=$logger)"
    }
  }
}
