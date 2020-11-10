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

import com.tersesystems.blindsight.core.{CoreLogger, ParameterList, CorePredicate}
import com.tersesystems.blindsight.{Condition, ToArgument}
import org.slf4j.event.Level
import sourcecode.{Args, Enclosing, File, Line}

import scala.util.{Failure, Success, Try}

/**
 * This trait wraps an execution block, and applies a [[FlowBehavior]] to it on entry, exit, and exception.
 *
 * Note that the return value must have a type class instance of [[ToArgument]] in scope, so that
 * the logging statement can render it appropriately.
 *
 * You should use `Condition.never` explicitly here to disable logging, as it will shortcut to a Noop
 * implementation.  Benchmarks show a noop flow takes 42ns to execute, 4.5ns if you remove sourcecode.Args
 * from the method signature.
 */
trait FlowMethod {
  def when(condition: Condition): FlowMethod

  def apply[B: ToArgument](
      block: => B
  )(implicit
      line: Line,
      file: File,
      enclosing: Enclosing,
      sourceArgs: Args,
      /* Adding args means an extra 42ns to assemble the seq even if we have a no-op. */
      mapping: FlowBehavior[B]
  ): B

}

object FlowMethod {

  /**
   * The implementation of the flow logger method.
   *
   * @param level the level to log a statement with.
   * @param core the parent logger.
   */
  class Impl(level: Level, core: CoreLogger) extends FlowMethod {

    private val predicate: CorePredicate = core.predicate(level)

    override def when(condition: Condition): FlowMethod = {
      if (condition == Condition.never) {
        Noop
      } else {
        new Impl(level, core.withCondition(condition))
      }
    }

    override def apply[B: ToArgument](
        attempt: => B
    )(implicit
        line: Line,
        file: File,
        enclosing: Enclosing,
        sourceArgs: Args,
        mapping: FlowBehavior[B]
    ): B = {
      if (predicate()) {
        import mapping._
        val parameterList: ParameterList = core.parameterList(level)

        val source = FlowBehavior.Source(line, file, enclosing, sourceArgs)
        entryStatement(source).foreach(parameterList.executeStatement)

        val result = Try(attempt)
        result match {
          case Success(resultValue) =>
            exitStatement(resultValue, source).foreach(parameterList.executeStatement)
          case Failure(exception) =>
            throwingStatement(exception, source).foreach { case (level, stmt) =>
              core.parameterList(level).executeStatement(stmt)
            }
        }
        result.get // rethrow the exception
      } else {
        attempt // just run the block.
      }
    }
  }

  /**
   * A no-operation flow method that can be inlined by the compiler.
   */
  object Noop extends FlowMethod {
    override def when(condition: Condition): FlowMethod = Noop

    override def apply[B: ToArgument](block: => B)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing,
        args: Args,
        mapping: FlowBehavior[B]
    ): B = block
  }
}
