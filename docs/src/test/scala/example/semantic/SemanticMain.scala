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

import com.tersesystems.blindsight.LoggerFactory
import com.tersesystems.blindsight.semantic.SemanticLogger

object SemanticMain {

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
