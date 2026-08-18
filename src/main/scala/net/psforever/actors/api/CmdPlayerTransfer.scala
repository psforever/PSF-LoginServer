package net.psforever.actors.api

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{Actor, ActorRef}
import net.psforever.objects.Player
import net.psforever.zones.Zones
import net.psforever.services.{InterstellarClusterService, ServiceManager}
import net.psforever.util.PointOfInterest

import scala.collection.mutable.Map

/**
  * Move a logged-in character somewhere else, as the world server.
  *
  * Two destinations, matching the two things the game itself can do:
  *
  *   - `sanctuary` -- a forced sanctuary recall, identical to the player typing `/recall`. The
  *     destination is derived from the player's OWN faction, so one call does the right thing for a
  *     mixed group and an administrator never has to look it up.
  *   - a zone id -- a forced transfer, identical to a CSR typing `/zone <zone> <gate>`. Without a
  *     gate, one of that zone's warp gates is chosen at random, which is what `/zone` does too.
  *
  * Neither is novel behaviour. The transfer itself can only be performed by the player's own session
  * actor, which this actor cannot reach; it sends `Player.ForceRecall`/`Player.ForceZone` to the
  * player's control actor, which relays through `AvatarActor` to the session. That is the same route
  * `Player.SetModePermissions` already takes.
  *
  * Targeting is BY NAME rather than by GUID: an administrator acting on a report has a character
  * name, and a GUID is only meaningful within one zone. The search covers every zone, so the caller
  * does not need to know where the player currently is.
  *
  * args: `<characterName>` `<destination>` `[gateId]`
  */
class CmdPlayerTransfer(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  private val name: String        = args.headOption.getOrElse("")
  private val destination: String = args.lift(1).getOrElse("")
  private val gateId: Option[String] = args.lift(2).filter(_.nonEmpty)

  private val toSanctuary: Boolean = destination.equalsIgnoreCase("sanctuary")

  override def preStart(): Unit = {
    if (name.isEmpty) {
      context.parent ! CommandErrorResponse("no character name given\n", Map[String, Any]())
      context.stop(self)
    } else if (destination.isEmpty) {
      context.parent ! CommandErrorResponse("no destination given (a zone id, or 'sanctuary')\n", Map[String, Any]())
      context.stop(self)
    } else if (!toSanctuary && PointOfInterest.get(destination).isEmpty) {
      context.parent ! CommandErrorResponse(s"no such zone '$destination'\n", Map[String, Any]())
      context.stop(self)
    } else {
      ServiceManager.receptionist ! Receptionist.Find(
        InterstellarClusterService.InterstellarClusterServiceKey,
        context.self
      )
    }
  }

  override def receive: Receive = {
    case InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings) =>
      listings.head ! InterstellarClusterService.FilterZones(_ => true, context.self)

    case InterstellarClusterService.ZonesResponse(zones) =>
      // A character name is unique, so at most one of these matches; searching every zone means the
      // caller never has to say where the player is.
      zones.flatMap(z => z.LivePlayers.map(p => (z, p)))
        .find { case (_, p) => p.Name.equalsIgnoreCase(name) } match {
        case None =>
          context.parent ! CommandErrorResponse(s"$name is not logged in\n", Map[String, Any]())
          context.stop(self)

        case Some((zone, player)) if toSanctuary =>
          val sanctuary = Zones.sanctuaryZoneId(player.Faction)
          if (zone.id == sanctuary) {
            // The session treats a recall from inside the sanctuary as a fault and disconnects the
            // player, so refuse it here instead of kicking somebody for an administrator's typo.
            context.parent ! CommandErrorResponse(
              s"${player.Name} is already in their sanctuary ($sanctuary)\n", Map[String, Any]()
            )
          } else {
            player.Actor ! Player.ForceRecall()
            log.info(s"${player.Name} recalled to $sanctuary by the admin API")
            respond(player.Name, sanctuary, zone.id, gate = None)
          }
          context.stop(self)

        case Some((zone, player)) =>
          val poi = PointOfInterest.get(destination).get
          val position = gateId match {
            case Some(id) => PointOfInterest.getWarpgate(poi, id)
            case None     => Some(PointOfInterest.selectRandom(poi))
          }
          position match {
            case None =>
              context.parent ! CommandErrorResponse(
                s"no warp gate '${gateId.getOrElse("")}' in $destination\n", Map[String, Any]()
              )
            case Some(pos) =>
              player.Actor ! Player.ForceZone(poi.zonename, pos)
              log.info(s"${player.Name} sent to ${poi.zonename} by the admin API")
              respond(player.Name, poi.zonename, zone.id, gateId)
          }
          context.stop(self)
      }

    case default => log.error(s"Unexpected message $default")
  }

  private def respond(who: String, to: String, from: String, gate: Option[String]): Unit = {
    val data = Map[String, Any]()
    data("name") = who
    data("from") = from
    data("to") = to
    data("sanctuary") = toSanctuary
    gate.foreach(g => data("gate") = g)
    val how = if (toSanctuary) "recalled to sanctuary" else s"sent to $to"
    context.parent ! CommandGoodResponse(s"$who $how\n", data)
  }
}
