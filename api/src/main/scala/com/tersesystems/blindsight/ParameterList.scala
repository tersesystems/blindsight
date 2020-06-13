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

import java.lang.invoke.MethodHandles.lookup
import java.lang.invoke.MethodType.methodType
import java.lang.invoke.{MethodHandle, MethodType}

import org.slf4j.{Logger, Marker}
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
  // https://wiki.openjdk.java.net/display/HotSpot/Method+handle+invocation

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

  object Invokers {
    import InliningCacheInvoker.createInvoker

    val predicate = createInvoker(3, methodType(classOf[Boolean], classOf[Logger]))
    val predicateMarker =
      createInvoker(3, methodType(classOf[Boolean], classOf[Logger], classOf[Marker]))

    val message = createInvoker(3, methodType(classOf[Unit], classOf[Logger], classOf[String]))
    val messageArg1 =
      createInvoker(3, methodType(classOf[Unit], classOf[Logger], classOf[String], classOf[Object]))
    val messageArg1Arg2 = createInvoker(
      3,
      methodType(classOf[Unit], classOf[Logger], classOf[String], classOf[Object], classOf[Object])
    )
    val messageArgs = createInvoker(
      3,
      methodType(classOf[Unit], classOf[Logger], classOf[String], classOf[Array[Object]])
    )
    val markerMessage =
      createInvoker(3, methodType(classOf[Unit], classOf[Logger], classOf[Marker], classOf[String]))
    val markerMessageArg1 = createInvoker(
      3,
      methodType(classOf[Unit], classOf[Logger], classOf[Marker], classOf[String], classOf[Object])
    )
    val markerMessageArg1Arg2 = createInvoker(
      3,
      methodType(
        classOf[Unit],
        classOf[Logger],
        classOf[Marker],
        classOf[String],
        classOf[Object],
        classOf[Object]
      )
    )
    val markerMessageArgs = createInvoker(
      3,
      methodType(
        classOf[Unit],
        classOf[Logger],
        classOf[Marker],
        classOf[String],
        classOf[Array[Object]]
      )
    )
  }

  object Types {
    val message: MethodType     = methodType(classOf[Unit], classOf[String])
    val messageArg1: MethodType = methodType(classOf[Unit], classOf[String], classOf[Object])
    val messageArg1Arg2: MethodType =
      methodType(classOf[Unit], classOf[String], classOf[Object], classOf[Object])
    val messageArgs: MethodType = methodType(classOf[Unit], classOf[String], classOf[Array[Object]])

    val markerMessage: MethodType = methodType(classOf[Unit], classOf[Marker], classOf[String])
    val markerMessageArg1: MethodType =
      methodType(classOf[Unit], classOf[Marker], classOf[String], classOf[Object])
    val markerMessageArg1Arg2: MethodType =
      methodType(classOf[Unit], classOf[Marker], classOf[String], classOf[Object], classOf[Object])
    val markerMessageArgs: MethodType =
      methodType(classOf[Unit], classOf[Marker], classOf[String], classOf[Array[Object]])
  }

  object Handles {
    private def handlesFor(argType: MethodType): Array[MethodHandle] = {
      def handleFor(level: Level): MethodHandle =
        lookup.findVirtual(classOf[Logger], level.toString.toLowerCase, argType)

      Level.values.sortBy(_.ordinal()).map(handleFor)
    }

    val message: Array[MethodHandle]         = handlesFor(Types.message)
    val messageArg1: Array[MethodHandle]     = handlesFor(Types.messageArg1)
    val messageArg1Arg2: Array[MethodHandle] = handlesFor(Types.messageArg1Arg2)
    val messageArgs: Array[MethodHandle]     = handlesFor(Types.messageArgs)

    val markerMessage: Array[MethodHandle]         = handlesFor(Types.markerMessage)
    val markerMessageArg1: Array[MethodHandle]     = handlesFor(Types.markerMessageArg1)
    val markerMessageArg1Arg2: Array[MethodHandle] = handlesFor(Types.markerMessageArg1Arg2)
    val markerMessageArgs: Array[MethodHandle]     = handlesFor(Types.markerMessageArgs)
  }

  final class Trace(logger: org.slf4j.Logger) extends ParameterList {

    import Trace._

    def executePredicate(): Boolean =
      Invokers.predicate.invokeExact(predicateHandle, logger)

    def executePredicate(marker: Marker): Boolean =
      Invokers.predicateMarker.invokeExact(predicateMarkerHandle, logger, marker)

    def message(msg: String): Unit =
      Invokers.message.invokeExact(messageHandle, logger, msg)

    def messageArg1(msg: String, arg: Any): Unit =
      Invokers.messageArg1.invokeExact(messageArg1Handle, logger, msg, arg)

    def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit =
      Invokers.messageArg1Arg2.invokeExact(messageArg1Arg2Handle, logger, msg, arg1, arg2)

    def messageArgs(msg: String, args: Array[Any]): Unit =
      Invokers.messageArgs.invokeExact(messageArgsHandle, logger, msg, args.toArray)

    def markerMessage(marker: Marker, msg: String): Unit =
      Invokers.markerMessage.invokeExact(markerMessageHandle, logger, marker, msg)

    def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit =
      Invokers.markerMessageArg1.invokeExact(markerMessageArg1Handle, logger, marker, msg, arg)

    def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit =
      Invokers.markerMessageArg1Arg2.invokeExact(
        markerMessageArg1Arg2Handle,
        logger,
        marker,
        msg,
        arg1,
        arg2
      )

    def markerMessageArgs(marker: Marker, msg: String, args: Array[Any]): Unit =
      Invokers.markerMessageArgs.invokeExact(
        markerMessageArgsHandle,
        logger,
        marker,
        msg,
        args.toArray
      )
  }

  object Trace {
    private val predicateHandle =
      lookup.findVirtual(classOf[Logger], "isTraceEnabled", methodType(classOf[Boolean]))
    private val predicateMarkerHandle = lookup.findVirtual(
      classOf[Logger],
      "isTraceEnabled",
      methodType(classOf[Boolean], classOf[Marker])
    )

    private val levelIndex                  = Level.TRACE.ordinal()
    private val messageHandle               = Handles.message(levelIndex)
    private val messageArg1Handle           = Handles.messageArg1(levelIndex)
    private val messageArg1Arg2Handle       = Handles.messageArg1Arg2(levelIndex)
    private val messageArgsHandle           = Handles.messageArgs(levelIndex)
    private val markerMessageHandle         = Handles.markerMessage(levelIndex)
    private val markerMessageArg1Handle     = Handles.markerMessageArg1(levelIndex)
    private val markerMessageArg1Arg2Handle = Handles.markerMessageArg1Arg2(levelIndex)
    private val markerMessageArgsHandle     = Handles.markerMessageArgs(levelIndex)
  }

  final class Debug(logger: org.slf4j.Logger) extends ParameterList {

    import Debug._

    def executePredicate(): Boolean =
      Invokers.predicate.invokeExact(predicateHandle, logger)

    def executePredicate(marker: Marker): Boolean =
      Invokers.predicateMarker.invokeExact(predicateMarkerHandle, logger, marker)

    def message(msg: String): Unit =
      Invokers.message.invokeExact(messageHandle, logger, msg)

    def messageArg1(msg: String, arg: Any): Unit =
      Invokers.messageArg1.invokeExact(messageArg1Handle, logger, msg, arg)

    def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit =
      Invokers.messageArg1Arg2.invokeExact(messageArg1Arg2Handle, logger, msg, arg1, arg2)

    def messageArgs(msg: String, args: Array[Any]): Unit =
      Invokers.messageArgs.invokeExact(messageArgsHandle, logger, msg, args.toArray)

    def markerMessage(marker: Marker, msg: String): Unit =
      Invokers.markerMessage.invokeExact(markerMessageHandle, logger, marker, msg)

    def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit =
      Invokers.markerMessageArg1.invokeExact(markerMessageArg1Handle, logger, marker, msg, arg)

    def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit =
      Invokers.markerMessageArg1Arg2.invokeExact(
        markerMessageArg1Arg2Handle,
        logger,
        marker,
        msg,
        arg1,
        arg2
      )

    def markerMessageArgs(marker: Marker, msg: String, args: Array[Any]): Unit =
      Invokers.markerMessageArgs.invokeExact(
        markerMessageArgsHandle,
        logger,
        marker,
        msg,
        args.toArray
      )
  }

  object Debug {
    private val predicateHandle =
      lookup.findVirtual(classOf[Logger], "isDebugEnabled", methodType(classOf[Boolean]))
    private val predicateMarkerHandle = lookup.findVirtual(
      classOf[Logger],
      "isDebugEnabled",
      methodType(classOf[Boolean], classOf[Marker])
    )

    private val levelIndex                  = Level.DEBUG.ordinal()
    private val messageHandle               = Handles.message(levelIndex)
    private val messageArg1Handle           = Handles.messageArg1(levelIndex)
    private val messageArg1Arg2Handle       = Handles.messageArg1Arg2(levelIndex)
    private val messageArgsHandle           = Handles.messageArgs(levelIndex)
    private val markerMessageHandle         = Handles.markerMessage(levelIndex)
    private val markerMessageArg1Handle     = Handles.markerMessageArg1(levelIndex)
    private val markerMessageArg1Arg2Handle = Handles.markerMessageArg1Arg2(levelIndex)
    private val markerMessageArgsHandle     = Handles.markerMessageArgs(levelIndex)
  }

  final class Info(logger: org.slf4j.Logger) extends ParameterList {

    import Info._

    def executePredicate(): Boolean =
      Invokers.predicate.invokeExact(predicateHandle, logger)

    def executePredicate(marker: Marker): Boolean =
      Invokers.predicateMarker.invokeExact(predicateMarkerHandle, logger, marker)

    def message(msg: String): Unit =
      Invokers.message.invokeExact(messageHandle, logger, msg)

    def messageArg1(msg: String, arg: Any): Unit =
      Invokers.messageArg1.invokeExact(messageArg1Handle, logger, msg, arg)

    def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit =
      Invokers.messageArg1Arg2.invokeExact(messageArg1Arg2Handle, logger, msg, arg1, arg2)

    def messageArgs(msg: String, args: Array[Any]): Unit =
      Invokers.messageArgs.invokeExact(messageArgsHandle, logger, msg, args.toArray)

    def markerMessage(marker: Marker, msg: String): Unit =
      Invokers.markerMessage.invokeExact(markerMessageHandle, logger, marker, msg)

    def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit =
      Invokers.markerMessageArg1.invokeExact(markerMessageArg1Handle, logger, marker, msg, arg)

    def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit =
      Invokers.markerMessageArg1Arg2.invokeExact(
        markerMessageArg1Arg2Handle,
        logger,
        marker,
        msg,
        arg1,
        arg2
      )

    def markerMessageArgs(marker: Marker, msg: String, args: Array[Any]): Unit =
      Invokers.markerMessageArgs.invokeExact(
        markerMessageArgsHandle,
        logger,
        marker,
        msg,
        args
      )
  }

  object Info {
    private val predicateHandle =
      lookup.findVirtual(classOf[Logger], "isInfoEnabled", methodType(classOf[Boolean]))
    private val predicateMarkerHandle = lookup.findVirtual(
      classOf[Logger],
      "isInfoEnabled",
      methodType(classOf[Boolean], classOf[Marker])
    )

    private val levelIndex                  = Level.INFO.ordinal()
    private val messageHandle               = Handles.message(levelIndex)
    private val messageArg1Handle           = Handles.messageArg1(levelIndex)
    private val messageArg1Arg2Handle       = Handles.messageArg1Arg2(levelIndex)
    private val messageArgsHandle           = Handles.messageArgs(levelIndex)
    private val markerMessageHandle         = Handles.markerMessage(levelIndex)
    private val markerMessageArg1Handle     = Handles.markerMessageArg1(levelIndex)
    private val markerMessageArg1Arg2Handle = Handles.markerMessageArg1Arg2(levelIndex)
    private val markerMessageArgsHandle     = Handles.markerMessageArgs(levelIndex)
  }

  final class Warn(logger: org.slf4j.Logger) extends ParameterList {

    import Warn._

    def executePredicate(): Boolean =
      Invokers.predicate.invokeExact(predicateHandle, logger)

    def executePredicate(marker: Marker): Boolean =
      Invokers.predicateMarker.invokeExact(predicateMarkerHandle, logger, marker)

    def message(msg: String): Unit =
      Invokers.message.invokeExact(messageHandle, logger, msg)

    def messageArg1(msg: String, arg: Any): Unit =
      Invokers.messageArg1.invokeExact(messageArg1Handle, logger, msg, arg)

    def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit =
      Invokers.messageArg1Arg2.invokeExact(messageArg1Arg2Handle, logger, msg, arg1, arg2)

    def messageArgs(msg: String, args: Array[Any]): Unit =
      Invokers.messageArgs.invokeExact(messageArgsHandle, logger, msg, args.toArray)

    def markerMessage(marker: Marker, msg: String): Unit =
      Invokers.markerMessage.invokeExact(markerMessageHandle, logger, marker, msg)

    def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit =
      Invokers.markerMessageArg1.invokeExact(markerMessageArg1Handle, logger, marker, msg, arg)

    def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit =
      Invokers.markerMessageArg1Arg2.invokeExact(
        markerMessageArg1Arg2Handle,
        logger,
        marker,
        msg,
        arg1,
        arg2
      )

    def markerMessageArgs(marker: Marker, msg: String, args: Array[Any]): Unit =
      Invokers.markerMessageArgs.invokeExact(
        markerMessageArgsHandle,
        logger,
        marker,
        msg,
        args.toArray
      )
  }

  object Warn {
    private val predicateHandle =
      lookup.findVirtual(classOf[Logger], "isWarnEnabled", methodType(classOf[Boolean]))
    private val predicateMarkerHandle = lookup.findVirtual(
      classOf[Logger],
      "isWarnEnabled",
      methodType(classOf[Boolean], classOf[Marker])
    )

    private val levelIndex                  = Level.WARN.ordinal()
    private val messageHandle               = Handles.message(levelIndex)
    private val messageArg1Handle           = Handles.messageArg1(levelIndex)
    private val messageArg1Arg2Handle       = Handles.messageArg1Arg2(levelIndex)
    private val messageArgsHandle           = Handles.messageArgs(levelIndex)
    private val markerMessageHandle         = Handles.markerMessage(levelIndex)
    private val markerMessageArg1Handle     = Handles.markerMessageArg1(levelIndex)
    private val markerMessageArg1Arg2Handle = Handles.markerMessageArg1Arg2(levelIndex)
    private val markerMessageArgsHandle     = Handles.markerMessageArgs(levelIndex)
  }

  final class Error(logger: org.slf4j.Logger) extends ParameterList {

    import Error._

    def executePredicate(): Boolean =
      Invokers.predicate.invokeExact(predicateHandle, logger)

    def executePredicate(marker: Marker): Boolean =
      Invokers.predicateMarker.invokeExact(predicateMarkerHandle, logger, marker)

    def message(msg: String): Unit =
      Invokers.message.invokeExact(messageHandle, logger, msg)

    def messageArg1(msg: String, arg: Any): Unit =
      Invokers.messageArg1.invokeExact(messageArg1Handle, logger, msg, arg)

    def messageArg1Arg2(msg: String, arg1: Any, arg2: Any): Unit =
      Invokers.messageArg1Arg2.invokeExact(messageArg1Arg2Handle, logger, msg, arg1, arg2)

    def messageArgs(msg: String, args: Array[Any]): Unit =
      Invokers.messageArgs.invokeExact(messageArgsHandle, logger, msg, args.toArray)

    def markerMessage(marker: Marker, msg: String): Unit =
      Invokers.markerMessage.invokeExact(markerMessageHandle, logger, marker, msg)

    def markerMessageArg1(marker: Marker, msg: String, arg: Any): Unit =
      Invokers.markerMessageArg1.invokeExact(markerMessageArg1Handle, logger, marker, msg, arg)

    def markerMessageArg1Arg2(marker: Marker, msg: String, arg1: Any, arg2: Any): Unit =
      Invokers.markerMessageArg1Arg2.invokeExact(
        markerMessageArg1Arg2Handle,
        logger,
        marker,
        msg,
        arg1,
        arg2
      )

    def markerMessageArgs(marker: Marker, msg: String, args: Array[Any]): Unit =
      Invokers.markerMessageArgs.invokeExact(
        markerMessageArgsHandle,
        logger,
        marker,
        msg,
        args.toArray
      )
  }

  object Error {
    private val predicateHandle =
      lookup.findVirtual(classOf[Logger], "isErrorEnabled", methodType(classOf[Boolean]))
    private val predicateMarkerHandle = lookup.findVirtual(
      classOf[Logger],
      "isErrorEnabled",
      methodType(classOf[Boolean], classOf[Marker])
    )

    private val levelIndex                  = Level.ERROR.ordinal()
    private val messageHandle               = Handles.message(levelIndex)
    private val messageArg1Handle           = Handles.messageArg1(levelIndex)
    private val messageArg1Arg2Handle       = Handles.messageArg1Arg2(levelIndex)
    private val messageArgsHandle           = Handles.messageArgs(levelIndex)
    private val markerMessageHandle         = Handles.markerMessage(levelIndex)
    private val markerMessageArg1Handle     = Handles.markerMessageArg1(levelIndex)
    private val markerMessageArg1Arg2Handle = Handles.markerMessageArg1Arg2(levelIndex)
    private val markerMessageArgsHandle     = Handles.markerMessageArgs(levelIndex)
  }

}
