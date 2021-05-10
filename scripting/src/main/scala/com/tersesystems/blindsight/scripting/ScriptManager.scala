package com.tersesystems.blindsight.scripting

import com.twineworks.tweakflow.lang.TweakFlow
import com.twineworks.tweakflow.lang.load.loadpath.{LoadPath, MemoryLocation}
import com.twineworks.tweakflow.lang.runtime.Runtime
import com.twineworks.tweakflow.lang.values.{Value, Values}
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
    try {
      if (mref.get == null || handle.isInvalid) {
        val script = handle.script
        eval(script).get
      }
      val levelV = Values.make(level.toInt)
      val encV = Values.make(enclosing.value)
      val lineV = Values.make(line.value)
      val fileV = Values.make(file.value)

      call(levelV, encV, lineV, fileV)
    } catch {
      case e: Exception =>
        handle.report(e)
        default // pass the default through on exception.
    }
  }

  protected def call(level: Value, enc: Value, line: Value, file: Value): Boolean = {
    val module = mref.get()
    val callSite = module.getLibrary("blindsight").getVar("evaluate")

    callSite.call(level, enc, line, file).bool()
  }

  private def compileModule(script: String): Runtime.Module = {
    val memLocation = new MemoryLocation.Builder()
      .allowNativeFunctions(false)
      .add("condition.tf", script)
      .build
    val loadPath    = new LoadPath.Builder().addStdLocation().add(memLocation).build()
    val runtime     = TweakFlow.compile(loadPath, "condition.tf")
    runtime.getModules.get(runtime.unitKey("condition.tf"))
  }

  def eval(script: String): Try[Runtime.Module] = {
    Try {
      val module: Runtime.Module = compileModule(script)
      module.evaluate()
      mref.set(module)
      module
    }
  }

}
