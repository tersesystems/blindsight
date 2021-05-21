package com.tersesystems.blindsight

import ch.qos.logback.classic.LoggerContext
import com.tersesystems.blindsight
import com.tersesystems.blindsight.core.CoreLogger
import com.tersesystems.blindsight.fixtures.OneContextPerTest
import com.tersesystems.blindsight.flow.{FlowBehavior, SimpleFlowBehavior}
import com.tersesystems.blindsight.slf4j.StrictSLF4JMethod
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.MarkerFactory
import org.slf4j.event.Level

import scala.collection.mutable

class LoggerSpec extends AnyWordSpec with Matchers with OneContextPerTest {

  override def resourceName: String = "/logback-test-slf4j.xml"

  def createLogger(implicit loggerContext: LoggerContext): Logger = {
    val underlying = loggerContext.getLogger("testing")
    new blindsight.Logger.Impl(CoreLogger(underlying))
  }

  "logger.when" should {
    "log on true condition" in {
      val logger = createLogger

      val condition = true
      logger.error.when(condition) { error =>
        error("this should be logged")
      }
      val event = listAppender.list.get(0)
      event.getMessage must equal("this should be logged")
    }

    "not log on false condition" in {
      val logger = createLogger

      val condition = false
      logger.error.when(condition) { error =>
        error("this should not be logged")
      }
      listAppender.list must be(empty)
    }

    "log on true with flow API" in {
      val logger = createLogger

      val condition                            = true
      implicit def flowBehavior[B: ToArgument]: FlowBehavior[B] = new SimpleFlowBehavior[B]
      def calcInt: Int =
        logger.flow.info.when(condition) { // line 53 :-)
          1 + 2
        }
      val result = calcInt

      result must be(3)
      val event = listAppender.list.get(0)
      event.getFormattedMessage must equal(
        " => 3     at com.tersesystems.blindsight.LoggerSpec#calcInt(LoggerSpec.scala:53)"
      )
    }

    "not log on false with flow API" in {
      val logger = createLogger

      val condition                            = false
      implicit def flowBehavior[B: ToArgument]: FlowBehavior[B]  = new SimpleFlowBehavior[B]

      def calcInt: Int =
        logger.flow.info.when(condition) {
          1 + 2
        }
      val result = calcInt
      result must be(3)
      listAppender.list must be(empty)
    }
  }

  "logger.withCondition" should {

    "log on true condition" in {
      val logger = createLogger

      val condition = true
      logger.withCondition(condition).error("this should be logged")
      val event = listAppender.list.get(0)
      event.getMessage must equal("this should be logged")
    }

    "not log on false condition" in {
      val logger = createLogger

      val condition = false
      logger.withCondition(condition).error("this should not be logged")
      listAppender.list must be(empty)
    }

    "log when matching logger state" in {
      val logger = createLogger

      val fooMarker = MarkerFactory.getMarker("FOO")
      val markerCondition =
        Condition((markers: Markers) => markers.contains(fooMarker))
      logger.withMarker(fooMarker).withCondition(markerCondition).error("this should be logged")
      val event = listAppender.list.get(0)
      event.getMessage must equal("this should be logged")
    }

    "not log when matching logger state" in {
      val logger = createLogger

      val fooMarker = MarkerFactory.getMarker("FOO")
      val markerCondition =
        Condition((markers: Markers) => markers.contains(fooMarker))
      logger.withCondition(markerCondition).error("this should not be logged")
      listAppender.list must be(empty)
    }

    "log on true using conditional with fluent API" in {
      val logger = createLogger

      val condition = true
      logger
        .withCondition(condition)
        .fluent
        .error
        .message("this should be logged")
        .log()
      val event = listAppender.list.get(0)
      event.getMessage must equal("this should be logged")
    }

    "not log on false using conditional with fluent API" in {
      val logger = createLogger

      val condition = false
      logger
        .withCondition(condition)
        .fluent
        .error
        .message("this should not be logged")
        .log()
      listAppender.list must be(empty)
    }

    "not log on false using conditional with flow API" in {
      val logger = createLogger

      val condition                            = false
      implicit def flowBehavior[B: ToArgument]: FlowBehavior[B]  = new SimpleFlowBehavior[B]
      logger.withCondition(condition).flow.info(1 + 2)
      listAppender.list must be(empty)
    }

    "not log on false using conditional with strict API" in {
      val logger = createLogger

      val condition = false
      logger.withCondition(condition).strict.info("do not log")
      listAppender.list must be(empty)
    }

    "not log on false using conditional with unchecked API" in {
      val logger = createLogger

      val condition       = false
      val uncheckedLogger = logger.withCondition(condition).unchecked
      val infoMethod      = uncheckedLogger.info
      infoMethod.apply("do not log")
      listAppender.list must be(empty)
    }

    "not log on false using conditional with semantic API" in {
      val logger = createLogger

      val condition   = false
      val conditional = logger.withCondition(condition)
      val s           = conditional.semantic[Statement]
      s.info(Message("do not log").toStatement)
      listAppender.list must be(empty)
    }

    "do not log with marker and conditional" in {
      val logger = createLogger

      val condition       = false
      val someMarker      = MarkerFactory.getMarker("SOME_MARKER")
      val falseWithMarker = logger.withCondition(condition).withMarker(someMarker)
      falseWithMarker.info("do not log")
      listAppender.list must be(empty)
    }

    "do not log with false and true conditional" in {
      val logger = createLogger

      val twoConditions = logger.withCondition(false).withCondition(true)
      twoConditions.info("do not log")
      listAppender.list must be(empty)
    }

    "do not log with never and true conditional" in {
      val logger = createLogger

      val twoConditions = logger.withCondition(Condition.never).withCondition(true)
      twoConditions.info("do not log")
      listAppender.list must be(empty)
    }

    "log on info conditional" in {
      val logger = createLogger

      val infoCondition = Condition((level, state) => level == Level.INFO)
      val conditional   = logger.withCondition(infoCondition)
      conditional.info("log on info")

      val event = listAppender.list.get(0)
      event.getMessage must equal("log on info")
    }

    "do not log on statement marker" in {
      val logger = createLogger

      // the condition can only see marker in the logger state, not in the statement
      val someMarker    = MarkerFactory.getMarker("SOME_MARKER")
      val infoCondition = Condition((level, markers) => markers.contains(someMarker))
      val conditional   = logger.withCondition(infoCondition)
      conditional.info(Markers(someMarker), "do not log")

      listAppender.list must be(empty)
    }

    "do not on marker based conditional when no marker" in {
      val logger = createLogger

      val someMarker    = MarkerFactory.getMarker("SOME_MARKER")
      val infoCondition = Condition((level, markers) => markers.contains(someMarker))
      val conditional   = logger.withCondition(infoCondition)
      conditional.info("do not log because no marker")
      listAppender.list must be(empty)
    }

    "log on marker based conditional when marker in state" in {
      val logger = createLogger

      val someMarker    = MarkerFactory.getMarker("SOME_MARKER")
      val infoCondition = Condition((level, markers) => markers.contains(someMarker))
      val conditional   = logger.withCondition(infoCondition)
      conditional.withMarker(someMarker).info("log because state has marker")

      val event = listAppender.list.get(0)
      event.getMarker.contains(someMarker) must be(true)
    }

    "log on tracer marker" in {
      val logger = createLogger

      val tracerMarker   = MarkerFactory.getMarker("TRACER_MARKER")
      val traceCondition = Condition((level, _) => level == Level.TRACE)
      val conditional    = logger.withCondition(traceCondition)
      conditional.withMarker(tracerMarker).trace("trace statement")

      val event = listAppender.list.get(0)
      event.getMarker.contains(tracerMarker) must be(true)
    }

    "do not log on debug" in {
      val logger = createLogger

      val tracerMarker   = MarkerFactory.getMarker("TRACER_MARKER")
      val traceCondition = Condition((level, _) => level == Level.TRACE)
      val conditional    = logger.withCondition(traceCondition)
      conditional.withMarker(tracerMarker).debug("trace statement")
      listAppender.list must be(empty)
    }

    "log on condition/marker/condition" in {
      val logger = createLogger

      val someMarker  = MarkerFactory.getMarker("SOME_MARKER")
      val conditional = logger.withCondition(true).withMarker(someMarker).withCondition(true)
      conditional.info("log with marker")
      val event = listAppender.list.get(0)
      event.getMessage must equal("log with marker")
      event.getMarker.contains(someMarker) must be(true)
    }
  }

  "logger.withTransform" should {

    "be called when logging" in {
      var called = false;
      val logger = createLogger.withEntryTransform(Level.INFO, st => { called = true; st })
      logger.info("test message")
      called must be(true)
    }

    "not change anything on identity" in {
      val logger = createLogger.withEntryTransform(Level.INFO, identity)
      logger.info("test message")

      val event = listAppender.list.get(0)
      event.getMessage must equal("test message")
    }

    "transform info message" in {
      val logger =
        createLogger.withEntryTransform(Level.INFO, st => st.copy(message = st.message.toUpperCase))
      logger.info("test message")

      val event = listAppender.list.get(0)
      event.getMessage must equal("TEST MESSAGE")
    }

    "transform info message with argument" in {
      val logger = createLogger.withEntryTransform(
        Level.INFO,
        st => st.copy(message = st.message.toUpperCase, args = Some(Array("IN BED")))
      )
      logger.info("test message")

      val event = listAppender.list.get(0)
      event.getMessage must equal("TEST MESSAGE")
      event.getArgumentArray.head must equal("IN BED")
    }

    "transform info message with marker" in {
      val security = MarkerFactory.getMarker("SECURITY")
      val logger = createLogger.withEntryTransform(
        Level.INFO,
        st => st.copy(marker = Some(security), message = st.message.toUpperCase)
      )
      logger.info("test message")

      val event = listAppender.list.get(0)
      event.getMessage must equal("TEST MESSAGE")
      event.getMarker must equal(security)
      event.getArgumentArray must be(null)
    }

    "transform info message with marker and argument" in {
      val security = MarkerFactory.getMarker("SECURITY")
      val logger = createLogger.withEntryTransform(
        Level.INFO,
        st =>
          st.copy(
            marker = Some(security),
            message = st.message.toUpperCase,
            args = Some(Array("IN BED"))
          )
      )
      logger.info("test message")

      val event = listAppender.list.get(0)
      event.getMessage must equal("TEST MESSAGE")
      event.getMarker must equal(security)
      event.getArgumentArray.head must equal("IN BED")
    }

    "not transform warn message" in {
      val logger =
        createLogger.withEntryTransform(Level.WARN, st => st.copy(message = st.message.toUpperCase))
      logger.info("warn message")

      val event = listAppender.list.get(0)
      event.getMessage must equal("warn message")
    }

    "transform in the right order" in {
      val logger = createLogger
        .withEntryTransform(st => st.copy(message = st.message + " ONE"))
        .withEntryTransform(st => st.copy(message = st.message + " TWO"))
      logger.info("MESSAGE")

      val event = listAppender.list.get(0)
      event.getMessage must equal("MESSAGE ONE TWO")
    }

    "transform in the right order when level specific" in {
      val logger = createLogger
        .withEntryTransform(st => st.copy(message = st.message + " ONE"))
        .withEntryTransform(Level.INFO, st => st.copy(message = st.message + " TWO"))
      logger.info("MESSAGE")

      val event = listAppender.list.get(0)
      event.getMessage must equal("MESSAGE ONE TWO")
    }
  }

  "logger.withBuffer" should {

    "log something and see it in buffer" in {
      val queueBuffer = new TestEventBuffer
      val logger      = createLogger.withEventBuffer(queueBuffer)

      logger.info("Hello world")

      val el = queueBuffer.headOption.get
      el.entry.marker must be(None)
      el.entry.message must be("Hello world")
      el.entry.args must be(empty)
    }

    "log something at a specific level and see it in buffer" in {
      val queueBuffer = new TestEventBuffer
      val logger      = createLogger.withEventBuffer(Level.INFO, queueBuffer)

      logger.info("Hello world")

      val el = queueBuffer.headOption.get
      el.entry.marker must be(None)
      el.entry.message must be("Hello world")
      el.entry.args must be(empty)
    }

    "log something at a different level and not see it in buffer" in {
      val queueBuffer = new TestEventBuffer
      val logger      = createLogger.withEventBuffer(Level.INFO, queueBuffer)

      logger.warn("Hello world")
      queueBuffer.headOption must be(None)
    }

    "buffer should work with transform when placed after" in {
      val queueBuffer = new TestEventBuffer

      val logger = createLogger
        .withEntryTransform(e => e.copy(message = e.message + " TRANSFORM"))
        .withEventBuffer(queueBuffer)
      logger.warn("Hello world")

      val el = queueBuffer.headOption.get
      el.entry.marker must be(None)
      el.entry.message must be("Hello world TRANSFORM")
      el.entry.args must be(empty)
    }
  }
}

class TestEventBuffer extends EventBuffer {
  private val queue: mutable.Queue[EventBuffer.Event] =
    scala.collection.mutable.Queue[EventBuffer.Event]()

  override def size: Int = queue.size

  override def take(count: Int) = queue.slice(0, count).toIndexedSeq

  def clear(): Unit = queue.clear()

  override def headOption: Option[EventBuffer.Event] = queue.headOption

  override def offer(event: EventBuffer.Event): Unit = queue.enqueue(event)

  override def capacity: Int = Int.MaxValue

  override def isEmpty: Boolean = queue.isEmpty

  override def head: EventBuffer.Event = queue.head
}
