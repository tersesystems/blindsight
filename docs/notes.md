## Logger Resolvers

https://github.com/twitter/util/blob/develop/util-slf4j-api/README.md

## Structured DSL

https://github.com/serilog/serilog/wiki/Structured-Data

## Metrics

- Handling metrics through schema?

Do it on the backend.  Handle events through means of several metrics appender.  When you post an event, there's a metrics appender than handles the aggregation.  This is actually much better than handling metrics inline with the code, because there are locks around histograms etc.  This makes it async and offline from the processing thread, and lets you replace your metrics code later.
