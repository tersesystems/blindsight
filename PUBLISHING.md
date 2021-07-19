# Publishing

Publishing is done using `sbt-sonatype` and `sbt-release`.

First check that GPG is loaded up right

```bash
$ export PGP_PASSPHRASE=<terse systems deployment key>
$ sbt publishLocalSigned
```

If that works right (it does scaladoc that isn't in the CI test suite) then do a release as follows:

Using sbt-projectmatrix uses lots of memory so better to use 4 GB:

```bash
$ export SBT_OPTS="-Xms512M -Xmx4g -Xss2M -XX:MaxMetaspaceSize=1024M" 
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
