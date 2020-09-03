import ReleaseTransformations._

lazy val V = _root_.scalafix.sbt.BuildInfo

lazy val commonSettings = Def.settings(
  List(
    organization := "io.github.godenji",
    homepage := Some(url("https://github.com/godenji/replace-symbol-literals-to-string")),
    licenses := Seq("MIT License" -> url("https://opensource.org/licenses/mit-license")),
    description := "scalafix rule to replace Symbol literals with String values",
    scalaVersion := V.scala212,
    addCompilerPlugin(scalafixSemanticdb),
    releaseCrossBuild := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publishSigned"),
      setNextVersion,
      commitNextVersion,
      releaseStepCommand("sonatypeReleaseAll"),
      pushChanges
    ),
    pomExtra := (
      <developers>
        <developer>
          <id>xuwei-k</id>
          <name>Kenji Yoshida</name>
          <url>https://github.com/xuwei-k</url>
        </developer>
        <developer>
          <id>godenji</id>
          <name>N.S. Cutler</name>
          <url>https://github.com/godenji</url>
        </developer>
      </developers>
      <scm>
        <url>git@github.com:godenji/replace-symbol-literals-to-string.git</url>
        <connection>scm:git:git@github.com:godenji/replace-symbol-literals-to-string.git</connection>
      </scm>
    ),
    publishTo := sonatypePublishTo.value,
    scalacOptions in (Compile, doc) ++= {
      val hash = sys.process.Process("git rev-parse HEAD").lineStream_!.head
      Seq(
        "-sourcepath",
        (baseDirectory in LocalRootProject).value.getAbsolutePath,
        "-doc-source-url",
        s"https://github.com/godenji/replace-symbol-literals-to-string/tree/${hash}€{FILE_PATH}.scala"
      )
    },
    scalacOptions ++= PartialFunction
      .condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
        case Some((2, v)) if v >= 12 =>
          Seq(
            "-Ywarn-unused:imports",
          )
      }
      .toList
      .flatten,
    scalacOptions ++= PartialFunction
      .condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
        case Some((2, v)) if v <= 12 =>
          Seq(
            "-Yno-adapted-args",
            "-Xfuture",
          )
      }
      .toList
      .flatten,
    scalacOptions ++= List(
      "-deprecation",
      "-unchecked",
      "-Yrangepos",
      "-P:semanticdb:synthetics:on"
    )
  )
)

commonSettings
skip in publish := true

lazy val rules = project.settings(
  commonSettings,
  name := "replace-symbol-literals-to-string",
  libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % V.scalafixVersion
)

lazy val input = project.settings(
  commonSettings,
  skip in publish := true
)

lazy val output = project.settings(
  commonSettings,
  skip in publish := true
)

lazy val tests = project
  .settings(
    commonSettings,
    skip in publish := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % V.scalafixVersion % Test cross CrossVersion.full,
    compile in Compile :=
      compile.in(Compile).dependsOn(compile.in(input, Compile)).value,
    scalafixTestkitOutputSourceDirectories :=
      sourceDirectories.in(output, Compile).value,
    scalafixTestkitInputSourceDirectories :=
      sourceDirectories.in(input, Compile).value,
    scalafixTestkitInputClasspath :=
      fullClasspath.in(input, Compile).value,
  )
  .dependsOn(rules)
  .enablePlugins(ScalafixTestkitPlugin)
