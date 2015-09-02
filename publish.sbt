publishMavenStyle := true

pomIncludeRepository := { _ => false }

publishArtifact in Test := false

bintrayRepository := "RC-releases"

bintrayPackageLabels := Seq("scala", "akka", "session-management", "session-manager")