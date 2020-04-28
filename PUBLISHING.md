# Publishing

This project uses `sbt-release` to publish:

```scala
sbt release
```

Publishing is done to bintray, so you need bintray credentials.  Here's an [example](http://queirozf.com/entries/publishing-an-sbt-project-onto-bintray-an-example).

Publishing documentation is done using `sbt-site`

```scala
sbt
> project docs 
> makeSite
> paradoxValidateInternalLinks
> paradoxValidateLinks
> ghpagesPushSite
```
