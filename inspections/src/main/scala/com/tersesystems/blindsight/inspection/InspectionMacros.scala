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

/**
 * Macros used for close inspection and debugging values.
 *
 * Easiest way to use these is to pay the import tax up front, or
 * incorporate them into an `Inspections` trait.
 *
 * {{{
 * import com.tersesystems.blindsight.inspection._
 * import com.tersesystems.blindsight.inspection.InspectionMacros._
 * }}}
 *
 * These macros are intended to be used in situations where you would use
 * "printf debugging" to get a read on individual values and branches.
 *
 * The macros here have not been rigorously tested and should be considered
 * experimental.
 */
trait InspectionMacros {
  import scala.language.experimental.macros
  import InspectionMacros.impl

  /**
   * Decorates the given if statement with logging statements.
   *
   * For example, the following code:
   *
   * {{{
   * decorateIfs(dif: BranchInspection => logger.debug(s"\${dif.code} = \${dif.result}")) {
   *   if (System.currentTimeMillis() % 17 == 0) {
   *     println("branch 1")
   *   } else if (System.getProperty("derp") == null) {
   *     println("branch 2")
   *   } else {
   *     println("else branch")
   *   }
   * }
   * }}}
   *
   * would cause the logger to debug two lines:
   *
   * "System.currentTimeMillis() % 17 == 0 = false"
   * "System.getProperty("derp") == null = true"
   *
   * @param output the logging statement to apply on each branch.
   * @param ifStatement the if statement to decorate with statements
   * @tparam A the result type
   * @return the result of the if statement.
   */
  def decorateIfs[A](output: BranchInspection => Unit)(ifStatement: A): A = macro impl.decorateIfs

  /**
   * Decorates a match statement with logging statements at each case.
   *
   * For example, given the following code:
   *
   * {{{
   * val string = java.time.Instant.now().toString
   * decorateMatch(dm: BranchInspection => logger.debug(s"\${dm.code} = \${dm.result}")) {
   *   string match {
   *     case s if s.startsWith("20") =>
   *       println("this example is still valid")
   *     case _ =>
   *       println("oh dear")
   *   }
   * }
   * }}}
   *
   * This will log the following at DEBUG level:
   *
   * "string match case s if s.startsWith("20") = true"
   *
   * @param output the logging statement to apply to each case
   * @param matchStatement the match statement to decorate with statements
   * @tparam A the result type
   * @return
   */
  def decorateMatch[A](output: BranchInspection => Unit)(matchStatement: A): A =
    macro impl.decorateMatch

  /**
   * Decorates a given block with logging statements after each `val` or `var` (technically a `ValDef`).
   *
   * For example, given the following statement:
   *
   * {{{
   * decorateVals(dval: ValDefInspection => logger.debug(s"\${dval.name} = \${dval.value}")) {
   *   val a = 5
   *   val b = 15
   *   a + b
   * }
   * }}}
   *
   * There would be two statements logged at DEBUG level:
   *
   * "a = 5"
   * "b = 15"
   *
   * @param output the logging statement to put after each ValDef
   * @param block the block to decorate with logging statements.
   * @tparam A the result type
   * @return the result, if any
   */
  def decorateVals[A](output: ValDefInspection => Unit)(block: A): A = macro impl.decorateVals[A]

  /**
   * Creates a `DebugResult` containing the code and result of a block / expression.
   *
   * For example, the following statement:
   *
   * {{{
   * val dr: ExprInspection[Int] = dumpExpression(1 + 1)
   * logger.debug(s"result: \${dr.code} = \${dr.value}")
   * }}}
   *
   * would result in "result: 1 + 1 = 2"
   *
   * @param block the block or expression
   * @tparam A the result type
   * @return a debug result containing the code and value.
   */
  def dumpExpression[A](block: A): ExprInspection[A] = macro impl.dumpExpression[A]

  /**
   * Dumps public fields from an object.
   *
   * {{{
   * class ExampleClass(val someInt: Int) {
   *   protected val protectedInt = 22
   * }
   * val exObj        = new ExampleClass(42)
   * val publicFields: Seq[ValDefInspection] = dumpPublicFields(exObj)
   *
   * logger.debug(publicFields.toString)
   * }}}
   *
   * Should result in:
   *
   * "Seq(DebugVal(someInt,42))"
   *
   * @param instance the object instance
   * @tparam A the type of the object
   * @return the `DebugVal` representing the public fields of the object.
   */
  def dumpPublicFields[A](instance: A): Seq[ValDefInspection] = macro impl.dumpPublicFields[A]
}

object InspectionMacros extends InspectionMacros {
  import scala.reflect.api.Trees
  import scala.reflect.macros.blackbox

  private class impl(val c: blackbox.Context) {
    import c.universe._

    def decorateIfs(output: c.Expr[BranchInspection => Unit])(ifStatement: c.Tree): c.Tree = {
      ifStatement match {
        case q"if ($cond) $thenp else $elsep" =>
          val condSource = extractRange(cond) getOrElse ""
          val printThen  = q"$output(com.tersesystems.blindsight.inspection.BranchInspection($condSource, true))"
          val elseThen   = q"$output(com.tersesystems.blindsight.inspection.BranchInspection($condSource, false))"
          val decElseP   = decorateIfs(output)(elsep.asInstanceOf[c.Tree])

          val thenTree = q"""{ $printThen; $thenp }"""
          val elseTree = if (isEmpty(decElseP)) decElseP else q"""{ $elseThen; $decElseP }"""
          q"if ($cond) $thenTree else $elseTree"
        case other =>
          other
      }
    }

    private def isEmpty(tree: Trees#Tree): Boolean = {
      tree match {
        case Literal(Constant(())) =>
          true
        case other =>
          false
      }
    }

    def decorateMatch(output: c.Expr[BranchInspection => Unit])(matchStatement: c.Tree): c.Tree = {
      matchStatement match {
        case q"$expr match { case ..$cases }" =>
          val enhancedCases = cases.map {
            case CaseDef(pat, guard, body) =>
              val exprSource  = extractRange(expr) getOrElse ""
              val patSource   = extractRange(pat).map(p => s" match case $p") getOrElse ""
              val guardSource = extractRange(guard).map(" if " + _).getOrElse("")
              val src         = exprSource + patSource + guardSource
              val debugIf     = q"com.tersesystems.blindsight.inspection.BranchInspection($src, true)"
              val stmt        = q"$output($debugIf); $body"
              CaseDef(pat, guard, stmt)
            case other =>
              throw new IllegalStateException("Unknown case " + other)
          }
          q"$expr match { case ..$enhancedCases }"
        case other =>
          other
      }
    }

    def dumpExpression[A](block: c.Expr[A]): c.Expr[ExprInspection[A]] = {
      val portion = extractRange(block.tree) getOrElse ""
      val const   = c.Expr[String](Literal(Constant(portion)))
      c.Expr[ExprInspection[A]](q"com.tersesystems.blindsight.inspection.ExprInspection($const, $block)")
    }

    def decorateVals[A](output: c.Expr[ValDefInspection => Unit])(block: c.Expr[A]): c.Expr[A] = {
      val loggedStats = block.tree.children.flatMap {
        case valdef @ ValDef(_, termName, _, _) =>
          List(valdef, q"$output(com.tersesystems.blindsight.inspection.ValDefInspection(${termName.encodedName.toString}, $termName))")
        case stat =>
          List(stat)
      }
      val outputExpr: c.Expr[A] = c.Expr[A](c.untypecheck(q"..$loggedStats"))
      outputExpr
    }

    def dumpPublicFields[A: WeakTypeTag](instance: c.Expr[A]): c.Expr[Seq[ValDefInspection]] = {
      def classVals(tpe: c.universe.Type) = {
        tpe.decls.collect {
          case method: MethodSymbol if method.isAccessor && method.isPublic =>
            val nameStr = method.name.decodedName.toString
            q"com.tersesystems.blindsight.inspection.ValDefInspection(${nameStr}, $instance.$method)"
        }
      }

      val classType = weakTypeTag[A].tpe
      val fields    = classVals(classType)

      c.Expr[Seq[ValDefInspection]](q"Seq(..$fields)")
    }

    private def extractRange(t: Trees#Tree): Option[String] = {
      val pos    = t.pos
      val source = pos.source.content
      if (pos.isRange) Option(new String(source.drop(pos.start).take(pos.end - pos.start)))
      else None
    }
  }

}
