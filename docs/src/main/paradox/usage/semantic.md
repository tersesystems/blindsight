# Semantic API

A semantic logging API is [strongly typed](https://github.com/microsoft/perfview/blob/main/documentation/TraceEvent/TraceEventProgrammersGuide.md) and does not have the same construction oriented approach as the fluent API.  Instead, the type of the instance is presumed to have a mapping directly to the attributes being logged.

The semantic API works against @scaladoc[Statement](com.tersesystems.blindsight.Statement) directly.  The application is expected to handle the type class mapping to @scaladoc[Statement](com.tersesystems.blindsight.Statement).

Here is an example:

@@snip [SemanticMain.scala](../../../test/scala/example/semantic/SemanticMain.scala) { #semantic-main }

in plain text:

```
FgEdhil2znw6O0Qbm7EAAA 2020-04-05T23:09:08.359+0000 [INFO ] example.semantic.SemanticMain$ in main  - UserLoggedInEvent(steve,127.0.0.1)
FgEdhil2zsg6O0Qbm7EAAA 2020-04-05T23:09:08.435+0000 [INFO ] example.semantic.SemanticMain$ in main  - UserLoggedOutEvent(steve,timeout)
FgEdhil2zsk6O0Qbm7EAAA 2020-04-05T23:09:08.436+0000 [INFO ] example.semantic.SemanticMain$ in main  - UserLoggedInEvent(mike,10.0.0.1)
```

and in JSON:

```json
{
  "id": "FgEdhil2znw6O0Qbm7EAAA",
  "relative_ns": -298700,
  "tse_ms": 1586128148359,
  "start_ms": null,
  "@timestamp": "2020-04-05T23:09:08.359Z",
  "@version": "1",
  "message": "UserLoggedInEvent(steve,127.0.0.1)",
  "logger_name": "example.semantic.SemanticMain$",
  "thread_name": "main",
  "level": "INFO",
  "level_value": 20000,
  "name": "steve",
  "ipAddr": "127.0.0.1"
}
```

## Refinement Types

Semantic Logging works very well with [refinement types](https://github.com/fthomas/refined).  

For example, you can add compile time limitations on the kinds of messages that are passed in:

@@snip [RefinedMain.scala](../../../test/scala/example/semantic/RefinedMain.scala) { #refined-main }

