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

package com.tersesystems.blindsight

import org.slf4j.Marker
import org.slf4j.event.Level

trait ParameterList {

  def executePredicate(): Boolean
  def executePredicate(marker: Marker): Boolean

  def message(msg: String): Unit
  def messageArg1(msg: String, arg: Any): Unit
  def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit
  def messageArgs(msg: String, args: Array[Any]): Unit
  def markerMessage(marker: Marker, msg: String): Unit
  def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit
  def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit
  def markerMessageArgs(marker: Marker, msg: String, args: Array[Any]): Unit

  def executeStatement(statement: Statement): Unit
}

object ParameterList {

  /**
   * Indexed by enum ordinal, i.e. to look up, use Level.TRACE.ordinal() as index.
   */
  def lists(logger: org.slf4j.Logger): Array[ParameterList] =
    Array(
      new ParameterList.Error(logger),
      new ParameterList.Warn(logger),
      new ParameterList.Info(logger),
      new ParameterList.Debug(logger),
      new ParameterList.Trace(logger)
    )

  trait ExecuteStatement { self: ParameterList =>
    def executeStatement(statement: Statement): Unit =
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
              val arrayPlusEx: Array[Any] = args.toArray :+ exception
              messageArgs(m.toString, arrayPlusEx)
            }
          } else {
            if (args.isEmpty) {
              markerMessageArg1(markers.marker, m.toString, exception)
            } else {
              val arrayPlusEx: Array[Any] = args.toArray :+ exception
              markerMessageArgs(markers.marker, m.toString, arrayPlusEx)
            }
          }
      }
  }

  class Trace(logger: org.slf4j.Logger) extends ParameterList with ExecuteStatement {
    override def executePredicate(): Boolean = {
      logger.isTraceEnabled()
    }
    override def executePredicate(marker: Marker): Boolean = {
      logger.isTraceEnabled(marker)
    }

    override def message(msg: String): Unit               = logger.trace(msg)
    override def messageArg1(msg: String, arg: Any): Unit = logger.trace(msg, arg)
    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit =
      logger.trace(msg, arg1, arg2)
    override def messageArgs(msg: String, args: Array[Any]): Unit =
      logger.trace(msg, args.asInstanceOf[Array[Object]]: _*)
    override def markerMessage(marker: Marker, msg: String): Unit = logger.trace(marker, msg)
    override def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit =
      logger.trace(marker, msg, arg)
    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit =
      logger.trace(marker, msg, arg1, arg2)
    override def markerMessageArgs(marker: Marker, msg: String, args: Array[Any]): Unit =
      logger.trace(marker, msg, args.asInstanceOf[Array[Object]]: _*)
  }

  class Debug(logger: org.slf4j.Logger) extends ParameterList with ExecuteStatement {
    override def executePredicate(): Boolean               = logger.isDebugEnabled()
    override def executePredicate(marker: Marker): Boolean = logger.isDebugEnabled(marker)

    override def message(msg: String): Unit               = logger.debug(msg)
    override def messageArg1(msg: String, arg: Any): Unit = logger.debug(msg, arg)
    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit =
      logger.debug(msg, arg1, arg2)
    override def messageArgs(msg: String, args: Array[Any]): Unit =
      logger.debug(msg, args.asInstanceOf[Array[Object]]: _*)
    override def markerMessage(marker: Marker, msg: String): Unit = logger.debug(marker, msg)
    override def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit =
      logger.debug(marker, msg, arg)
    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit =
      logger.debug(marker, msg, arg1, arg2)
    override def markerMessageArgs(marker: Marker, msg: String, args: Array[Any]): Unit =
      logger.debug(marker, msg, args.asInstanceOf[Array[Object]]: _*)
  }

  class Info(logger: org.slf4j.Logger) extends ParameterList with ExecuteStatement {
    override def executePredicate(): Boolean               = logger.isInfoEnabled
    override def executePredicate(marker: Marker): Boolean = logger.isInfoEnabled(marker)

    override def message(msg: String): Unit               = logger.info(msg)
    override def messageArg1(msg: String, arg: Any): Unit = logger.info(msg, arg)
    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit =
      logger.info(msg, arg1, arg2)
    override def messageArgs(msg: String, args: Array[Any]): Unit =
      logger.info(msg, args.asInstanceOf[Array[Object]]: _*)
    override def markerMessage(marker: Marker, msg: String): Unit = logger.info(marker, msg)
    override def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit =
      logger.info(marker, msg, arg)
    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit =
      logger.info(marker, msg, arg1, arg2)
    override def markerMessageArgs(marker: Marker, msg: String, args: Array[Any]): Unit =
      logger.info(marker, msg, args.asInstanceOf[Array[Object]]: _*)
  }

  class Warn(logger: org.slf4j.Logger) extends ParameterList with ExecuteStatement {
    override def executePredicate(): Boolean               = logger.isWarnEnabled()
    override def executePredicate(marker: Marker): Boolean = logger.isWarnEnabled(marker)

    override def message(msg: String): Unit               = logger.warn(msg)
    override def messageArg1(msg: String, arg: Any): Unit = logger.warn(msg, arg)
    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit =
      logger.warn(msg, arg1, arg2)
    override def messageArgs(msg: String, args: Array[Any]): Unit =
      logger.warn(msg, args.asInstanceOf[Array[Object]]: _*)
    override def markerMessage(marker: Marker, msg: String): Unit = logger.warn(marker, msg)
    override def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit =
      logger.warn(marker, msg, arg)
    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit =
      logger.warn(marker, msg, arg1, arg2)
    override def markerMessageArgs(marker: Marker, msg: String, args: Array[Any]): Unit =
      logger.warn(marker, msg, args.asInstanceOf[Array[Object]]: _*)
  }

  class Error(logger: org.slf4j.Logger) extends ParameterList with ExecuteStatement {
    override def executePredicate(): Boolean               = logger.isErrorEnabled
    override def executePredicate(marker: Marker): Boolean = logger.isErrorEnabled(marker)

    override def message(msg: String): Unit               = logger.error(msg)
    override def messageArg1(msg: String, arg: Any): Unit = logger.error(msg, arg)
    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit =
      logger.error(msg, arg1, arg2)
    override def messageArgs(msg: String, args: Array[Any]): Unit =
      logger.error(msg, args.asInstanceOf[Array[Object]]: _*)
    override def markerMessage(marker: Marker, msg: String): Unit = logger.error(marker, msg)
    override def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit =
      logger.error(marker, msg, arg)
    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit =
      logger.error(marker, msg, arg1, arg2)
    override def markerMessageArgs(marker: Marker, msg: String, args: Array[Any]): Unit =
      logger.error(marker, msg, args.asInstanceOf[Array[Object]]: _*)
  }

  /**
   * A spy can intercept log entries before they're sent to SLF4J and change the contents.
   *
   * @param delegate the delegate parameter list.
   * @param transform the transformation log entry.
   */
  class Spy(delegate: ParameterList, transform: LogEntry => LogEntry)
      extends ParameterList {
    override def executePredicate(): Boolean = delegate.executePredicate()

    override def executePredicate(marker: Marker): Boolean = {
      delegate.executePredicate(marker)
    }

    override def message(msg: String): Unit = {
      executeLogEntry(transform(LogEntry(None, msg, Array.empty)))
    }

    override def messageArg1(msg: String, arg: Any): Unit = {
      executeLogEntry(transform(LogEntry(None, msg, Array(arg))))
    }

    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit = {
      executeLogEntry(transform(LogEntry(None, msg, Array(arg1, arg2))))
    }

    override def messageArgs(msg: String, args: Array[Any]): Unit = {
      executeLogEntry(transform(LogEntry(None, msg, Array(args))))
    }

    override def markerMessage(marker: Marker, msg: String): Unit = {
      executeLogEntry(transform(LogEntry(Some(marker), msg, Array.empty)))
    }

    override def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit = {
      executeLogEntry(transform(LogEntry(Some(marker), msg, Array(arg))))
    }

    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit = {
      executeLogEntry(transform(LogEntry(Some(marker), msg, Array(arg1, arg2))))
    }

    override def markerMessageArgs(marker: Marker, msg: String, args: Array[Any]): Unit = {
      executeLogEntry(transform(LogEntry(Some(marker), msg, args)))
    }

    override def executeStatement(statement: Statement): Unit = {
      val markers = if (statement.markers.isEmpty) None else Some(statement.markers.marker)
      val message = statement.message.toString
      val args = statement.throwable match {
        case Some(t) =>
          statement.arguments.toArray :+ t
        case None =>
          statement.arguments.toArray
      }
      val raw = LogEntry(markers, message, args)
      executeLogEntry(transform(raw))
    }

    def executeLogEntry(entry: LogEntry): Unit = {
      entry match {
        case LogEntry(None, m, Array()) =>
          delegate.message(m)
        case LogEntry(None, m, args) =>
          delegate.messageArgs(m, args)
        case LogEntry(Some(marker), m, Array()) =>
          delegate.markerMessage(marker, m)
        case LogEntry(Some(marker), m, args) =>
          delegate.markerMessageArgs(marker, m, args)
      }
    }
  }

  object Noop extends ParameterList {
    override def executePredicate(): Boolean = false

    override def executePredicate(marker: Marker): Boolean = false

    override def message(msg: String): Unit = ()

    override def messageArg1(msg: String, arg: Any): Unit = ()

    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit = ()

    override def messageArgs(msg: String, args: Array[Any]): Unit = ()

    override def markerMessage(marker: Marker, msg: String): Unit = ()

    override def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit = ()

    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit = ()

    override def markerMessageArgs(marker: Marker, msg: String, args: Array[Any]): Unit = ()

    override def executeStatement(statement: Statement): Unit = ()
  }

  class Conditional(level: Level, core: CoreLogger) extends ParameterList {

    override def executePredicate(): Boolean = {
      core.condition(level, core.state) && core.parameterList(level).executePredicate()
    }
    override def executePredicate(marker: Marker): Boolean = {
      core.condition(level, core.state) && core.parameterList(level).executePredicate(marker)
    }

    override def message(msg: String): Unit =
      if (core.condition(level, core.state)) core.parameterList(level).message(msg)
    override def messageArg1(msg: String, arg: Any): Unit =
      if (core.condition(level, core.state)) core.parameterList(level).messageArg1(msg, arg)
    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit =
      if (core.condition(level, core.state))
        core.parameterList(level).messageArg1Arg2(msg, arg1, arg2)
    override def messageArgs(msg: String, args: Array[Any]): Unit =
      if (core.condition(level, core.state)) core.parameterList(level).messageArgs(msg, args)
    override def markerMessage(marker: Marker, msg: String): Unit =
      if (core.condition(level, core.state)) core.parameterList(level).markerMessage(marker, msg)
    override def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit =
      if (core.condition(level, core.state))
        core.parameterList(level).markerMessageArg1(marker, msg, arg)
    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit =
      if (core.condition(level, core.state))
        core.parameterList(level).markerMessageArg1Arg2(marker, msg, arg1, arg2)
    override def markerMessageArgs(marker: Marker, msg: String, args: Array[Any]): Unit =
      if (core.condition(level, core.state))
        core.parameterList(level).markerMessageArgs(marker, msg, args)

    override def executeStatement(statement: Statement): Unit =
      if (core.condition(level, core.state)) core.parameterList(level).executeStatement(statement)
  }
}
