name := """play-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  cache,
  ws,
  "io.igl" %% "jwt" % "1.2.0",
  "com.auth0" % "java-jwt" % "2.1.0",
  "org.anormcypher" %% "anormcypher" % "0.7.1",
  "org.scalatestplus" %% "play" % "1.4.0-M3" % "test"
)

resolvers ++= Seq(
  "anormcypher" at "http://repo.anormcypher.org/",
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
