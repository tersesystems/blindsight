package example.semantic

import java.time.Instant

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.semantic.SemanticLogger
import org.slf4j.MarkerFactory

object InterpolationMain {

  private val logger = LoggerFactory.getLogger(getClass)

  sealed trait Food

  case class Pizza(topping: String)   extends Food
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
    val pizza     = Pizza("sweetcorn")
    val burrito   = Burrito("chicken")
    val anything  = "anything"
    val marker1   = MarkerFactory.getMarker("MARKER1")
    val marker2   = MarkerFactory.getMarker("MARKER2")
    val throwable = new IllegalStateException("illegal state")

    logger.info(st"")            // nothing at all
    logger.info(st"I like food") // constant
    logger.info(st"I like $pizza")
    logger.info(st"I like $burrito")
    logger.info(st"I like both $pizza and $burrito")
    logger.info(st"I like $anything")
    logger.info(st"I like ${burrito: Food} which is a food") // require the food type

    // DSL statement can be handled inline
    import DSL._
    logger.info(st"Time since epoch is ${bobj("instant_tse" -> Instant.now.toEpochMilli)}")

    //logger.info(st"${marker1} ${marker2} two markers won't compile")
    logger.info(st"${Markers(marker1) + Markers(marker2)}a single Markers will compile")

    // exception should be handled specially.
    logger.error(st"this is an $throwable")

    // marker must be the first argument, and will not show up as {}.
    logger.error(st"${marker1}I like both $pizza and $burrito and $throwable")

    // exception can happen anywhere and will still be added to the end.
    val statement = st"message has three args, plus throwable [$throwable] $pizza $burrito"
    logger.error(statement)
    println(statement.arguments)
    println(statement.throwable)
  }

}
