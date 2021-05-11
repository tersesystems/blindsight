package example.scripting

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.core.CoreLogger
import com.tersesystems.blindsight.logstash.LogstashLoggerFactory
import com.tersesystems.blindsight.scripting.{ScriptAwareLogger, ScriptHandle, ScriptManager}
import com.twineworks.tweakflow.lang.errors.LangException

// #scripting_logger_factory
class ScriptingLoggerFactory extends LogstashLoggerFactory {

  private val logger = org.slf4j.LoggerFactory.getLogger(getClass)

  val scriptHandle = new ScriptHandle {
    override def isInvalid: Boolean = false

    override val script: String =
      """library blindsight {
        |  # level: the result of org.slf4j.event.Level.toInt()
        |  # enc: <class>.<method> i.e. com.tersesystems.blindsight.groovy.Main.logDebugSpecial
        |  # line: line number of the source code where condition was created
        |  # file: absolute path of the file containing the condition
        |  #
        |  doc 'Evaluates a condition'
        |  function evaluate: (long level, string enc, long line, string file) ->
        |    level >= 20; # info_int = 20
        |}
        |""".stripMargin

    override def report(e: Throwable): Unit = e match {
      case lang: LangException =>
        val info = lang.getSourceInfo
        if (info != null) {
          logger.error("Cannot evaluate script {}", info: Any, e: Any)
        } else {
          logger.error("Cannot evaluate script", e)
        }
      case other: Throwable =>
        logger.error("Cannot evaluate script", other)
    }
  }
  private val cm = new ScriptManager(scriptHandle)

  override def getLogger[T: LoggerResolver](instance: T): Logger = {
    val underlying = implicitly[LoggerResolver[T]].resolveLogger(instance)
    new ScriptAwareLogger(CoreLogger(underlying), cm)
  }
}
// #scripting_logger_factory
