# CHANGELOG

## 1.4.0-RC1 [diff](https://github.com/tersesystems/blindsight/compare/v1.3.0...v1.4.0-RC1)

* Add entry transformation
* Add event buffers, with JCTools arrayqueue implementation
* Move core loggers and `ParameterList` to `core` package
* Deprecate `logger.onCondition` for `withCondition` to make consistent
* Remove `Condition(slf4jLogger)` as it's useless in itself
* Refactor state marker logic inside `ParameterList.StateMarker`
* Refactor `ParameterList` static methods to be more `CoreLogger` based
* Refactor source info markers inside `ParameterList` and fix bug always adding empty `Markers`

## 1.3.0 [diff](https://github.com/tersesystems/blindsight/compare/v1.2.1...v1.3.0)

* Replace `AsArguments` with macro expansion [#149](https://github.com/tersesystems/blindsight/pull/149)
* Add lazy methods to slf4j [#146](https://github.com/tersesystems/blindsight/pull/146)
* Add page on memory usage
* Add this changelog
* Optimizations to `Arguments` and `Arguments` to remove wrapping `Seq` and `foldLeft`. [#141](https://github.com/tersesystems/blindsight/pull/141)
* Add `prof gc` to JMH benchmarks to see memory churn. [#136](https://github.com/tersesystems/blindsight/pull/136)
* Add statement interpolation. [#135](https://github.com/tersesystems/blindsight/pull/135)
* JMH statement construction benchmarks.
* Make `Statement` a trait and inline construction for optimization.
* Change methods in APIs so they can take `Statement` to make `logger.info(st"some statement ${arg}")"` convenient.

## 1.2.1 [diff](https://github.com/tersesystems/blindsight/compare/v1.2.0...v1.2.1)

* Fix release bug where benchmarks was considered a library (no code changes)

## 1.2.0 [diff](https://github.com/tersesystems/blindsight/compare/v1.1.0...v1.2.0)

* Fix a bug where conditional state was lost after calling `withMarker`.
* Fix a bug where not all the source information was rendered correctly in `SourceCodeImplicits`.
* Add `SourceInfoBehavior` to `CoreLogger.State`.
* Make adding source info to `Markers` configurable from logback property, false by default.
* Optimizations to move marker construction inside conditional block.
* Optimizations to short circuit logging if `Condition.never` is seen.
* Optimizations to short circuit `Markers` construction on `Markers.empty`.
* Optimizations to short circuit `Arguments` construction on `Arguments.empty`.
* Add JMH benchmarks and notes.
* Documentation page on benchmarks, link to JMH results in [JMH Visualizer](https://jmh.morethan.io/).

## 1.1.0 [diff](https://github.com/tersesystems/blindsight/compare/v1.0.0...v1.1.0)
 
* Extract `CoreLogger` and `CoreLogger.State`
* Add `SimplePredicate`
* Move conditional / markers into core logger and `ParameterList.Conditional`.

## 1.0

* First public release
