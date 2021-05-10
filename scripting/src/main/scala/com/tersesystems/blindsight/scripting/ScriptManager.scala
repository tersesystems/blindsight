package com.tersesystems.blindsight.scripting

import com.twineworks.tweakflow.lang.TweakFlow
import com.twineworks.tweakflow.lang.load.loadpath.{LoadPath, MemoryLocation}
import com.twineworks.tweakflow.lang.runtime.Runtime
import com.twineworks.tweakflow.lang.values.Values
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

import java.util.concurrent.atomic.AtomicReference
import scala.util.Try

class ScriptManager(handle: ScriptHandle) {
  private val mref: AtomicReference[Runtime.Module] = new AtomicReference[Runtime.Module]()

  private[scripting] def execute(
      default: Boolean,
      level: Level,
      enclosing: Enclosing,
      line: Line,
      file: File
  ): Boolean = {
    if (mref.get == null || handle.isInvalid) {
      eval()
    }

    try {
      val callSite = mref.get().getLibrary("condition").getVar("evaluate")
      val result = callSite
        .call(
          Values.make(level.toInt),
          Values.make(enclosing.value),
          Values.make(line.value),
          Values.make(file.value)
        )
        .bool()
      result
    } catch {
      case e: Exception =>
        handle.report(e)
        default // pass the default through on exception.
    }
  }

  private def compileModule(script: String): Runtime.Module = {
    val memLocation = new MemoryLocation.Builder().add("condition.tf", script).build
    val loadPath    = new LoadPath.Builder().addStdLocation().add(memLocation).build()
    val runtime     = TweakFlow.compile(loadPath, "condition.tf")
    runtime.getModules.get(runtime.unitKey("condition.tf"))
  }

  def eval(): Try[Runtime] = {
    Try {
      val module = compileModule(handle.script)
      module.evaluate()
      mref.set(module)
      module.getRuntime
    }
  }

}
