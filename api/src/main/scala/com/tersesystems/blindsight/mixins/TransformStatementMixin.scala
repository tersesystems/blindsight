package com.tersesystems.blindsight.mixins

import com.tersesystems.blindsight.UnderlyingStatement
import org.slf4j.event.Level

/**
 * Adds the ability for logged statements to go though a transformation
 * before being sent to SLF4J.
 */
trait TransformStatementMixin {
  type Self

  def withTransform(level: Level, f: UnderlyingStatement => UnderlyingStatement): Self
}
