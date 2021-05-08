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
import com.tersesystems.blindsight.core.{CoreLogger, ParameterList}
import org.slf4j.Marker
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

import scala.annotation.nowarn

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

  def apply(block: StrictSLF4JMethod => Unit): Unit

  def apply(
      st: Statement
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

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
    protected val parameterList: ParameterList = core.parameterList(level)

    import parameterList._

    override def when(condition: Condition)(block: StrictSLF4JMethod => Unit): Unit = {
      if (core.when(level, condition)) {
        block(this)
      }
    }

    override def apply(
        st: Statement
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) parameterList.executeStatement(st)
    }

    def apply(block: StrictSLF4JMethod => Unit): Unit = {
      if (enabled) block(this)
    }

    override def apply(
        msg: Message
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        message(msg.toString)
      }
    }

    override def apply(
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArg1("", throwable)
      }
    }

    override def apply[A: ToArgument](
        message: Message,
        arg: A
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArg1(message.toString, Argument(arg).value)
      }
    }

    override def apply(
        message: Message,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArg1(message.toString, throwable)
      }
    }

    override def apply[A: ToArgument](
        message: Message,
        arg: A,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArg1Arg2(
          message.toString,
          Argument(arg).value,
          throwable
        )
      }
    }

    override def apply[A1: ToArgument, A2: ToArgument](
        message: Message,
        arg1: A1,
        arg2: A2
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArg1Arg2(
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
      if (enabled) {
        messageArgs(message.toString, args.toArray)
      }
    }

    override def apply(
        message: Message,
        args: Arguments,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArgs(message.toString, args.toArray :+ throwable)
      }
    }

    override def apply(
        markers: Markers
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled(markers.marker)) {
        markerMessage(markers.marker, "")
      }
    }

    override def apply(
        markers: Markers,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled(markers.marker)) {
        markerMessageArg1(markers.marker, "", throwable)
      }
    }

    override def apply(
        markers: Markers,
        message1: Message
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled(markers.marker)) {
        markerMessage(markers.marker, message1.toString)
      }
    }

    override def apply[A: ToArgument](
        markers: Markers,
        message1: Message,
        arg: A
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled(markers.marker)) {
        markerMessageArg1(markers.marker, message1.toString, Argument(arg).value)
      }
    }

    override def apply(
        markers: Markers,
        message: Message,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled(markers.marker)) {
        markerMessageArg1(markers.marker, message.toString, throwable)
      }
    }

    override def apply[A1: ToArgument, A2: ToArgument](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled(markers.marker)) {
        markerMessageArg1Arg2(
          markers.marker,
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
      if (enabled(markers.marker)) {
        markerMessageArg1Arg2(
          markers.marker,
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
      if (enabled(markers.marker)) {
        markerMessageArgs(markers.marker, message.toString, args.toArray)
      }
    }

    override def apply(
        markers: Markers,
        message: Message,
        args: Arguments,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled(markers.marker)) {
        markerMessageArgs(markers.marker, message.toString, args.toArray :+ throwable)
      }
    }

    override def toString: String = {
      s"${getClass.getName}(logger=$core)"
    }

    @nowarn
    protected def enabled(implicit line: Line, file: File, enclosing: Enclosing): Boolean = {
      parameterList.executePredicate()
    }

    @nowarn
    protected def enabled(
        marker: Marker
    )(implicit line: Line, file: File, enclosing: Enclosing): Boolean = {
      parameterList.executePredicate(marker)
    }
  }

}
