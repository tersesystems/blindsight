package com.tersesystems.blindsight.ringbuffer

import com.tersesystems.blindsight.{EntryBuffer, EntryBufferFactory}

class RingEntryBufferFactory extends EntryBufferFactory {
  override def createEntryBuffer(capacity: Int): EntryBuffer = new RingEntryBuffer(capacity)
}
