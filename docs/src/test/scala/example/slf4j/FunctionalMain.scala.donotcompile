/*
 * Copyright 2020 Terse Systems
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

package example.slf4j

import com.tersesystems.blindsight.LoggerFactory

object FunctionalMain {

  def main(args: Array[String]): Unit = {
    val logger = LoggerFactory.getLogger(getClass)

    // Would like a causality tracking system generally for tracing (no instrumentation/weaving required)
    // https://pythonspeed.com/articles/logging-for-scientific-computing/
    // https://blog.acolyer.org/2015/10/13/pivot-tracing-dynamic-causal-monitoring-for-distributed-systems/
    // http://www.erights.org/elang/tools/causeway/index.html
    // https://www.hpl.hp.com/techreports/2009/HPL-2009-78.html
    // https://www.youtube.com/watch?v=QeqcGa7HlBk
    // https://web.archive.org/web/20160730075837/http://www.eros-os.org/pipermail/e-lang/2002-November/007811.html

    import cats.implicits._
    import treelog.LogTreeSyntaxWithoutAnnotations._

    // treelog using cats
    // https://github.com/lancewalton/treelog
    // Should print out each individual label at trace level with a marker
    // indicating the computation, then produce the result at debug level.
    // https://github.com/lancewalton/treelog/blob/master/src/test/scala/QuadraticRootsExample.scala

    // Should be able to wedge it into SLF4J using this:
    // https://github.com/oranda/treelog-scalajs/blob/master/src/main/scala/com/oranda/treelogui/LogTreeItem.scala
    val oneA: DescribedComputation[Int] = 1 ~> (v => s"The value is $v")
    oneA.value.run match {
      case (logTree, value) =>
        logger.trace(logTree.show)
        logger.info(s"result is ${value.show}")
    }

    // Also possible
    //val written = oneA.value.written
    //logger.info(written.show)
  }

}
