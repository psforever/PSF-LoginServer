package net.psforever.actors.api

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{Actor, ActorRef}
import net.psforever.objects.Player
import net.psforever.persistence
import net.psforever.services.{InterstellarClusterService, ServiceManager}
import net.psforever.util.Database._

import scala.collection.mutable.Map
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Grant or revoke a character's GM and spectator permissions, as the world server.
  *
  * The portal used to write `avatarmodepermission` in Postgres directly. That works, but the value is
  * only read once -- when the avatar loads -- so a GM promotion did nothing until the player logged
  * out and back in, and a revocation left them with their powers for the rest of the session. Running
  * it through the world server persists the row AND pushes the change into the live session.
  *
  * The row is upserted: a character that has never had either permission has no row at all, which is
  * why the portal's own implementation inserted before updating.
  *
  * args: `<avatarId>` followed by any of `gm:<true|false>` and `spectate:<true|false>`
  */
class CmdSetAvatarPermissions(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  private val avatarId: Int = args.headOption.flatMap(_.toIntOption).getOrElse(-1)

  /** Only the fields the caller actually named are changed; the rest keep their stored value. */
  private val requested: scala.collection.immutable.Map[String, Boolean] = args
    .drop(1)
    .flatMap { token =>
      token.split(":", 2) match {
        case Array(k, v) if k == "gm" || k == "spectate" => Some(k -> v.equalsIgnoreCase("true"))
        case _                                           => None
      }
    }
    .toMap

  /** Resolved state after the write, used for both the live push and the response. */
  private var canGm: Boolean = false
  private var canSpectate: Boolean = false
  private var name: String = ""

  override def preStart(): Unit = {
    if (avatarId < 0) {
      context.parent ! CommandErrorResponse("invalid avatar id\n", Map[String, Any]())
      context.stop(self)
    } else if (requested.isEmpty) {
      context.parent ! CommandErrorResponse("no permissions given\n", Map[String, Any]())
      context.stop(self)
    } else {
      updatePermissions()
    }
  }

  private def updatePermissions(): Unit = {
    import ctx._
    val work = for {
      avatars <- ctx.run(query[persistence.Avatar].filter(_.id == lift(avatarId)))
      existing <- ctx.run(query[persistence.Avatarmodepermission].filter(_.avatarId == lift(avatarId)))
      // Merge over whatever is stored so a caller flipping only GM does not clear spectator.
      gm       = requested.getOrElse("gm", existing.headOption.exists(_.canGm))
      spectate = requested.getOrElse("spectate", existing.headOption.exists(_.canSpectate))
      _ <-
        if (avatars.isEmpty) {
          // No such character; skip the write and let the reply below report it.
          scala.concurrent.Future.successful(0L)
        } else if (existing.isEmpty) {
          ctx.run(
            query[persistence.Avatarmodepermission].insert(
              _.avatarId    -> lift(avatarId),
              _.canGm       -> lift(gm),
              _.canSpectate -> lift(spectate)
            )
          )
        } else {
          ctx.run(
            query[persistence.Avatarmodepermission]
              .filter(_.avatarId == lift(avatarId))
              .update(_.canGm -> lift(gm), _.canSpectate -> lift(spectate))
          )
        }
    } yield (avatars.headOption.map(_.name), gm, spectate)

    work.onComplete {
      case Success((who, gm, spectate)) => self ! CmdSetAvatarPermissions.Updated(who, gm, spectate)
      case Failure(e)                   => self ! CmdSetAvatarPermissions.Failed(e.getMessage)
    }
  }

  override def receive: Receive = {
    case CmdSetAvatarPermissions.Updated(who, gm, spectate) =>
      who match {
        case None =>
          context.parent ! CommandErrorResponse(s"no character with id $avatarId\n", Map[String, Any]())
          context.stop(self)
        case Some(charName) =>
          name = charName
          canGm = gm
          canSpectate = spectate
          // Reach the character if it happens to be in the world right now.
          ServiceManager.receptionist ! Receptionist.Find(
            InterstellarClusterService.InterstellarClusterServiceKey,
            context.self
          )
      }

    case CmdSetAvatarPermissions.Failed(msg) =>
      context.parent ! CommandErrorResponse(s"database error: $msg\n", Map[String, Any]())
      context.stop(self)

    case InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings) =>
      listings.head ! InterstellarClusterService.FilterZones(_ => true, context.self)

    case InterstellarClusterService.ZonesResponse(zones) =>
      val live = zones.flatMap(_.LivePlayers).filter(_.avatar.id == avatarId)
      live.foreach { p =>
        p.Actor ! Player.SetModePermissions(canSpectate, canGm)
        log.info(s"${p.Name} permissions changed while logged in: spectate=$canSpectate gm=$canGm")
      }
      finish(live.nonEmpty)

    case default => log.error(s"Unexpected message $default")
  }

  private def finish(appliedLive: Boolean): Unit = {
    val data = Map[String, Any]()
    data("avatar_id") = avatarId
    data("name") = name
    data("can_gm") = canGm
    data("can_spectate") = canSpectate
    data("applied_live") = appliedLive
    val tail = if (appliedLive) "; applied to the live session" else ""
    context.parent ! CommandGoodResponse(
      s"$name permissions: gm=$canGm spectate=$canSpectate$tail\n",
      data
    )
    context.stop(self)
  }
}

private object CmdSetAvatarPermissions {
  case class Updated(name: Option[String], canGm: Boolean, canSpectate: Boolean)
  case class Failed(message: String)
}
