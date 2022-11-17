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

import com.tersesystems.blindsight.mixins._
import com.tersesystems.blindsight.{Condition, Entry, EventBuffer, Markers, ToMarkers}
import org.slf4j.event.Level

/**
 * A trait that ties the logger to the core logger internals.
 */
trait CoreLoggerDefaults {
  mixin: ConditionMixin
    with UnderlyingMixin
    with MarkerMixin
    with EntryTransformMixin
    with EventBufferMixin =>
  type Self

  protected def core: CoreLogger

  protected def self(core: CoreLogger): Self

  def markers: Markers = core.markers

  def underlying: org.slf4j.Logger = core.underlying

  def withCondition(condition: Condition): Self = {
    self(core.withCondition(condition))
  }

  def withMarker[T: ToMarkers](markerInstance: T): Self = {
    self(core.withMarker(markerInstance))
  }

  def withEntryTransform(level: Level, f: Entry => Entry): Self = {
    self(core.withEntryTransform(level, f))
  }

  def withEntryTransform(f: Entry => Entry): Self =
    self(core.withEntryTransform(f))

  def withEventBuffer(buffer: EventBuffer): Self =
    self(core.withEventBuffer(buffer))

  def withEventBuffer(level: Level, buffer: EventBuffer): Self =
    self(core.withEventBuffer(level, buffer))
}
