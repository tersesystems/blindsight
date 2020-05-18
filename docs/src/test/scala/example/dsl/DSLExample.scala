package example.dsl

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.AST._
import com.tersesystems.blindsight.DSL._
import com.tersesystems.blindsight.LoggerFactory

object DSLExample {

  def main(args: Array[String]): Unit = {

    val logger = LoggerFactory.getLogger

    val winners = List(Winner(23, List(2, 45, 34, 23, 3, 5)), Winner(54, List(52, 3, 12, 11, 18, 22)))
    val lotto = Lotto(5, List(2, 45, 34, 23, 7, 5, 3), winners, None)

    logger.info("message {}", lotto)
  }

  case class Winner(id: Long, numbers: List[Int]) {
    lazy val asBObject: BObject = ("winner-id" -> id) ~ ("numbers" -> numbers)
  }

  object Winner {
    implicit val toArgument: ToArgument[Winner] = ToArgument { w =>
      Argument(w.asBObject)
    }
  }

  case class Lotto(id: Long, winningNumbers: List[Int], winners: List[Winner], drawDate: Option[java.util.Date]) {
    lazy val asBObject: BObject = "lotto" ->
      ("lotto-id" -> id) ~
        ("winning-numbers" -> winningNumbers) ~
        ("draw-date" -> drawDate.map(_.toString)) ~
        ("winners" -> winners.map(w => w.asBObject))
  }

  object Lotto {
    implicit val toArgument: ToArgument[Lotto] = ToArgument { lotto =>
      Argument(lotto.asBObject)
    }
  }
}
