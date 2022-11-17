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

package com.tersesystems.blindsight

import org.slf4j.event.Level

trait Condition extends ((Level, Markers) => Boolean)

object Condition {

  implicit def functionToCondition(f: => Boolean): Condition = Condition(f)

  def apply(f: => Boolean): Condition =
    new Condition {
      override def apply(level: Level, markers: Markers): Boolean = f
    }

  def apply(predicate: java.util.function.Predicate[Level]): Condition =
    new Condition {
      override def apply(level: Level, markers: Markers): Boolean = predicate.test(level)
    }

  def apply(f: Markers => Boolean): Condition =
    new Condition {
      override def apply(level: Level, markers: Markers): Boolean = f(markers)
    }

  def apply(f: (Level, Markers) => Boolean): Condition =
    new Condition {
      override def apply(level: Level, markers: Markers): Boolean = f(level, markers)
    }

  val always: Condition = Condition(true)

  val never: Condition = Condition(false)
}
