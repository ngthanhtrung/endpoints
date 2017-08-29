import EndpointsSettings._
import LocalCrossProject._
import org.scalajs.sbtplugin.cross.CrossProject

val `algebra-jvm` = LocalProject("algebraJVM")
val `algebra-circe-jvm` = LocalProject("algebra-circeJVM")

val `play-circe` = LocalProject("play-circe")
val `play-client` = LocalProject("play-client")
val `play-client-circe` = LocalProject("play-client-circe")
val `play-server` = LocalProject("play-server")
val `play-server-circe` = LocalProject("play-server-circe")

val `akka-http-server-circe` = LocalProject("akka-http-server-circe")

val `xhr-client` = LocalProject("xhr-client")
val `xhr-client-circe` = LocalProject("xhr-client-circe")
val `xhr-client-faithful` = LocalProject("xhr-client-faithful")

val `scalaj-client` = LocalProject("scalaj-client")
val `scalaj-client-circe` = LocalProject("scalaj-client-circe")

val `openapi-jvm` = LocalProject("openapiJVM")
val `openapi-circe-jvm` = LocalProject("openapi-circeJVM")

import sbtunidoc.Plugin.UnidocKeys.unidoc

val apiDoc =
  project.in(file("api-doc"))
    .settings(noPublishSettings ++ `scala 2.11` ++ unidocSettings: _*)
    .settings(
      scalacOptions in (ScalaUnidoc, unidoc) ++= Seq(
        "-diagrams",
        "-groups",
        "-doc-source-url", s"https://github.com/julienrf/endpoints/blob/v${version.value}€{FILE_PATH}.scala",
        "-sourcepath", (baseDirectory in ThisBuild).value.absolutePath
      ),
      sbtunidoc.Plugin.UnidocKeys.unidocProjectFilter in (ScalaUnidoc, unidoc) := inProjects(
        `algebra-jvm`, `algebra-circe-jvm`,
        `play-circe`,
        `play-client`, `play-client-circe`,
        `play-server`, `play-server-circe`,
        `xhr-client`, `xhr-client-circe`, `xhr-client-faithful`,
        `scalaj-client`, `scalaj-client-circe`,
        `openapi-jvm`, `openapi-circe-jvm`
      )
    )

val ornateTarget = Def.setting(target.value / "ornate")

val manual =
  project.in(file("manual"))
    .enablePlugins(OrnatePlugin)
    .settings(noPublishSettings ++ ghpages.settings: _*)
    .settings(
      scalaVersion := "2.11.8",
      git.remoteRepo := "git@github.com:julienrf/endpoints.git",
      ornateSourceDir := Some(sourceDirectory.value / "ornate"),
      ornateTargetDir := Some(ornateTarget.value),
      ornateSettings := Map("version" -> version.value),
      siteSubdirName in ornate := "",
      addMappingsToSiteDir(mappings in ornate, siteSubdirName in ornate),
      mappings in ornate := {
        val _ = ornate.value
        val output = ornateTarget.value
        output ** AllPassFilter --- output pair relativeTo(output)
      },
      siteSubdirName in packageDoc := s"api/${version.value}",
      addMappingsToSiteDir(mappings in ScalaUnidoc in packageDoc in apiDoc, siteSubdirName in packageDoc),
      previewLaunchBrowser := false
    )


// Example for the “Overview” page of the documentation
val `example-overview-endpoints` =
  crossProject.crossType(CrossType.Pure)
    .in(file("examples/overview/endpoints"))
    .settings(noPublishSettings ++ `scala 2.11 to 2.12`: _*)
    .settings(
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    )
    .dependsOnLocalCrossProjects("algebra-circe")

val `example-overview-endpoints-jvm` = `example-overview-endpoints`.jvm

val `example-overview-endpoints-js` = `example-overview-endpoints`.js

val `example-overview-client` =
  project.in(file("examples/overview/client"))
    .enablePlugins(ScalaJSPlugin)
    .settings(noPublishSettings ++ `scala 2.11 to 2.12`: _*)
    .dependsOn(`example-overview-endpoints-js`, `xhr-client-circe`)

val `example-overview-server` =
  project.in(file("examples/overview/server"))
    .settings(noPublishSettings ++ `scala 2.11`: _*)
    .dependsOn(`example-overview-endpoints-jvm`, `play-server-circe`)

val `example-overview-play-client` =
  project.in(file("examples/overview/play-client"))
    .settings(noPublishSettings ++ `scala 2.11`: _*)
    .dependsOn(`example-overview-endpoints-jvm`, `play-client-circe`)

// Basic example
val `example-basic-shared` = {
  val assetsDirectory = (base: File) => base / "src" / "main" / "assets"
  CrossProject("example-basic-shared-jvm", "example-basic-shared-js", file("examples/basic/shared"), CrossType.Pure)
    .settings(noPublishSettings ++ `scala 2.11 to 2.12`: _*)
    .settings(
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
      (sourceGenerators in Compile) += Def.task {
        assets.AssetsTasks.generateDigests(
          baseDirectory = baseDirectory.value.getParentFile,
          targetDirectory = (target in Compile).value,
          generatedObjectName = "AssetsDigests",
          generatedPackage = Some("sample")
        )
      }.taskValue
    )
    .jvmSettings(
      (resourceGenerators in Compile) += Def.task {
        assets.AssetsTasks.gzipAssets(
          baseDirectory = baseDirectory.value.getParentFile,
          targetDirectory = (target in Compile).value
        )
      }.taskValue,
      unmanagedResourceDirectories in Compile += assetsDirectory(baseDirectory.value.getParentFile)
    )
    .enablePlugins(ScalaJSPlugin)
    .dependsOnLocalCrossProjects("algebra", "algebra-circe", "openapi-circe")
}

val `example-basic-shared-jvm` = `example-basic-shared`.jvm

val `example-basic-shared-js` = `example-basic-shared`.js

val `example-basic-client` =
  project.in(file("examples/basic/client"))
    .enablePlugins(ScalaJSPlugin)
    .settings(noPublishSettings ++ `scala 2.11 to 2.12`: _*)
    .dependsOn(`example-basic-shared-js`, `xhr-client-circe`)

val `example-basic-play-server` =
  project.in(file("examples/basic/play-server"))
    .settings(noPublishSettings ++ `scala 2.11`: _*)
    .settings(
      unmanagedResources in Compile += (fastOptJS in (`example-basic-client`, Compile)).map(_.data).value,
      libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.6.2"
    )
    .dependsOn(`example-basic-shared-jvm`, `play-server-circe`)

val `example-basic-akkahttp-server` =
  project.in(file("examples/basic/akkahttp-server"))
    .settings(commonSettings: _*)
    .settings(`scala 2.11 to 2.12`: _*)
    .settings(
      publishArtifact := false
    )
    .dependsOn(`example-basic-shared-jvm`, `akka-http-server-circe`)


// CQRS Example
// public endpoints definitions
val `example-cqrs-public-endpoints` =
  CrossProject("example-cqrs-public-endpoints-jvm", "example-cqrs-public-endpoints-js", file("examples/cqrs/public-endpoints"), CrossType.Pure)
    .settings(noPublishSettings ++ `scala 2.11 to 2.12`: _*)
    .dependsOnLocalCrossProjects("openapi-circe", "example-cqrs-circe-instant", "json-schema-generic")

val `example-cqrs-public-endpoints-jvm` = `example-cqrs-public-endpoints`.jvm

val `example-cqrs-public-endpoints-js` = `example-cqrs-public-endpoints`.js

// web-client, *uses* the public endpoints’ definitions
val `example-cqrs-web-client` =
  project.in(file("examples/cqrs/web-client"))
    .enablePlugins(ScalaJSPlugin)
    .settings(noPublishSettings ++ `scala 2.11`: _*)
    .settings(
      libraryDependencies ++= Seq(
        "in.nvilla" %%% "monadic-html" % "0.2.2",
        "in.nvilla" %%% "monadic-rx-cats" % "0.2.2",
        "org.julienrf" %%% "faithful-cats" % "0.2",
        "org.scala-js" %%% "scalajs-java-time" % "0.2.0"
      ),
      scalaJSUseMainModuleInitializer := true
    )
    .dependsOn(`xhr-client-faithful`, `xhr-client-circe`)
    .dependsOn(`example-cqrs-public-endpoints-js`)

// public server implementation, *implements* the public endpoints’ definitions and *uses* the commands and queries definitions
val `example-cqrs-public-server` =
  project.in(file("examples/cqrs/public-server"))
    .settings(noPublishSettings ++ `scala 2.11`: _*)
    .settings(
      libraryDependencies += "com.typesafe.play" %% "twirl-api" % "1.2.0",
      unmanagedResources in Compile += (fastOptJS in (`example-cqrs-web-client`, Compile)).map(_.data).value,
      (sourceGenerators in Compile) += Def.task {
        assets.AssetsTasks.generateDigests(
          baseDirectory = (crossTarget in fastOptJS in `example-cqrs-web-client`).value,
          targetDirectory = (target in Compile).value,
          generatedObjectName = "BootstrapDigests",
          generatedPackage = Some("cqrs.publicserver"),
          assetsPath = identity
        )
      }.dependsOn(fastOptJS in Compile in `example-cqrs-web-client`).taskValue
    )
    .dependsOn(`play-server-circe`, `play-client-circe`)
    .dependsOn(`example-cqrs-public-endpoints-jvm`, `example-cqrs-commands-endpoints`, `example-cqrs-queries-endpoints`)

// commands endpoints definitions
lazy val `example-cqrs-commands-endpoints` =
  project.in(file("examples/cqrs/commands-endpoints"))
    .settings(noPublishSettings ++ `scala 2.11 to 2.12`: _*)
    .dependsOn(`algebra-circe-jvm`, `circe-instant-jvm`)

// commands implementation
val `example-cqrs-commands` =
  project.in(file("examples/cqrs/commands"))
    .settings(noPublishSettings ++ `scala 2.11`: _*)
    .settings(
      libraryDependencies ++= Seq(
        "org.scalacheck" %% "scalacheck" % "1.13.4" % Test,
        "org.scalatest" %% "scalatest" % "3.0.1" % Test
      )
    )
    .dependsOn(`play-server-circe`, `play-client-circe` % Test)
    .dependsOn(`example-cqrs-commands-endpoints`)

// queries endpoints definitions
lazy val `example-cqrs-queries-endpoints` =
  project.in(file("examples/cqrs/queries-endpoints"))
    .settings(noPublishSettings ++ `scala 2.11 to 2.12`: _*)
    .dependsOn(`algebra-circe-jvm`, `example-cqrs-public-endpoints-jvm` /* because we reuse the DTOs */)

// queries implementation
val `example-cqrs-queries` =
  project.in(file("examples/cqrs/queries"))
    .settings(noPublishSettings ++ `scala 2.11`: _*)
    .dependsOn(`play-server-circe`, `play-client-circe`)
    .dependsOn(`example-cqrs-queries-endpoints`, `example-cqrs-commands-endpoints`)

// this one exists only for the sake of simplifying the infrastructure: it runs all the HTTP services
val `example-cqrs` =
  project.in(file("examples/cqrs/infra"))
    .settings(noPublishSettings ++ `scala 2.11`: _*)
    .settings(
      cancelable in Global := true,
      libraryDependencies ++= Seq(
        "org.scalacheck" %% "scalacheck" % "1.13.4" % Test,
        "org.scalatest" %% "scalatest" % "3.0.1" % Test
      )
    )
    .dependsOn(`example-cqrs-queries`, `example-cqrs-commands`, `example-cqrs-public-server`, `example-cqrs-web-client`/*, `circe-instant-js`*/ /*, `circe-instant-jvm`*/)

lazy val `circe-instant` =
  CrossProject("example-cqrs-circe-instantJVM", "example-cqrs-circe-instantJS", file("examples/cqrs/circe-instant"), CrossType.Pure)
    .settings(noPublishSettings ++ `scala 2.11 to 2.12`: _*)
    .settings(
      libraryDependencies += "io.circe" %%% "circe-core" % circeVersion
    )

lazy val `circe-instant-js` = `circe-instant`.js
lazy val `circe-instant-jvm` = `circe-instant`.jvm
