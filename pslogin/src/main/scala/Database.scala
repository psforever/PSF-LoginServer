// Copyright (c) 2017 PSForever
import net.psforever.WorldConfig
import io.getquill.PostgresJAsyncContext
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import io.getquill.SnakeCase
import net.psforever.persistence

object Database {
  import WorldConfig.ConfigDatabaseSSL._

  private val config = ConfigFactory
    .empty()
    .withValue("host", ConfigValueFactory.fromAnyRef(WorldConfig.Get[String]("database.Hostname")))
    .withValue("port", ConfigValueFactory.fromAnyRef(WorldConfig.Get[Int]("database.Port")))
    .withValue("username", ConfigValueFactory.fromAnyRef(WorldConfig.Get[String]("database.Username")))
    .withValue("password", ConfigValueFactory.fromAnyRef(WorldConfig.Get[String]("database.Password")))
    .withValue("database", ConfigValueFactory.fromAnyRef(WorldConfig.Get[String]("database.Database")))
    .withValue(
      "sslmode",
      ConfigValueFactory.fromAnyRef(
        WorldConfig
          .Get[WorldConfig.ConfigDatabaseSSL.Value]("database.SSL") match {
          case Disable => "disable"
          case Prefer  => "prefer"
          case Require => "require"
          case Verify  => "verify-full"
        }
      )
    )

  implicit val accountSchemaMeta   = ctx.schemaMeta[persistence.Account]("accounts", _.id -> "id")
  implicit val characterSchemaMeta = ctx.schemaMeta[persistence.Character]("characters", _.id -> "id")
  implicit val loadoutSchemaMeta   = ctx.schemaMeta[persistence.Loadout]("loadouts", _.id -> "id")
  implicit val lockerSchemaMeta    = ctx.schemaMeta[persistence.Locker]("lockers", _.id -> "id")
  implicit val loginSchemaMeta     = ctx.schemaMeta[persistence.Login]("logins", _.id -> "id")

  // TODO remove if this gets merged https://github.com/getquill/quill/pull/1765
  implicit class ILike(s1: String) {
    import ctx._
    def ilike(s2: String) = quote(infix"$s1 ilike $s2".as[Boolean])
  }

  val ctx = new PostgresJAsyncContext(SnakeCase, config)
}
