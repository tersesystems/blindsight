package com.tersesystems.blindsight.flow

import com.tersesystems.blindsight.{Markers, Statement}
import org.slf4j.event.Level

/**
 * A type class that is used to provide a behavior to a logging flow.
 *
 * You can implement your own behavior and place it in scope for the code that you're using:
 *
 * {{{
 * private implicit def flowBehavior[B: ToArguments]: FlowBehavior[B] = new FlowBehavior[B] {
 *   override def createEntryStatement(source: FlowBehavior.Source): Option[Statement] = {
 *     Some(
 *       Statement().withMessage(s"\${source.enclosing.value} entry")
 *     )
 *   }
 * }
 * }}}
 *
 * You can use the `createEntryStatement` to start a timer and then complete it on exit/throwing,
 * or use the flow as hooks into the tracing framework.
 *
 * The hooks are used in the FlowLoggerMethod roughly as follows:
 *
 * {{{
 * val source: FlowBehavior.Source = ???
 * if (isLoggingInfo(entryMarkers(source)) {
 *   entryStatement(source).foreach(logger.info)
 * }
 * Try(executeCode) match {
 *   case Success(value) =>
 *     if (isLoggingInfo(exitMarkers(source))) {
 *       createExitStatement(source).foreach(logger.info)
 *     }
 *   case Failure(e) =>
 *     if (isLoggingInfo(throwingMarkers(source))) {
 *        createThrowingStatement(source).foreach(logger.info)
 *     }
 * }
 * }}}
 */
trait FlowBehavior[B] {
  import FlowBehavior._

  /**
   * Returns the markers used by the flow logger method on entry.
   *
   * @param source the source info
   * @return the entry markers, empty by default.
   */
  def entryMarkers(source: Source): Markers = Markers.empty

  /**
   * Creates an entry statement, if specified.
   *
   * @param source the source info
   * @return the entry statement, None by default.
   */
  def entryStatement(source: Source): Option[Statement] = None

  /**
   * Provides exit markers for the predicate.
   *
   * @param source the source info
   * @return exit markers, empty by default.
   */
  def exitMarkers(source: Source): Markers = Markers.empty

  /**
   * Returns an exit statement, using the result value and the source.
   *
   * @param resultValue the result of the flow.
   * @param source the source info
   * @return the statement, None by default.
   */
  def exitStatement(
      resultValue: B,
      source: Source
  ): Option[Statement] = None

  /**
   * Returns a tuple describing the statement and the level to log the exception at; if you
   * return `(Level.ERROR, statement)` then it will log the exception at error level, for example.
   *
   * @param exc the throwable returned from flow
   * @param source the source info
   * @return a tuple containing the statement and the level to execute the throwing statement.
   */
  def throwingStatement(
      exc: Throwable,
      source: Source
  ): Option[(Level, Statement)] = None
}

object FlowBehavior {
  import sourcecode._

  /**
   * A convenient packaging of source code information.
   *
   * @param line the source code line
   * @param file the source code file
   * @param enclosing the enclosing method.
   * @param args the arguments.
   */
  case class Source(line: Line, file: File, enclosing: Enclosing, args: Args)
}
