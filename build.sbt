name := "shue"

version := "1.0"

scalaVersion := "2.12.1"

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies ++= List(
  "de.heikoseeberger" %% "akka-http-circe" % "1.17.0",
  "com.typesafe.akka" %% "akka-http" % "10.0.9"
)

val circeVersion = "0.8.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)