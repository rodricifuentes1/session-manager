import sbt._

object Resolvers {

	val all = Seq(
		"Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases",
		"Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
		"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
	)

}

object Version {

	// AKKA LIBRARIES
	val akkaVersion: String = "2.3.12"

	// TESTING LIBRARIES
	val specs2Version: String = "3.6.4"

	// LOGGING LIBRARIES
	val logbackVersion: String = "1.1.3"
	val scalaloggingVersion: String = "3.1.0"

}

object Library {

  import Version._

  // AKKA LIBRARIES
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaTestkit = "com.typesafe.akka" %%  "akka-testkit" % akkaVersion % "test"
  
  // TESTING LIBRARIES

  	// -- SPECS2
  	val specs2 = "org.specs2" %% "specs2-core" % specs2Version % "test"

  // LOGGING LIBRARIES

  	// -- LOGBACK
  	val logback = "ch.qos.logback" % "logback-classic" % logbackVersion

  	// -- SCALALOGGING
  	val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaloggingVersion

}

object Dependencies {

  import Library._

  // AKKA LIBRARIES
  val akkaLibraries = Seq(akkaActor, akkaTestkit)

  // TESTING LIBRARIES
  val testingLibraries = Seq(specs2)

  // LOGGING LIBRARIES
  val loggingLibraries = Seq(logback, scalaLogging)

  // ------------ ALL LIBRARIES -------------------
  val all = akkaLibraries ++ testingLibraries ++ loggingLibraries

}
