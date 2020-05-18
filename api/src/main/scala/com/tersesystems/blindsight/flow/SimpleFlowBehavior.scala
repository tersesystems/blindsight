package com.tersesystems.blindsight.flow

import java.io.File

import com.tersesystems.blindsight._
import org.slf4j.event.Level

/**
 * A simple flow behavior that prints out the arguments and result, with the line number.
 *
 * @tparam B the type of the result.
 */
class SimpleFlowBehavior[B: ToArgument] extends FlowBehavior[B] {

  override def entryStatement(source: FlowBehavior.Source): Option[Statement] = None

  override def throwingStatement(
      throwable: Throwable,
      source: FlowBehavior.Source
  ): Option[(Level, Statement)] = {
    val args = Arguments(findArgs(source), throwable.getMessage, findPos(source))
    Some(
      Level.ERROR,
      Statement()
        .withThrowable(throwable)
        .withMessage(s"{} throws {} at {}")
        .withArguments(args)
    )
  }

  override def exitStatement(resultValue: B, source: FlowBehavior.Source): Option[Statement] = {
    val args = Arguments(findArgs(source), resultValue, findPos(source))
    Some(
      Statement()
        .withMarkers(exitMarkers(source))
        .withMessage("{} => {} {}")
        .withArguments(args)
    )
  }

  protected def findArgs(source: FlowBehavior.Source): String = {
    source.args.value.flatMap(_.map(a => s"${a.source}=${a.value}")).mkString(",")
  }

  protected def findPos(source: FlowBehavior.Source): String = {
    val file     = source.file.value
    val index    = file.lastIndexOf(File.separator)
    val filename = if (index == -1) file else file.substring(index + 1)
    s"    at ${source.enclosing.value}(${filename}:${source.line.value})"
  }

}
