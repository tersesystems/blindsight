# Blindsight

[![Build Status](https://travis-ci.org/tersesystems/blindsight.svg?branch=master)](https://travis-ci.org/tersesystems/blindsight)

Blindsight is a Scala logging API that allows for fluent logging, semantic logging, and context aware logging.
 
The name is taken from Peter Watts's excellent first contact novel, [Blindsight](https://en.wikipedia.org/wiki/Blindsight_\(Watts_novel\)).

## Documentation 

See [the documentation](https://tersesystems.github.io/blindsight/) for more details.

## Requirements

Blindsight is based around SLF4J.  It does not configure or constrain SLF4J in any way, and is designed to defer layout decisions to the backend logging framework, so you can write the same logging event out to a text file, console, to a database, and to JSON on the backend.  

Having said that, the default assumption in the examples is that you are using [logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder) and [Terse Logback](https://tersesystems.github.io/terse-logback/) on the backend, and are roughly familiar with the [blog posts at tersesystems.com](https://tersesystems.com/category/logging/) and the [diagnostic logging showcase](https://github.com/tersesystems/terse-logback-showcase).

## Publishing

This project uses `sbt-bintray` to publish.  Here's an [example](http://queirozf.com/entries/publishing-an-sbt-project-onto-bintray-an-example).  This project stages artifacts first before releasing them.

```scala
sbt publish
sbt bintrayRelease 
```

Publishing documentation is done using `sbt-site`

```scala
sbt
> project docs 
> makeSite
> ghpagesPushSite
```

## License

Blindsight is released under the "Apache 2" license. See [LICENSE](LICENSE) for specifics and copyright declaration.