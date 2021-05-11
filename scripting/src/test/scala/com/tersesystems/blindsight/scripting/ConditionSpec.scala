package com.tersesystems.blindsight.scripting

import com.tersesystems.blindsight.core.CoreLogger
import com.tersesystems.blindsight.fixtures.OneContextPerTest
import com.tersesystems.blindsight.{Logger, LoggerFactory, LoggerResolver}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ConditionSpec extends AnyWordSpec with Matchers with OneContextPerTest {
  override def resourceName: String = "/logback-test.xml"
  import com.tersesystems.blindsight.LoggerResolver
  implicit val logbackLoggerToLoggerResolver: LoggerResolver[ch.qos.logback.classic.Logger] = {
    LoggerResolver(identity)
  }

  val loggerFactory = new BoringLoggerFactory()

  "script condition" should {
    "work with a simple script" in {
      val scriptHandle = new ScriptHandle {
        override def isInvalid: Boolean = false
        override val script: String =
          """library blindsight {
            |  function evaluate: (long level, string enc, long line, string file) ->
            |    if (line == 37) then true
            |    else false;
            |}
            |""".stripMargin

        override def report(e: Throwable): Unit = e.printStackTrace()
      }
      val sm = new ScriptManager(scriptHandle);
      val underlying     = loggerContext.getLogger(this.getClass)
      val logger = loggerFactory.getLogger(underlying)

      val location = new ScriptBasedLocation(sm, true)
      logger.debug.when(location.here) { log => // line 37 :-)
        log("Hello world!")
      }

      logger.debug.when(location.here) { log =>
        log("should not see this")
      }

      listAppender.list.size must be(1)
      val event = listAppender.list.get(0)
      event.getMessage must equal("Hello world!")
    }
  }
}


// Very boring logger
class BoringLoggerFactory extends LoggerFactory {
  override def getLogger[T: LoggerResolver](instance: T): Logger = {
    val underlying = implicitly[LoggerResolver[T]].resolveLogger(instance)
    new Logger.Impl(CoreLogger(underlying))
  }
}