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
      Statement()
        .withMarkers(entryMarkers(source))
        .withMessage(s"${source.enclosing.value} entry {}")
        .withArguments(Argument(findArgs(source)).arguments)
    )
  }

  override def throwingStatement(
      throwable: Throwable,
      source: FlowBehavior.Source
  ): Option[(Level, Statement)] = {
    Some(
      Level.ERROR, // xlogger logs exceptions at an error level.
      Statement()
        .withThrowable(throwable)
        .withMarkers(XLoggerFlowBehavior.throwingMarkers)
        .withMessage(s"${source.enclosing.value} exception")
    )
  }

  override def exitStatement(resultValue: B, source: FlowBehavior.Source): Option[Statement] = {
    Some(
      Statement()
        .withMarkers(exitMarkers(source))
        .withMessage(s"${source.enclosing.value} exit with result {}")
        .withArguments(Arguments(Argument(resultValue)))
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
