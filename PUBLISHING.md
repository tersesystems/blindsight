# Publishing

Publishing is done using `sbt-sonatype` and `sbt-release`.

First check that GPG is loaded up right

```bash
$ export PGP_PASSPHRASE=<terse systems deployment key>
$ sbt publishLocalSigned
```

If that works right (it does scaladoc that isn't in the CI test suite) then do a release as follows:

```bash
$ sbt release 
```

If it goes bad, you have to delete the tag locally, and rollback the local commit.

Publishing documentation is done using `sbt-site`

```
sbt
> project docs 
> makeSite
> paradoxValidateInternalLinks
> paradoxValidateLinks
> ghpagesPushSite
```
