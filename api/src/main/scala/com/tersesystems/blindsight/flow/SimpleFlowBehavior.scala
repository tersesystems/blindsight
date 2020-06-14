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
    Some(
      (
        Level.ERROR,
        Statement(
          markers = Markers.empty,
          message = "{} throws {} at {}",
          arguments = Arguments(findArgs(source), throwable.getMessage, findPos(source)),
          throwable = Some(throwable)
        )
      )
    )
  }

  override def exitStatement(resultValue: B, source: FlowBehavior.Source): Option[Statement] = {
    Some(
      Statement(
        markers = exitMarkers(source),
        message = "{} => {} {}",
        arguments = Arguments(findArgs(source), resultValue, findPos(source)),
        throwable = None
      )
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
