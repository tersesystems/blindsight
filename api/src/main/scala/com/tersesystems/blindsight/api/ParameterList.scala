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

package com.tersesystems.blindsight.api

import org.slf4j.Marker
import org.slf4j.event.Level

import scala.collection.JavaConverters._

/**
 * This is the calling site of the SLF4J method, where parameters and arguments meet.
 *
 * You should not need to use this as an end user, but it is very useful for extending loggers.
 */
abstract class ParameterList(val level: Level, val logger: org.slf4j.Logger) {
  def executePredicate(): Boolean
  def executePredicate(marker: Marker): Boolean

  def message(msg: String): Unit
  def messageArg1(msg: String, arg: Any): Unit
  def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit
  def messageArgs(msg: String, args: Seq[_]): Unit
  def markerMessage(marker: Marker, msg: String): Unit
  def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit
  def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit
  def markerMessageArgs(marker: Marker, msg: String, args: Seq[_]): Unit

  def executeStatement(statement: Statement): Unit =
    statement match {
      case Statement(markers, message, args, None) =>
        if (markers.isEmpty) {
          messageArgs(message.toString, args.toArray)
        } else {
          markerMessageArgs(markers.marker, message.toString, args.toArray)
        }

      case Statement(markers, message, args, Some(exception)) =>
        if (markers.isEmpty) {
          messageArgs(message.toString, args.toArray :+ exception)
        } else {
          markerMessageArgs(markers.marker, message.toString, args.toArray :+ exception)
        }
    }
}

object ParameterList {

  /**
   * Indexed by enum ordinal, i.e. to look up, use Level.TRACE.ordinal() as index.
   */
  def lists(logger: org.slf4j.Logger): Array[ParameterList] = Array(
    new ParameterList.Error(logger),
    new ParameterList.Warn(logger),
    new ParameterList.Info(logger),
    new ParameterList.Debug(logger),
    new ParameterList.Trace(logger)
  )

  class Trace(logger: org.slf4j.Logger) extends ParameterList(Level.TRACE, logger) {
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
    override def messageArgs(msg: String, args: Seq[_]): Unit =
      logger.trace(msg, args.asJava.toArray: _*)
    override def markerMessage(marker: Marker, msg: String): Unit = logger.trace(marker, msg)
    override def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit =
      logger.trace(marker, msg, arg)
    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit =
      logger.trace(marker, msg, arg1, arg2)
    override def markerMessageArgs(marker: Marker, msg: String, args: Seq[_]): Unit =
      logger.trace(marker, msg, args.asJava.toArray: _*)
  }

  class Debug(logger: org.slf4j.Logger) extends ParameterList(Level.DEBUG, logger) {
    override def executePredicate(): Boolean               = logger.isDebugEnabled()
    override def executePredicate(marker: Marker): Boolean = logger.isDebugEnabled(marker)

    override def message(msg: String): Unit               = logger.debug(msg)
    override def messageArg1(msg: String, arg: Any): Unit = logger.debug(msg, arg)
    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit =
      logger.debug(msg, arg1, arg2)
    override def messageArgs(msg: String, args: Seq[_]): Unit =
      logger.debug(msg, args.asJava.toArray: _*)
    override def markerMessage(marker: Marker, msg: String): Unit = logger.debug(marker, msg)
    override def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit =
      logger.debug(marker, msg, arg)
    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit =
      logger.debug(marker, msg, arg1, arg2)
    override def markerMessageArgs(marker: Marker, msg: String, args: Seq[_]): Unit =
      logger.debug(marker, msg, args.asJava.toArray: _*)
  }

  class Info(logger: org.slf4j.Logger) extends ParameterList(Level.INFO, logger) {
    override def executePredicate(): Boolean               = logger.isInfoEnabled
    override def executePredicate(marker: Marker): Boolean = logger.isInfoEnabled(marker)

    override def message(msg: String): Unit               = logger.info(msg)
    override def messageArg1(msg: String, arg: Any): Unit = logger.info(msg, arg)
    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit =
      logger.info(msg, arg1, arg2)
    override def messageArgs(msg: String, args: Seq[_]): Unit =
      logger.info(msg, args.asJava.toArray: _*)
    override def markerMessage(marker: Marker, msg: String): Unit = logger.info(marker, msg)
    override def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit =
      logger.info(marker, msg, arg)
    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit =
      logger.info(marker, msg, arg1, arg2)
    override def markerMessageArgs(marker: Marker, msg: String, args: Seq[_]): Unit =
      logger.info(marker, msg, args.asJava.toArray: _*)
  }

  class Warn(logger: org.slf4j.Logger) extends ParameterList(Level.WARN, logger) {
    override def executePredicate(): Boolean               = logger.isWarnEnabled()
    override def executePredicate(marker: Marker): Boolean = logger.isWarnEnabled(marker)

    override def message(msg: String): Unit               = logger.warn(msg)
    override def messageArg1(msg: String, arg: Any): Unit = logger.warn(msg, arg)
    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit =
      logger.warn(msg, arg1, arg2)
    override def messageArgs(msg: String, args: Seq[_]): Unit =
      logger.warn(msg, args.asJava.toArray: _*)
    override def markerMessage(marker: Marker, msg: String): Unit = logger.warn(marker, msg)
    override def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit =
      logger.warn(marker, msg, arg)
    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit =
      logger.warn(marker, msg, arg1, arg2)
    override def markerMessageArgs(marker: Marker, msg: String, args: Seq[_]): Unit =
      logger.warn(marker, msg, args.asJava.toArray: _*)
  }

  class Error(logger: org.slf4j.Logger) extends ParameterList(Level.ERROR, logger) {
    override def executePredicate(): Boolean               = logger.isErrorEnabled
    override def executePredicate(marker: Marker): Boolean = logger.isErrorEnabled(marker)

    override def message(msg: String): Unit               = logger.error(msg)
    override def messageArg1(msg: String, arg: Any): Unit = logger.error(msg, arg)
    override def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit =
      logger.error(msg, arg1, arg2)
    override def messageArgs(msg: String, args: Seq[_]): Unit =
      logger.error(msg, args.asJava.toArray: _*)
    override def markerMessage(marker: Marker, msg: String): Unit = logger.error(marker, msg)
    override def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit =
      logger.error(marker, msg, arg)
    override def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit =
      logger.error(marker, msg, arg1, arg2)
    override def markerMessageArgs(marker: Marker, msg: String, args: Seq[_]): Unit =
      logger.error(marker, msg, args.asJava.toArray: _*)
  }
}
