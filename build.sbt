name := "arraydeque-talk"
version := "0.0.1-SNAPSHOT"
scalaVersion := "2.12.6"

resolvers += Resolver.bintrayRepo("stanch", "maven")

libraryDependencies ++= Seq(
  "org.stanch" %% "reftree" % "1.2.0"
)