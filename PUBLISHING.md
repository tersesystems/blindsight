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
