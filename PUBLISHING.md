# Publishing

Publishing is done using `sbt-sonatype` and `sbt-release`:

```bash
$ sbt release release-version 1.4.1
```

Publishing documentation is done using `sbt-site`

```
sbt
> project docs 
> makeSite
> paradoxValidateInternalLinks
> paradoxValidateLinks
> ghpagesPushSite
```

Should be possible to have a branch push documentation automatically:

* [Publishing to Github Pages from Travis CI](https://www.scala-sbt.org/sbt-site/publishing.html#publishing-to-github-pages-from-travis-ci)

`sbt-release-early` does not handle cross-publishing, and we only want the documentation to be published once, so it's not subject to the github build matrix.