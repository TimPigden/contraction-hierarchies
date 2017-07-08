name := "newapps"
organization := "com.optrak"

version := "1.0"

scalaVersion := "2.11.11"
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

val integration = "latest.integration"

val softmillCommon = "com.softwaremill.common" %% "tagging" % "2.1.0"
val junit = "junit" % "junit" % "4.10" % "test"
val clapper = "org.clapper" % "grizzled-slf4j_2.11" % "1.3.0"
val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test
val utilCore = "com.optrak" %% "util-core" % integration
val scalaTestutils = "com.optrak" %% "scala-testutils" % integration % "test"
val extendedData = "com.optrak" %% "extended-data" % integration
val configs = "com.github.kxbmap" %% "configs" % "0.4.4"
val parboiled = "org.parboiled" %% "parboiled" % "2.1.3"
val scalaUtils = "com.optrak" %% "scalautils" % integration
val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.13.4" % Test
val scalaCheckShapeless = "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.3" % Test
val optrakJson = "com.optrak" %% "scala-json" % integration
val optrakXml = "com.optrak" %% "scala-xml" % integration
val optrakExtended = "com.optrak" %% "extended-data" % integration
val json4s = "org.json4s" % "json4s-jackson_2.11" % "3.5.0"
val squants = "org.typelevel"  %% "squants"  % "1.3.0"


val myScalacOptions = Seq(
  "-feature",
  "-deprecation",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-unchecked",
  "-deprecation",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-unused"
)

parallelExecution in Test := false

scalacOptions ++= myScalacOptions

resolvers ++= Seq(
"optrak repo" at "https://office.optrak.com/code/releases/",
"optrak thirdparty" at "https://office.optrak.com/code/thirdparty/"
)

libraryDependencies ++= Seq(
utilCore,
optrakJson,
scalaTestutils,
squants,
junit
)
