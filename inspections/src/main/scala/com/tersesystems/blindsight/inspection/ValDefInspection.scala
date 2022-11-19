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

package com.tersesystems.blindsight.inspection

/**
 * Debugs a valdef.
 *
 * @param name name of the val or var
 * @param value the value of the val or var
 */
case class ValDefInspection(name: String, value: Any)

/**
 * Debugs a branch (if / match).
 *
 * @param code the condition of the branch
 * @param result the result of evaluating the condition
 */
case class BranchInspection(code: String, result: Boolean)

/**
 * Debugs a result.
 *
 * @param code the code that went into the result
 * @param value the result
 * @tparam A the type of the result.
 */
case class ExprInspection[A](code: String, value: A)
