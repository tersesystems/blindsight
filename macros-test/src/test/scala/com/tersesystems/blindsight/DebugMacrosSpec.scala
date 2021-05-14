package com.tersesystems.blindsight

import ch.qos.logback.classic.LoggerContext
import com.tersesystems.blindsight.DebugMacros._
import com.tersesystems.blindsight.core.CoreLogger
import com.tersesystems.blindsight.fixtures.OneContextPerTest
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DebugMacrosSpec extends AnyWordSpec with Matchers with OneContextPerTest {

  "macro" should {
    "decorateVals" in {
      val logger = createLogger
      decorateVals(dval => logger.debug(s"${dval.name} = ${dval.value}")) {
        val a = 5
        val b = 15
        a + b
      }

      val list = listAppender.list
      list.get(0).getMessage must equal("a = 5")
      list.get(1).getMessage must equal("b = 15")
    }

    "decorateIfs" in {
      val logger = createLogger
      decorateIfs(dif => logger.debug(s"${dif.code} = ${dif.result}")) {
        if (System.currentTimeMillis() % 17 == 0) {
          println("decorateIfs: if block")
        } else if (System.getProperty("derp") == null) {
          println("decorateIfs: derp is null")
        } else {
          println("decorateIfs: else block")
        }
      }

      val list = listAppender.list
      list.get(0).getMessage must equal("System.currentTimeMillis() % 17 == 0 = false")
      list.get(1).getMessage must equal("System.getProperty(\"derp\") == null = true")
    }

    "decorateMatch" in {
      val logger = createLogger
      val string = java.time.Instant.now().toString
      decorateMatch(dif => logger.debug(s"${dif.code} = ${dif.result}")) {
        string match {
          case s if s.startsWith("2021") =>
            println("SWEET")

          case _ =>
            println("OH NOES")
        }
      }

      val list = listAppender.list
      list.get(0).getMessage must equal("string match case s if s.startsWith(\"2021\") = true")
    }

    "debugExpr" in {
      val logger = createLogger
      val output: Int = debugExpr[Int]((result: DebugResult[Int]) => logger.debug(s"result = ${result.code} = ${result.value}")) {
        (1 + 1)
      }

      output must be(2)
      val list = listAppender.list
      list.get(0).getMessage must equal("result = 1 + 1 = 2")
    }
  }

  override def resourceName: String = "/logback-test.xml"

  def createLogger(implicit loggerContext: LoggerContext): Logger = {
    val underlying = loggerContext.getLogger("testing")
    new Logger.Impl(CoreLogger(underlying))
  }
}
