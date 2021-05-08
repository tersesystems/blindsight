package com.tersesystems.blindsight.scripting

import com.tersesystems.blindsight.{Condition, LoggerFactory, Markers}
import com.twineworks.tweakflow.lang.TweakFlow
import com.twineworks.tweakflow.lang.errors.LangException
import com.twineworks.tweakflow.lang.load.loadpath.{LoadPath, MemoryLocation}
import com.twineworks.tweakflow.lang.runtime.Runtime
import com.twineworks.tweakflow.lang.values.Values
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference

class TweakFlowConditionManager(path: Path, verifier: String => Boolean) {
  private val mref: AtomicReference[Runtime.Module] = new AtomicReference[Runtime.Module]()

  private val logger = LoggerFactory.getLogger

  private val source = new FileConditionSource(path, verifier)

  def condition()(implicit line: Line, enclosing: Enclosing, file: sourcecode.File): Condition = {
    new TweakFlowDynamicCondition(line, enclosing, file)
  }

  private[scripting] def execute(level: Level, enclosing: Enclosing, line: Line, file: File): Boolean = {
    if (mref.get == null || source.isInvalid) {
      eval()
    }

    try {
      val callSite = mref.get().getLibrary("condition").getVar("evaluate")
      val result = callSite.call(
        Values.make(level.toInt),
        Values.make(enclosing.value),
        Values.make(line.value),
        Values.make(file.value)
      ).bool()
      result
    } catch {
      case e: LangException =>
        import com.tersesystems.blindsight.DSL._
        import com.tersesystems.blindsight._
        val info = e.getSourceInfo
        logger.error("Cannot execute script: {}", bobj(
          "line" -> info.getLine,
          "column" -> info.getCharWithinLine,
          "error" -> e.getCode.getName,
          "message" -> e.getDigestMessage
        ))
        false
    }
  }

  private def compileModule(script: String): Runtime.Module = {
    val memLocation = new MemoryLocation.Builder().add("condition.tf", script).build
    val loadPath = new LoadPath.Builder().addStdLocation().add(memLocation).build()
    val runtime = TweakFlow.compile(loadPath, "condition.tf")
    runtime.getModules.get(runtime.unitKey("condition.tf"))
  }

  private def eval(): Unit = {
    try {
      val module = compileModule(source.script)
      module.evaluate()
      mref.set(module)
    } catch {
      case e: LangException =>
        import com.tersesystems.blindsight.DSL._
        import com.tersesystems.blindsight._
        val info = e.getSourceInfo
        logger.warn("Cannot compile script: {}", bobj(
          "line" -> info.getLine,
          "column" -> info.getCharWithinLine,
          "error" -> e.getCode.getName,
          "message" -> e.getDigestMessage
        ))
    }
  }

  eval()

  class TweakFlowDynamicCondition(line: Line, enclosing: Enclosing, file: File) extends Condition {
    override def apply(level: Level, markers: Markers): Boolean = {
      execute(level, enclosing, line, file)
    }
  }

}
