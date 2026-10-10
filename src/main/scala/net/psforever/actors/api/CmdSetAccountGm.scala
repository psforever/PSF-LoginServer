package net.psforever.actors.api

import akka.actor.{Actor, ActorRef}
import net.psforever.persistence
import net.psforever.util.Database._

import scala.collection.mutable.Map
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Set or clear an account's game-master flag, as the world server.
  *
  * Unlike a ban or a character's mode permissions, this one has nothing to push into a live session:
  * `account.gm` is read once by [[net.psforever.actors.net.LoginActor]] and carried on the session's
  * account record, but no in-world logic consults it -- the permissions that actually gate anything
  * live on the character (`avatarmodepermission`), which [[CmdSetAvatarPermissions]] handles. It moves
  * here anyway so that every write to the account table goes through the world server, leaving one
  * authority over the schema rather than two writers with their own ideas about it.
  *
  * args: `<accountId> <true|false>`
  */
class CmdSetAccountGm(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  private val accountId: Int = args.headOption.flatMap(_.toIntOption).getOrElse(-1)
  private val gm: Boolean = args.lift(1).exists(_.equalsIgnoreCase("true"))

  override def preStart(): Unit = {
    if (accountId < 0) {
      context.parent ! CommandErrorResponse("invalid account id\n", Map[String, Any]())
      context.stop(self)
    } else {
      import ctx._
      ctx
        .run(query[persistence.Account].filter(_.id == lift(accountId)).update(_.gm -> lift(gm)))
        .onComplete {
          case Success(updated) => self ! CmdSetAccountGm.Updated(updated)
          case Failure(e)       => self ! CmdSetAccountGm.Failed(e.getMessage)
        }
    }
  }

  override def receive: Receive = {
    case CmdSetAccountGm.Updated(updated) =>
      if (updated == 0) {
        context.parent ! CommandErrorResponse(s"no account with id $accountId\n", Map[String, Any]())
      } else {
        val data = Map[String, Any]()
        data("account_id") = accountId
        data("gm") = gm
        val verb = if (gm) "granted" else "revoked"
        context.parent ! CommandGoodResponse(s"account $accountId game-master $verb\n", data)
      }
      context.stop(self)

    case CmdSetAccountGm.Failed(msg) =>
      context.parent ! CommandErrorResponse(s"database error: $msg\n", Map[String, Any]())
      context.stop(self)

    case default => log.error(s"Unexpected message $default")
  }
}

private object CmdSetAccountGm {
  case class Updated(rowsChanged: Long)
  case class Failed(message: String)
}
