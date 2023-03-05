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

package com.tersesystems.blindsight.slf4j

import com.tersesystems.blindsight.{ToArgument, _}
import com.tersesystems.blindsight.core.{CoreLogger, ParameterList}
import org.slf4j.Marker
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

import scala.annotation.nowarn

/**
 * A strict logger method that only takes type class instances for statements.
 */
trait StrictSLF4JMethod {
  def level: Level

  /**
   * Runs with a block function that is only called when condition is true.
   *
   * @param condition the call by name boolean that must return true
   * @param block the block executed when condition is true.
   */
  def when(condition: Condition)(block: StrictSLF4JMethod => Unit): Unit

  def apply(block: StrictSLF4JMethod => Unit): Unit

  def apply(
      st: Statement
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      message: Message
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      throwable: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[A: ToArgument](
      message: Message,
      arg: A
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[A1: ToArgument, A2: ToArgument](
      message: Message,
      arg1: A1,
      arg2: A2
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument
  ](
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument
  ](
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument
  ](
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument
  ](
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument
  ](
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument
  ](
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument
  ](
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument
  ](
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument
  ](
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument
  ](
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11,
      arg12: A12
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument
  ](
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11,
      arg12: A12,
      arg13: A13
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument
  ](
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11,
      arg12: A12,
      arg13: A13,
      arg14: A14
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument
  ](
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11,
      arg12: A12,
      arg13: A13,
      arg14: A14,
      arg15: A15
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument,
      A16: ToArgument
  ](
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11,
      arg12: A12,
      arg13: A13,
      arg14: A14,
      arg15: A15,
      arg16: A16
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument,
      A16: ToArgument,
      A17: ToArgument
  ](
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11,
      arg12: A12,
      arg13: A13,
      arg14: A14,
      arg15: A15,
      arg16: A16,
      arg17: A17
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument,
      A16: ToArgument,
      A17: ToArgument,
      A18: ToArgument
  ](
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11,
      arg12: A12,
      arg13: A13,
      arg14: A14,
      arg15: A15,
      arg16: A16,
      arg17: A17,
      arg18: A18
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument,
      A16: ToArgument,
      A17: ToArgument,
      A18: ToArgument,
      A19: ToArgument
  ](
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11,
      arg12: A12,
      arg13: A13,
      arg14: A14,
      arg15: A15,
      arg16: A16,
      arg17: A17,
      arg18: A18,
      arg19: A19
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument,
      A16: ToArgument,
      A17: ToArgument,
      A18: ToArgument,
      A19: ToArgument,
      A20: ToArgument
  ](
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11,
      arg12: A12,
      arg13: A13,
      arg14: A14,
      arg15: A15,
      arg16: A16,
      arg17: A17,
      arg18: A18,
      arg19: A19,
      arg20: A20
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      message: Message,
      args: Arguments
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      markers: Markers
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      markers: Markers,
      message: Message
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      markers: Markers,
      throwable: Throwable
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[A: ToArgument](
      markers: Markers,
      message: Message,
      arg: A
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[A1: ToArgument, A2: ToArgument](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument
  ](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument
  ](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument
  ](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument
  ](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument
  ](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument
  ](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument
  ](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument
  ](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument
  ](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument
  ](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11,
      arg12: A12
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument
  ](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11,
      arg12: A12,
      arg13: A13
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument
  ](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11,
      arg12: A12,
      arg13: A13,
      arg14: A14
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument
  ](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11,
      arg12: A12,
      arg13: A13,
      arg14: A14,
      arg15: A15
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument,
      A16: ToArgument
  ](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11,
      arg12: A12,
      arg13: A13,
      arg14: A14,
      arg15: A15,
      arg16: A16
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument,
      A16: ToArgument,
      A17: ToArgument
  ](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11,
      arg12: A12,
      arg13: A13,
      arg14: A14,
      arg15: A15,
      arg16: A16,
      arg17: A17
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument,
      A16: ToArgument,
      A17: ToArgument,
      A18: ToArgument
  ](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11,
      arg12: A12,
      arg13: A13,
      arg14: A14,
      arg15: A15,
      arg16: A16,
      arg17: A17,
      arg18: A18
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument,
      A16: ToArgument,
      A17: ToArgument,
      A18: ToArgument,
      A19: ToArgument
  ](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11,
      arg12: A12,
      arg13: A13,
      arg14: A14,
      arg15: A15,
      arg16: A16,
      arg17: A17,
      arg18: A18,
      arg19: A19
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply[
      A1: ToArgument,
      A2: ToArgument,
      A3: ToArgument,
      A4: ToArgument,
      A5: ToArgument,
      A6: ToArgument,
      A7: ToArgument,
      A8: ToArgument,
      A9: ToArgument,
      A10: ToArgument,
      A11: ToArgument,
      A12: ToArgument,
      A13: ToArgument,
      A14: ToArgument,
      A15: ToArgument,
      A16: ToArgument,
      A17: ToArgument,
      A18: ToArgument,
      A19: ToArgument,
      A20: ToArgument
  ](
      markers: Markers,
      message: Message,
      arg1: A1,
      arg2: A2,
      arg3: A3,
      arg4: A4,
      arg5: A5,
      arg6: A6,
      arg7: A7,
      arg8: A8,
      arg9: A9,
      arg10: A10,
      arg11: A11,
      arg12: A12,
      arg13: A13,
      arg14: A14,
      arg15: A15,
      arg16: A16,
      arg17: A17,
      arg18: A18,
      arg19: A19,
      arg20: A20
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

  def apply(
      markers: Markers,
      message: Message,
      args: Arguments
  )(implicit line: Line, file: File, enclosing: Enclosing): Unit

}

object StrictSLF4JMethod {

  /**
   * Strict method implementation.
   */
  class Impl(val level: Level, core: CoreLogger) extends StrictSLF4JMethod {
    protected val parameterList: ParameterList = core.parameterList(level)

    import parameterList._

    override def when(condition: Condition)(block: StrictSLF4JMethod => Unit): Unit = {
      if (core.when(level, condition)) {
        block(this)
      }
    }

    override def apply(
        st: Statement
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) parameterList.executeStatement(st)
    }

    def apply(block: StrictSLF4JMethod => Unit): Unit = {
      if (enabled) block(this)
    }

    override def apply(
        msg: Message
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        message(msg.toString)
      }
    }

    override def apply(
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArg1("", throwable)
      }
    }

    override def apply[A: ToArgument](
        message: Message,
        arg: A
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArg1(message.toString, Argument(arg).value)
      }
    }

    override def apply[A1: ToArgument, A2: ToArgument](
        message: Message,
        arg1: A1,
        arg2: A2
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArg1Arg2(
          message.toString,
          Argument(arg1).value,
          Argument(arg2).value
        )
      }
    }

    override def apply[A1: ToArgument, A2: ToArgument, A3: ToArgument](
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArgs(
          message.toString,
          Arguments(arg1, arg2, arg3).toArray
        )
      }
    }

    override def apply[A1: ToArgument, A2: ToArgument, A3: ToArgument, A4: ToArgument](
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArgs(
          message.toString,
          Arguments(arg1, arg2, arg3, arg4).toArray
        )
      }
    }

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument
    ](message: Message, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      if (enabled) {
        messageArgs(
          message.toString,
          Arguments(arg1, arg2, arg3, arg4, arg5).toArray
        )
      }
    }

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument
    ](message: Message, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      if (enabled) {
        messageArgs(
          message.toString,
          Arguments(arg1, arg2, arg3, arg4, arg5, arg6).toArray
        )
      }
    }

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument
    ](message: Message, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6, arg7: A7)(
        implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = {
      if (enabled) {
        messageArgs(
          message.toString,
          Arguments(arg1, arg2, arg3, arg4, arg5, arg6, arg7).toArray
        )
      }
    }

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument
    ](
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArgs(
          message.toString,
          Arguments(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8).toArray
        )
      }
    }

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument
    ](
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArgs(
          message.toString,
          Arguments(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9).toArray
        )
      }
    }

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument
    ](
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArgs(
          message.toString,
          Arguments(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10).toArray
        )
      }
    }

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument
    ](
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArgs(
          message.toString,
          Arguments(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11).toArray
        )
      }
    }

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument,
        A12: ToArgument
    ](
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11,
        arg12: A12
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArgs(
          message.toString,
          Arguments(
            arg1,
            arg2,
            arg3,
            arg4,
            arg5,
            arg6,
            arg7,
            arg8,
            arg9,
            arg10,
            arg11,
            arg12
          ).toArray
        )
      }
    }

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument,
        A12: ToArgument,
        A13: ToArgument
    ](
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11,
        arg12: A12,
        arg13: A13
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArgs(
          message.toString,
          Arguments(
            arg1,
            arg2,
            arg3,
            arg4,
            arg5,
            arg6,
            arg7,
            arg8,
            arg9,
            arg10,
            arg11,
            arg12,
            arg13
          ).toArray
        )
      }
    }

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument,
        A12: ToArgument,
        A13: ToArgument,
        A14: ToArgument
    ](
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11,
        arg12: A12,
        arg13: A13,
        arg14: A14
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArgs(
          message.toString,
          Arguments(
            arg1,
            arg2,
            arg3,
            arg4,
            arg5,
            arg6,
            arg7,
            arg8,
            arg9,
            arg10,
            arg11,
            arg12,
            arg13,
            arg14
          ).toArray
        )
      }
    }

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument,
        A12: ToArgument,
        A13: ToArgument,
        A14: ToArgument,
        A15: ToArgument
    ](
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11,
        arg12: A12,
        arg13: A13,
        arg14: A14,
        arg15: A15
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArgs(
          message.toString,
          Arguments(
            arg1,
            arg2,
            arg3,
            arg4,
            arg5,
            arg6,
            arg7,
            arg8,
            arg9,
            arg10,
            arg11,
            arg12,
            arg13,
            arg14,
            arg15
          ).toArray
        )
      }
    }

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument,
        A12: ToArgument,
        A13: ToArgument,
        A14: ToArgument,
        A15: ToArgument,
        A16: ToArgument
    ](
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11,
        arg12: A12,
        arg13: A13,
        arg14: A14,
        arg15: A15,
        arg16: A16
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArgs(
          message.toString,
          Arguments(
            arg1,
            arg2,
            arg3,
            arg4,
            arg5,
            arg6,
            arg7,
            arg8,
            arg9,
            arg10,
            arg11,
            arg12,
            arg13,
            arg14,
            arg15,
            arg16
          ).toArray
        )
      }
    }

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument,
        A12: ToArgument,
        A13: ToArgument,
        A14: ToArgument,
        A15: ToArgument,
        A16: ToArgument,
        A17: ToArgument
    ](
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11,
        arg12: A12,
        arg13: A13,
        arg14: A14,
        arg15: A15,
        arg16: A16,
        arg17: A17
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArgs(
          message.toString,
          Arguments(
            arg1,
            arg2,
            arg3,
            arg4,
            arg5,
            arg6,
            arg7,
            arg8,
            arg9,
            arg10,
            arg11,
            arg12,
            arg13,
            arg14,
            arg15,
            arg16,
            arg17
          ).toArray
        )
      }
    }

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument,
        A12: ToArgument,
        A13: ToArgument,
        A14: ToArgument,
        A15: ToArgument,
        A16: ToArgument,
        A17: ToArgument,
        A18: ToArgument
    ](
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11,
        arg12: A12,
        arg13: A13,
        arg14: A14,
        arg15: A15,
        arg16: A16,
        arg17: A17,
        arg18: A18
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArgs(
          message.toString,
          Arguments(
            arg1,
            arg2,
            arg3,
            arg4,
            arg5,
            arg6,
            arg7,
            arg8,
            arg9,
            arg10,
            arg11,
            arg12,
            arg13,
            arg14,
            arg15,
            arg16,
            arg17,
            arg18
          ).toArray
        )
      }
    }

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument,
        A12: ToArgument,
        A13: ToArgument,
        A14: ToArgument,
        A15: ToArgument,
        A16: ToArgument,
        A17: ToArgument,
        A18: ToArgument,
        A19: ToArgument
    ](
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11,
        arg12: A12,
        arg13: A13,
        arg14: A14,
        arg15: A15,
        arg16: A16,
        arg17: A17,
        arg18: A18,
        arg19: A19
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArgs(
          message.toString,
          Arguments(
            arg1,
            arg2,
            arg3,
            arg4,
            arg5,
            arg6,
            arg7,
            arg8,
            arg9,
            arg10,
            arg11,
            arg12,
            arg13,
            arg14,
            arg15,
            arg16,
            arg17,
            arg18,
            arg19
          ).toArray
        )
      }
    }

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument,
        A12: ToArgument,
        A13: ToArgument,
        A14: ToArgument,
        A15: ToArgument,
        A16: ToArgument,
        A17: ToArgument,
        A18: ToArgument,
        A19: ToArgument,
        A20: ToArgument
    ](
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11,
        arg12: A12,
        arg13: A13,
        arg14: A14,
        arg15: A15,
        arg16: A16,
        arg17: A17,
        arg18: A18,
        arg19: A19,
        arg20: A20
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArgs(
          message.toString,
          Arguments(
            arg1,
            arg2,
            arg3,
            arg4,
            arg5,
            arg6,
            arg7,
            arg8,
            arg9,
            arg10,
            arg11,
            arg12,
            arg13,
            arg14,
            arg15,
            arg16,
            arg17,
            arg18,
            arg19
          ).toArray
        )
      }
    }

    override def apply(
        message: Message,
        args: Arguments
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled) {
        messageArgs(message.toString, args.toArray)
      }
    }

    override def apply(
        markers: Markers
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled(markers.marker)) {
        markerMessage(markers.marker, "")
      }
    }

    override def apply(
        markers: Markers,
        throwable: Throwable
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled(markers.marker)) {
        markerMessageArg1(markers.marker, "", throwable)
      }
    }

    override def apply(
        markers: Markers,
        message1: Message
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled(markers.marker)) {
        markerMessage(markers.marker, message1.toString)
      }
    }

    override def apply[A: ToArgument](
        markers: Markers,
        message1: Message,
        arg: A
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled(markers.marker)) {
        markerMessageArg1(markers.marker, message1.toString, Argument(arg).value)
      }
    }

    override def apply[A1: ToArgument, A2: ToArgument](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled(markers.marker)) {
        markerMessageArg1Arg2(
          markers.marker,
          message.toString,
          Argument(arg1).value,
          Argument(arg2).value
        )
      }
    }

    override def apply[A1: ToArgument, A2: ToArgument, A3: ToArgument](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = apply(
      markers,
      message,
      Arguments(
        arg1,
        arg2,
        arg3
      )
    )

    override def apply[A1: ToArgument, A2: ToArgument, A3: ToArgument, A4: ToArgument](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = apply(
      markers,
      message,
      Arguments(
        arg1,
        arg2,
        arg3,
        arg4
      )
    )

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument
    ](markers: Markers, message: Message, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5)(implicit
        line: Line,
        file: File,
        enclosing: Enclosing
    ): Unit = apply(
      markers,
      message,
      Arguments(
        arg1,
        arg2,
        arg3,
        arg4,
        arg5
      )
    )

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument
    ](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = apply(
      markers,
      message,
      Arguments(
        arg1,
        arg2,
        arg3,
        arg4,
        arg5,
        arg6
      )
    )

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument
    ](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = apply(
      markers,
      message,
      Arguments(
        arg1,
        arg2,
        arg3,
        arg4,
        arg5,
        arg6,
        arg7
      )
    )

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument
    ](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = apply(
      markers,
      message,
      Arguments(
        arg1,
        arg2,
        arg3,
        arg4,
        arg5,
        arg6,
        arg7,
        arg8
      )
    )

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument
    ](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = apply(
      markers,
      message,
      Arguments(
        arg1,
        arg2,
        arg3,
        arg4,
        arg5,
        arg6,
        arg7,
        arg8,
        arg9
      )
    )

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument
    ](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = apply(
      markers,
      message,
      Arguments(
        arg1,
        arg2,
        arg3,
        arg4,
        arg5,
        arg6,
        arg7,
        arg8,
        arg9,
        arg10
      )
    )

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument
    ](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = apply(
      markers,
      message,
      Arguments(
        arg1,
        arg2,
        arg3,
        arg4,
        arg5,
        arg6,
        arg7,
        arg8,
        arg9,
        arg10,
        arg11
      )
    )

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument,
        A12: ToArgument
    ](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11,
        arg12: A12
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = apply(
      markers,
      message,
      Arguments(
        arg1,
        arg2,
        arg3,
        arg4,
        arg5,
        arg6,
        arg7,
        arg8,
        arg9,
        arg10,
        arg11,
        arg12
      )
    )

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument,
        A12: ToArgument,
        A13: ToArgument
    ](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11,
        arg12: A12,
        arg13: A13
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = apply(
      markers,
      message,
      Arguments(
        arg1,
        arg2,
        arg3,
        arg4,
        arg5,
        arg6,
        arg7,
        arg8,
        arg9,
        arg10,
        arg11,
        arg12,
        arg13
      )
    )

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument,
        A12: ToArgument,
        A13: ToArgument,
        A14: ToArgument
    ](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11,
        arg12: A12,
        arg13: A13,
        arg14: A14
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = apply(
      markers,
      message,
      Arguments(
        arg1,
        arg2,
        arg3,
        arg4,
        arg5,
        arg6,
        arg7,
        arg8,
        arg9,
        arg10,
        arg11,
        arg12,
        arg13,
        arg14
      )
    )

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument,
        A12: ToArgument,
        A13: ToArgument,
        A14: ToArgument,
        A15: ToArgument
    ](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11,
        arg12: A12,
        arg13: A13,
        arg14: A14,
        arg15: A15
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = apply(
      markers,
      message,
      Arguments(
        arg1,
        arg2,
        arg3,
        arg4,
        arg5,
        arg6,
        arg7,
        arg8,
        arg9,
        arg10,
        arg11,
        arg12,
        arg13,
        arg14,
        arg15
      )
    )

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument,
        A12: ToArgument,
        A13: ToArgument,
        A14: ToArgument,
        A15: ToArgument,
        A16: ToArgument
    ](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11,
        arg12: A12,
        arg13: A13,
        arg14: A14,
        arg15: A15,
        arg16: A16
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = apply(
      markers,
      message,
      Arguments(
        arg1,
        arg2,
        arg3,
        arg4,
        arg5,
        arg6,
        arg7,
        arg8,
        arg9,
        arg10,
        arg11,
        arg12,
        arg13,
        arg14,
        arg15,
        arg16
      )
    )

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument,
        A12: ToArgument,
        A13: ToArgument,
        A14: ToArgument,
        A15: ToArgument,
        A16: ToArgument,
        A17: ToArgument
    ](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11,
        arg12: A12,
        arg13: A13,
        arg14: A14,
        arg15: A15,
        arg16: A16,
        arg17: A17
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = apply(
      markers,
      message,
      Arguments(
        arg1,
        arg2,
        arg3,
        arg4,
        arg5,
        arg6,
        arg7,
        arg8,
        arg9,
        arg10,
        arg11,
        arg12,
        arg13,
        arg14,
        arg15,
        arg16,
        arg17
      )
    )

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument,
        A12: ToArgument,
        A13: ToArgument,
        A14: ToArgument,
        A15: ToArgument,
        A16: ToArgument,
        A17: ToArgument,
        A18: ToArgument
    ](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11,
        arg12: A12,
        arg13: A13,
        arg14: A14,
        arg15: A15,
        arg16: A16,
        arg17: A17,
        arg18: A18
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = apply(
      markers,
      message,
      Arguments(
        arg1,
        arg2,
        arg3,
        arg4,
        arg5,
        arg6,
        arg7,
        arg8,
        arg9,
        arg10,
        arg11,
        arg12,
        arg13,
        arg14,
        arg15,
        arg16,
        arg17,
        arg18
      )
    )

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument,
        A12: ToArgument,
        A13: ToArgument,
        A14: ToArgument,
        A15: ToArgument,
        A16: ToArgument,
        A17: ToArgument,
        A18: ToArgument,
        A19: ToArgument
    ](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11,
        arg12: A12,
        arg13: A13,
        arg14: A14,
        arg15: A15,
        arg16: A16,
        arg17: A17,
        arg18: A18,
        arg19: A19
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = apply(
      markers,
      message,
      Arguments(
        arg1,
        arg2,
        arg3,
        arg4,
        arg5,
        arg6,
        arg7,
        arg8,
        arg9,
        arg10,
        arg11,
        arg12,
        arg13,
        arg14,
        arg15,
        arg16,
        arg17,
        arg18,
        arg19
      )
    )

    override def apply[
        A1: ToArgument,
        A2: ToArgument,
        A3: ToArgument,
        A4: ToArgument,
        A5: ToArgument,
        A6: ToArgument,
        A7: ToArgument,
        A8: ToArgument,
        A9: ToArgument,
        A10: ToArgument,
        A11: ToArgument,
        A12: ToArgument,
        A13: ToArgument,
        A14: ToArgument,
        A15: ToArgument,
        A16: ToArgument,
        A17: ToArgument,
        A18: ToArgument,
        A19: ToArgument,
        A20: ToArgument
    ](
        markers: Markers,
        message: Message,
        arg1: A1,
        arg2: A2,
        arg3: A3,
        arg4: A4,
        arg5: A5,
        arg6: A6,
        arg7: A7,
        arg8: A8,
        arg9: A9,
        arg10: A10,
        arg11: A11,
        arg12: A12,
        arg13: A13,
        arg14: A14,
        arg15: A15,
        arg16: A16,
        arg17: A17,
        arg18: A18,
        arg19: A19,
        arg20: A20
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      apply(
        markers,
        message,
        Arguments(
          arg1,
          arg2,
          arg3,
          arg4,
          arg5,
          arg6,
          arg7,
          arg8,
          arg9,
          arg10,
          arg11,
          arg12,
          arg13,
          arg14,
          arg15,
          arg16,
          arg17,
          arg18,
          arg19,
          arg20
        )
      )
    }

    override def apply(
        markers: Markers,
        message: Message,
        args: Arguments
    )(implicit line: Line, file: File, enclosing: Enclosing): Unit = {
      if (enabled(markers.marker)) {
        markerMessageArgs(markers.marker, message.toString, args.toArray)
      }
    }

    override def toString: String = {
      s"${getClass.getName}(logger=$core)"
    }

    @nowarn
    protected def enabled(implicit line: Line, file: File, enclosing: Enclosing): Boolean = {
      parameterList.executePredicate()
    }

    @nowarn
    protected def enabled(
        marker: Marker
    )(implicit line: Line, file: File, enclosing: Enclosing): Boolean = {
      parameterList.executePredicate(marker)
    }

  }

}
