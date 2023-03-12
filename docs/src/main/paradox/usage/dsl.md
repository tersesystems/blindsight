# Structured DSL

Structured logging is a form of logging that adds structure to logs.  

The simplest form of structured logging is key/value data: when you add arguments to a log statement, an argument may come in the format `"key": value` where the value is a primitive type like a string, a number, or a boolean.  More advanced structure adds [semi-structured data](https://en.wikipedia.org/wiki/Semi-structured_data) to logs, where the value can be an argument or an array of argments.

Blindsight comes with an internal DSL which lets you work with Scala types intuitively while ensuring that you cannot produce an invalid structure.

@@@ note

If you are looking for a strict structured logging solution in Scala, please checkout [Echopraxia](https://github.com/tersesystems/echopraxia-plusscala).  Structured logging is optional in Blindsight, and it's possible to mix structured and "flat" arguments and markers into a logging statement.  In contrast, [Echopraxia](https://github.com/tersesystems/echopraxia-plusscala) **requires** structured logging in its API and does not allow unstructured data as input.

@@@


## Installation

This library is in `blindsight-dsl`: 

@@dependency[sbt,Maven,Gradle] {
group="com.tersesystems.blindsight"
artifact="blindsight-dsl_$scala.binary.version$"
version="$project.version.short$"
}

This is included automatically in `blindsight-logstash` as a dependency.

## Constructing DSL

The DSL is imported by adding the following import statement:

```scala
import com.tersesystems.blindsight.DSL._
```

Primitive types map to primitives.  Any seq produces JSON array.

```scala
val json: BArray = List(1, 2, 3)
```

* Tuple2[String, A] produces a field.

```scala
val field: BField = ("name" -> "joe")
```

* ~ operator produces object by combining fields.

```scala
val bobject: BObject = ("name" -> "joe") ~ ("age" -> 35)
```

* ~~ operator works the same as ~ and is useful in situations where ~ is shadowed, eg. when using Spray or akka-http.

```scala
val bobject: BObject = ("name" -> "joe") ~~ ("age" -> 35)
```

## Representing Complex Data

Using the DSL works very well with @scaladoc[ToArgument](com.tersesystems.blindsight.ToArgument) and @scaladoc[ToMarkers](com.tersesystems.blindsight.ToMarkers), because there is a type class instance for `BObject`.  Here's a fully worked example:

@@snip [DSLExample.scala](../../../test/scala/example/dsl/DSLExample.scala) { #dsl-example }

This produces the following:

```text
Fg91NlmNpRodHaINzdiAAA 9:45:57.374 [INFO ] e.d.D.main logger -  message {lotto={lotto-id=5, winning-numbers=[2, 45, 34, 23, 7, 5, 3], draw-date=null, winners=[{winner-id=23, numbers=[2, 45, 34, 23, 3, 5]}, {winner-id=54, numbers=[52, 3, 12, 11, 18, 22]}]}}
```

in JSON:

```json
{
  "id": "Fg91NlmNpRodHaINzdiAAA",
  "relative_ns": -135695,
  "tse_ms": 1589820357374,
  "@timestamp": "2020-05-18T16:45:57.374Z",
  "@version": "1",
  "message": "message {lotto={lotto-id=5, winning-numbers=[2, 45, 34, 23, 7, 5, 3], draw-date=null, winners=[{winner-id=23, numbers=[2, 45, 34, 23, 3, 5]}, {winner-id=54, numbers=[52, 3, 12, 11, 18, 22]}]}}",
  "logger_name": "example.dsl.DSLExample.main logger",
  "thread_name": "main",
  "level": "INFO",
  "level_value": 20000,
  "lotto": {
    "lotto-id": 5,
    "winning-numbers": [
      2,
      45,
      34,
      23,
      7,
      5,
      3
    ],
    "draw-date": null,
    "winners": [
      {
        "winner-id": 23,
        "numbers": [
          2,
          45,
          34,
          23,
          3,
          5
        ]
      },
      {
        "winner-id": 54,
        "numbers": [
          52,
          3,
          12,
          11,
          18,
          22
        ]
      }
    ]
  }
}
```

## Representing Times

You will want to be consistent and organized about how you represent your field names, and you will typically want to include a representation of the unit used a scalar quantity, particularly time-based fields.  [Honeycomb suggests](https://www.honeycomb.io/blog/event-foo-building-better-events/) a suffix with unit quantity -- `_ms`, `_sec`, `_ns`, `_µs`, etc.

This also follows for specific points in time.  If you represent an instant as a time since epoch, use `_tse` along with the unit, i.e. milliseconds since epoch is `created_tse_ms`:
  
@@snip [TimeExample.scala](../../../test/scala/example/dsl/TimeExample.scala) { #created_tse_ms }

If you represent an instant in [RFC 3339](https://tools.ietf.org/html/rfc3339#section-5.7) / ISO 8601 format (ideally in UTC), use "_ts", i.e. `created_ts`:

@@snip [TimeExample.scala](../../../test/scala/example/dsl/TimeExample.scala) { #created_ts }

If you are representing a duration, then specify `_dur` and the unit, i.e. a backoff duration between retries may be `backoff_dur_ms=150`.  

@@snip [TimeExample.scala](../../../test/scala/example/dsl/TimeExample.scala) { #backoff_dur_ms }

If you are using Java durations, then use `dur_iso` and the ISO-8601 duration format `PnDTnHnMn.nS`, i.e. the duration of someone's bike ride may be `ride_dur_iso="PT2H15M"` 

@@snip [TimeExample.scala](../../../test/scala/example/dsl/TimeExample.scala) { #ride_dur_iso }

This is because both JSON and logfmt do not come with any understanding of dates themselves, and logs are not always kept under tight control under a schema.  Keeping the units explicit lets the logs be self-documenting.  Using @ref:[JSON-LD](jsonld.md) can be more convenient, as there is a built-in understanding of date formatting with typed values.

## Implementations

The default implementation is provided with `blindsight-logstash` module, through @scaladoc[LogstashArgumentResolver](com.tersesystems.blindsight.logstash.LogstashArgumentResolver) and @scaladoc[LogstashMarkersResolver](com.tersesystems.blindsight.logstash.LogstashMarkersResolver).  You must configure a JSON encoder to render JSON output.  See [Terse Logback](https://tersesystems.github.io/terse-logback/) and the [Terse Logback Showcase](https://github.com/tersesystems/terse-logback-showcase) for examples of how to configure logstash-logback-encoder for JSON. 

Internally, the DSL is rendered using @scaladoc[MarkersResolver](com.tersesystems.blindsight.MarkersResolver) and @scaladoc[ArgumentResolver](com.tersesystems.blindsight.ArgumentResolver) to [Markers and StructuredArguments](https://github.com/logstash/logstash-logback-encoder#event-specific-custom-fields), using the ServiceLoader pattern.  This approach produces ideomatic structured output for both line oriented encoders and JSON encoders.

@@@ note

The generic implementation at `blindsight-generic` does not come with resolvers, and so you must provide your own @scaladoc[MarkersResolver](com.tersesystems.blindsight.MarkersResolver) and @scaladoc[ArgumentResolver](com.tersesystems.blindsight.ArgumentResolver) implementations to enable DSL support.

@@@
