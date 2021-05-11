package example.scripting

import com.tersesystems.blindsight.LoggerFactory
import com.tersesystems.blindsight.scripting.{ScriptBasedLocation, ScriptHandle, ScriptManager}

// #scripting_condition
object ConditionExample {
  def main(args: Array[String]): Unit = {
    // or use FileScriptHandle
    val scriptHandle = new ScriptHandle {
      override def isInvalid: Boolean = false
      override val script: String =
        """library blindsight {
          |  function evaluate: (long level, string enc, long line, string file) ->
          |    true;
          |}
          |""".stripMargin
      override def report(e: Throwable): Unit = e.printStackTrace()
    }
    val sm = new ScriptManager(scriptHandle)
    val logger = LoggerFactory.getLogger

    val location = new ScriptBasedLocation(sm, true)
    logger.debug.when(location.here) { log => // line 37 :-)
      log("Hello world!")
    }
  }
}
// #scripting_condition