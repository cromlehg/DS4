name := """ds4"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "mysql" % "mysql-connector-java" % "5.1.38",
  "com.typesafe.play" %% "play-slick" % "2.0.0",

  "com.github.nscala-time" %% "nscala-time" % "2.12.0",

  "org.webjars" % "jquery" % "2.2.1",
  "org.webjars" % "bootstrap" % "3.3.6" exclude("org.webjars", "jquery"),


  "com.github.t3hnar" %% "scala-bcrypt" % "2.6",
  "org.webjars" % "respond" % "1.4.2",
  "org.webjars" % "html5shiv" % "3.7.3",
  "org.webjars" % "font-awesome" % "4.6.1",
  "org.webjars" % "ionicons" % "2.0.1",
  "org.webjars" % "dropzone" % "4.2.0",
  "org.webjars.bower" % "iCheck" % "1.0.2",
  
  "jp.t2v" %% "play2-auth"        % "0.14.2"
  
  //"net.codingwell" %% "scala-guice" % "4.0.1"

)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

includeFilter in (Assets, LessKeys.less) := "*.less"
