package com.tersesystems.blindsight.scripting

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.core.CoreLogger

/**
 */
class ScriptingLoggerFactory(scriptManager: ScriptManager) extends LoggerFactory {
  override def getLogger[T: LoggerResolver](instance: T): Logger = {
    val underlying = implicitly[LoggerResolver[T]].resolveLogger(instance)
    new ScriptAwareLogger(CoreLogger(underlying), scriptManager)
  }
}
