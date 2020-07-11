package net.psforever.util

import io.getquill.{PostgresJAsyncContext, SnakeCase}
import net.psforever.persistence

object Database {
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

  val ctx = new PostgresJAsyncContext(SnakeCase, Config.config.getConfig("database"))
}
