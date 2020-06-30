package com.tersesystems.blindsight.mixins

import com.tersesystems.blindsight.EntryBuffer

trait EntryBufferMixin {
  type Self

  def withEntryBuffer(buffer: EntryBuffer): Self

  def entries: Option[EntryBuffer]
}
