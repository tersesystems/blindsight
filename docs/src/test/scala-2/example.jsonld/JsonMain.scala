package example.jsonld

import com.tersesystems.blindsight.AST.BObject
import com.tersesystems.blindsight._
import com.tersesystems.blindsight.jsonld._

import java.time._
import java.util.Currency

object JsonMain {
  val logger: Logger = LoggerFactory.getLogger

  // ------------------------------------------------------
  // Map NodeObject to Blindsight Logging

  implicit val nodeObjectToArgument: ToArgument[NodeObject] = ToArgument[NodeObject] { nodeObject =>
    Argument(BlindsightASTMapping.toBObject(nodeObject))
  }

  implicit val nodeObjectToMarkers: ToMarkers[NodeObject] = ToMarkers { nodeObject =>
    Markers(BlindsightASTMapping.toBObject(nodeObject))
  }

  def main(args: Array[String]): Unit = {
    val foo = new Foo
    foo.sayHello()
  }

  class Foo extends LDContext {
    def sayHello(): Unit = {
      val willPerson = NodeObject(
        `@type`    -> schemaOrg("Person"),
        givenName  -> "Will",
        familyName -> "Sargent",
        birthDate  -> LocalDate.now,
        nick       -> Set("will", "william", "bill", "billy"),
        url        -> URL.unsafeFrom("http://example.com"),
        gender     -> Male,
        occupation -> Occupation(
          estimatedSalary = MonetaryAmount(Currency.getInstance("USD"), 1),
          name = "Code Monkey"
        )
      )

      logger.info("as an argument {}", willPerson)
      logger.info(Markers(willPerson), "as a marker")
    }
  }
}

// Map NodeObject to Statement here and use marker enrichment for context
trait StatementWithContextMarker extends LDContext with MarkersEnrichment {

  implicit val nodeObjectToArgument: ToArgument[NodeObject] = ToArgument { nodeObject =>
    Argument(BlindsightASTMapping.toBObject(nodeObject))
  }

  implicit val nodeObjectToMarkers: ToMarkers[NodeObject] = ToMarkers { nodeObject =>
    Markers(BlindsightASTMapping.toBObject(nodeObject))
  }

  // Map NodeObject to Statement here and use marker enrichment for context
  implicit val nodeObjectToStatement: ToStatement[NodeObject] = ToStatement { nodeObject =>
    val markers = contextMarker
    val args    = Arguments(Argument(bobj("@graph" -> BlindsightASTMapping.toBObject(nodeObject))))
    Statement(markers = markers, message = "{}", arguments = args)
  }

  // The JSON-LD context is not magically provided, you have to populate it yourself
  val contextValue: BObject = {
    import DSL._
    bobj(
      schemaOrg.name -> schemaOrg.iri.value,
      foaf.name      -> foaf.iri.value
    )
  }

  val contextMarker: Markers = {
    Markers(bobj("@context" -> contextValue))
  }
}
