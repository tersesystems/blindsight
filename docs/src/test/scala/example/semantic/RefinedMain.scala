package example.semantic

// #refined-main
object RefinedMain {
  import com.tersesystems.blindsight._
  import com.tersesystems.blindsight.semantic._
  import eu.timepit.refined.api.Refined
  import eu.timepit.refined.collection.NonEmpty
  import eu.timepit.refined.string._
  import eu.timepit.refined._

  implicit def stringToStatement[R]: ToStatement[Refined[String, R]] =
    ToStatement { str =>
      Statement().withMessage(str.value)
    }

  def main(args: Array[String]): Unit = {
    val logger = LoggerFactory.getLogger

    val notEmptyLogger: SemanticLogger[String Refined NonEmpty] =
      logger.semantic[String Refined NonEmpty]
    notEmptyLogger.info(refineMV[NonEmpty]("this is a statement"))
    // will not compile
    //notEmptyLogger.info(refineMV(""))

    val urlLogger: SemanticLogger[String Refined Url] = logger.semantic[String Refined Url]
    urlLogger.info(refineMV[Url]("http://google.com"))
    // will not compile
    //urlLogger.info(refineMV("this is a statement"))
  }

}
// #refined-main
