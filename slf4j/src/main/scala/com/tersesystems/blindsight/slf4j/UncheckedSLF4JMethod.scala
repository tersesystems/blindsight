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
import org.slf4j.Marker
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

/**
 * The logger method
 */
trait UncheckedSLF4JMethod {
  def level: Level

  def when(condition: => Boolean)(block: UncheckedSLF4JMethod => Unit): Unit

  def apply(
      instance: => Message
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      message: => Message,
      args: Arguments
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      message: => Message,
      arg1: Any,
      arg2: Any
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      message: => Message,
      args: Any*
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      markers: Marker,
      message: => Message
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      markers: Marker,
      message: => Message,
      args: Arguments
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      markers: Marker,
      message: => Message,
      arg1: Any,
      arg2: Any
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(markers: Marker, message: => Message, args: Any*)(
      implicit line: Line,
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
   * This class does the work of taking various input parameters, and determining what SLF4J method to call
   * with those parameters.
   */
  class Impl(val level: Level, logger: ExtendedSLF4JLogger[UncheckedSLF4JMethod])
      extends UncheckedSLF4JMethod {

    @inline
    protected def markers: Markers = logger.markers

    protected val parameterList: ParameterList = logger.parameterList(level)

    import parameterList._

    override def apply(
        msg: => Message
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val markers = collateMarkers
      if (markers.nonEmpty) {
        if (executePredicate(markers.marker)) {
          markerMessage(markers.marker, msg.toString)
        }
      } else {
        if (executePredicate()) {
          message(msg.toString)
        }
      }
    }

    override def apply(
        format: => Message,
        args: Arguments
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val markers: Markers = collateMarkers
      if (markers.nonEmpty) {
        if (executePredicate(markers.marker)) {
          markerMessageArgs(markers.marker, format.toString, args.asArray)
        }
      } else {
        if (executePredicate()) {
          messageArgs(format.toString, args.asArray)
        }
      }
    }

    override def apply(
        format: => Message,
        arg1: Any,
        arg2: Any
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      warnIfChecked(
        "Use apply(format, Arguments(arg1, arg2)) as Any cannot be type checked"
      )
      val markers: Markers = collateMarkers
      if (markers.nonEmpty) {
        if (executePredicate(markers.marker)) {
          markerMessageArg1Arg2(markers.marker, format.toString, arg1, arg2)
        }
      } else {
        if (executePredicate()) {
          messageArg1Arg2(format.toString, arg1, arg2)
        }
      }
    }

    override def apply(
        format: => Message,
        args: Any*
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      warnIfChecked("Use apply(format, Arguments(args)) as Any* cannot be type checked")
      val markers = collateMarkers
      if (markers.nonEmpty) {
        if (executePredicate(markers.marker)) {
          markerMessageArgs(markers.marker, format.toString, args)
        }
      } else {
        if (executePredicate()) {
          messageArgs(format.toString, args)
        }
      }
    }

    override def apply(
        marker: Marker,
        msg: => Message
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val markers = collateMarkers(marker)
      if (executePredicate(markers.marker)) {
        markerMessage(markers.marker, msg.toString)
      }
    }

    override def apply(marker: Marker, msg: => Message, args: Arguments)(
        implicit line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      val markers = collateMarkers(marker)
      if (executePredicate(markers.marker)) {
        markerMessageArgs(markers.marker, msg.toString, args.asArray)
      }
    }

    override def apply(marker: Marker, format: => Message, arg1: Any, arg2: Any)(
        implicit line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      warnIfChecked(
        "Use apply(marker, format, Arguments(arg1, arg2)) as Any cannot be type checked"
      )
      val markers = collateMarkers(marker)
      if (executePredicate(markers.marker)) {
        markerMessageArg1Arg2(markers.marker, format.toString, arg1, arg2)
      }
    }

    override def apply(
        marker: Marker,
        format: => Message,
        args: Any*
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      warnIfChecked(
        s"Use apply(marker, format, Arguments(args)) as Any* cannot be type checked: ${file}:${line}"
      )
      val markers = collateMarkers(marker)
      if (executePredicate(markers.marker)) {
        markerMessageArgs(markers.marker, format.toString, args)
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

    override def when(condition: => Boolean)(block: UncheckedSLF4JMethod => Unit): Unit = {
      if (condition && executePredicate(collateMarkers.marker)) {
        block(this)
      }
    }

    override def toString: String = {
      s"${getClass.getName}(logger=${logger})"
    }
  }

  class Conditional(
      val level: Level,
      test: => Boolean,
      logger: ExtendedSLF4JLogger[UncheckedSLF4JMethod]
  ) extends UncheckedSLF4JMethod {

    private def parameterList = logger.parameterList(level)

    override def apply(
        msg: => Message
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
      parameterList.message(msg.toString)
    }

    override def apply(
        msg: => Message,
        args: Arguments
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
      parameterList.messageArgs(msg.toString, args.asArray)
    }

    override def apply(
        msg: => Message,
        arg1: Any,
        arg2: Any
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
      parameterList.messageArg1Arg2(msg.toString, arg1: Any, arg2)
    }

    override def apply(
        msg: => Message,
        args: Any*
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
      parameterList.messageArgs(msg.toString, args)
    }

    override def apply(
        marker: Marker,
        msg: => Message
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
      parameterList.markerMessage(marker, msg.toString)
    }

    override def apply(
        marker: Marker,
        msg: => Message,
        args: Arguments
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
      parameterList.markerMessageArgs(marker, msg.toString, args.asArray)
    }

    override def apply(marker: Marker, msg: => Message, arg1: Any, arg2: Any)(
        implicit line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = if (test) {
      parameterList.markerMessageArg1Arg2(marker, msg.toString, arg1, arg2)
    }

    override def apply(
        marker: Marker,
        msg: => Message,
        args: Any*
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = if (test) {
      parameterList.markerMessageArgs(marker, msg.toString, args)
    }

    override def when(condition: => Boolean)(block: UncheckedSLF4JMethod => Unit): Unit = {
      if (test && condition) {
        block(this)
      }
    }

    override def toString: String = {
      s"${getClass.getName}(logger=${logger})"
    }
  }
}
