package com.tersesystems.blindsight.logstash

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.AST._

import net.logstash.logback.marker.{Markers => LogstashMarkers}

class LogstashMarkersResolver extends MarkersResolver {
  override def resolve(bobject: BObject): Markers = {
    import BObjectConverters._
    Markers(LogstashMarkers.appendEntries(asJava(bobject)))
  }
}
