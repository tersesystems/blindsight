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
  def when(condition: Condition)(block: StrictSLF4JMethod => Unit): Unit

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
  class Impl(val level: Level, core: CoreLogger) extends StrictSLF4JMethod {
    import core.markers

    protected val parameterList: ParameterList = core.parameterList(level)

    import parameterList._

    override def apply(
        msg: Message
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (shouldLog)
        markerMessage(markersPlusSource.marker, msg.toString)
    }

    override def apply(
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (shouldLog)
        markerMessageArg1(markersPlusSource.marker, "", throwable)
    }

    override def apply[A: ToArgument](
        message: Message,
        arg: A
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (shouldLog)
        markerMessageArg1(markersPlusSource.marker, message.toString, Argument(arg).value)
    }

    override def apply(
        message: Message,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (shouldLog)
        markerMessageArg1(markersPlusSource.marker, message.toString, throwable)
    }

    override def apply[A: ToArgument](
        message: Message,
        arg: A,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (shouldLog)
        markerMessageArg1Arg2(
          markersPlusSource.marker,
          message.toString,
          Argument(arg).value,
          throwable
        )
    }

    override def apply[A1: ToArgument, A2: ToArgument](
        message: Message,
        arg1: A1,
        arg2: A2
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (shouldLog) {
        markerMessageArg1Arg2(
          markersPlusSource.marker,
          message.toString,
          Argument(arg1).value,
          Argument(arg2).value
        )
      }
    }

    override def apply(
        message: Message,
        args: Arguments
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (shouldLog)
        markerMessageArgs(markersPlusSource.marker, message.toString, args.toSeq)
    }

    override def apply(
        message: Message,
        args: Arguments,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (shouldLog) {
        markerMessageArgs(markersPlusSource.marker, message.toString, args.toSeq :+ throwable)
      }
    }

    override def apply(
        markers: Markers
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (shouldLog(markers)) {
        markerMessage(markersPlusSource(markers).marker, "")
      }
    }

    override def apply(
        markers: Markers,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (shouldLog(markers)) {
        markerMessageArg1(markersPlusSource(markers).marker, "", throwable)
      }
    }

    override def apply(
        markers: Markers,
        message1: Message
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (shouldLog(markers)) {
        markerMessage(markersPlusSource(markers).marker, message1.toString)
      }
    }

    override def apply[A: ToArgument](
        markers: Markers,
        message1: Message,
        arg: A
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (shouldLog(markers)) {
        markerMessageArg1(markersPlusSource(markers).marker, message1.toString, Argument(arg).value)
      }
    }

    override def apply(
        markers: Markers,
        message: Message,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (shouldLog(markers)) {
        markerMessageArg1(markersPlusSource(markers).marker, message.toString, throwable)
      }
    }

    override def apply[A1: ToArgument, A2: ToArgument](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (shouldLog(markers)) {
        markerMessageArg1Arg2(
          markersPlusSource(markers).marker,
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
      val m = markersPlusSource(markers)
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
      val m = markersPlusSource(markers)
      if (executePredicate(m.marker)) {
        markerMessageArgs(m.marker, message.toString, args.toSeq)
      }
    }

    override def apply(
        markers: Markers,
        message: Message,
        args: Arguments,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m = markersPlusSource(markers)
      if (executePredicate(m.marker)) {
        markerMessageArgs(m.marker, message.toString, args.toSeq :+ throwable)
      }
    }

    @inline
    private def shouldLog: Boolean = {
      val m: Markers = core.state.markers
      if (m.isEmpty) executePredicate() else executePredicate(m.marker)
    }

    // optimize for the conditional, even if we have to reconstruct the marker twice
    @inline
    private def shouldLog(markers: Markers): Boolean = {
      val m: Markers = core.state.markers + markers
      executePredicate(m.marker)
    }

    @inline
    private def markersPlusSource(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Markers = {
      val sourceMarker: Markers = core.sourceInfoBehavior(level, line, file, enclosing)
      sourceMarker + markers
    }

    @inline
    private def markersPlusSource[MR: ToMarkers](
        marker: MR
    )(implicit line: Line, file: File, enclosing: Enclosing): Markers = {
      markersPlusSource + implicitly[ToMarkers[MR]].toMarkers(marker)
    }

    override def when(condition: Condition)(block: StrictSLF4JMethod => Unit): Unit = {
      if (core.when(level, condition)) {
        block(this)
      }
    }

    override def toString: String = {
      s"${getClass.getName}(logger=$core)"
    }
  }
}
