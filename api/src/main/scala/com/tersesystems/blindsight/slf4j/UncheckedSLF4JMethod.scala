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

package com.tersesystems.blindsight.slf4j

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.core.{CoreLogger, ParameterList}
import org.slf4j.Marker
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

import scala.annotation.nowarn

/**
 * An unchecked SLF4J method that takes `Any` as arguments.
 */
trait UncheckedSLF4JMethod {
  def level: Level

  /**
   * Runs with a block function that is only called when condition is true.
   *
   * @param condition the call by name boolean that must return true
   * @param block the block executed when condition is true.
   */
  def when(condition: Condition)(block: UncheckedSLF4JMethod => Unit): Unit

  def apply(block: UncheckedSLF4JMethod => Unit): Unit

  def apply(
      st: Statement
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      instance: String
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      message: String,
      arg: Any
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      message: String,
      arg1: Any,
      arg2: Any
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      message: String,
      args: Arguments
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      markers: Marker,
      message: String
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      markers: Marker,
      message: String,
      arg: Any
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      markers: Marker,
      message: String,
      arg1: Any,
      arg2: Any
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(markers: Marker, message: String, args: Arguments)(implicit
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Unit
}

object UncheckedSLF4JMethod {

  /**
   * @return the boolean system property "blindsight.anywarn" if set, otherwise false.
   */
  def isAnyWarning: Boolean = sys.props.getOrElse("blindsight.anywarn", "false").toBoolean

  /**
   * Prints a warning message to System.err if isAnyWarning returns true.
   *
   * @param warning the warning message
   * @param line the line where the warning happened
   * @param file the file where the warning happened
   */
  def warnIfChecked(warning: => String)(implicit line: Line, file: File): Unit = {
    if (isAnyWarning) {
      System.err.println(s"${file.value}:${line.value} -- $warning")
    }
  }

  /**
   * Unchecked method implementation.
   */
  class Impl(val level: Level, core: CoreLogger) extends UncheckedSLF4JMethod {
    protected val parameterList: ParameterList = core.parameterList(level)

    import parameterList._

    override def when(condition: Condition)(block: UncheckedSLF4JMethod => Unit): Unit = {
      if (core.when(level, condition)) {
        block(this)
      }
    }

    def apply(block: UncheckedSLF4JMethod => Unit): Unit = {
      if (enabled) block(this)
    }

    override def apply(
        st: Statement
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) executeStatement(st)
    }

    override def apply(
        msg: String
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        message(msg)
      }
    }

    override def apply(
        format: String,
        arg: Any
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        warnIfChecked(
          "Use apply(format, Argument(arg)) as Any cannot be type checked"
        )
        messageArg1(format, arg)
      }
    }

    override def apply(
        format: String,
        arg1: Any,
        arg2: Any
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        warnIfChecked(
          "Use apply(format, Arguments(arg1, arg2)) as Any cannot be type checked"
        )
        messageArg1Arg2(format, arg1, arg2)
      }
    }

    override def apply(
        format: String,
        args: Arguments
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArgs(format, args.toArray)
      }
    }

    override def apply(
        marker: Marker,
        msg: String
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled(marker)) {
        markerMessage(marker, msg)
      }
    }

    override def apply(marker: Marker, msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      if (enabled(marker)) {
        warnIfChecked(
          "Use apply(marker, format, Arguments(arg1, arg2)) as Any cannot be type checked"
        )
        markerMessageArg1(marker, msg, arg)
      }
    }

    override def apply(marker: Marker, format: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      if (enabled(marker)) {
        warnIfChecked(
          "Use apply(marker, format, Arguments(arg1, arg2)) as Any cannot be type checked"
        )
        markerMessageArg1Arg2(marker, format, arg1, arg2)
      }
    }

    override def apply(
        marker: Marker,
        format: String,
        args: Arguments
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled(marker)) {
        markerMessageArgs(marker, format, args.toArray)
      }
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

    override def toString: String = {
      s"${getClass.getName}(logger=$core)"
    }
  }

}
