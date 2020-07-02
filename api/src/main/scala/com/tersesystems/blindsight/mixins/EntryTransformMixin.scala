package com.tersesystems.blindsight.mixins

import com.tersesystems.blindsight.Entry
import org.slf4j.event.Level

/**
 * Adds the ability for logged entries to go though a transformation
 * before being sent to SLF4J.
 */
trait EntryTransformMixin {
  type Self

  def withEntryTransform(level: Level, f: Entry => Entry): Self

  def withEntryTransform(f: Entry => Entry): Self
}
