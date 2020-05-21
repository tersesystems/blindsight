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

import com.tersesystems.blindsight.{Markers, ToMarkers}

/**
 * A marker mixin that returns a new instance of the logger with the marker always applied.
 *
 * @tparam StatementType the type class instance of [[com.tersesystems.blindsight.ToStatement]].
 */
trait SemanticMarkerMixin[StatementType] {
  type Self[T]

  /**
   * @param instance a type class instance of ToMarkers
   * @tparam T the instance type.
   * @return a new logger instance with the marker.
   */
  def withMarker[T: ToMarkers](instance: => T): Self[StatementType]

  /**
   * @return the accumulated markers, may be `Markers.empty`.
   */
  def markers: Markers
}
