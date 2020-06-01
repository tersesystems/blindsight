package com.tersesystems.blindsight

import ch.qos.logback.classic.LoggerContext
import com.tersesystems.blindsight.fixtures.OneContextPerTest
import com.tersesystems.blindsight.flow.SimpleFlowBehavior
import com.tersesystems.blindsight.slf4j.SLF4JLogger
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LoggerSpec extends AnyWordSpec with Matchers with OneContextPerTest {

  override def resourceName: String = "/logback-test-slf4j.xml"

  class TestLogger(strict: SLF4JLogger.Strict) extends Logger.Impl(strict)

  def createLogger(implicit loggerContext: LoggerContext): Logger = {
    val strict = new SLF4JLogger.Strict(loggerContext.getLogger("testing"), Markers.empty)
    new TestLogger(strict)
  }

  "logger" should {

    "log on true condition" in {
      val logger = createLogger

      val condition = true
      logger.onCondition(condition).error("this should be logged")
      val event = listAppender.list.get(0)
      event.getMessage must equal("this should be logged")
    }

    "not log on false condition" in {
      val logger = createLogger

      val condition = false
      logger.onCondition(condition).error("this should not be logged")
      listAppender.list must be(empty)
    }

    "log on true using conditional with fluent API" in {
      val logger = createLogger

      val condition = true
      logger.onCondition(condition).fluent.error.message("this should be logged").log()
      val event = listAppender.list.get(0)
      event.getMessage must equal("this should be logged")
    }

    "not log on false using conditional with fluent API" in {
      val logger = createLogger

      val condition = false
      logger.onCondition(condition).fluent.error.message("this should not be logged").log()
      listAppender.list must be(empty)
    }

    "not log on false using conditional with flow API" in {
      val logger = createLogger

      val condition                            = false
      implicit def flowBehavior[B: ToArgument] = new SimpleFlowBehavior[B]
      logger.onCondition(condition).flow.info(1 + 2)
      listAppender.list must be(empty)
    }

    "not log on false using conditional with strict API" in {
      val logger = createLogger

      val condition = false
      logger.onCondition(condition).strict.info("do not log")
      listAppender.list must be(empty)
    }
    //
    //    "not log on false using conditional with unchecked API" in {
    //      val logger = createLogger
    //
    //      val condition = false
    //      val uncheckedLogger = logger.onCondition(condition).unchecked
    //      val infoMethod = uncheckedLogger.info
    //      infoMethod.apply("do not log")
    //      listAppender.list must be(empty)
    //    }

    "not log on false using conditional with semantic API" in {
      val logger = createLogger

      val condition   = false
      val conditional = logger.onCondition(condition)
      val s           = conditional.semantic[Statement]
      val statement   = Statement().withMessage("do not log")
      val infoMethod  = s.info
      infoMethod.apply(statement)
      listAppender.list must be(empty)
    }
  }

}
