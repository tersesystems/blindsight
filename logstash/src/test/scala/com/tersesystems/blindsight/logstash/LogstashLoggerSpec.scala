package com.tersesystems.blindsight.logstash

import com.tersesystems.blindsight.DSL._
import com.tersesystems.blindsight._
import com.tersesystems.blindsight.fixtures.OneContextPerTest
import com.tersesystems.blindsight.fluent.FluentLogger
import com.tersesystems.blindsight.semantic.SemanticLogger
import net.logstash.logback.argument.StructuredArguments.entries
import net.logstash.logback.marker.{Markers => LogstashMarkers}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.jdk.CollectionConverters._

class LogstashLoggerSpec extends AnyWordSpec with Matchers with OneContextPerTest {

  override def resourceName: String = "/logback-test-logstash.xml"

  import com.tersesystems.blindsight.LoggerResolver
  implicit val logbackLoggerToLoggerResolver: LoggerResolver[ch.qos.logback.classic.Logger] = {
    LoggerResolver(identity)
  }

  "logstash logger" should {

    "with SLF4J" should {

      "work with a plain info message" in {
        val underlying     = loggerContext.getLogger(this.getClass)
        val logger: Logger = LoggerFactory.getLogger(underlying)
        logger.info("a=b")

        val event = listAppender.list.get(0)
        event.getMessage must equal("a=b")
      }

      "work with a info message with marker" in {
        val underlying     = loggerContext.getLogger(this.getClass)
        val logger: Logger = LoggerFactory.getLogger(underlying)
        logger.info(Markers(bobj("1" -> "2")), "a=b")

        val event = listAppender.list.get(0)
        event.getMessage must equal("a=b")
        event.getMarker.contains(LogstashMarkers.appendEntries(Map("1" -> "2").asJava)) must be(
          true
        )
      }

      "work with a info message with argument" in {
        val underlying     = loggerContext.getLogger(this.getClass)
        val logger: Logger = LoggerFactory.getLogger(underlying)
        logger.info("a=b", bobj("1" -> "2"))

        val event = listAppender.list.get(0)
        event.getMessage must equal("a=b")
        val array: Array[AnyRef] = event.getArgumentArray
        array(0) must be(entries(Map("1" -> "2").asJava))
      }

      "work with a info message with exception" in {
        val underlying     = loggerContext.getLogger(this.getClass)
        val logger: Logger = LoggerFactory.getLogger(underlying)
        val e              = new Exception("derp")
        logger.info("a=b", e)

        val event = listAppender.list.get(0)
        event.getMessage must equal("a=b")
        event.getThrowableProxy.getMessage must be("derp")
      }

      "work with a info message with argument and exception" in {
        val underlying     = loggerContext.getLogger(this.getClass)
        val logger: Logger = LoggerFactory.getLogger(underlying)
        val e              = new Exception("derp")
        logger.info("a=b", bobj("1" -> "2"), e)

        val event = listAppender.list.get(0)
        event.getMessage must equal("a=b")
        val array: Array[AnyRef] = event.getArgumentArray
        array(0) must be(entries(Map(("1" -> "2")).asJava))
        event.getThrowableProxy.getMessage must be("derp")
      }

      "work with a info message with marker, argument and exception" in {
        val underlying     = loggerContext.getLogger(this.getClass)
        val logger: Logger = LoggerFactory.getLogger(underlying)
        val e              = new Exception("derp")
        logger.info(Markers(bobj("markerKey" -> "markerValue")), "a=b", bobj("1" -> "2"), e)

        val event = listAppender.list.get(0)
        event.getMarker.contains(
          LogstashMarkers.appendEntries(Map("markerKey" -> "markerValue").asJava)
        ) must be(true)
        event.getMessage must equal("a=b")
        val array: Array[AnyRef] = event.getArgumentArray
        array(0) must be(entries(Map(("1" -> "2")).asJava))
        event.getThrowableProxy.getMessage must be("derp")
      }

      "work with a info message with marker, arguments and exception" in {
        val underlying     = loggerContext.getLogger(this.getClass)
        val logger: Logger = LoggerFactory.getLogger(underlying)
        val e              = new Exception("derp")
        logger.info(
          Markers(bobj("markerKey" -> "markerValue")),
          "a=b",
          bobj("1" -> "2"),
          bobj("3" -> "4"),
          e
        )

        val event = listAppender.list.get(0)
        event.getMessage must equal("a=b")
        event.getMarker.contains(
          LogstashMarkers.appendEntries(Map("markerKey" -> "markerValue").asJava)
        ) must be(true)
        val array: Array[AnyRef] = event.getArgumentArray
        array(0) must be(entries(Map(("1" -> "2")).asJava))
        array(1) must be(entries(Map(("3" -> "4")).asJava))
        event.getThrowableProxy.getMessage must be("derp")
      }

      "work with when" in {
        val underlying     = loggerContext.getLogger(this.getClass)
        val logger: Logger = LoggerFactory.getLogger(underlying)

        // val infoFunction = logger.info.when(1 == 1)(_)
        logger.info.when(1 == 1) { info => info("when true") }

        val event = listAppender.list.get(0)
        event.getMessage must equal("when true")
      }

      "work with a condition" in {
        val underlying     = loggerContext.getLogger(this.getClass)
        val logger: Logger = LoggerFactory.getLogger(underlying)
        val conditional    = logger.withCondition(1 == 0)
        val infoMethod     = conditional.info
        infoMethod.apply("this is a failed message")

        logger.withCondition(1 == 1).info("this is a successful message")
        listAppender.list.size() must be(1)

        val event = listAppender.list.get(0)
        event.getMessage must equal("this is a successful message")
      }

      "work with a state marker" in {
        val underlying     = loggerContext.getLogger(this.getClass)
        val logger: Logger = LoggerFactory.getLogger(underlying)
        val infoMethod     = logger.withMarker(bobj("a" -> "b")).info

        infoMethod.apply("I have a marker")

        val event = listAppender.list.get(0)
        event.getMessage must equal("I have a marker")
        event.getMarker.contains(LogstashMarkers.appendEntries(Map("a" -> "b").asJava)) must be(
          true
        )
      }
    }

    "with a fluent logger" should {

      "work with plain info" in {
        val underlying                 = loggerContext.getLogger(this.getClass)
        val logger: Logger             = LoggerFactory.getLogger(underlying)
        val fluentLogger: FluentLogger = logger.fluent
        fluentLogger.info.message("a" -> "b").log()
        val event = listAppender.list.get(0)
        event.getMessage must equal("a=b")
      }

      "work with a condition" in {
        val underlying                 = loggerContext.getLogger(this.getClass)
        val logger: Logger             = LoggerFactory.getLogger(underlying)
        val fluentLogger: FluentLogger = logger.fluent

        fluentLogger.withCondition(1 == 0).info.message("this is a failed message").log()
        fluentLogger.withCondition(1 == 1).info.message("this is a successful message").log()
        listAppender.list.size() must be(1)

        val event = listAppender.list.get(0)
        event.getMessage must equal("this is a successful message")
      }

      "work with a state marker" in {
        val underlying                 = loggerContext.getLogger(this.getClass)
        val logger: Logger             = LoggerFactory.getLogger(underlying)
        val fluentLogger: FluentLogger = logger.fluent
        fluentLogger.withMarker(bobj("a" -> "b")).info.message("I have a marker").log()

        val event = listAppender.list.get(0)
        event.getMessage must equal("I have a marker")
        event.getMarker.contains(LogstashMarkers.appendEntries(Map("a" -> "b").asJava)) must be(
          true
        )
      }

    }

    "with a semantic logger" should {

      "work with a plain info statement" in {
        val underlying     = loggerContext.getLogger(this.getClass)
        val logger: Logger = LoggerFactory.getLogger(underlying)

        val semanticLogger: SemanticLogger[Message] = logger.semantic[Message]
        semanticLogger.info(Message("this is a semantic message"))
        val event = listAppender.list.get(0)
        event.getMessage must equal("this is a semantic message")
      }

      "work with a condition" in {
        val underlying                              = loggerContext.getLogger(this.getClass)
        val logger: Logger                          = LoggerFactory.getLogger(underlying)
        val semanticLogger: SemanticLogger[Message] = logger.semantic[Message]

        semanticLogger.withCondition(1 == 0).info(Message("this is a failed message"))
        semanticLogger.withCondition(1 == 1).info(Message("this is a successful message"))
        listAppender.list.size() must be(1)

        val event = listAppender.list.get(0)
        event.getMessage must equal("this is a successful message")
      }

      "work with a state marker" in {
        val underlying                              = loggerContext.getLogger(this.getClass)
        val logger: Logger                          = LoggerFactory.getLogger(underlying)
        val semanticLogger: SemanticLogger[Message] = logger.semantic[Message]

        semanticLogger.withMarker(bobj("a" -> "b")).info(Message("I have a marker"))

        val event = listAppender.list.get(0)
        event.getMessage must equal("I have a marker")
        event.getMarker.contains(LogstashMarkers.appendEntries(Map("a" -> "b").asJava)) must be(
          true
        )
      }
    }
  }

  implicit val messageToStatement: ToStatement[Message] = ToStatement { message =>
    message.toStatement
  }

}
