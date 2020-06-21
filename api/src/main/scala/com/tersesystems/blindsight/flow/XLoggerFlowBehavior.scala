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

package com.tersesystems.blindsight.flow

import com.tersesystems.blindsight._
import org.slf4j.event.Level
import org.slf4j.{Marker, MarkerFactory}

/**
 * A flow behavior that implements the <a href="http://www.slf4j.org/extensions.html#extended_logger">extended
 * logger</a> interface.
 *
 * @see https://github.com/qos-ch/slf4j/blob/master/slf4j-ext/src/main/java/org/slf4j/ext/XLogger.java
 * @tparam B the return type.
 */
class XLoggerFlowBehavior[B: ToArgument] extends FlowBehavior[B] {
  override def entryMarkers(source: FlowBehavior.Source): Markers = XLoggerFlowBehavior.entryMarkers
  override def exitMarkers(source: FlowBehavior.Source): Markers  = XLoggerFlowBehavior.exitMarkers

  protected def findArgs(source: FlowBehavior.Source): String = {
    source.args.value.flatMap(_.map(a => s"${a.source}=${a.value}")).mkString(",")
  }

  override def entryStatement(source: FlowBehavior.Source): Option[Statement] = {
    Some(
      Statement(
        markers = entryMarkers(source),
        message = s"${source.enclosing.value} entry {}",
        arguments = Arguments(findArgs(source))
      )
    )
  }

  override def throwingStatement(
      throwable: Throwable,
      source: FlowBehavior.Source
  ): Option[(Level, Statement)] = {
    Some(
      (
        Level.ERROR, // xlogger logs exceptions at an error level.
        Statement(
          markers = XLoggerFlowBehavior.throwingMarkers,
          message = s"${source.enclosing.value} exception",
          throwable = throwable
        )
      )
    )
  }

  override def exitStatement(resultValue: B, source: FlowBehavior.Source): Option[Statement] = {
    Some(
      Statement(
        markers = exitMarkers(source),
        message = s"${source.enclosing.value} exit with result {}",
        arguments = Arguments(resultValue)
      )
    )
  }
}

object XLoggerFlowBehavior {
  // markers are same as XLogger
  val flowMarker: Marker = MarkerFactory.getMarker("FLOW")
  val entryMarker: Marker = {
    val entry = MarkerFactory.getMarker("ENTRY")
    entry.add(flowMarker)
    entry
  }
  val exitMarker: Marker = {
    val exit = MarkerFactory.getMarker("EXIT")
    exit.add(flowMarker)
    exit
  }
  val exceptionMarker: Marker = MarkerFactory.getMarker("EXCEPTION")
  val throwingMarker: Marker = {
    val throwing = MarkerFactory.getMarker("THROWING")
    throwing.add(exceptionMarker)
    throwing
  }

  val flowMarkers: Markers     = Markers(flowMarker)
  val entryMarkers: Markers    = Markers(entryMarker)
  val exitMarkers: Markers     = Markers(exitMarker)
  val throwingMarkers: Markers = Markers(throwingMarker)

}
