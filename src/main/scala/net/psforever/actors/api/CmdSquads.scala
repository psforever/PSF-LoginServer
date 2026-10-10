package net.psforever.actors.api

import akka.actor.{Actor, ActorRef}
import net.psforever.services.ServiceManager
import net.psforever.services.ServiceManager.Lookup
import net.psforever.services.teamwork.SquadService

import scala.collection.mutable.Map

/**
  * List every squad the world server currently holds.
  *
  * Squads exist only in `SquadService`'s memory -- there is no table to read and no other way to see
  * one from outside. The listing is built inside that actor's own message loop, so it is a consistent
  * snapshot rather than a walk over maps something else may be changing.
  */
class CmdListSquads(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  override def preStart(): Unit = {
    ServiceManager.serviceManager ! Lookup("squad")
  }

  override def receive: Receive = {
    case ServiceManager.LookupResult(_, endpoint) =>
      endpoint ! SquadService.ListSquads(context.self)

    case SquadService.SquadListing(squads) =>
      val data = Map[String, Any]()
      data("count") = squads.size
      data("squads") = squads.map { s =>
        Map[String, Any](
          "id"             -> s.id,
          "faction"        -> s.faction,
          "leader"         -> s.leader,
          "leader_char_id" -> s.leaderCharId,
          "task"           -> s.task,
          "zone_id"        -> s.zoneId,
          "size"           -> s.size,
          "capacity"       -> s.capacity,
          "listed"         -> s.listed,
          "members" -> s.members.map { m =>
            Map[String, Any](
              "position" -> m.position,
              "char_id"  -> m.charId,
              "name"     -> m.name,
              "role"     -> m.role,
              "zone_id"  -> m.zoneId,
              "health"   -> m.health,
              "armor"    -> m.armor
            )
          }
        )
      }
      context.parent ! CommandGoodResponse(s"${squads.size} squad(s)\n", data)
      context.stop(self)

    case default =>
      log.error(s"Unexpected message $default")
  }
}

/**
  * Disband a squad, or remove one character from whichever squad holds them.
  *
  * Both reuse the lifecycle methods the game itself calls -- `DisbandSquad` and `LeaveSquad` -- rather
  * than a second implementation. That matters because leaving a squad is not just clearing a slot: it
  * unsubscribes a channel, updates the faction's published listing, and closes a squad that has just
  * been emptied. A bespoke "remove" here would have to reproduce all of that, and would drift from the
  * real one the first time either changed.
  *
  * args: `disband <squadId>` or `remove <charId>`
  */
class CmdSquadAction(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  private val action: String = args.headOption.getOrElse("")
  private val target: Option[Long] = args.lift(1).flatMap(_.toLongOption)

  override def preStart(): Unit = {
    (action, target) match {
      case ("disband", Some(_)) | ("remove", Some(_)) =>
        ServiceManager.serviceManager ! Lookup("squad")
      case (_, None) =>
        context.parent ! CommandErrorResponse("a numeric target is required\n", Map[String, Any]())
        context.stop(self)
      case _ =>
        context.parent ! CommandErrorResponse(s"unknown squad action '$action'\n", Map[String, Any]())
        context.stop(self)
    }
  }

  override def receive: Receive = {
    case ServiceManager.LookupResult(_, endpoint) =>
      (action, target) match {
        case ("disband", Some(id)) => endpoint ! SquadService.AdminDisband(id.toInt, context.self)
        case ("remove", Some(id))  => endpoint ! SquadService.AdminRemoveMember(id, context.self)
        case _                     => context.stop(self)
      }

    case SquadService.AdminResult(ok, message) =>
      val data = Map[String, Any]()
      data("action") = action
      data("target") = target.getOrElse(-1L)
      data("ok") = ok
      if (ok) context.parent ! CommandGoodResponse(s"$message\n", data)
      else context.parent ! CommandErrorResponse(s"$message\n", data)
      context.stop(self)

    case default =>
      log.error(s"Unexpected message $default")
  }
}
