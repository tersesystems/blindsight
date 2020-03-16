import Dependencies._

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.tersesystems"
ThisBuild / organizationName := "rifter"

lazy val root = (project in file("."))
  .settings(
    name := "rifter",
    libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.30",
    libraryDependencies += "io.soabase.maple" % "maple-slf4j" % "1.2.1",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3",
    libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "6.3",
    libraryDependencies += "com.lihaoyi" %% "sourcecode" % "0.1.9"
  )
