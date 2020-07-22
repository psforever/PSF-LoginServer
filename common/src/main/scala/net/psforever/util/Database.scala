package net.psforever.util

import io.getquill.{PostgresJAsyncContext, SnakeCase}
import net.psforever.persistence.{Account, Building, Loadout, Locker, Login, Character}

object Database {
  val ctx = new PostgresJAsyncContext(SnakeCase, Config.config.getConfig("database"))

  implicit val accountSchemaMeta: ctx.SchemaMeta[Account]     = ctx.schemaMeta[Account]("accounts")
  implicit val characterSchemaMeta: ctx.SchemaMeta[Character] = ctx.schemaMeta[Character]("characters")
  implicit val loadoutSchemaMeta: ctx.SchemaMeta[Loadout]     = ctx.schemaMeta[Loadout]("loadouts")
  implicit val lockerSchemaMeta: ctx.SchemaMeta[Locker]       = ctx.schemaMeta[Locker]("lockers")
  implicit val loginSchemaMeta: ctx.SchemaMeta[Login]         = ctx.schemaMeta[Login]("logins")
  implicit val buildingSchemaMeta: ctx.SchemaMeta[Building]   = ctx.schemaMeta[Building]("buildings")

  // TODO remove if this gets merged https://github.com/getquill/quill/pull/1765
  implicit class ILike(s1: String) {

    import ctx._

    def ilike(s2: String) = quote(infix"$s1 ilike $s2".as[Boolean])
  }

}
