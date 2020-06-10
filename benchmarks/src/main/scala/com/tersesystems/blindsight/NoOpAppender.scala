package com.tersesystems.blindsight

import ch.qos.logback.classic.spi.ILoggingEvent

class NoOpAppender extends ch.qos.logback.core.AppenderBase[ILoggingEvent] {
  override def append(eventObject: ILoggingEvent): Unit = {}
}
