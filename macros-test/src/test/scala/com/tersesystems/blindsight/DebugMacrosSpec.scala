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
        if (System.currentTimeMillis() - 1 == 0) {
          assert("decorateIfs: if block" != null)
        } else if (System.getProperty("derp") == null) {
          assert("decorateIfs: derp is null" != null)
        } else {
          assert("decorateIfs: else block" != null)
        }
      }

      val list = listAppender.list
      list.get(0).getMessage must equal("System.currentTimeMillis() - 1 == 0 = false")
      list.get(1).getMessage must equal("System.getProperty(\"derp\") == null = true")
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

    "debugExpr" in {
      val logger = createLogger
      val dr = dumpExpr(1 + 1)
      dr.value must be(2)
      logger.debug(s"result: ${dr.code} = ${dr.value}")
      val list = listAppender.list
      list.get(0).getMessage must equal("result: 1 + 1 = 2")
    }

    "dumpMethod" in {
      def foo: DumpMethod = {
        dumpMethod
      }

      foo.name must be("foo")
      //foo.location.line must be(73) // XXX FIXME
    }

    "debugPublicFields" in {
      val exObj        = new ExampleClass(42)
      val publicFields = debugPublicFields(exObj)

      val head = publicFields.head
      head.name must be("someInt")
      head.value must be(42)
    }

  }

  override def resourceName: String = "/logback-test.xml"

  def createLogger(implicit loggerContext: LoggerContext): Logger = {
    val underlying = loggerContext.getLogger("testing")
    new Logger.Impl(CoreLogger(underlying))
  }
}

class ExampleClass(val someInt: Int) {
  protected val privateInt = 22
}
