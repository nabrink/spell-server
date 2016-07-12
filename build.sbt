
name := "akka-remote-simple-scala"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq (
  "com.typesafe.akka" %% "akka-actor" % "2.4.6",
  "com.typesafe.akka" %% "akka-remote" % "2.4.6",
  "joda-time" % "joda-time"  % "2.9.3"
)
