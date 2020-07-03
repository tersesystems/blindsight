package com.tersesystems.blindsight.mixins

import com.tersesystems.blindsight.EventBuffer

trait EventBufferMixin {
  type Self

  def withEventBuffer(buffer: EventBuffer): Self
}
