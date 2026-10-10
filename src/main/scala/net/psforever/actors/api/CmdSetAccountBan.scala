package net.psforever.actors.api

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{Actor, ActorRef}
import net.psforever.objects.Default
import net.psforever.persistence
import net.psforever.services.account.AccountPersistenceService
import net.psforever.services.ServiceManager.{Lookup, LookupResult}
import net.psforever.services.{InterstellarClusterService, ServiceManager}
import net.psforever.util.Database._

import scala.collection.mutable.Map
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Ban or unban an account, as the world server rather than as an outside database writer.
  *
  * Doing this here is the entire point: the portal used to flip `account.inactive` in Postgres
  * directly, which stopped the account logging in NEXT time but left any characters already in the
  * world playing happily. Running it through the world server means the same operation can both
  * persist the ban and act on the live world -- every character belonging to the account is kicked
  * immediately.
  *
  * The kick mirrors what an in-game administrator's `/kick` does (GeneralOperations.administrativeKick):
  * mark the player so their own session sends the disconnect, and tell the account-persistence service
  * so the logout is recorded rather than looking like a dropped connection.
  *
  * args: `<accountId> <true|false>`
  */
class CmdSetAccountBan(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  private val accountId: Int = args.headOption.flatMap(_.toIntOption).getOrElse(-1)
  private val banned: Boolean = args.lift(1).exists(_.equalsIgnoreCase("true"))

  /** (avatar id, name) owned by the account; populated from the database before the world is touched. */
  private var avatars: Set[(Int, String)] = Set.empty
  private var accountPersistence: ActorRef = Default.Actor
  private var persistenceReady: Boolean = false
  private var zonesPending: Boolean = false

  override def preStart(): Unit = {
    if (accountId < 0) {
      context.parent ! CommandErrorResponse("invalid account id\n", Map[String, Any]())
      context.stop(self)
    } else {
      ServiceManager.serviceManager ! Lookup("accountPersistence")
      updateAccount()
    }
  }

  /** Persist the ban flag, then (when banning) go looking for that account's characters in-world. */
  private def updateAccount(): Unit = {
    import ctx._
    val work = for {
      updated <- ctx.run(
        query[persistence.Account].filter(_.id == lift(accountId)).update(_.inactive -> lift(banned))
      )
      // Needed for BOTH directions: banning kicks these characters, unbanning clears the kick state
      // that would otherwise keep refusing their logins.
      avatars <- ctx.run(query[persistence.Avatar].filter(_.accountId == lift(accountId)))
    } yield (updated, avatars.map(a => (a.id, a.name)).toSet)

    work.onComplete {
      case Success((updated, ids)) => self ! CmdSetAccountBan.Updated(updated, ids)
      case Failure(e)              => self ! CmdSetAccountBan.Failed(e.getMessage)
    }
  }

  /**
    * Banning has to find the characters in-world to kick them; unbanning only has to undo the kick.
    *
    * Clearing the kick matters: the account-persistence service keeps a per-character monitor that
    * stays `kicked` for several minutes and answers further logins with `DeniedLoginReason.Kicked`.
    * Flipping the database row back does not touch it, so without this an unbanned player keeps being
    * told they were "logged out by a Customer Service Representative". Dropping the monitor makes the
    * next login build a clean one.
    */
  private def proceed(): Unit = {
    zonesPending = false
    if (banned) {
      ServiceManager.receptionist ! Receptionist.Find(
        InterstellarClusterService.InterstellarClusterServiceKey,
        context.self
      )
    } else {
      avatars.foreach { case (_, name) =>
        accountPersistence ! AccountPersistenceService.Logout(name)
      }
      finish(Nil)
    }
  }

  override def receive: Receive = {
    case LookupResult("accountPersistence", endpoint) =>
      accountPersistence = endpoint
      persistenceReady = true
      if (zonesPending) proceed()

    case CmdSetAccountBan.Updated(updated, ids) =>
      if (updated == 0) {
        context.parent ! CommandErrorResponse(s"no account with id $accountId\n", Map[String, Any]())
        context.stop(self)
      } else if (ids.isEmpty) {
        // No characters on the account: nothing to kick, and no kick state to clear.
        finish(Nil)
      } else {
        avatars = ids
        if (persistenceReady) proceed() else zonesPending = true
      }

    case CmdSetAccountBan.Failed(msg) =>
      context.parent ! CommandErrorResponse(s"database error: $msg\n", Map[String, Any]())
      context.stop(self)

    case InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings) =>
      listings.head ! InterstellarClusterService.FilterZones(_ => true, context.self)

    case InterstellarClusterService.ZonesResponse(zones) =>
      // Every live character owned by this account, wherever it happens to be standing.
      val ids = avatars.map(_._1)
      val targets = zones.flatMap(_.LivePlayers).filter(p => ids.contains(p.avatar.id))
      targets.foreach { p =>
        // The session watches for this and sends the client its disconnect notice.
        p.death_by = -1
        accountPersistence ! AccountPersistenceService.Kick(p.Name)
        log.warn(s"${p.Name} kicked: account $accountId was banned")
      }
      finish(targets.map(_.Name).toSeq)

    case default => log.error(s"Unexpected message $default")
  }

  private def finish(kicked: Seq[String]): Unit = {
    val data = Map[String, Any]()
    data("account_id") = accountId
    data("banned") = banned
    data("kicked") = kicked.toArray
    val verb = if (banned) "banned" else "unbanned"
    val tail = if (kicked.isEmpty) "" else s"; kicked ${kicked.mkString(", ")}"
    context.parent ! CommandGoodResponse(s"account $accountId $verb$tail\n", data)
    context.stop(self)
  }
}

private object CmdSetAccountBan {
  case class Updated(rowsChanged: Long, avatars: Set[(Int, String)])
  case class Failed(message: String)
}
