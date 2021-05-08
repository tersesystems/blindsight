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
