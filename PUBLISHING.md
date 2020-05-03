# Publishing

Publishing a new version is done by updating `master` to the revision that you want, and then pushing the tag to `origin`:

```bash
export RELEASE_TAG=...
export RELEASE_SHA=...
git tag -fa $RELEASE_TAG $RELEASE_SHA
git push origin $RELEASE_TAG
```

This creates a build from a tag in github, and from there, Travis CI should push documentation and run `releaseEarly` for all three versions.  

* [How to release in Travis CI](https://github.com/jvican/sbt-release-early/wiki/How-to-release-in-Travis-%28CI%29)

Publishing documentation is done using `sbt-site`

```scala
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