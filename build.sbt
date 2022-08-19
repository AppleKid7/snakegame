scalaVersion := "3.1.3"
lazy val zioVersion = "2.0.0"

name := "SnakeGame"
version := "0.1.0-SNAPSHOT"


libraryDependencies += "dev.zio" %% "zio" % zioVersion
libraryDependencies += "dev.zio" %% "zio-test" % zioVersion
libraryDependencies += "dev.zio" %% "zio-test-sbt" % zioVersion
libraryDependencies += "dev.zio" %% "zio-streams" % zioVersion
libraryDependencies += "dev.zio" %% "zio-test-junit" % zioVersion
libraryDependencies += "org.scalafx" %% "scalafx" % "18.0.2-R29"
// libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

// Add dependency on JavaFX libraries, OS dependent
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map(m =>
  "org.openjfx" % s"javafx-$m" % "18.0.2" classifier osName
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
