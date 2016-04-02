//enablePlugins(JavaAppPackaging)

organization := "io.forward"

name := """akka-http-slick-ddd"""

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV       = "2.4.2"
  val akkaStreamV = "2.0.1"
  val scalaTestV  = "2.2.5"
  Seq(
    "com.typesafe.slick" %% "slick" % "3.1.1",
    "org.slf4j" % "slf4j-api" % "1.7.20",
    "mysql" % "mysql-connector-java" % "5.1.35",
    "com.typesafe"       % "config"                               % "1.3.0",
    "com.typesafe.akka" %% "akka-actor"                           % akkaV,
    "com.typesafe.akka" %% "akka-stream-experimental"             % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-core-experimental"          % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-experimental"               % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental"    % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-testkit-experimental"       % akkaStreamV,
    "org.scalatest"     %% "scalatest"                            % scalaTestV % "test"
  )
}

resolvers += Resolver.typesafeRepo("releases")

