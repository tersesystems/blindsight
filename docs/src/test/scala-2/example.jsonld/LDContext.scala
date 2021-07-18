package example.jsonld

import com.tersesystems.blindsight.jsonld._
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.string.Url

import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Currency

trait LDContext {

  // Map LocalDate to a localdate value object in ISO format
  implicit val localDateMapper: ValueMapper[LocalDate] = ValueMapper { date =>
    Value(DateTimeFormatter.ISO_DATE.format(date))
  }

  // Map java.util.Currency to a currency code value object
  implicit val currencyMapper: ValueMapper[Currency] = ValueMapper { currency =>
    Value(currency.getCurrencyCode)
  }

  // Demo that refined types can be used to map to IRI
  type URL = String Refined Url
  object URL extends RefinedTypeOps[URL, String]
  implicit val urlMapper: IRIValueMapper[URL] = IRIValueMapper[URL] { url =>
    IRI(url.value)
  }

  // Create a custom occupation class that we can represent as a JSON-LD node object
  case class Occupation(estimatedSalary: MonetaryAmount, name: String)

  object Occupation {
    implicit val nodeMapper: NodeObjectMapper[Occupation] = NodeObjectMapper { occ =>
      NodeObject(
        `@type`         -> schemaOrg("Occupation"),
        name            -> occ.name,
        estimatedSalary -> occ.estimatedSalary
      )
    }
  }

  sealed trait Gender

  object Gender {
    // https://schema.org/gender
    // The GenderType here indicates Male or Female, but also says
    // string is possible.  I don't think that strings can be used in the
    // IRI value though.
    implicit val maleMapper: IRIValueMapper[Gender] = IRIValueMapper {
      case Male =>
        schemaOrg("Male") // https://schema.org/Male
      case Female =>
        schemaOrg("Female") // https://schema.org/Female
    }
  }

  case object Male   extends Gender
  case object Female extends Gender

  // Create a custom MonetaryAmount that's used by Occupation
  case class MonetaryAmount(currency: Currency, value: Number)

  object MonetaryAmount {
    implicit val nodeMapper: NodeObjectMapper[MonetaryAmount] = NodeObjectMapper { ma =>
      NodeObject(
        `@type`  -> schemaOrg("MonetaryAmount"),
        currency -> ma.currency,
        value    -> ma.value.intValue()
      )
    }
  }

  // Bind the @type keyword to an IRI (this is the only possible binding)
  val `@type`: IRIBinding[IRIValue] = Keyword.`@type`.bindIRI

  // Set up the basic contexts that will be used in a JSON-LD statement

  // "@vocab": "https://schema.org/"
  val schemaOrg: Vocab = IRI(URL.unsafeFrom("https://schema.org/").value).vocab

  // "xsd": "http://www.w3.org/2001/XMLSchema#"
  val xsd: Term = IRI(new java.net.URL("http://www.w3.org/2001/XMLSchema#")).term("xsd")

  // "foaf": "http://xmlns.com/foaf/0.1/"
  val foaf: Term = IRI(new URI("http://xmlns.com/foaf/0.1/")).term("foaf")

  // Set up properties based off the schema.
  // Vocab will not have a prefix,
  // term will produce CompactIRI that will have a prefix.
  val givenName: ValueBinding[String]  = schemaOrg("givenName").bindValue[String]
  val familyName: ValueBinding[String] = schemaOrg("familyName").bindValue[String]
  val name: ValueBinding[String]       = schemaOrg("name").bindValue[String]
  val value: ValueBinding[Int]         = schemaOrg("value").bindValue[Int]

  // Bind a URL using the refined URL mapper specifically.
  val url: IRIBinding[URL] = schemaOrg("url").bindIRI[URL]
  val gender               = schemaOrg("gender").bindIRI[Gender]

  // Bind using a LocalDate mapping.
  val birthDate: ValueBinding[LocalDate] = schemaOrg("birthDate").bindValue[LocalDate]

  // Because foaf is a term, this will produce "foaf:nick" as the key, with a Set[String]
  // as the value.
  val nick: SetBinding[String] = foaf("nick").bindSet[String]

  // Set up some custom bindings!
  val occupation = schemaOrg("occupation").bindObject[Occupation]

  val estimatedSalary = schemaOrg("estimatedSalary").bindObject[MonetaryAmount]

  val currency: ValueBinding[Currency] = schemaOrg("currency").bindValue[Currency]

}
