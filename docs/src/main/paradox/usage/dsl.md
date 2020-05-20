# Structured DSL

Structured logging is the basis for logging rich events.  Blindsight comes with an internal DSL which lets you work with Scala types intuitively while ensuring that you cannot produce an invalid structure.  Internally, the DSL is rendered using @scaladoc[MarkersResolver](com.tersesystems.blindsight.MarkersResolver) and @scaladoc[ArgumentResolver](com.tersesystems.blindsight.ArgumentResolver) to [Markers and StructuredArguments](https://github.com/logstash/logstash-logback-encoder#event-specific-custom-fields), using the ServiceLoader pattern.  This approach produces ideomatic structured output for both line oriented encoders and JSON encoders.

The default implementation is provided with `blindsight-logstash` module, through @scaladoc[LogstashArgumentResolver](com.tersesystems.blindsight.logstash.LogstashArgumentResolver) and @scaladoc[LogstashMarkersResolver](com.tersesystems.blindsight.logstash.LogstashMarkersResolver).  You must configure a JSON encoder to render JSON output.  See [Terse Logback](https://tersesystems.github.io/terse-logback/) and the [Terse Logback Showcase](https://github.com/tersesystems/terse-logback-showcase) for examples of how to configure logstash-logback-encoder for JSON. 

@@@ note

The generic implementation at `blindsight-generic` does not come with resolvers, and so you must provide your own @scaladoc[MarkersResolver](com.tersesystems.blindsight.MarkersResolver) and @scaladoc[ArgumentResolver](com.tersesystems.blindsight.ArgumentResolver) implementations to enable DSL support.

@@@

## Constructing DSL

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
