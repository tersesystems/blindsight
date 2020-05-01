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

import com.tersesystems.blindsight.api.ToArguments
import com.tersesystems.blindsight.slf4j.SLF4JPredicate
import org.slf4j.event.Level
import sourcecode.{Args, Enclosing, File, Line}

import scala.util.{Failure, Success, Try}

/**
 * This trait wraps an execution block, and applies a `FlowBehavior` to it on entry, exit, and exception.
 *
 * Note that the return value must have a type class instance of `ToArguments` in scope, so that
 * the logging statement can render it appropriately.
 */
trait FlowMethod {

  def apply[B: ToArguments](
      block: => B
  )(
      implicit line: Line,
      file: File,
      enclosing: Enclosing,
      sourceArgs: Args,
      mapping: FlowBehavior[B]
  ): B
}

object FlowMethod {

  /**
   * The implementation of the flow logger method.
   *
   * @param level the level to log a statement with.
   * @param logger the parent logger.
   */
  class Impl(level: Level, logger: ExtendedFlowLogger) extends FlowMethod {

    private val predicate: SLF4JPredicate = logger.predicate(level)
    private val parameterList             = logger.parameterList(level)

    override def apply[B: ToArguments](
        attempt: => B
    )(
        implicit line: Line,
        file: File,
        enclosing: Enclosing,
        sourceArgs: Args,
        mapping: FlowBehavior[B]
    ): B = {
      import mapping._
      val source = FlowBehavior.Source(line, file, enclosing, sourceArgs)
      if (predicate(entryMarkers(source))) {
        entryStatement(source).foreach(parameterList.executeStatement)
      }
      tryExecution(attempt)
    }

    protected def tryExecution[B: ToArguments](
        attempt: => B
    )(
        implicit line: Line,
        file: File,
        enclosing: Enclosing,
        sourceArgs: Args,
        mapping: FlowBehavior[B]
    ): B = {
      import mapping._
      val source = FlowBehavior.Source(line, file, enclosing, sourceArgs)
      // We always run through the predicate on exit marker, as this will not add more
      // than a nanosecond to execution typically:
      // https://github.com/wsargent/slf4j-benchmark
      if (predicate(exitMarkers(source))) {
        val result = Try(attempt)
        result match {
          case Success(resultValue) =>
            exitStatement(resultValue, source).foreach(parameterList.executeStatement)
          case Failure(exception) =>
            throwingStatement(exception, source).foreach {
              case (level, stmt) =>
                logger.parameterList(level).executeStatement(stmt)
            }
        }
        result.get // rethrow the exception
      } else {
        attempt // just run the block.
      }
    }
  }

  class Conditional(test: => Boolean, level: Level, logger: ExtendedFlowLogger)
      extends Impl(level, logger) {
    override def apply[B: ToArguments](
        attempt: => B
    )(
        implicit line: Line,
        file: File,
        enclosing: Enclosing,
        sourceArgs: Args,
        mapping: FlowBehavior[B]
    ): B = {
      if (test) {
        super.apply(attempt)
      } else {
        attempt
      }
    }
  }
}
