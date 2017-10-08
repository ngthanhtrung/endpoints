bintrayRepository := "endpoints"
publishMavenStyle := false

addCommandAlias("shadowPublish", List(
  "akka-http-server",
  "akka-http-server-circe",
  "algebra",
  "algebra-circe",
  "xhr-client",
  "xhr-client-circe"
).map(_ + "/publish").mkString(";", ";", ""))
