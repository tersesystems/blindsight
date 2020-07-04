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

package com.tersesystems.blindsight.core

import com.tersesystems.blindsight.{Entry, Markers, Statement}
import org.slf4j.Marker
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

/**
 */
trait ParameterList {

  def executePredicate(): Boolean
  def executePredicate(marker: Marker): Boolean

  def message(msg: String)(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def messageArg1(msg: String, arg: Any)(implicit
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Unit

  def messageArg1Arg2(msg: String, arg1: Any, arg2: Any)(implicit
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Unit

  def messageArgs(msg: String, args: Array[Any])(implicit
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Unit

  def markerMessage(marker: Marker, msg: String)(implicit
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Unit

  def markerMessageArg1(marker: Marker, msg: String, arg: Any)(implicit
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Unit

  def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any)(implicit
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Unit

  def markerMessageArgs(marker: Marker, msg: String, args: Array[Any])(implicit
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Unit

  def executeStatement(
      statement: Statement
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit
}

object ParameterList {

  /**
   */
  trait ExecuteStatement { self: ParameterList =>
    def executeStatement(
        statement: Statement
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit =
      statement match {
        case Statement(markers, m, args, None) =>
          if (markers.isEmpty) {
            if (args.isEmpty) {
              message(m.toString)
            } else {
              messageArgs(m.toString, args.toArray)
            }
          } else {
            if (args.isEmpty) {
              markerMessage(markers.marker, m.toString)
            } else {
              markerMessageArgs(markers.marker, m.toString, args.toArray)
            }
          }

        case Statement(markers, m, args, Some(exception)) =>
          if (markers.isEmpty) {
            if (args.isEmpty) {
              messageArg1(m.toString, exception)
            } else {
              messageArgs(m.toString, args.toArray :+ exception)
            }
          } else {
            if (args.isEmpty) {
              markerMessageArg1(markers.marker, m.toString, exception)
            } else {
              markerMessageArgs(markers.marker, m.toString, args.toArray :+ exception)
            }
          }
      }
  }

  /**
   * Trace level.
   *
   * @param logger the SLF4J logger.
   */
  class Trace(logger: org.slf4j.Logger) extends ParameterList with ExecuteStatement {

    override def executePredicate(): Boolean = logger.isTraceEnabled()

    override def executePredicate(marker: Marker): Boolean =
      logger.isTraceEnabled(marker)

    override def message(msg: String)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
      logger.trace(msg)

    override def messageArg1(msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.trace(msg, arg)

    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.trace(msg, arg1, arg2)

    override def messageArgs(msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.trace(msg, args.asInstanceOf[Array[Object]]: _*)

    override def markerMessage(marker: Marker, msg: String)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.trace(marker, msg)

    override def markerMessageArg1(marker: Marker, msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.trace(marker, msg, arg)

    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.trace(marker, msg, arg1, arg2)

    override def markerMessageArgs(marker: Marker, msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.trace(marker, msg, args.asInstanceOf[Array[Object]]: _*)
  }

  /**
   * Debug level.
   *
   * @param logger the SLF4J logger.
   */
  class Debug(logger: org.slf4j.Logger) extends ParameterList with ExecuteStatement {

    override def executePredicate(): Boolean = logger.isDebugEnabled()

    override def executePredicate(marker: Marker): Boolean =
      logger.isDebugEnabled(marker)

    override def message(msg: String)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
      logger.debug(msg)

    override def messageArg1(msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.debug(msg, arg)

    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.debug(msg, arg1, arg2)

    override def messageArgs(msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.debug(msg, args.asInstanceOf[Array[Object]]: _*)

    override def markerMessage(marker: Marker, msg: String)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.debug(marker, msg)

    override def markerMessageArg1(marker: Marker, msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.debug(marker, msg, arg)

    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.debug(marker, msg, arg1, arg2)

    override def markerMessageArgs(marker: Marker, msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.debug(marker, msg, args.asInstanceOf[Array[Object]]: _*)
  }

  /**
   * Info level.
   *
   * @param logger the SLF4J logger.
   */
  class Info(logger: org.slf4j.Logger) extends ParameterList with ExecuteStatement {
    override def executePredicate(): Boolean = logger.isInfoEnabled
    override def executePredicate(marker: Marker): Boolean =
      logger.isInfoEnabled(marker)

    override def message(msg: String)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
      logger.info(msg)

    override def messageArg1(msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.info(msg, arg)

    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.info(msg, arg1, arg2)

    override def messageArgs(msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.info(msg, args.asInstanceOf[Array[Object]]: _*)

    override def markerMessage(marker: Marker, msg: String)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.info(marker, msg)

    override def markerMessageArg1(marker: Marker, msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.info(marker, msg, arg)

    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.info(marker, msg, arg1, arg2)

    override def markerMessageArgs(marker: Marker, msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.info(marker, msg, args.asInstanceOf[Array[Object]]: _*)
  }

  /**
   * Warn level.
   *
   * @param logger the SLF4J logger.
   */
  class Warn(logger: org.slf4j.Logger) extends ParameterList with ExecuteStatement {

    override def executePredicate(): Boolean = logger.isWarnEnabled()

    override def executePredicate(marker: Marker): Boolean =
      logger.isWarnEnabled(marker)

    override def message(msg: String)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
      logger.warn(msg)

    override def messageArg1(msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.warn(msg, arg)

    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.warn(msg, arg1, arg2)

    override def messageArgs(msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.warn(msg, args.asInstanceOf[Array[Object]]: _*)

    override def markerMessage(marker: Marker, msg: String)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.warn(marker, msg)

    override def markerMessageArg1(marker: Marker, msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.warn(marker, msg, arg)

    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.warn(marker, msg, arg1, arg2)

    override def markerMessageArgs(marker: Marker, msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.warn(marker, msg, args.asInstanceOf[Array[Object]]: _*)
  }

  /**
   * Error level.
   *
   * @param logger the SLF4J logger.
   */
  class Error(logger: org.slf4j.Logger) extends ParameterList with ExecuteStatement {

    override def executePredicate(): Boolean = logger.isErrorEnabled

    override def executePredicate(marker: Marker): Boolean =
      logger.isErrorEnabled(marker)

    override def message(msg: String)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
      logger.error(msg)

    override def messageArg1(msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.error(msg, arg)

    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.error(msg, arg1, arg2)

    override def messageArgs(msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.error(msg, args.asInstanceOf[Array[Object]]: _*)

    override def markerMessage(marker: Marker, msg: String)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.error(marker, msg)

    override def markerMessageArg1(marker: Marker, msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.error(marker, msg, arg)

    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.error(marker, msg, arg1, arg2)

    override def markerMessageArgs(marker: Marker, msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = logger.error(marker, msg, args.asInstanceOf[Array[Object]]: _*)
  }

  /**
   */
  class WithSource(behavior: SourceInfoBehavior, delegate: ParameterList)
      extends ParameterList
      with ExecuteStatement {

    def sourceInfoMarker(implicit line: Line, file: File, enclosing: Enclosing): Markers =
      behavior.apply(line, file, enclosing)

    def sourceInfoMarker(
        marker: org.slf4j.Marker
    )(implicit line: Line, file: File, enclosing: Enclosing): Markers = {
      Markers(marker) + behavior.apply(line, file, enclosing)
    }

    override def executePredicate(): Boolean = delegate.executePredicate()

    override def executePredicate(marker: Marker): Boolean =
      delegate.executePredicate(marker)

    override def message(msg: String)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
      delegate.markerMessage(sourceInfoMarker.marker, msg)

    override def messageArg1(msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = delegate.markerMessageArg1(sourceInfoMarker.marker, msg, arg)

    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = delegate.markerMessageArg1Arg2(sourceInfoMarker.marker, msg, arg1, arg2)

    override def messageArgs(msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = delegate.markerMessageArgs(sourceInfoMarker.marker, msg, args)

    override def markerMessage(marker: Marker, msg: String)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = delegate.markerMessage(sourceInfoMarker(marker).marker, msg)

    override def markerMessageArg1(marker: Marker, msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = delegate.markerMessageArg1(sourceInfoMarker(marker).marker, msg, arg)

    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = delegate.markerMessageArg1Arg2(sourceInfoMarker(marker).marker, msg, arg1, arg2)

    override def markerMessageArgs(marker: Marker, msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = delegate.markerMessageArgs(sourceInfoMarker(marker).marker, msg, args)
  }

  /**
   * A proxy can intercept log entries before they're sent to SLF4J and change the contents.
   *
   * @param delegate the delegate parameter list.
   * @param transform the transformation log entry.
   */
  class Proxy(val delegate: ParameterList, val transform: Entry => Entry) extends ParameterList {

    override def executePredicate(): Boolean = delegate.executePredicate()

    override def executePredicate(marker: Marker): Boolean = {
      delegate.executePredicate(marker)
    }

    override def message(
        msg: String
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      executeEntry(transform(Entry(None, msg, None)))
    }

    override def messageArg1(msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      executeEntry(transform(Entry(None, msg, Some(Array(arg)))))
    }

    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      executeEntry(transform(Entry(None, msg, Some(Array(arg1, arg2)))))
    }

    override def messageArgs(msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      executeEntry(transform(Entry(None, msg, Some(args))))
    }

    override def markerMessage(marker: Marker, msg: String)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      executeEntry(transform(Entry(Some(marker), msg, Some(Array.empty))))
    }

    override def markerMessageArg1(marker: Marker, msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      executeEntry(transform(Entry(Some(marker), msg, Some(Array(arg)))))
    }

    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      executeEntry(transform(Entry(Some(marker), msg, Some(Array(arg1, arg2)))))
    }

    override def markerMessageArgs(marker: Marker, msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      executeEntry(transform(Entry(Some(marker), msg, Some(args))))
    }

    override def executeStatement(
        statement: Statement
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val markers =
        if (statement.markers.isEmpty) None else Some(statement.markers.marker)
      val message = statement.message.toString
      val args = statement.throwable match {
        case Some(t) =>
          statement.arguments.toArray :+ t
        case None =>
          statement.arguments.toArray
      }
      val raw = Entry(markers, message, Option(args))
      executeEntry(transform(raw))
    }

    def executeEntry(entry: Entry)(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      entry match {
        case Entry(None, m, None) =>
          delegate.message(m)
        case Entry(None, m, Some(args)) =>
          delegate.messageArgs(m, args)
        case Entry(Some(marker), m, None) =>
          delegate.markerMessage(marker, m)
        case Entry(Some(marker), m, Some(args)) =>
          delegate.markerMessageArgs(marker, m, args)
      }
    }
  }

  /**
   * State marker parameter list
   *
   * @param m state marker
   * @param delegate delegate parameter list
   */
  class StateMarker(m: Markers, delegate: ParameterList) extends ParameterList {
    override def executePredicate(): Boolean =
      delegate.executePredicate(m.marker)

    override def executePredicate(marker: Marker): Boolean = {
      val markers: Markers = collateMarkers(marker)
      delegate.executePredicate(markers.marker)
    }

    override def message(msg: String)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
      delegate.markerMessage(m.marker, msg)

    override def messageArg1(msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit =
      delegate.markerMessageArg1(m.marker, msg, arg)

    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit =
      delegate.markerMessageArg1Arg2(m.marker, msg, arg1, arg2)

    override def messageArgs(msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit =
      delegate.markerMessageArgs(m.marker, msg, args)

    override def markerMessage(marker: Marker, msg: String)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      val markers: Markers = collateMarkers(marker)
      delegate.markerMessage(markers.marker, msg)
    }

    override def markerMessageArg1(marker: Marker, msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      val markers: Markers = collateMarkers(marker)
      delegate.markerMessageArg1(markers.marker, msg, arg)
    }

    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      val markers: Markers = collateMarkers(marker)
      delegate.markerMessageArg1Arg2(markers.marker, msg, arg1, arg2)
    }

    override def markerMessageArgs(marker: Marker, msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      val markers: Markers = collateMarkers(marker)
      delegate.markerMessageArgs(markers.marker, msg, args)
    }

    override def executeStatement(
        statement: Statement
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      val markers = m + statement.markers
      delegate.executeStatement(statement.withMarkers(markers))
    }

    private def collateMarkers(marker: Marker) = m + Markers(marker)
  }

  class Conditional(level: Level, core: CoreLogger) extends ParameterList {
    private val parameterList: ParameterList = core.parameterList(level)

    override def executePredicate(): Boolean = {
      core.condition(level, core.markers) && parameterList.executePredicate()
    }

    override def executePredicate(marker: Marker): Boolean = {
      core.condition(level, core.markers) && parameterList.executePredicate(marker)
    }

    override def message(
        msg: String
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (core.condition(level, core.markers))
        parameterList.message(msg)
    }

    override def messageArg1(msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      if (core.condition(level, core.markers))
        parameterList.messageArg1(msg, arg)
    }

    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      if (core.condition(level, core.markers))
        parameterList.messageArg1Arg2(msg, arg1, arg2)
    }

    override def messageArgs(msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      if (core.condition(level, core.markers))
        parameterList.messageArgs(msg, args)
    }

    override def markerMessage(marker: Marker, msg: String)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      if (core.condition(level, core.markers))
        parameterList.markerMessage(marker, msg)
    }

    override def markerMessageArg1(marker: Marker, msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      if (core.condition(level, core.markers))
        parameterList.markerMessageArg1(marker, msg, arg)
    }

    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      if (core.condition(level, core.markers))
        parameterList.markerMessageArg1Arg2(marker, msg, arg1, arg2)
    }

    override def markerMessageArgs(marker: Marker, msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      if (core.condition(level, core.markers))
        parameterList.markerMessageArgs(marker, msg, args)
    }

    override def executeStatement(
        statement: Statement
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (core.condition(level, core.markers))
        parameterList.executeStatement(statement)
    }
  }

  object Noop extends ParameterList {
    override def executePredicate(): Boolean = false

    override def executePredicate(marker: Marker): Boolean = false

    override def message(msg: String)(implicit line: Line, file: File, enclosing: Enclosing): Unit =
      ()

    override def messageArg1(msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = ()

    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = ()

    override def messageArgs(msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = ()

    override def markerMessage(marker: Marker, msg: String)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = ()

    override def markerMessageArg1(marker: Marker, msg: String, arg: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = ()

    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit =
      ()

    override def markerMessageArgs(marker: Marker, msg: String, args: Array[Any])(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = ()

    override def executeStatement(
        statement: Statement
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = ()
  }

}
