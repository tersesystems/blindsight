package com.tersesystems.blindsight.fixtures

import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Logger, LoggerContext}
import ch.qos.logback.core.Appender
import ch.qos.logback.core.read.ListAppender
import org.scalatest.{Outcome, TestData, TestSuite, TestSuiteMixin}

import java.util.Objects.requireNonNull
import java.util.{Optional, Spliterator, Spliterators}
import java.util.stream.StreamSupport

trait OneContextPerTest extends TestSuiteMixin {
  this: TestSuite =>

  private var contextPerTest: LoggerContext = _

  final implicit def loggerContext: LoggerContext =
    synchronized {
      contextPerTest
    }

  abstract override def withFixture(test: NoArgTest): Outcome = {
    synchronized {
      contextPerTest = newContextForTest(test)
    }
    running(loggerContext) {
      super.withFixture(test)
    }
  }

  def running[T](ctx: LoggerContext)(block: => T): T = {
    try {
      block
    } finally {
      ctx.stop()
    }
  }

  def newContextForTest(testData: TestData): LoggerContext = createLoggerContext()

  def resourceName: String

  def createLoggerContext(): LoggerContext = {
    val context      = new LoggerContext
    val resource     = getClass.getResource(resourceName)
    val configurator = new JoranConfigurator
    configurator.setContext(context)
    configurator.doConfigure(resource)
    context
  }

  def listAppender(implicit context: LoggerContext): ListAppender[ILoggingEvent] = {
    def getFirstAppender(logger: Logger): Optional[Appender[ILoggingEvent]] = {
      val appenderStream = fromIterator(logger.iteratorForAppenders)
      appenderStream.findFirst
    }

    val maybeAppender = getFirstAppender(context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME))
    if (maybeAppender.isPresent)
      requireNonNull(maybeAppender.get).asInstanceOf[ListAppender[ILoggingEvent]]
    else throw new IllegalStateException("Cannot find appender")
  }

  private def fromIterator[E](iterator: java.util.Iterator[E]) = {
    val spliterator: Spliterator[E] = Spliterators.spliteratorUnknownSize(iterator, 0)
    StreamSupport.stream(spliterator, false)
  }

}
