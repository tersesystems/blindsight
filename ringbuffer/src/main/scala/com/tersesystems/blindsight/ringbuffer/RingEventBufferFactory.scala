package com.tersesystems.blindsight.ringbuffer

import com.tersesystems.blindsight.{EventBuffer, EventBufferFactory}

/**
 * A factory that creates ring buffers.
 */
class RingEventBufferFactory extends EventBufferFactory {
  override def create(capacity: Int): EventBuffer = new RingEventBuffer(capacity)
}
