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

package com.tersesystems.blindsight.mixins

import com.tersesystems.blindsight.bobj
import com.tersesystems.blindsight.Markers
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

/**
 * A mixin that provides the marker information for source code data.
 */
trait SourceInfoMixin {

  /**
   * Returns marker information containing source code data.
   *
   * @param level the level of the log method.
   * @param line the line that the log method was called on.
   * @param file the file name that the log method was in.
   * @param enclosing the enclosing method and class of the log method.
   * @return a marker containing source code information.
   */
  def sourceInfoMarker(
      level: Level,
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Markers = {
    import com.tersesystems.blindsight.SourceCodeImplicits._
    Markers(bobj(line)) + Markers(bobj(line)) + Markers(bobj(enclosing))
  }

}
