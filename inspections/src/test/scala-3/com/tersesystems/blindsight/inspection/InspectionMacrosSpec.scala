package com.tersesystems.blindsight.inspection

import ch.qos.logback.classic.LoggerContext
import com.tersesystems.blindsight.Logger
import com.tersesystems.blindsight.inspection.InspectionMacros._
import com.tersesystems.blindsight.core.CoreLogger
import com.tersesystems.blindsight.fixtures.OneContextPerTest
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class InspectionMacrosSpec extends AnyWordSpec with Matchers with OneContextPerTest {

  "macro" should {

    "decorateIfs" in {
      val logger = createLogger
      decorateIfs(dif => logger.debug(s"${dif.code} = ${dif.result}")) {
        if (System.currentTimeMillis() - 1 == 0) {
          assert("decorateIfs: if block" != null)
        } else if (System.getProperty("derp") == null) {
          assert("decorateIfs: derp is null" != null)
        } else {
          assert("decorateIfs: else block" != null)
        }
      }

      val list = listAppender.list
      list.get(0).getMessage must equal("java.lang.System.currentTimeMillis().-(1).==(0) = false")
      list.get(1).getMessage must equal("java.lang.System.getProperty(\"derp\").==(null) = true")
    }

    "decorateMatch" in {
      val logger = createLogger
      val string = java.time.Instant.now().toString
      decorateMatch(dif => logger.debug(s"${dif.code} = ${dif.result}")) {
        string match {
          case s if s.startsWith("20") =>
            assert("10".toInt > 1)

          case _ =>
            assert("true".toBoolean == true)
        }
      }

      val list = listAppender.list
      list.get(0).getMessage must equal("string match case s if s.startsWith(\"20\") = true")
    }

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

    "decorateVals with vars" in {
      val logger = createLogger
      decorateVals(dval => logger.debug(s"${dval.name} = ${dval.value}")) {
        var result = 0
        result = 5 + 15
        result
      }

      val list = listAppender.list
      list.get(0).getMessage must equal("result = 0")
    }

    "dumpExpression" in {
      val logger = createLogger
      val dr     = dumpExpression(1 + 1)
      dr.value must be(2)
      logger.debug(s"result: ${dr.code} = ${dr.value}")
      val list = listAppender.list
      list.get(0).getMessage must equal("result: 1 + 1 = 2")
    }

    "dumpPublicFields" in {
      val exObj        = new ExampleClass(42)
      val publicFields = dumpPublicFields(exObj)

      val head = publicFields.head
      head.name must be("paramInt")
      head.value must be(42)

      publicFields(1).name must be("fieldInt")
      publicFields(1).value must be(1337)
      
    }

  }

  override def resourceName: String = "/logback-test.xml"

  def createLogger(implicit loggerContext: LoggerContext): Logger = {
    val underlying = loggerContext.getLogger("testing")
    new Logger.Impl(CoreLogger(underlying))
  }
}

class ExampleClass(val paramInt: Int,
   private val privateParamInt: Int = 20,
   protected val protectedParamInt: Int = 19) {
  val fieldInt: Int = 1337
  private val privateInt = 22
  protected val protectedInt = 21
}
