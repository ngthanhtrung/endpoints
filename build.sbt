import EndpointsSettings._

// Algebra interfaces
val algebras = project.in(file("algebras"))
val openapi = project.in(file("openapi"))

// Interpreters
val xhr = project.in(file("xhr"))
val play = project.in(file("play"))
val `akka-http` = project.in(file("akka-http"))
val scalaj = project.in(file("scalaj"))

// Test kit
val testsuite = project.in(file("testsuite"))

// Documentation and examples
val documentation = project.in(file("documentation"))

import ReleaseTransformations._

noPublishSettings

enablePlugins(CrossPerProjectPlugin)

releaseCrossBuild := false

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  releaseStepCommandAndRemaining("+test"),
  setReleaseVersion,
  releaseStepTask(makeSite in LocalProject("manual")),
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommandAndRemaining("+manual/ghpagesPushSite"),
  pushChanges
)

addCommandAlias("shadowPublish", List(
  "akka-http-server",
  "akka-http-server-circe",
  "algebra",
  "algebra-circe",
  "xhr-client",
  "xhr-client-circe"
).map(_ + "/publish").mkString(";", ";", ""))
