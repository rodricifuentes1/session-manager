organization := "co.rc"

name := "session-manager"

scalaVersion := "2.11.7"

description := "Agnostic session manager based in akka actors"

resolvers ++= Resolvers.all

libraryDependencies ++= Dependencies.all

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Xfuture",
  "-Xcheckinit"
)
