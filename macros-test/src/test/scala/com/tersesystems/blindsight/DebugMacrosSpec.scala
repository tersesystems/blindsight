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
  }

  override def resourceName: String = "/logback-test.xml"

  def createLogger(implicit loggerContext: LoggerContext): Logger = {
    val underlying = loggerContext.getLogger("testing")
    new Logger.Impl(CoreLogger(underlying))
  }
}
