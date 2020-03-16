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

package com.tersesystems.blindsight.api.mixins

/**
 * A conditional logger mixin.
 */
trait OnConditionMixin {
  type Self

  /**
   * Returns a new instance of the logger that will only log if the
   * condition is met.
   *
   * @param test the call by name boolean that is a prerequisite for logging.
   * @return the new conditional logger instance.
   */
  def onCondition(test: => Boolean): Self
}
