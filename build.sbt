name := "spell-server"

version := "1.0"

scalaVersion := "2.11.8"

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq (
  "com.typesafe.akka" %% "akka-actor" % "2.4.6",
  "com.typesafe.akka" %% "akka-remote" % "2.4.6",
  "joda-time" % "joda-time"  % "2.9.3",
  "org.joda" % "joda-convert" % "1.7"
)
