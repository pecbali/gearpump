import java.nio.file.Files
import java.util.regex.Pattern

import com.typesafe.sbt.SbtPgp.autoImport._
import de.johoop.jacoco4sbt.JacocoPlugin.jacoco
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import sbtassembly.Plugin.AssemblyKeys._
import sbtassembly.Plugin._
import xerial.sbt.Pack._
import xerial.sbt.Sonatype._

import scala.collection.immutable.Map.WithDefault

object Build extends sbt.Build {

  class DefaultValueMap[+B](value : B) extends WithDefault[String, B](null, (key) => value) {
    override def get(key: String) = Some(value)
  }

  /**
   * deploy can recognize the path
   */
  val travis_deploy = taskKey[Unit]("use this after sbt assembly packArchive, it will rename the package so that travis deploy can find the package.")
  
  val akkaVersion = "2.3.6"
  val kryoVersion = "0.3.2"
  val clouderaVersion = "2.5.0-cdh5.3.2"
  val clouderaHBaseVersion = "0.98.6-cdh5.3.2"
  val codahaleVersion = "3.0.2"
  val commonsCodecVersion = "1.6"
  val commonsHttpVersion = "3.1"
  val commonsLangVersion = "3.3.2"
  val commonsLoggingVersion = "1.1.3"
  val commonsIOVersion = "2.4"
  val findbugsVersion = "2.0.1"
  val guavaVersion = "15.0"
  val dataReplicationVersion = "0.7"
  val hadoopVersion = "2.5.1"
  val jgraphtVersion = "0.9.0"
  val json4sVersion = "3.2.10"
  val junitVersion = "4.12"
  val kafkaVersion = "0.8.2.1"
  val stormVersion = "0.9.3"
  val sigarVersion = "1.6.4"
  val slf4jVersion = "1.7.7"
  
  val scalaVersionMajor = "scala-2.11"
  val scalaVersionNumber = "2.11.6"
  val sprayVersion = "1.3.2"
  val sprayJsonVersion = "1.3.1"
  val sprayWebSocketsVersion = "0.1.4"
  val scalaTestVersion = "2.2.0"
  val scalaCheckVersion = "1.11.3"
  val mockitoVersion = "1.10.17"
  val bijectionVersion = "0.7.0"
  val scalazVersion = "7.1.1"
  val algebirdVersion = "0.9.0"
  val chillVersion = "0.6.0"

  val commonSettings = Seq(jacoco.settings:_*) ++ sonatypeSettings  ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++
    Seq(
        resolvers ++= Seq(
          "patriknw at bintray" at "http://dl.bintray.com/patriknw/maven",
          "maven-repo" at "http://repo.maven.apache.org/maven2",
          "maven1-repo" at "http://repo1.maven.org/maven2",
          "maven2-repo" at "http://mvnrepository.com/artifact",
          "sonatype" at "https://oss.sonatype.org/content/repositories/releases",
          "bintray/non" at "http://dl.bintray.com/non/maven",
          "cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos",
          "clockfly" at "http://dl.bintray.com/clockfly/maven",
          "OSSSnapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
        ),
        addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
    ) ++
    Seq(
      scalaVersion := scalaVersionNumber,
      crossScalaVersions := Seq("2.10.5"),
      organization := "com.github.intel-hadoop",
      useGpg := false,
      pgpSecretRing := file("./secring.asc"),
      pgpPublicRing := file("./pubring.asc"),
      scalacOptions ++= Seq("-Yclosure-elim","-Yinline"),
      publishMavenStyle := true,

      pgpPassphrase := Option(System.getenv().get("PASSPHRASE")).map(_.toArray),
      credentials += Credentials(
                   "Sonatype Nexus Repository Manager", 
                   "oss.sonatype.org", 
                   System.getenv().get("SONATYPE_USERNAME"), 
                   System.getenv().get("SONATYPE_PASSWORD")),
      
      pomIncludeRepository := { _ => false },

      publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot.value)
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases"  at nexus + "service/local/staging/deploy/maven2")
      },

      publishArtifact in Test := true,

      pomExtra := {
      <url>https://github.com/intel-hadoop/gearpump</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        </license>
      </licenses>
      <scm>
        <connection>scm:git:github.com/intel-hadoop/gearpump</connection>
        <developerConnection>scm:git:git@github.com:intel-hadoop/gearpump</developerConnection>
        <url>github.com/intel-hadoop/gearpump</url>
      </scm>
      <developers>
        <developer>
          <id>gearpump</id>
          <name>Gearpump Team</name>
          <url>https://github.com/intel-hadoop/teams/gearpump</url>
        </developer>
      </developers>
    }
  )

  val coreDependencies = Seq(
        libraryDependencies ++= Seq(
        "org.jgrapht" % "jgrapht-core" % jgraphtVersion,
        "com.codahale.metrics" % "metrics-core" % codahaleVersion,
        "com.codahale.metrics" % "metrics-graphite" % codahaleVersion,
        "org.slf4j" % "slf4j-api" % slf4jVersion,
        "org.slf4j" % "slf4j-log4j12" % slf4jVersion,
        "org.slf4j" % "jul-to-slf4j" % slf4jVersion intransitive(),
        "org.slf4j" % "jcl-over-slf4j" % slf4jVersion % "provided",
        "org.fusesource" % "sigar" % sigarVersion classifier "native",
        "com.google.code.findbugs" % "jsr305" % findbugsVersion,
        "org.apache.commons" % "commons-lang3" % commonsLangVersion,
        "commons-logging" % "commons-logging" % commonsLoggingVersion,
        "commons-httpclient" % "commons-httpclient" % commonsHttpVersion,
        "commons-codec" % "commons-codec" % commonsCodecVersion,
        "com.google.guava" % "guava" % guavaVersion,
        "com.typesafe.akka" %% "akka-actor" % akkaVersion,
        "com.typesafe.akka" %% "akka-remote" % akkaVersion,
        "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
        "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
        "com.typesafe.akka" %% "akka-agent" % akkaVersion,
        "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
        "com.typesafe.akka" %% "akka-kernel" % akkaVersion,
        "org.scala-lang" % "scala-compiler" % scalaVersion.value,
        "com.github.romix.akka" %% "akka-kryo-serialization" % kryoVersion,
        "com.github.patriknw" %% "akka-data-replication" % dataReplicationVersion,
        ("org.apache.hadoop" % "hadoop-common" % clouderaVersion).
            exclude("org.mortbay.jetty", "jetty-util")
            exclude("org.mortbay.jetty", "jetty")
            exclude("tomcat", "jasper-runtime")
            exclude("commons-beanutils", "commons-beanutils-core")
            exclude("commons-beanutils", "commons-beanutils"),
        ("org.apache.hadoop" % "hadoop-hdfs" % clouderaVersion).
            exclude("org.mortbay.jetty", "jetty-util")
            exclude("org.mortbay.jetty", "jetty")
            exclude("tomcat", "jasper-runtime"),
        "io.spray" %%  "spray-can"       % sprayVersion,
        "io.spray" %%  "spray-routing-shapeless2"   % sprayVersion,
        "commons-io" % "commons-io" % commonsIOVersion,
        "org.scala-js" %% "scalajs-library" % "0.6.3",
        "com.lihaoyi" %% "upickle" % "0.2.8",
        "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
        "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
        "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
        "org.mockito" % "mockito-core" % mockitoVersion % "test",
        "junit" % "junit" % junitVersion % "test"
      ),
     libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _),
     libraryDependencies ++= (
        if (scalaVersion.value.startsWith("2.10")) List("org.scalamacros" %% "quasiquotes" % "2.1.0-M5")
        else Nil
      )
  )

  val myAssemblySettings = assemblySettings ++ Seq(
    test in assembly := {},
    assemblyOption in assembly ~= { _.copy(includeScala = false) }
  )

  lazy val root = Project(
    id = "gearpump",
    base = file("."),
    settings = commonSettings ++
      Seq(
        travis_deploy := {
          val packagePath = s"output/target/gearpump-pack-${version.value}.tar.gz"
          val target = s"target/binary.gearpump.tar.gz"
          println(s"[Travis-Deploy] Move file $packagePath to $target")
          new File(packagePath).renameTo(new File(target))
        }
      )
  ).dependsOn(core, streaming, services, external_kafka)
   .aggregate(core, streaming, fsio, examples_kafka, sol, wordcount, complexdag, services, external_kafka, stockcrawler,
      transport, examples, distributedshell, distributeservice, storm, yarn, dsl, pagerank,hbase, pack, pipeline, state)

  lazy val pack = Project(
    id = "gearpump-pack",
    base = file("output"),
    settings = commonSettings ++
      packSettings ++
      Seq(
        packMain := Map("gear" -> "org.apache.gearpump.cluster.main.Gear",
          "local" -> "org.apache.gearpump.cluster.main.Local",
          "master" -> "org.apache.gearpump.cluster.main.Master",
          "worker" -> "org.apache.gearpump.cluster.main.Worker",
          "services" -> "org.apache.gearpump.cluster.main.Services",
          "yarnclient" -> "org.apache.gearpump.experiments.yarn.client.Client"
        ),
        packJvmOpts := Map("local" -> Seq("-server", "-DlogFilename=local"),
          "master" -> Seq("-server", "-DlogFilename=master"),
          "worker" -> Seq("-server", "-DlogFilename=worker"),
          "services" -> Seq("-server")
        ),
        packResourceDir += (baseDirectory.value / ".." / "conf" -> "conf"),
        packResourceDir += (baseDirectory.value / ".." / "services" / "dashboard" -> "dashboard"),
        packResourceDir += (baseDirectory.value / ".." / "services" / "js" / "target" / scalaVersionMajor -> "dashboard"),
        packResourceDir += (baseDirectory.value / ".." / "services" / "js" / "src" -> "dashboard/src"),
        packResourceDir += (baseDirectory.value / ".." / "examples" / "target" / scalaVersionMajor -> "examples"),

        // The classpath should not be expanded. Otherwise, the classpath maybe too long.
        // On windows, it may report shell error "command line too long"
        packExpandedClasspath := false,
        packExtraClasspath := new DefaultValueMap(Seq("/etc/gearpump/conf", "${PROG_HOME}/conf",
          "${PROG_HOME}/dashboard", "/etc/hadoop/conf", "/etc/hbase/conf"))
      )
  ).dependsOn(core, streaming, services, external_kafka, yarn,storm,dsl,pagerank,hbase, state)

  lazy val core = Project(
    id = "gearpump-core",
    base = file("core"),
    settings = commonSettings ++ coreDependencies
  )

  lazy val streaming = Project(
    id = "gearpump-streaming",
    base = file("streaming"),
    settings = commonSettings
  )  dependsOn(core % "test->test;compile->compile")

  lazy val external_kafka = Project(
    id = "gearpump-external-kafka",
    base = file("external/kafka"),
    settings = commonSettings ++ myAssemblySettings ++
      Seq(
        libraryDependencies ++= Seq(
          "org.apache.kafka" %% "kafka" % kafkaVersion,
          "com.twitter" %% "bijection-core" % bijectionVersion,
          "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
          "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
          "org.mockito" % "mockito-core" % mockitoVersion % "test",
          ("org.apache.kafka" %% "kafka" % kafkaVersion classifier "test") % "test"
        ),
        unmanagedClasspath in Test += baseDirectory.value.getParentFile.getParentFile / "conf"
      )
  ) dependsOn (streaming % "provided")

  lazy val examples_kafka = Project(
    id = "gearpump-examples-kafka",
    base = file("examples/streaming/kafka"),
    settings = commonSettings ++ myAssemblySettings ++
      Seq(
        libraryDependencies ++= Seq(
          "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
          "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
          "org.mockito" % "mockito-core" % mockitoVersion % "test",
          "junit" % "junit" % junitVersion % "test"
        ),
        target in assembly := baseDirectory.value.getParentFile.getParentFile / "target" / scalaVersionMajor
      )
  ) dependsOn(streaming % "test->test", streaming % "provided", external_kafka  % "test->test; provided")

  lazy val fsio = Project(
    id = "gearpump-examples-fsio",
    base = file("examples/streaming/fsio"),
    settings = commonSettings ++ myAssemblySettings ++
      Seq(
        libraryDependencies ++= Seq(
          "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
          "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
          "org.mockito" % "mockito-core" % mockitoVersion % "test"
        ),
        mainClass in (Compile, packageBin) := Some("org.apache.gearpump.streaming.examples.fsio.SequenceFileIO"),
        target in assembly := baseDirectory.value.getParentFile.getParentFile / "target" / scalaVersionMajor
      )
  ) dependsOn (streaming % "test->test", streaming % "provided")

  lazy val sol = Project(
    id = "gearpump-examples-sol",
    base = file("examples/streaming/sol"),
    settings = commonSettings ++ myAssemblySettings ++
      Seq(
        libraryDependencies ++= Seq(
          "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
          "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
          "org.mockito" % "mockito-core" % mockitoVersion % "test"
        ),
        mainClass in (Compile, packageBin) := Some("org.apache.gearpump.streaming.examples.sol.SOL"),
        target in assembly := baseDirectory.value.getParentFile.getParentFile / "target" / scalaVersionMajor
      )
  ) dependsOn (streaming % "test->test", streaming % "provided")

  lazy val wordcount = Project(
    id = "gearpump-examples-wordcount",
    base = file("examples/streaming/wordcount"),
    settings = commonSettings ++ myAssemblySettings ++
      Seq(
        libraryDependencies ++= Seq(
          "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
          "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
          "org.mockito" % "mockito-core" % mockitoVersion % "test"
        ),
        mainClass in (Compile, packageBin) := Some("org.apache.gearpump.streaming.examples.wordcount.WordCount"),
        target in assembly := baseDirectory.value.getParentFile.getParentFile / "target" / scalaVersionMajor
      )
  ) dependsOn (streaming % "test->test", streaming % "provided")

  lazy val stockcrawler = Project(
    id = "gearpump-examples-stockcrawler",
    base = file("examples/streaming/stockcrawler"),
    settings = commonSettings ++ myAssemblySettings ++
      Seq(
        libraryDependencies ++= Seq(
          "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.2",
          "joda-time" % "joda-time" % "2.7",
          "io.spray" %%  "spray-json"    % sprayJsonVersion
        ),
        mainClass in (Compile, packageBin) := Some("org.apache.gearpump.streaming.examples.stock.main.Stock"),
        target in assembly := baseDirectory.value.getParentFile.getParentFile / "target" / scalaVersionMajor
      )
  ) dependsOn (streaming % "test->test", streaming % "provided", external_kafka  % "test->test; provided")

  lazy val complexdag = Project(
    id = "gearpump-examples-complexdag",
    base = file("examples/streaming/complexdag"),
    settings = commonSettings ++ myAssemblySettings ++
      Seq(
        libraryDependencies ++= Seq(
          "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
          "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
          "org.mockito" % "mockito-core" % mockitoVersion % "test"
        ),
        mainClass in (Compile, packageBin) := Some("org.apache.gearpump.streaming.examples.complexdag.Dag"),
        target in assembly := baseDirectory.value.getParentFile.getParentFile / "target" / scalaVersionMajor
      )
  ) dependsOn (streaming % "test->test", streaming % "provided")

  lazy val transport = Project(
    id = "gearpump-examples-transport",
    base = file("examples/streaming/transport"),
    settings = commonSettings ++ myAssemblySettings ++
      Seq(
        libraryDependencies ++= Seq(
          "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
          "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
          "org.mockito" % "mockito-core" % mockitoVersion % "test",
          "io.spray" %%  "spray-json"    % sprayJsonVersion
        ),
        mainClass in (Compile, packageBin) := Some("org.apache.gearpump.streaming.examples.transport.Transport"),
        target in assembly := baseDirectory.value.getParentFile.getParentFile / "target" / scalaVersionMajor
      )
  ) dependsOn (streaming % "test->test", streaming % "provided")

  lazy val examples = Project(
    id = "gearpump-examples",
    base = file("examples"),
    settings = commonSettings
  ) dependsOn (wordcount, complexdag, sol, fsio, examples_kafka, distributedshell, stockcrawler, transport)

  lazy val copySharedResources = Def.task {
    ConsoleLogger().info("copying shared resources")
    val sharedMessages = "src/main/scala/org/apache/gearpump/shared/Messages.scala"
    IO.copyFile(core.base / sharedMessages, file("services/js") / sharedMessages)
  }

  lazy val services = Project(id = "gearpump-services", base = file("services")).
    settings(commonSettings: _*).
    aggregate(servicesjs, servicesjvm).dependsOn(streaming % "test->test;compile->compile")
  
  lazy val servicesjvm = Project(id = "gearpump-services-jvm", base = file("services/jvm")).
    settings(jvmSettings : _*).dependsOn(streaming % "test->test;compile->compile")

  lazy val servicesjs = Project(id = "gearpump-services-js", base = file("services/js")).
    enablePlugins(ScalaJSPlugin).
    settings(commonSettings: _*).
    settings(jsSettings : _*)

  def findDirs(currentDir: String): Array[File] = {
    val dirs = IO.listFiles(file(currentDir),  DirectoryFilter)
    dirs ++ dirs.flatMap(dir => {
      val childDir = currentDir + "/" + dir.getName
      findDirs(file(childDir).getPath)
    })
  }

  def findFiles(dir: File, pattern: PatternFilter): Array[File] = {
    val dirs = findDirs(dir.getPath)
    IO.listFiles(dir, pattern) ++ dirs.flatMap(dir => {
      IO.listFiles(dir, pattern)
    })
  }

  def copyJSArtifactsToOutput: Unit = {
    ConsoleLogger().info("copying JS artifacts")
    val in = file("services/js/target/scala-2.11/gearpump-services-js-fastopt.js.map")
    val out = file("output/target/pack/dashboard/gearpump-services-js-fastopt.js.map")
    val input = IO.read(in)
    val data = input.replaceAll("../../src","src")
    IO.write(out, data)
    IO.copyFile(
      file("services/js/target/scala-2.11/gearpump-services-js-fastopt.js"),
      file("output/target/pack/dashboard/gearpump-services-js-fastopt.js")
    )
    IO.copyDirectory(
      file("services/js/src"),
      file("output/target/pack/dashboard"),
      true
    )
    IO.copyDirectory(
      file("services/js"), 
      file("output") / "target" / "pack" / "dashboard",
      true
    )
    val htmlFiles = findFiles(file("services/dashboard"), new PatternFilter(Pattern.compile("^.*.html$")))
    htmlFiles.foreach(htmlFile => {
      val source = htmlFile.getPath
      val target = "output/target/pack" + htmlFile.getPath.replaceFirst("services/", "/")
      IO.copyFile(file(source), file(target))
    })
  }

  lazy val jvmSettings = commonSettings ++ Seq(libraryDependencies ++= Seq(
    "io.spray" %% "spray-testkit" % sprayVersion % "test",
    "io.spray" %% "spray-httpx" % sprayVersion,
    "io.spray" %% "spray-client" % sprayVersion,
    "io.spray" %% "spray-json" % sprayJsonVersion,
    "com.wandoulabs.akka" %% "spray-websocket" % sprayWebSocketsVersion
      exclude("com.typesafe.akka", "akka-actor_2.11"),
    "org.json4s" %% "json4s-jackson" % json4sVersion,
    "org.json4s" %% "json4s-native" % json4sVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
    "org.webjars" % "angularjs" % "1.4.0",
    "org.webjars" % "jquery" % "2.1.3",
    "org.webjars" % "angular-motion" % "0.3.3",
    "org.webjars" % "angular-strap" % "2.2.3",
    "org.webjars" % "angular-ui-select" % "0.11.2",
    "org.webjars" % "bootstrap" % "3.3.4",
    "org.webjars" % "d3js" % "3.5.5",
    "org.webjars" % "momentjs" % "2.10.3",
    "org.webjars" % "smart-table" % "2.0.3",
    "org.webjars.bower" % "vis" % "4.2.0"
    ).map(_.exclude("org.scalamacros", "quasiquotes_2.10")).map(_.exclude("org.scalamacros", "quasiquotes_2.10.3")),
    compile in Compile <<=
      (compile in Compile) dependsOn (fastOptJS in (servicesjs, Compile)),
    ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }
  )

  lazy val jsSettings = Seq(
    resolvers ++= Seq(
      "OSSSnapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    ),
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "0.2.8",
      "com.greencatsoft" %%% "scalajs-angular" % "0.5-SNAPSHOT"
    ),
    scalaVersion := scalaVersionNumber,
    persistLauncher in Compile := true,
    persistLauncher in Test := false,
    skip in packageJSDependencies := false,
    jsDependencies += "org.webjars" % "angularjs" % "1.3.15" / "angular.js",
    fastOptJS in Compile := {
      val originalResult = (fastOptJS in Compile).value
      copyJSArtifactsToOutput
      originalResult
    },
    compile in Compile <<=
      (compile in Compile) dependsOn copySharedResources,
    relativeSourceMaps := true)

  lazy val distributedshell = Project(
    id = "gearpump-examples-distributedshell",
    base = file("examples/distributedshell"),
    settings = commonSettings ++ myAssemblySettings ++
      Seq(
        target in assembly := baseDirectory.value.getParentFile / "target" / scalaVersionMajor
      )
  ) dependsOn(core % "test->test", core % "provided")

  lazy val distributeservice = Project(
    id = "gearpump-experiments-distributeservice",
    base = file("experiments/distributeservice"),
    settings = commonSettings
  ) dependsOn(core % "test->test;compile->compile")

  lazy val storm = Project(
    id = "gearpump-experiments-storm",
    base = file("experiments/storm"),
    settings = commonSettings ++
      Seq(
        libraryDependencies ++= Seq(
          "org.apache.storm" % "storm-core" % stormVersion
            exclude("ch.qos.logback", "logback-classic")
            exclude("ch.qos.logback", "logback-core")
            exclude("clj-stacktrace", "clj-stacktrace")
            exclude("clj-time", "clj-time")
            exclude("clout", "clout")
            exclude("compojure", "compojure")
            exclude("com.esotericsoftware.kryo", "kryo")
            exclude("com.esotericsoftware.minlog", "minlog")
            exclude("com.esotericsoftware.reflectasm", "reflectasm")
            exclude("com.googlecode.disruptor", "disruptor")
            exclude("com.twitter", "carbonite")
            exclude("com.twitter", "chill-java")
            exclude("commons-codec", "commons-codec")
            exclude("commons-fileupload", "commons-fileupload")
            exclude("commons-io", "commons-io")
            exclude("commons-lang", "commons-lang")
            exclude("commons-logging", "commons-loggin")
            exclude("hiccup", "hiccup")
            exclude("javax.servlet", "servlet-api")
            exclude("jgrapht", "jgrapht-core")
            exclude("jline", "jline")
            exclude("joda-time", "joda-time")
            exclude("org.apache.commons", "commons-exec")
            exclude("org.clojure", "core.incubator")
            exclude("org.clojure", "math.numeric-tower")
            exclude("org.clojure", "tools.logging")
            exclude("org.clojure", "tools.cli")
            exclude("org.clojure", "tools.macro")
            exclude("org.mortbay.jetty", "jetty-util")
            exclude("org.mortbay.jetty", "jetty")
            exclude("org.objenisis", "objenisis")
            exclude("org.ow2.asm", "asm")
            exclude("org.slf4j", "log4j-over-slf4j")
            exclude("org.slf4j", "slf4j-api")
            exclude("ring", "ring-core")
            exclude("ring", "ring-devel")
            exclude("ring", "ring-jetty-adapter")
            exclude("ring", "ring-servlet"),
          "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
          "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
          "org.mockito" % "mockito-core" % mockitoVersion % "test"
        )
      )
  ) dependsOn (streaming % "test->test; provided")


  lazy val yarn = Project(
    id = "gearpump-experiments-yarn",
    base = file("experiments/yarn"),
    settings = commonSettings ++
      Seq(
        libraryDependencies ++= Seq(
          "org.apache.hadoop" % "hadoop-yarn-api" % clouderaVersion
            exclude("com.google.guava", "guava")
            exclude("com.google.protobuf", "protobuf-java")
            exclude("commons-lang", "commons-lang")
            exclude("commons-logging", "commons-logging")
            exclude("org.apache.hadoop", "hadoop-annotations"),
          "org.apache.hadoop" % "hadoop-yarn-client" % clouderaVersion
            exclude("com.google.guava", "guava")
            exclude("com.sun.jersey", "jersey-client")
            exclude("commons-cli", "commons-cli")
            exclude("commons-lang", "commons-lang")
            exclude("commons-logging", "commons-logging")
            exclude("log4j", "log4j")
            exclude("org.apache.hadoop", "hadoop-annotations")
            exclude("org.mortbay.jetty", "jetty-util")
            exclude("org.apache.hadoop", "hadoop-yarn-api")
            exclude("org.apache.hadoop", "hadoop-yarn-common"),
          "org.apache.hadoop" % "hadoop-yarn-common" % clouderaVersion
            exclude("com.google.guava", "guava")
            exclude("com.google.inject.extensions", "guice-servlet")
            exclude("com.google.inject", "guice")
            exclude("com.google.protobuf", "protobuf-java")
            exclude("com.sun.jersey.contribs", "jersey.guice")
            exclude("com.sun.jersey", "jersey-core")
            exclude("com.sun.jersey", "jersey-json")
            exclude("commons-cli", "commons-cli")
            exclude("commons-codec", "commons-codec")
            exclude("commons-io", "commons-io")
            exclude("commons-lang", "commons-lang")
            exclude("commons-logging", "commons-logging")
            exclude("javax.servlet", "servlet-api")
            exclude("javax.xml.bind", "jaxb-api")
            exclude("log4j", "log4j")
            exclude("org.apache.commons", "commons-compress")
            exclude("org.apache.hadoop", "hadoop-annotations")
            exclude("org.codehaus.jackson", "jackson-core-asl")
            exclude("org.codehaus.jackson", "jackson-jaxrs")
            exclude("org.codehaus.jackson", "jackson-mapper-asl")
            exclude("org.codehaus.jackson", "jackson-xc")
            exclude("org.slf4j", "slf4j-api"),
          "org.apache.hadoop" % "hadoop-yarn-server-resourcemanager" % clouderaVersion % "provided",
          "org.apache.hadoop" % "hadoop-yarn-server-nodemanager" % clouderaVersion % "provided"
        )
      )
  ) dependsOn(services % "test->test;compile->compile", core % "provided", servicesjvm % "provided")

  lazy val dsl = Project(
    id = "gearpump-experiments-dsl",
    base = file("experiments/dsl"),
    settings = commonSettings ++
      Seq(
        libraryDependencies ++= Seq(
          "org.scalaz" %% "scalaz-core" % scalazVersion
        )
      )
  ) dependsOn(streaming % "test->test;compile->compile", external_kafka % "test->test;compile->compile", hbase % "test->test;compile->compile")
  
  lazy val pagerank = Project(
    id = "gearpump-experiments-pagerank",
    base = file("experiments/pagerank"),
    settings = commonSettings
  ) dependsOn(streaming % "test->test;compile->compile")

  lazy val hbase = Project(
    id = "gearpump-experiments-hbase",
    base = file("experiments/hbase"),
    settings = commonSettings ++
      Seq(
        resolvers ++= Seq(
          "cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos"
        )
      ) ++
      Seq(
        libraryDependencies ++= Seq(
          "org.apache.hadoop" % "hadoop-common" % clouderaVersion % "provided",
          "org.apache.hadoop" % "hadoop-hdfs" % clouderaVersion % "provided",
          "org.apache.hbase" % "hbase-client" % clouderaHBaseVersion
            exclude("com.github.stephenc.findbugs", "findbugs-annotations")
            exclude("com.google.guava", "guava")
            exclude("commons-codec", "commons-codec")
            exclude("commons-io", "commons-io")
            exclude("commons-lang", "commons-lang")
            exclude("commons-logging", "commons-logging")
            exclude("io.netty", "netty")
            exclude("junit", "junit")
            exclude("log4j", "log4j")
            exclude("org.apache.zookeeper", "zookeeper")
            exclude("org.codehaus.jackson", "jackson-mapper-asl"),
          "org.apache.hbase" % "hbase-common" % clouderaHBaseVersion
            exclude("com.github.stephenc.findbugs", "findbugs-annotations")
            exclude("com.google.guava", "guava")
            exclude("commons-codec", "commons-codec")
            exclude("commons-collections", "commons-collections")
            exclude("commons-io", "commons-io")
            exclude("commons-lang", "commons-lang")
            exclude("commons-logging", "commons-logging")
            exclude("junit", "junit")
            exclude("log4j", "log4j"),
          "org.scalaz" %% "scalaz-core" % scalazVersion
        )
      )
  ) dependsOn(core % "provided")

  lazy val pipeline = Project(
    id = "gearpump-experiments-kafka-hbase-pipeline",
    base = file("experiments/kafka-hbase-pipeline"),
    settings = commonSettings ++ myAssemblySettings 
  ) dependsOn(streaming % "test->test", streaming % "provided", external_kafka  % "test->test; provided", hbase, dsl)

  lazy val state = Project(
    id = "gearpump-experiments-state",
    base = file("experiments/state"),
    settings = commonSettings ++
      Seq(
        libraryDependencies ++= Seq(
          "com.twitter" %% "bijection-core" % bijectionVersion,
          "com.twitter" %% "algebird-core" % algebirdVersion,
          "com.twitter" %% "chill-bijection" % chillVersion,
          "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
          "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
          "org.mockito" % "mockito-core" % mockitoVersion % "test"
        )
      )
  ) dependsOn(streaming % "test->test; provided", external_kafka % "test->test; provided")
}
