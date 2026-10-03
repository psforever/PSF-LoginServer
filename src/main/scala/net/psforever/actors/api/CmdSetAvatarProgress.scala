package net.psforever.actors.api

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{Actor, ActorRef}
import net.psforever.objects.Player
import net.psforever.objects.avatar.{BattleRank, CommandRank}
import net.psforever.persistence
import net.psforever.services.{InterstellarClusterService, ServiceManager}
import net.psforever.util.Database._

import scala.collection.mutable.Map
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Set a character's battle and command experience, as the world server.
  *
  * The portal offers this as "set a rank" and as "set the raw experience", but those are the same
  * operation: a rank IS its experience threshold, so choosing BR12 means writing BR12's BEP. Only
  * experience is stored, and the rank is always derived from it -- there is no separate rank column
  * that could disagree with the points behind it.
  *
  * ==Why this is not a database write==
  * Doing it in Postgres would set the number and nothing else. Battle rank governs how many implant
  * slots a character has and which uniform they wear, so a rank lowered in the database leaves a
  * character holding implants they can no longer support, and a rank raised there grants nothing
  * until the avatar is next loaded. Routing through the avatar's own experience path makes the
  * change mean what it says: implants beyond the new slot limit are removed, cosmetics reset across
  * the BR24 boundary, and the client is told, all by the same code that runs when a player earns the
  * experience honestly.
  *
  * ==Online and offline are different paths, deliberately==
  * A logged-in character is changed through its session, which persists as it goes. A character
  * nobody is playing has no session to change, so the row is written directly -- and because the
  * live path already writes, this command must pick one or the other and never both.
  *
  * ==BEP is sent as a delta==
  * The avatar's battle-experience path adds rather than assigns, so an absolute target is turned
  * into a delta here. That is not a workaround: a player can earn experience between this command
  * reading their total and the session applying it, and a delta composes with that where an absolute
  * write would silently discard it. Command experience is a plain assignment and is sent as a total.
  *
  * args: `<avatarId>` followed by any of `bep:<n>` and `cep:<n>`, as absolute totals
  */
class CmdSetAvatarProgress(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  private val avatarId: Int = args.headOption.flatMap(_.toIntOption).getOrElse(-1)

  /** Only the fields the caller actually named are changed; the other keeps its stored value. */
  private val requested: scala.collection.immutable.Map[String, Long] = args
    .drop(1)
    .flatMap { token =>
      token.split(":", 2) match {
        case Array(k, v) if k == "bep" || k == "cep" => v.toLongOption.map(n => k -> math.max(0L, n))
        case _                                       => None
      }
    }
    .toMap

  private var name: String       = ""
  private var currentBep: Long   = 0L
  private var targetBep: Long    = 0L
  private var targetCep: Long    = 0L

  override def preStart(): Unit = {
    if (avatarId < 0) {
      context.parent ! CommandErrorResponse("invalid avatar id\n", Map[String, Any]())
      context.stop(self)
    } else if (requested.isEmpty) {
      context.parent ! CommandErrorResponse("no experience values given\n", Map[String, Any]())
      context.stop(self)
    } else {
      lookup()
    }
  }

  /** Read the stored totals first: the BEP delta cannot be worked out without the current value. */
  private def lookup(): Unit = {
    import ctx._
    ctx
      .run(query[persistence.Avatar].filter(_.id == lift(avatarId)))
      .onComplete {
        case Success(rows) => self ! CmdSetAvatarProgress.Loaded(rows.headOption)
        case Failure(e)    => self ! CmdSetAvatarProgress.Failed(e.getMessage)
      }
  }

  override def receive: Receive = {
    case CmdSetAvatarProgress.Loaded(None) =>
      context.parent ! CommandErrorResponse(s"no character with id $avatarId\n", Map[String, Any]())
      context.stop(self)

    case CmdSetAvatarProgress.Loaded(Some(avatar)) =>
      name = avatar.name
      currentBep = avatar.bep
      targetBep = requested.getOrElse("bep", avatar.bep)
      targetCep = requested.getOrElse("cep", avatar.cep)
      // Is this character being played right now? That decides which path applies the change.
      ServiceManager.receptionist ! Receptionist.Find(
        InterstellarClusterService.InterstellarClusterServiceKey,
        context.self
      )

    case CmdSetAvatarProgress.Failed(msg) =>
      context.parent ! CommandErrorResponse(s"database error: $msg\n", Map[String, Any]())
      context.stop(self)

    case InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings) =>
      listings.head ! InterstellarClusterService.FilterZones(_ => true, context.self)

    case InterstellarClusterService.ZonesResponse(zones) =>
      val live = zones.flatMap(_.LivePlayers).filter(_.avatar.id == avatarId)
      if (live.nonEmpty) {
        val bepDelta = Option.when(requested.contains("bep"))(targetBep - currentBep)
        val cep      = Option.when(requested.contains("cep"))(targetCep)
        live.foreach { p =>
          p.Actor ! Player.SetExperience(bepDelta, cep)
          log.info(s"${p.Name} experience changed while logged in: bep=$targetBep cep=$targetCep")
        }
        finish(appliedLive = true)
      } else {
        writeRows()
      }

    case CmdSetAvatarProgress.Written =>
      finish(appliedLive = false)

    case default => log.error(s"Unexpected message $default")
  }

  /** Nobody is playing this character, so there is no session to change -- write the row. */
  private def writeRows(): Unit = {
    import ctx._
    val bep = targetBep
    val cep = targetCep
    ctx
      .run(query[persistence.Avatar].filter(_.id == lift(avatarId)).update(_.bep -> lift(bep), _.cep -> lift(cep)))
      .onComplete {
        case Success(_) => self ! CmdSetAvatarProgress.Written
        case Failure(e) => self ! CmdSetAvatarProgress.Failed(e.getMessage)
      }
  }

  private def finish(appliedLive: Boolean): Unit = {
    val br = BattleRank.withExperience(targetBep).value
    val cr = CommandRank.withExperience(targetCep).value
    val data = Map[String, Any]()
    data("avatar_id") = avatarId
    data("name") = name
    data("bep") = targetBep
    data("cep") = targetCep
    data("battle_rank") = br
    data("command_rank") = cr
    data("applied_live") = appliedLive
    val tail = if (appliedLive) "; applied to the live session" else ""
    context.parent ! CommandGoodResponse(
      s"$name is now BR$br ($targetBep BEP), CR$cr ($targetCep CEP)$tail\n",
      data
    )
    context.stop(self)
  }
}

private object CmdSetAvatarProgress {
  case class Loaded(avatar: Option[persistence.Avatar])
  case object Written
  case class Failed(message: String)
}
