package net.psforever.actors.api

import scala.collection.mutable

/**
  * The result of an admin command. The command actors reply with one of these; the HTTP service
  * renders `data` (a json4s-serialisable map) as the response body, adding `message` and, for errors,
  * `error: true`.
  */
sealed trait CommandResponse {

  /** Human-readable outcome, common to both cases so callers (e.g. the audit log) can read it. */
  def message: String
}
case class CommandGoodResponse(message: String, data: mutable.Map[String, Any]) extends CommandResponse
case class CommandErrorResponse(message: String, data: mutable.Map[String, Any]) extends CommandResponse
