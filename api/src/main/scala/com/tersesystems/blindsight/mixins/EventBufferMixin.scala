package com.tersesystems.blindsight.mixins

import com.tersesystems.blindsight.EventBuffer
import org.slf4j.event.Level

trait EventBufferMixin {
  type Self

  def withEventBuffer(buffer: EventBuffer): Self

  def withEventBuffer(level: Level, buffer: EventBuffer): Self
}
