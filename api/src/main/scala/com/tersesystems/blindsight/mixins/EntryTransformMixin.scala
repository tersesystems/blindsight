package com.tersesystems.blindsight.mixins

import com.tersesystems.blindsight.Entry
import org.slf4j.event.Level

/**
 * Adds the ability for logged statements to go though a transformation
 * before being sent to SLF4J.
 */
trait EntryTransformMixin {
  type Self

  def withTransform(level: Level, f: Entry => Entry): Self

  def withTransform(f: Entry => Entry): Self
}
