import sbt._
import sbt.Keys._

import bintray.BintrayPlugin.autoImport._

object EndpointsSettings {

  val commonSettings = Seq(
    organization := "org.julienrf",
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-encoding", "UTF-8",
      "-unchecked",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Xfuture",
      "-Xexperimental"
    )
  )
  val `scala 2.11` = Seq(
    scalaVersion := "2.11.8",
    crossScalaVersions := Seq("2.11.8")
  )
  val `scala 2.11 to 2.12` = Seq(
    scalaVersion := "2.12.3",
    crossScalaVersions := Seq("2.11.8", "2.12.3")
  )

  val `scala 2.10 to 2.12` = Seq(
    scalaVersion := "2.12.3",
    crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.3")
  )

  val publishSettings = commonSettings ++ Seq(
    pomExtra :=
      <developers>
        <developer>
          <id>julienrf</id>
          <name>Julien Richard-Foy</name>
          <url>http://julien.richard-foy.fr</url>
        </developer>
      </developers>,
    scalacOptions in (Compile, doc) ++= Seq(
      "-doc-source-url", s"https://github.com/julienrf/endpoints/tree/v${version.value}€{FILE_PATH}.scala",
      "-sourcepath", baseDirectory.in(LocalRootProject).value.getAbsolutePath
    ),
    apiURL := Some(url(s"http://julienrf.github.io/endpoints/api/${version.value}/")),
    autoAPIMappings := true,
    homepage := Some(url(s"https://github.com/julienrf/endpoints")),
    licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
    scmInfo := Some(
      ScmInfo(
        url(s"https://github.com/julienrf/endpoints"),
        s"scm:git:git@github.com:julienrf/endpoints.git"
      )
    ),
    bintrayRepository := "endpoints"
  )

  val noPublishSettings = commonSettings ++ Seq(
    publishArtifact := false,
    publish := ()
    //  publishLocal := ()
  )

  // --- Common dependencies

  val circeVersion = "0.9.0-M1"

}
