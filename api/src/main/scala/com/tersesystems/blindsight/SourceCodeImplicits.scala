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

import java.util.Arrays.deepToString

import sourcecode._

/**
 * This trait converts SourceCode to BFields
 */
trait SourceCodeImplicits {
  import AST._
  import DSL._

  implicit def fileToBObject(file: File): BField = "source.file" -> file.value

  implicit def lineToField(line: Line): BField = "source.line" -> line.value

  implicit def enclosingToField(enclosing: Enclosing): BField =
    "source.enclosing" -> enclosing.value

  private def argToField(value: Any): BValue = {
    value match {
      case null =>
        BString("null")
      case s: String =>
        BString(s)
      case array: Array[_] =>
        BString(deepToString(array.asInstanceOf[Array[Object]]))
      case other =>
        BString(other.toString)
    }
  }

  implicit def argsToField(sourceArgs: Args): BField = {
    val args: Seq[BField] =
      sourceArgs.value.flatMap(_.map(a => a.source -> argToField(a.value)))
    BField("source.arguments", args)
  }
}

object SourceCodeImplicits extends SourceCodeImplicits
