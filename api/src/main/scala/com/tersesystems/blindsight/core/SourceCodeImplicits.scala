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

package com.tersesystems.blindsight.core

import com.tersesystems.blindsight.{AST, DSL}
import sourcecode._

/**
 * This trait converts SourceCode to BFields, using labels.
 *
 * {{{
 * val implicits = new SourceCodeImplicits(/* labels */)
 * import implicits._
 * }}}
 */
class SourceCodeImplicits(
    fileLabel: String,
    lineLabel: String,
    enclosingLabel: String
) {
  import AST._
  import DSL._

  implicit def fileToBObject(file: File): BField = fileLabel -> file.value

  implicit def lineToField(line: Line): BField = lineLabel -> line.value

  implicit def enclosingToField(enclosing: Enclosing): BField =
    enclosingLabel -> enclosing.value
}
