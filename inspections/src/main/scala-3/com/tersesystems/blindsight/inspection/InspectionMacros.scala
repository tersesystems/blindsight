package com.tersesystems.blindsight.inspection

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
  inline def decorateIfs[A](output: BranchInspection => Unit)(ifStatement: => A): A

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
  inline def decorateMatch[A](output: BranchInspection => Unit)(matchStatement: => A): A

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
  inline def decorateVals[A](output: ValDefInspection => Unit)(inline block: A): A

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
  inline def dumpExpression[A](block: A): ExprInspection[A]

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
  inline def dumpPublicFields[A](instance: A): Seq[ValDefInspection]
}

// https://docs.scala-lang.org/scala3/guides/migration/tutorial-macro-cross-building.html
// You must run with "project inspections3; test" for this to work with sbt-projectmatrix
object InspectionMacros extends InspectionMacros {
  import scala.quoted.*

  /*
   inline def location: Location = ${locationImpl}

  private def locationImpl(using quotes: Quotes): Expr[Location] =
    import quotes.reflect.Position
    val pos = Position.ofMacroExpansion
    val file = Expr(pos.sourceFile.jpath.toString)
    val line = Expr(pos.startLine + 1)
    '{new Location($file, $line)}
   */

  inline def decorateIfs[A](output: BranchInspection => Unit)(ifStatement: => A): A =
    ${ Impl.decorateIfsImpl('output, 'ifStatement) }

  inline def decorateMatch[A](output: BranchInspection => Unit)(matchStatement: => A): A =
    ${ Impl.decorateMatchImpl('output, 'matchStatement) }

  inline def dumpExpression[A](block: A): ExprInspection[A] =
    ${ Impl.dumpExpressionImpl('block) }

  inline def decorateVals[A](output: ValDefInspection => Unit)(inline block: A): A =
    ${ Impl.decorateValsImpl('output, 'block) }

  inline def dumpPublicFields[A](instance: A): Seq[ValDefInspection] =
    ${ Impl.dumpPublicFields('instance) }

  object Impl {

    def dumpPublicFields[A: Type](instance: Expr[A])(using Quotes): Expr[Seq[ValDefInspection]] = {
      import quotes.reflect.*

      def isPublic(sym: Symbol): Boolean = {
        !(sym.flags.is(Flags.Protected) || sym.flags.is(Flags.Private))
      }

      def classVals(repr: TypeRepr): Seq[Expr[ValDefInspection]] = {

        val exprs: Seq[Expr[ValDefInspection]] = repr.typeSymbol.declaredFields.collect {
          case other if isPublic(other) =>
            val name: String = other.name
            val field: Term  = Select.unique(instance.asTerm, name)
            '{ ValDefInspection(${ Expr(name) }, ${ field.asExpr }) }
        }
        exprs
      }
      val fields: Seq[Expr[ValDefInspection]] = classVals(TypeRepr.of[A])
      val expr: Expr[Seq[ValDefInspection]]   = Expr.ofSeq(fields)
      expr
    }

    def decorateMatchImpl[A: Type](
        output: Expr[BranchInspection => Unit],
        matchStatement: Expr[A]
    )(using Quotes): Expr[A] = {
      import quotes.reflect.*

      matchStatement.asTerm match {
        case Inlined(_, _, matchIdent: Ident) =>
          matchIdent.symbol.tree match {
            case DefDef(_, _, _, Some(term)) =>
              // println(s"matchStatement = ${term}")
              val modifiedMatch = term match {
                case Block(b, Match(m, cases)) =>
                  // List of case defs
                  // println(s"cases = ${cases}")
                  val enhancedCases = cases.map {
                    case CaseDef(pat: Tree, guard: Option[Term], body: Term) =>
                      val guardSource = guard.map(t => s" if ${t.show}").getOrElse("")
                      val patSource = pat match {
                        case Bind(b, f) => s"case $b"
                        case Ident(i)   => s"case $i"
                        case other =>
                          println(s"Unexpected macro case $other")
                          s"case $other"
                      }
                      val name: String = m.asExpr.show
                      val src          = Expr(s"$name match $patSource$guardSource")
                      val bodyExpr     = body.asExpr
                      val stmt         = '{ $output(BranchInspection($src, true)); $bodyExpr }
                      CaseDef(pat, guard, stmt.asTerm)
                  }
                  Block(b, Match(m, enhancedCases))
                case other =>
                  println(s"other = $other")
                  other
              }
              // println(s"${modifiedMatch.show}")
              modifiedMatch.asExprOf[A]

            case otherIdent =>
              report.error(s"Not a valid identifier: ${otherIdent}")
              matchStatement
          }
        case other =>
          report.error(s"Parameter must be a known ident: ${other.show}")
          matchStatement
      }
    }

    def decorateIfsImpl[A: Type](
        output: Expr[BranchInspection => Unit],
        ifStatement: Expr[A]
    )(using Quotes): Expr[A] = {
      import quotes.reflect.*

      def findIfMethod(ifTerm: Term): Expr[A] = {
        ifTerm match {
          case Block(stats, If(condTerm, thenTerm, elseTerm)) =>
            constructIf(condTerm, thenTerm, elseTerm)

          case If(condTerm, thenTerm, elseTerm) =>
            constructIf(condTerm, thenTerm, elseTerm)

          case other =>
            // println(s"Rendering block as $other")
            ifTerm.asExprOf[A]
        }
      }

      def constructIf(condTerm: Term, thenTerm: Term, elseTerm: Term): Expr[A] = {
        val condSource          = condTerm.show
        val branchTrue          = '{ BranchInspection(${ Expr(condSource) }, true) }
        val branchFalse         = '{ BranchInspection(${ Expr(condSource) }, false) }
        val cond: Expr[Boolean] = condTerm.asExprOf[Boolean]
        val thenp: Expr[A]      = thenTerm.asExprOf[A]
        val elsep: Expr[A]      = findIfMethod(elseTerm)

        // Return a construction with the new statement
        val remade = '{
          if ($cond) { $output($branchTrue); $thenp }
          else { $output($branchFalse); $elsep }
        }
        // println(s"remade = ${remade.show}")
        remade
      }

      // What we get is an inlined ident pointing to the method.
      // so we have to dig a little to get the actual if statement.
      ifStatement.asTerm match {
        case Inlined(_, _, ifIdent: Ident) =>
          // XXX blockTerm.symbol
          ifIdent.symbol.tree match {
            case DefDef(_, _, _, Some(term)) =>
              findIfMethod(term)

            case otherIdent =>
              report.error(s"Not a valid identifier: ${otherIdent}")
              ifStatement
          }
        case other =>
          report.error(s"Parameter must be a known ident: ${other.show}")
          ifStatement
      }
    }

    def dumpExpressionImpl[A: Type](block: Expr[A])(using Quotes): Expr[ExprInspection[A]] = {
      import quotes.reflect.*

      // this is the same as sourcecode.Text :-/
      val result = block.asTerm.pos.sourceCode.get
      val const  = Expr(result)
      '{ ExprInspection($const, $block) }
    }

    def decorateValsImpl[A: Type](output: Expr[ValDefInspection => Unit], block: Expr[A])(using
        Quotes
    ): Expr[A] = {
      import quotes.reflect.*

      def rewriteBlock(data: Term): Term = {
        data match {
          case Block(stmts, expr) =>
            val newStmts = stmts.flatMap(rewriteStatement)
            Block(newStmts, expr)
        }
      }

      def rewriteStatement(statement: Statement): List[Statement] = {
        statement match {
          case valdef: ValDef =>
            val termExpr: Expr[String] = Expr(valdef.name)
            val termRef                = TermRef(valdef.tpt.tpe, valdef.name)
            val identExpr              = Ref(valdef.symbol).asExpr
            val inspection: Term = '{ $output(ValDefInspection($termExpr, $identExpr)) }.asTerm
            List(valdef, inspection)
          case other =>
            List(other)
        }
      }

      block.asTerm match {
        case tree: Inlined =>
          Inlined
            .copy(tree)(
              call = tree.call,
              bindings = tree.bindings,
              expansion = rewriteBlock(tree.body)
            )
            .asExprOf[A]
        case _ =>
          block
      }
    }
  }

}
