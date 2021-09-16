import sbt._

object Dependencies {
  lazy val akkaVersion = "2.6.16"
  lazy val akkaActorTyped = "com.typesafe.akka" %% "akka-actor-typed"
  lazy val akkaPersistenceTyped = "com.typesafe.akka" %% "akka-persistence-typed"
  lazy val akkaClusterTyped = "com.typesafe.akka" %% "akka-cluster-typed"
  lazy val akkaClusterShardingTyped =
    "com.typesafe.akka" %% "akka-cluster-sharding-typed"

  lazy val akka =
    Seq(
      akkaActorTyped,
      akkaClusterTyped,
      akkaClusterShardingTyped,
      akkaPersistenceTyped
    ).map(_ % akkaVersion)

  lazy val catsVersion = "2.6.1"
  lazy val cats =
    Seq(
      "org.typelevel" %% "cats-core",
      "org.typelevel" %% "cats-kernel"
    ).map(_ % catsVersion)

  lazy val catsTaglessVersion = "0.14.0"
  lazy val catsTagless = Seq("org.typelevel" %% "cats-tagless-macros" % catsTaglessVersion)

  lazy val catsEffectVersion = "3.2.8"
  lazy val catsEffect = Seq("org.typelevel" %% "cats-effect" % catsEffectVersion)

  val circeVersion = "0.14.1"
  lazy val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion)
}
