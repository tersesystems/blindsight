import de.heikoseeberger.sbtheader.HeaderPlugin
import sbt.Keys._
import sbt._

object Common extends AutoPlugin {
  override def requires: Plugins = plugins.JvmPlugin && HeaderPlugin
  override def trigger           = allRequirements

  def javaCompileSettings = {
    if (scala.util.Properties.isJavaAtLeast("9")) {
      Seq("--release", "8")
    } else {
      Seq("-source", "1.8", "-target", "1.8")
    }
  }

  override lazy val projectSettings = {
    inTask(doc)(
      Seq(
        Compile / scalacOptions ++= scaladocOptions(
          scalaBinaryVersion.value,
          version.value,
          (ThisBuild / baseDirectory).value
        ),
        autoAPIMappings := true
      )
    ) ++ Seq(
      javacOptions ++= javaCompileSettings
    )
  }

  def scaladocOptions(binVer: String, ver: String, base: File): List[String] = {
    val sourceUrlOptions = binVer match {
      case "2.12" | "2.13" | "3" =>
        Seq(
          "-doc-source-url", {
            val branch = if (ver.endsWith("SNAPSHOT")) "master" else "v" + ver
            s"https://github.com/tersesystems/blindsight/tree/${branch}€{FILE_PATH_EXT}#L€{FILE_LINE}"
          },
          "-doc-canonical-base-url",
          "https://tersesystems.github.io/blindsight/api/"
        )
      case "2.11" =>
        Seq(
          "-doc-source-url", {
            val branch = if (ver.endsWith("SNAPSHOT")) "master" else "v" + ver
            s"https://github.com/tersesystems/blindsight/tree/${branch}€{FILE_PATH}.scala#L1"
          }
        )
    }

    List(
      "-implicits",
      "-groups",
      "-sourcepath",
      base.getAbsolutePath,
      "-doc-title",
      "Blindsight",
      "-doc-version",
      ver
    ) ++ sourceUrlOptions
  }

}
