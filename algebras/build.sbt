import EndpointsSettings._

val algebra =
  crossProject.crossType(CrossType.Pure).in(file("algebra"))
    .settings(publishSettings ++ `scala 2.10 to 2.12`: _*)
    .settings(
      name := "endpoints-algebra"
    )

val `algebra-js` = algebra.js

val `algebra-jvm` = algebra.jvm

val circeVersion = "0.9.0-M1"

val `algebra-circe` =
  crossProject.crossType(CrossType.Pure).in(file("algebra-circe"))
    .settings(publishSettings ++ `scala 2.10 to 2.12`: _*)
    .settings(
      name := "endpoints-algebra-circe",
      libraryDependencies += "io.circe" %%% "circe-generic" % circeVersion
    )
    .dependsOn(`algebra`)

val `algebra-circe-js` = `algebra-circe`.js

val `algebra-circe-jvm` = `algebra-circe`.jvm

