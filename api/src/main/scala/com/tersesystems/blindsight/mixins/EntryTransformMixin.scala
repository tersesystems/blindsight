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

package com.tersesystems.blindsight.mixins

import com.tersesystems.blindsight.Entry
import org.slf4j.event.Level

/**
 * Adds the ability for logged entries to go though a transformation
 * before being sent to SLF4J.
 */
trait EntryTransformMixin {
  type Self

  def withEntryTransform(level: Level, f: Entry => Entry): Self

  def withEntryTransform(f: Entry => Entry): Self
}
