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

import com.tersesystems.blindsight.AST._
import com.tersesystems.blindsight.{MarkersResolver, _}
import net.logstash.logback.marker.{Markers => LogstashMarkers}

class LogstashMarkersResolver extends MarkersResolver {
  override def resolve(bobject: BObject): Markers = {
    import BObjectConverters._
    Markers(LogstashMarkers.appendEntries(asJava(bobject)))
  }
}
