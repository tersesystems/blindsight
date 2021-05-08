package com.tersesystems.blindsight.core

import com.tersesystems.blindsight.mixins._
import com.tersesystems.blindsight.{Condition, Entry, EventBuffer, Markers, ToMarkers}
import org.slf4j.event.Level

/**
 * A trait that ties the logger to the core logger internals.
 */
trait CoreLoggerDefaults { mixin: ConditionMixin with UnderlyingMixin with MarkerMixin with EntryTransformMixin with EventBufferMixin =>
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
