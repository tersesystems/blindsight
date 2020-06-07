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

package example.flow

// #flow_example
object SimpleFlow {
  import com.tersesystems.blindsight._
  import com.tersesystems.blindsight.flow._

  private implicit def flowBehavior[B: ToArgument]: FlowBehavior[B] = new SimpleFlowBehavior

  private def loggingEnabled: Boolean = true

  private val logger                 = LoggerFactory.getLogger.onCondition(loggingEnabled)
  private val flowLogger: FlowLogger = logger.flow.onCondition(loggingEnabled)

  def main(args: Array[String]): Unit = {
    logger.info("About to execute number flow")
    val intResult = flowMethod(1, 2)
    println("This is " + intResult)

    logger.info("About to execute person flow")
    val personResult = personFlowMethod(1, 2)
    println("This is " + personResult)
  }

  // #flow_method
  def flowMethod(arg1: Int, arg2: Int): Int =
    flowLogger.trace {
      arg1 + arg2
    }
  // #flow_method

  // #flow_person_definition
  def personFlowMethod(arg1: Int, arg2: Int): Person =
    flowLogger.trace {
      Person(name = "Will", age = arg1 + arg2)
    }

  case class Person(name: String, age: Int)

  object Person {
    implicit val personToArguments: ToArgument[Person] = ToArgument { person =>
      import com.tersesystems.blindsight.AST._
      import com.tersesystems.blindsight.DSL._

      val personObj: BObject = "person" -> (
        ("name" -> person.name) ~ ("age" -> person.age)
      )
      Argument(personObj)
    }
  }
  // #flow_person_definition
}
// #flow_example
