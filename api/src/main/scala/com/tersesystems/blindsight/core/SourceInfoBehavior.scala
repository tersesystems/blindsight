/*
 * Copyright 2020 com.tersesystems.blindsight
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

import com.tersesystems.blindsight.AST.BObject
import com.tersesystems.blindsight.Markers
import sourcecode.{Enclosing, File, Line}

trait SourceInfoBehavior {
  def apply(
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Markers
}

object SourceInfoBehavior {

  class Impl(
      fileLabel: String,
      lineLabel: String,
      enclosingLabel: String
  ) extends SourceCodeImplicits(fileLabel, lineLabel, enclosingLabel)
      with SourceInfoBehavior {
    override def apply(line: Line, file: File, enclosing: Enclosing): Markers = {
      val obj = BObject(List(line, file, enclosing))
      Markers(obj)
    }
  }
}
