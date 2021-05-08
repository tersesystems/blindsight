package example.typeclasses

import com.tersesystems.blindsight._
import org.slf4j.MarkerFactory

object TypeClassExample {

  private val logger = LoggerFactory.getLogger

  def main(args: Array[String]): Unit = {

    // #markers-example
    val marker = MarkerFactory.getDetachedMarker("SOME_MARKER")
    logger.info(Markers(marker), "message with marker")
    // #markers-example

    // #marker-enrichment
    import MarkersEnrichment._ // adds "asMarkers" to org.slf4j.Marker
    val markers: Markers = marker.asMarkers
    // #marker-enrichment

    // #combine-markers
    val markers1: Markers          = Markers(MarkerFactory.getDetachedMarker("MARKER1"))
    val markers2: Markers          = Markers(MarkerFactory.getDetachedMarker("MARKER2"))
    val markersOnePlusTwo: Markers = markers1 + markers2
    // #combine-markers

    // #combined-slf4j-marker
    val combinedMarker: org.slf4j.Marker = markersOnePlusTwo.marker
    // #combined-slf4j-marker

    // #weekday-to-marker
    sealed trait Weekday {
      def value: String
    }

    case object Monday extends Weekday { val value = "MONDAY" }

    object Weekday {
      import scala.language.implicitConversions
      implicit def weekday2Marker(weekday: Weekday): Markers = Markers(weekday)
      implicit val toMarkers: ToMarkers[Weekday] = ToMarkers { weekday =>
        Markers(MarkerFactory.getDetachedMarker(weekday.value))
      }
    }
    logger.debug(Monday, "this is a test")
    // #weekday-to-marker

    // #argument-int
    logger.info("one argument {}", 42) // works, because default
    // #argument-int

    // #argument-chronounit
    import java.time.temporal.ChronoUnit
    implicit val chronoUnitToArgument: ToArgument[ChronoUnit] = ToArgument[ChronoUnit] { unit =>
      new Argument(unit.toString)
    }
    logger.info("chronounit is {}", ChronoUnit.MILLIS)
    // #argument-chronounit

    // #argument-person
    case class Person(name: String, age: Int)
    implicit val personToArgument: ToArgument[Person] = ToArgument[Person] { person =>
      import DSL._
      Argument(("name" -> person.name) ~ ("age_year" -> person.age))
    }
    logger.info("person is {}", Person("steve", 12))
    // #argument-person

  }

}
