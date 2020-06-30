package com.tersesystems.blindsight.mixins

import com.tersesystems.blindsight.LogEntry
import org.slf4j.event.Level

/**
 * Adds the ability for logged statements to go though a transformation
 * before being sent to SLF4J.
 */
trait TransformLogEntryMixin {
  type Self

  def withTransform(level: Level, f: LogEntry => LogEntry): Self
}
