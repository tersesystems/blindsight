package com.tersesystems.blindsight.ringbuffer

import java.time.Clock

import com.tersesystems.blindsight.{EntryBuffer, EntryBufferFactory}

/**
 * A factory that creates ring buffers.
 */
class RingEntryBufferFactory extends EntryBufferFactory {
  private val clock: Clock = Clock.systemUTC()

  override def create(capacity: Int): EntryBuffer = new RingEntryBuffer(capacity, clock)
}
