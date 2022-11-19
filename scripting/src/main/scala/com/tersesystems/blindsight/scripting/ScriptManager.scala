/*
 * Copyright 2020 com.tersesystems.blindsight
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tersesystems.blindsight.scripting

import com.twineworks.tweakflow.lang.TweakFlow
import com.twineworks.tweakflow.lang.load.loadpath.{LoadPath, MemoryLocation}
import com.twineworks.tweakflow.lang.runtime.Runtime
import com.twineworks.tweakflow.lang.values.{Value, Values}
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

import java.util.concurrent.atomic.AtomicReference
import scala.util.Try

/**
 * A script manager that parses and evaluates tweakflow scripts.
 */
class ScriptManager(handle: ScriptHandle) {
  protected val mref: AtomicReference[Runtime.Module] = new AtomicReference[Runtime.Module]()

  protected def path = "condition.tf"

  protected def libraryName: String = "blindsight"

  protected def functionName: String = "evaluate"

  def execute(
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
      val encV   = Values.make(enclosing.value)
      val lineV  = Values.make(line.value)
      val fileV  = Values.make(file.value)

      call(levelV, encV, lineV, fileV)
    } catch {
      case e: Exception =>
        handle.report(e)
        default // pass the default through on exception.
    }
  }

  protected def call(level: Value, enc: Value, line: Value, file: Value): Boolean = {
    val module   = mref.get()
    val callSite = module.getLibrary(libraryName).getVar(functionName)

    callSite.call(level, enc, line, file).bool()
  }

  protected def compileModule(script: String): Runtime.Module = {
    val memLocation = new MemoryLocation.Builder()
      .add(path, script)
      .build
    val loadPath = new LoadPath.Builder().addStdLocation().add(memLocation).build()
    val runtime  = TweakFlow.compile(loadPath, path)
    runtime.getModules.get(runtime.unitKey(path))
  }

  protected def eval(script: String): Try[Runtime.Module] = {
    Try {
      val module: Runtime.Module = compileModule(script)
      module.evaluate()
      mref.set(module)
      module
    }
  }

}
