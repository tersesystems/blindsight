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
import com.tersesystems.blindsight.mixins.SourceInfoMixin
import org.slf4j.Marker
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

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
  def when(condition: => Boolean)(block: UncheckedSLF4JMethod => Unit): Unit

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
  class Impl(val level: Level, logger: LoggerState)
      extends UncheckedSLF4JMethod
      with SourceInfoMixin {

    @inline
    protected def markers: Markers = logger.markers

    val parameterList: ParameterList = logger.parameterList(level)

    import parameterList._

    private def collateMarkers(implicit line: Line, file: File, enclosing: Enclosing): Markers = {
      val sourceMarker: Markers = sourceInfoMarker(level, line, file, enclosing)
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

    override def apply(
        msg: String
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val markers = collateMarkers
      if (markers.nonEmpty) {
        if (executePredicate(markers.marker)) {
          markerMessage(markers.marker, msg)
        }
      } else {
        if (executePredicate()) {
          message(msg)
        }
      }
    }

    override def apply(
        format: String,
        arg: Any
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val markers: Markers = collateMarkers
      if (markers.nonEmpty) {
        if (executePredicate(markers.marker)) {
          markerMessageArg1(markers.marker, format, arg)
        }
      } else {
        if (executePredicate()) {
          messageArg1(format, arg)
        }
      }
    }

    override def apply(
        format: String,
        arg1: Any,
        arg2: Any
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      warnIfChecked(
        "Use apply(format, Arguments(arg1, arg2)) as Any cannot be type checked"
      )
      val markers: Markers = collateMarkers
      if (markers.nonEmpty) {
        if (executePredicate(markers.marker)) {
          markerMessageArg1Arg2(markers.marker, format, arg1, arg2)
        }
      } else {
        if (executePredicate()) {
          messageArg1Arg2(format, arg1, arg2)
        }
      }
    }

    override def apply(
        format: String,
        args: Arguments
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      warnIfChecked("Use apply(format, Arguments(args)) as Any* cannot be type checked")
      val markers = collateMarkers
      if (markers.nonEmpty) {
        if (executePredicate(markers.marker)) {
          markerMessageArgs(markers.marker, format, args.toArray)
        }
      } else {
        if (executePredicate()) {
          messageArgs(format, args.toArray)
        }
      }
    }

    override def apply(
        marker: Marker,
        msg: String
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val markers = collateMarkers(marker)
      if (executePredicate(markers.marker)) {
        markerMessage(markers.marker, msg)
      }
    }

    override def apply(marker: Marker, msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      val markers = collateMarkers(marker)
      if (executePredicate(markers.marker)) {
        markerMessageArg1(markers.marker, msg, arg)
      }
    }

    override def apply(marker: Marker, format: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      warnIfChecked(
        "Use apply(marker, format, Arguments(arg1, arg2)) as Any cannot be type checked"
      )
      val markers = collateMarkers(marker)
      if (executePredicate(markers.marker)) {
        markerMessageArg1Arg2(markers.marker, format, arg1, arg2)
      }
    }

    override def apply(
        marker: Marker,
        format: String,
        args: Arguments
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      warnIfChecked(
        s"Use apply(marker, format, Arguments(args)) as Any* cannot be type checked: ${file}:${line}"
      )
      val markers = collateMarkers(marker)
      if (executePredicate(markers.marker)) {
        markerMessageArgs(markers.marker, format, args.toArray)
      }
    }

    override def toString: String = {
      s"${getClass.getName}(logger=$logger)"
    }
  }

  /**
   * Conditional method implementation.  Only calls when test evaluates to true.
   */
  class Conditional(
      level: Level,
      logger: LoggerState
  ) extends Impl(level, logger) {

    override val parameterList: ParameterList =
      new ParameterList.Conditional(logger.condition.get, logger.parameterList(level))

    override def when(condition: => Boolean)(block: UncheckedSLF4JMethod => Unit): Unit = {
      if (logger.condition.get() && condition) {
        block(this)
      }
    }

    override def toString: String = {
      s"${getClass.getName}(logger=$logger)"
    }
  }
}
