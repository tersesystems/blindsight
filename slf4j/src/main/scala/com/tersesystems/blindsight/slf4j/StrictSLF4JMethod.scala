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
   * Runs with a block function that is only called when condition is true.
   *
   * @param condition the call by name boolean that must return true
   * @param block the block executed when condition is true.
   */
  def when(condition: => Boolean)(block: StrictSLF4JMethod => Unit): Unit

  def apply(
      message: => Message
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      message: => Message,
      throwable: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[A: ToArgument](
      message: => Message,
      arg: A
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[A: ToArgument](
      message: => Message,
      arg: A,
      throwable: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[A1: ToArgument, A2: ToArgument](
      message: => Message,
      arg1: A1,
      arg2: A2
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[A1: ToArgument, A2: ToArgument](
      message: => Message,
      arg1: A1,
      arg2: A2,
      throwable: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      message: => Message,
      args: Arguments
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      message: => Message,
      args: Arguments,
      throwable: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[M: ToMarkers](
      markers: M,
      message: => Message
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[M: ToMarkers](
      markers: M,
      message: => Message,
      throwable: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[M: ToMarkers, A: ToArgument](
      markers: M,
      message: => Message,
      arg: A
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[M: ToMarkers, A: ToArgument](
      markers: M,
      message: => Message,
      arg: A,
      throwable: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[M: ToMarkers, A1: ToArgument, A2: ToArgument](
      markers: M,
      message: => Message,
      arg1: A1,
      arg2: A2
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[M: ToMarkers, A1: ToArgument, A2: ToArgument](
      markers: M,
      message: => Message,
      arg1: A1,
      arg2: A2,
      throwable: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[M: ToMarkers](
      markers: M,
      message: => Message,
      args: Arguments
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[M: ToMarkers](
      markers: M,
      message: => Message,
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

    override def apply[A: ToArgument](
        message: => Message,
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

    override def apply[A1: ToArgument, A2: ToArgument](
        message: => Message,
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
        message: => Message,
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

    override def apply[M: ToMarkers](
        marker: M,
        message1: => Message
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m = collateMarkers(marker)
      if (executePredicate(m.marker)) {
        markerMessage(m.marker, message1.toString)
      }
    }

    override def apply[M: ToMarkers, A: ToArgument](
        markers: M,
        message1: => Message,
        arg: A
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m = collateMarkers(markers)
      if (executePredicate(m.marker)) {
        val arguments = implicitly[ToArgument[A]].toArgument(arg)
        markerMessageArg1(m.marker, message1.toString, arguments.value)
      }
    }

    override def apply[M: ToMarkers, A1: ToArgument, A2: ToArgument](
        markers: M,
        message: => Message,
        arg1: A1,
        arg2: A2
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m = collateMarkers(markers)
      if (executePredicate(m.marker)) {
        markerMessageArg1Arg2(m.marker,
                              message.toString,
                              Argument(arg1).value,
                              Argument(arg2).value)
      }
    }

    override def apply[M: ToMarkers](
        markers: M,
        message: => Message,
        args: Arguments
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val m = collateMarkers(markers)
      if (executePredicate(m.marker)) {
        markerMessageArgs(m.marker, message.toString, args.toArray)
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

    override def apply(
        message: => Message,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = ???

    override def apply[A: ToArgument](
        message: => Message,
        arg: A,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = ???

    override def apply[A1: ToArgument, A2: ToArgument](
        message: => Message,
        arg1: A1,
        arg2: A2,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = ???

    override def apply(
        message: => Message,
        args: Arguments,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = ???

    override def apply[M: ToMarkers](
        markers: M,
        message: => Message,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = ???

    override def apply[M: ToMarkers, A: ToArgument](
        markers: M,
        message: => Message,
        arg: A,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = ???

    override def apply[M: ToMarkers, A1: ToArgument, A2: ToArgument](
        markers: M,
        message: => Message,
        arg1: A1,
        arg2: A2,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = ???

    override def apply[M: ToMarkers](
        markers: M,
        message: => Message,
        args: Arguments,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = ???
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

    override def apply[A: ToArgument](
        message: => Message,
        arg: A
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (test) method.apply(message, arg)
    }

    override def apply[A1: ToArgument, A2: ToArgument](
        message: => Message,
        arg1: A1,
        arg2: A2
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (test) method.apply(message, arg1, arg2)
    }

    override def apply(
        message: => Message,
        args: Arguments
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (test) method.apply(message, args)
    }

    override def apply[M: ToMarkers](
        markers: M,
        message: => Message
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (test) method.apply(markers, message)
    }

    override def apply[M: ToMarkers, A1: ToArgument, A2: ToArgument](
        markers: M,
        message: => Message,
        arg1: A1,
        arg2: A2
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (test) method.apply(markers, message, arg1, arg2)
    }

    override def apply[M: ToMarkers](
        markers: M,
        message: => Message,
        args: Arguments
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (test) method.apply(markers, message, args)
    }

    override def apply[M: ToMarkers, A: ToArgument](
        markers: M,
        message: => Message,
        arg: A
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (test) method.apply(markers, message, arg)
    }

    override def apply(
       message: => Message,
       throwable: Throwable
     )(implicit line: Line, file: File, enclosing: Enclosing): Unit = ???

    override def apply[A: ToArgument](
     message: => Message,
     arg: A,
     throwable: Throwable
   )(implicit line: Line, file: File, enclosing: Enclosing): Unit = ???

    override def apply[A1: ToArgument, A2: ToArgument](
      message: => Message,
      arg1: A1,
      arg2: A2,
      throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = ???

    override def apply(
      message: => Message,
      args: Arguments,
      throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = ???

    override def apply[M: ToMarkers](
      markers: M,
      message: => Message,
      throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = ???

    override def apply[M: ToMarkers, A: ToArgument](
       markers: M,
       message: => Message,
       arg: A,
       throwable: Throwable
     )(implicit line: Line, file: File, enclosing: Enclosing): Unit = ???

    override def apply[M: ToMarkers, A1: ToArgument, A2: ToArgument](
      markers: M,
      message: => Message,
      arg1: A1,
      arg2: A2,
      throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = ???

    override def apply[M: ToMarkers](
      markers: M,
      message: => Message,
      args: Arguments,
      throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = ???

    override def toString: String = {
      s"${getClass.getName}(logger=${logger})"
    }
  }
}
