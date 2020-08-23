package net.psforever.util

import io.getquill.{PostgresJAsyncContext, SnakeCase}

object Database {
  val ctx = new PostgresJAsyncContext(SnakeCase, Config.config.getConfig("database"))

  // TODO remove if this gets merged https://github.com/getquill/quill/pull/1765
  implicit class ILike(s1: String) {
    import ctx._
    def ilike(s2: String) = quote(infix"$s1 ilike $s2".as[Boolean])
  }

}
