# Semantic Logging

Why strongly typed (semantic) logging is important.
https://github.com/microsoft/perfview/blob/master/documentation/TraceEvent/TraceEventProgrammersGuide.md

https://martinfowler.com/articles/domain-oriented-observability.html

https://github.com/SemanticRecord/talaan

- https://github.com/aQute-os/biz.aQute.semantic-logging
- https://github.com/UnquietCode/LogMachine
- http://talaan.semanticrecord.org/
- https://github.com/SemanticRecord/talaan
- https://looking4q.blogspot.com/2019/01/level-up-logs-and-elk-contract-first.html
- https://looking4q.blogspot.com/2018/11/logging-cutting-edge-practices.html
- https://github.com/skjolber/json-log-domain
- https://github.com/ffissore/slf4j-fluent/blob/master/src/main/java/org/fissore/slf4j/LoggerAtLevel.java
- http://www.erights.org/elib/Tracing.html

* http://tech.opentable.co.uk/blog/2015/01/23/on-strongly-typed-logging/

## Metrics

- Handling metrics through schema?

Do it on the backend.  Handle events through means of several metrics appender.  When you post an event, there's a metrics appender than handles the aggregation.  This is actually much better than handling metrics inline with the code, because there are locks around histograms etc.  This makes it async and offline from the processing thread, and lets you replace your metrics code later.
