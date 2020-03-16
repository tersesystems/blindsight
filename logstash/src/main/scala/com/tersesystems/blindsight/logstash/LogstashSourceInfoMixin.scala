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

package com.tersesystems.blindsight.logstash

import com.tersesystems.blindsight.api.Markers
import com.tersesystems.blindsight.api.mixins.SourceInfoMixin
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

/**
 * This mixin implements source information as logstash markers.
 */
trait LogstashSourceInfoMixin extends SourceInfoMixin {
  override def sourceInfoMarker(
      level: Level,
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Markers = {
    import Implicits._
    Markers(line) + Markers(file) + Markers(enclosing)
  }
}
