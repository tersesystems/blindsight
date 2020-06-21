package com.tersesystems

package object blindsight {

  implicit class StatementContext(val sc: StringContext) extends AnyVal {
    def st(args: Any*): Statement = macro impl.statement
  }

  private object impl {

    import scala.reflect.macros.blackbox

    def statement(c: blackbox.Context)(args: c.Expr[Any]*): c.Expr[Statement] = {
      import c.universe._

      if (args.nonEmpty) {
        c.prefix.tree match {
          case Apply(_, List(Apply(_, partz))) =>
            // Filter into throwables and arguments
            val arguments: Seq[c.Expr[Argument]] = args.map { t =>
              val nextElement = t.tree
              val tag         = c.WeakTypeTag(nextElement.tpe)

              val field = if (tag.tpe <:< typeOf[Throwable]) {
                q"""com.tersesystems.blindsight.Argument(${nextElement}.getMessage)"""
              } else {
                q"""implicitly[com.tersesystems.blindsight.ToArgument[${tag.tpe}]].toArgument($nextElement)"""
              }
              c.Expr[Argument](field)
            }

            val t      = args.filter(t => c.WeakTypeTag(t.tree.tpe).tpe <:< typeOf[Throwable])
            val format = partz.map { case Literal(Constant(const: String)) => const }.mkString("{}")
            if (t.isEmpty) {
              c.Expr(
                q"com.tersesystems.blindsight.Statement($format, com.tersesystems.blindsight.Arguments(..$arguments))"
              )
            } else {
              // last throwable has the stack trace rendered.
              val throwable = t.last
              c.Expr(
                q"com.tersesystems.blindsight.Statement($format, com.tersesystems.blindsight.Arguments(..$arguments), $throwable)"
              )
            }
          case _ =>
            c.abort(c.prefix.tree.pos, "The pattern can't be used with the interpolation.")
        }
      } else {
        val constants = (c.prefix.tree match {
          case Apply(_, List(Apply(_, literals))) => literals
        }).map { case Literal(Constant(s: String)) => s }
        c.Expr(q"com.tersesystems.blindsight.Statement(${constants.mkString})")
      }
    }
  }
}
