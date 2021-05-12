package com.tersesystems.blindsight

import scala.annotation.tailrec
import scala.language.experimental.macros
import scala.reflect.api.Trees
import scala.reflect.macros.blackbox

// Roughly the same as a ValDef
case class DumpTerm(name: String, value: Any)

case class DumpConstructor(location: Location, params: Seq[ParameterList])

case class DebugVal(name: String, value: Any)

case class DebugIf(code: String, result: Boolean)

case class DebugResult[A](code: String, value: A)

case class Location(line: Int, column: Int)

case class DumpMethod(location: Location, name: String, params: Seq[ParameterList])

// Should not be a case class, should let you pull out by name and
// have decent toString
case class ParameterList(parameters: Seq[DumpTerm]) {
  override def toString: String = {
    val results = parameters.map(dt => s"${dt.name} = ${dt.value}")
    s"ParameterList($results)"
  }
}

object DebugMacros {

  def decorateIfs[A](output: DebugIf => Unit)(a: A): A = macro BlackboxMacros.decorateIfs

  def decorateMatch[A](output: DebugIf => Unit)(a: A): A = macro BlackboxMacros.decorateMatch

  def decorateVals[A](output: DebugVal => Unit)(expr: A): A = macro BlackboxMacros.decorateVals[A]

  def dumpMethod: DumpMethod = macro BlackboxMacros.dumpMethodImpl

  def dumpConstructor: DumpConstructor = macro BlackboxMacros.dumpConstructorImpl

  def debugFields: Seq[DebugVal] = macro BlackboxMacros.debugFields

  def debugPublicFields[A](instance: A): Seq[DebugVal] = macro BlackboxMacros.debugPublicFields[A]

  def debugExpr[A](a: A)(implicit output: DebugResult[A] => A): A =
    macro BlackboxMacros.debugExprImpl[A]

  /** Gives the source code of an expression and the result of the expr in a tuple */
  def sourceExpr[A](a: A): (String, A) = macro BlackboxMacros.sourceExprImpl[A]

}

/**
 * Blackbox macros to expose internal state in a loggable format.
 *
 * @param c
 */
class BlackboxMacros(val c: blackbox.Context) {
  import c.universe._

  /**
   * Provides the source for the given expression.
   */
  def sourceExprImpl[A: c.WeakTypeTag](a: c.Expr[A]): c.Expr[(String, A)] = {
    // previously based on log and logImpl
    // from https://github.com/retronym/macrocosm/blob/master/src/main/scala/com/github/retronym/macrocosm/Macrocosm.scala
    val portion = extractRange(a.tree) getOrElse ""
    val t2      = Select(Select(Ident(TermName("scala")), TermName("Tuple2")), TermName("apply"))

    // following advice from https://github.com/kevinwright/macroflection/blob/master/kernel/src/main/scala/net/thecoda/macroflection/Validation.scala
    //clone to avoid "Synthetic tree contains nonsynthetic tree" error under -Yrangepos
    val adup: Tree = a.tree.duplicate
    val tree       = internal.gen.mkMethodCall(t2, List(Literal(Constant(portion)), adup))

    val expr = c.Expr[(String, A)](tree)

    expr
  }

  /**
   * Add debugging information to if statements
   */
  def decorateIfs(output: c.Expr[DebugIf => Unit])(a: c.Tree): c.Tree = {
    a match {
      // https://docs.scala-lang.org/overviews/quasiquotes/expression-details.html#if
      case q"if ($cond) $thenp else $elsep" =>
        val condSource = extractRange(cond) getOrElse ""
        val printThen  = q"$output(DebugIf($condSource, true))"
        val elseThen   = q"$output(DebugIf($condSource, false))"

        //elsep match {
        //  case elseIf: If =>
        //    println(s"[$elseIf] :-)")
        //  case other =>
        //    println(s"other = [$other]")
        //}
        val decElseP = decorateIfs(output)(elsep.asInstanceOf[c.Tree])

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

  def decorateMatch(output: c.Expr[DebugIf => Unit])(a: c.Tree): c.Tree = {
    a match {
      // https://docs.scala-lang.org/overviews/quasiquotes/expression-details.html#if
      case q"$expr match { case ..$cases }" =>
        val enhancedCases = cases.map {
          case CaseDef(pat, guard, body) =>
            val exprSource  = extractRange(expr) getOrElse ""
            val patSource   = extractRange(pat).map(p => s" match case $p") getOrElse ""
            val guardSource = extractRange(guard).map(" if " + _).getOrElse("")
            val src         = exprSource + patSource + guardSource
            val debugIf     = q"DebugIf($src, true)"
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

  /**
   * Provides the string with the given source.
   */
  def debugExprImpl[A: c.WeakTypeTag](
      a: c.Expr[A]
  )(output: c.Expr[DebugResult[A] => A]): c.Expr[A] = {
    val portion = extractRange(a.tree) getOrElse ""
    val const   = c.Expr[String](Literal(Constant(portion)))
    c.Expr[A](q"""$output(DebugResult($const, $a))""")
  }

  /**
   * Inserts debugging statements after the vals in the given block.
   */
  def decorateVals[A](output: c.Expr[DebugVal => Unit])(expr: c.Expr[A]): c.Expr[A] = {
    val loggedStats = expr.tree.children.flatMap {
      case valdef @ ValDef(_, termName, _, _) =>
        List(valdef, q"$output(DebugVal(${termName.encodedName.toString}, $termName))")
      case stat =>
        List(stat)
    }
    val outputExpr: c.Expr[A] = c.Expr[A](c.untypecheck(q"..$loggedStats"))
    outputExpr
  }

  // https://github.com/lloydmeta/unless-when/blob/master/src/main/scala/scala/ext/UnlessWhen/Macros.scala

  def debugPublicFields[A: WeakTypeTag](instance: c.Expr[A]): c.Expr[Seq[DebugVal]] = {
    def classVals(tpe: c.universe.Type) = {
      tpe.decls.collect {
        case method: MethodSymbol if method.isAccessor && method.isPublic =>
          val nameStr = method.name.decodedName.toString
          q"DebugVal(${nameStr}, $instance.$method)"
      }
    }

    val classType = weakTypeTag[A].tpe
    val fields    = classVals(classType)

    c.Expr[Seq[DebugVal]](q"Seq(..$fields)")
  }

  /**
   * Debug all the fields in this type.
   *
   * The enclosing class provides the fields that are produced here.
   *
   * XXX should test for lazy and by-name fields
   * XXX What does isAccessor actually do?
   */
  def debugFields: c.Expr[Seq[DebugVal]] = {
    def classVals(tpe: c.universe.Type) = {
      // decls contains directly declared variables
      tpe.decls.collect {
        case method: MethodSymbol if method.isAccessor =>
          //println(s"[method.isAccessor] yields [${method.isAccessor}]")
          val nameStr = method.name.decodedName.toString
          q"DebugVal(${nameStr}, ${method})"

        //        case symbol: Symbol if symbol.isTerm =>
        //          //val methodSymbol: MethodSymbol = tpe.decl(symbol.name).asMethod
        //          println(s"[isTerm] yields [${symbol.isTerm}]")
        //          println(s"[isMethod] yields [${symbol.isMethod}]")
        //
        //          val nameStr = symbol.toString
        //          println(s"[${nameStr}] is raw [${showRaw(symbol)}]")
        //          q"DebugVal(${nameStr}, ${showRaw(symbol)})"
      }
    }

    val classSym = getClassSymbol()
    val fields   = classVals(classSym.info)

    c.Expr[Seq[DebugVal]](q"Seq(..$fields)")
  }

  /**
   * Returns information on the given method name, position, and parameter values.
   *
   * @return
   */
  def dumpMethodImpl: c.Expr[DumpMethod] = {
    val enclosingMethodSymbol = getMethodSymbol()
    val methodName            = getMethodName(enclosingMethodSymbol)
    val paramLists            = parameterLists(enclosingMethodSymbol.info.paramLists)

    val pos      = methodName.pos
    val location = c.Expr(q"Location(${pos.line}, ${pos.column})")
    c.Expr(q"DumpMethod($location, $methodName, $paramLists)")
  }

  /**
   * This returns information on the parameters and location that the constructor was called with.
   *
   * @return
   */
  def dumpConstructorImpl: c.Expr[DumpConstructor] = {
    val classSymbol = getClassSymbol()

    val pos        = classSymbol.pos
    val paramLists = parameterLists(classSymbol.info.paramLists)

    val location = c.Expr(q"Location(${pos.line}, ${pos.column})")
    c.Expr(q"DumpConstructor($location, $paramLists)")
  }

  /**
   * Get the symbol of the method that encloses the macro,
   * or abort the compilation if we can't find one.
   */
  private def getMethodSymbol(): c.Symbol = {

    @tailrec
    def getMethodSymbolRecursively(sym: Symbol): Symbol = {
      if (sym == null || sym == NoSymbol || sym.owner == sym)
        c.abort(c.enclosingPosition, "This does not appear to be inside a method.")
      else if (sym.isMethod)
        sym
      else
        getMethodSymbolRecursively(sym.owner)
    }

    getMethodSymbolRecursively(c.internal.enclosingOwner)
  }

  /**
   * Convert the given method symbol to a tree representing the method name.
   */
  private def getMethodName(methodSymbol: c.Symbol): c.Tree = {
    val methodName = methodSymbol.asMethod.name.toString
    // return a Tree
    q"$methodName"
  }

  private def parameterLists(symbolss: List[List[c.Symbol]]): c.Expr[Seq[ParameterList]] = {
    val lists = symbolss.map { ss =>
      // 2.11 macro is a bit odd about this
      val partialFunction: PartialFunction[c.Symbol, c.Tree] =
        new PartialFunction[c.Symbol, c.Tree] {
          override def isDefinedAt(x: c.Symbol): Boolean = true
          override def apply(s: c.Symbol): c.Tree =
            q"DumpTerm(${Literal(Constant(s.name.toString))}, $s)"
        }
      val paramLists = ss.collect(partialFunction)
      q"ParameterList(Seq(..$paramLists))"
    }
    c.Expr(q"Seq(..$lists)")
  }

  private def getClassSymbol(): c.Symbol = {
    @tailrec
    def getClassSymbolRecursively(sym: Symbol): Symbol = {
      if (sym == null)
        c.abort(
          c.enclosingPosition,
          "Encountered a null symbol while searching for enclosing class"
        )
      else if (sym.isClass || sym.isModule)
        sym
      else
        getClassSymbolRecursively(sym.owner)
    }

    getClassSymbolRecursively(c.internal.enclosingOwner)
  }

  /** Given a Tree, extracts the source code in its range. Assumes -Yrangepos scalac argument. */
  private def extractRange(t: Trees#Tree): Option[String] = {
    val pos    = t.pos
    val source = pos.source.content
    if (pos.isRange) Option(new String(source.drop(pos.start).take(pos.end - pos.start))) else None
  }

  // https://github.com/JohnReedLOL/pos
  // https://github.com/com-lihaoyi/sourcecode
  // https://stackoverflow.com/questions/25418685/how-to-print-source-code-of-if-condition-in-then
  // https://github.com/liuhongchao/debug4s/blob/master/src/main/scala/it/softfork/debug4s/DebugMacro.scala
  // https://github.com/vn971/macro-format/blob/master/shared/src/main/scala/net/pointsgame/macros/MacroImpl.scala
  // https://stackoverflow.com/questions/28852972/scala-reflection-for-logging-purposes
  // https://github.com/cb372/scalacache/blob/master/modules/core/src/main/scala/scalacache/memoization/Macros.scala

  //
  //  private def getConstructorParams(classSymbol: c.Symbol): c.Tree = {
  //    if (classSymbol.isClass) {
  //      val symbolss = classSymbol.asClass.primaryConstructor.asMethod.paramLists
  //      if (symbolss == List(Nil)) {
  //        q"_root_.scala.collection.immutable.Vector.empty"
  //      } else {
  //        parameterLists(symbolss)
  //      }
  //    } else {
  //      q"_root_.scala.collection.immutable.Vector.empty"
  //    }
  //  }

  // https://users.scala-lang.org/t/getting-supertype-information-in-an-annotation-macro/5872
  //  def macroImpl(classDef: ClassDef, companionObjectDef: Option[ModuleDef]) = {
  //    val ClassDef(mods, className, typeParams, impl) = classDef
  //    val parentTypes: Seq[Type] = impl.parents.map(tree => c.typecheck(tree, c.TYPEmode).tpe)
  //  }

  // Print the name of the variable
  //  def showMacro(expr: c.Tree) = {
  //    import c.universe._
  //    q"""${show(expr)}"""
  //  }
  //
  //  def showCodeMacro(expr: c.Tree) = {
  //    import c.universe._
  //    q"""${(showCode(expr))}"""
  //  }
  //
  //  def showRawMacro(expr: c.Tree) = {
  //    import c.universe._
  //    q"""${(showRaw(expr))}"""
  //  }

}
