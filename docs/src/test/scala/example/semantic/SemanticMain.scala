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

package example.semantic

import java.time.LocalTime

import com.tersesystems.blindsight.semantic.SemanticLogger
import com.tersesystems.blindsight._

// #semantic-main
object SemanticMain {

  sealed trait UserEvent {
    def name: String
  }

  final case class UserLoggedInEvent(name: String, ipAddr: String) extends UserEvent

  object UserLoggedInEvent {
    implicit val toStatement: ToStatement[UserLoggedInEvent] = ToStatement { instance =>
      import com.tersesystems.blindsight.DSL._
      Statement()
        .withMessage("UserLoggedInEvent message with args {}")
        .withArguments(
          Arguments(
            bobj(
              "user-logged-out-event" ->
                ("name"     -> instance.name) ~
                  ("ipAddr" -> instance.ipAddr)
            )
          )
        )
    }
  }

  final case class UserLoggedOutEvent(name: String, reason: String) extends UserEvent

  object UserLoggedOutEvent {
    implicit val toStatement: ToStatement[UserLoggedOutEvent] = ToStatement { instance =>
      import com.tersesystems.blindsight.DSL._
      Statement()
        .withMessage("UserLoggedOutEvent message with args {}")
        .withArguments(
          Arguments(
            bobj(
              "user-logged-out-event" ->
                ("name"     -> instance.name) ~
                  ("reason" -> instance.reason)
            )
          )
        )
    }
  }

  final case class UserIsUpLateEvent(name: String, excuse: String) extends UserEvent

  object UserIsUpLateEvent {
    implicit val toStatement: ToStatement[UserIsUpLateEvent] = ToStatement { instance =>
      import com.tersesystems.blindsight.DSL._

      Statement()
        .withMessage(instance.toString)
        .withArguments(
          Arguments(
            bobj(
              "user-is-up-late-event" ->
                ("name"     -> instance.name) ~
                  ("excuse" -> instance.excuse)
            )
          )
        )
    }
  }

  def main(args: Array[String]): Unit = {

    val userEventLogger: SemanticLogger[UserEvent] =
      LoggerFactory.getLogger(getClass).semantic[UserEvent]

    userEventLogger.info(UserLoggedInEvent("steve", "127.0.0.1"))
    userEventLogger.info(UserLoggedOutEvent("steve", "timeout"))

    userEventLogger.warn.when(LocalTime.now().isAfter(LocalTime.of(23, 0))) { log =>
      log(UserIsUpLateEvent("will", "someone is WRONG on the internet"))
    }

    val onlyLoggedInEventLogger: SemanticLogger[UserLoggedInEvent] =
      userEventLogger.refine[UserLoggedInEvent]
    onlyLoggedInEventLogger.info(UserLoggedInEvent("mike", "10.0.0.1"))
  }
}
// #semantic-main
