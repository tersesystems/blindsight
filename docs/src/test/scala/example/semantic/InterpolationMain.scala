package example.semantic

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.semantic.SemanticLogger

object InterpolationMain {

  val logger: SemanticLogger[Statement] = LoggerFactory.getLogger(getClass).semantic[Statement]

  sealed trait Food

  case class Pizza(topping: String) extends Food
  case class Burrito(filling: String) extends Food

  implicit val pizzaToArgument: ToArgument[Pizza] = ToArgument[Pizza] { pizza =>
    Argument("pizza with " + pizza.topping + " toppings")
  }

  implicit val burritoToArgument: ToArgument[Burrito] = ToArgument[Burrito] { burrito =>
    Argument("burrito with " + burrito.filling + " fillings")
  }

  implicit val foodToArgument: ToArgument[Food] = ToArgument[Food] { food =>
    Argument("<insert food here>")
  }

  def main(args: Array[String]): Unit = {
    val pizza = Pizza("sweetcorn")
    val burrito = Burrito("chicken")
    val anything = "anything"

    logger.info(st"") // nothing at all
    logger.info(st"I like food") // constant
    logger.info(st"I like $pizza")
    logger.info(st"I like $burrito")
    logger.info(st"I like both $pizza and $burrito")
    logger.info(st"I like $anything")
    logger.info(st"I like ${burrito: Food} which is a food") // require the food type

    val ex = new IllegalStateException("illegal state")
    logger.info(st"this is an ${ex}") // exception should be handled specially.
    logger.info(st"I like both $pizza and $burrito and ${ex}")
  }

}
