package net.psforever.actors.api

import akka.actor.typed.receptionist.Receptionist
import akka.actor.{Actor, ActorRef}
import net.psforever.services.{InterstellarClusterService, ServiceManager}
import net.psforever.types.PlanetSideEmpire
import scala.collection.mutable.Map
import akka.actor.typed.scaladsl.adapter._

private case class ZoneState(
    zone_id: String,
    zone_number: Int,
    locked_by: Option[Int],
    benefit_recipient: Option[Int],
    /** Online player count by faction id: index 0 TR / 1 NC / 2 VS / 3 Black Ops (NEUTRAL). */
    players: Array[Int],
    /** Number of active combat hotspots currently displayed on this zone. */
    hotspots: Int,
    /** World-coordinate [x, y] of each active hotspot, for placement on the zone map. */
    hotspot_points: Array[Array[Float]],
    /** Per live player: position + facing + faction + name. Admin-only (never exposed publicly). */
    player_markers: Array[PlayerMarker]
)

/** A single online player's map marker: world x/y, facing yaw (degrees), faction id, character name. */
private case class PlayerMarker(x: Float, y: Float, facing: Float, faction: Int, name: String)

/**
  * Report per-zone continental state: which empire, if any, currently holds the zone locked.
  *
  * Read from the live zones rather than inferred from base ownership, so a caller sees the lock the
  * server is actually broadcasting (`ContinentalLockUpdateMessage`). `locked_by` is a faction id
  * (0 TR / 1 NC / 2 VS) or null when the zone is unlocked -- the server models "unlocked" as the
  * NEUTRAL empire, which is not a real faction and would be misleading to report as one.
  */
class CmdListZones(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  private def factionId(empire: PlanetSideEmpire.Value): Option[Int] =
    if (empire == PlanetSideEmpire.NEUTRAL) None else Some(empire.id)

  override def preStart() = {
    ServiceManager.receptionist ! Receptionist.Find(
      InterstellarClusterService.InterstellarClusterServiceKey,
      context.self
    )
  }

  override def receive = {
    case InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings) =>
      listings.head ! InterstellarClusterService.FilterZones(_ => true, context.self)

    case InterstellarClusterService.ZonesResponse(zones) =>
      val entries = zones.toSeq
        .map { zone =>
          val pop = Array(0, 0, 0, 0)
          zone.Players.foreach { a =>
            val f = a.faction.id
            if (f >= 0 && f < 4) pop(f) += 1
          }
          val hotspots = zone.HotSpots
          val markers = zone.LivePlayers.map { p =>
            PlayerMarker(p.Position.x, p.Position.y, p.Orientation.z, p.Faction.id, p.Name)
          }.toArray
          ZoneState(
            zone.id,
            zone.Number,
            factionId(zone.lockedBy),
            factionId(zone.benefitRecipient),
            pop,
            hotspots.size,
            hotspots.map(h => Array(h.DisplayLocation.x, h.DisplayLocation.y)).toArray,
            markers
          )
        }
        .sortBy(_.zone_number)

      val locked = entries.count(_.locked_by.nonEmpty)
      val data   = Map[String, Any]()
      data("zone_count")   = entries.size
      data("locked_count") = locked
      data("zones")        = entries.toArray

      context.parent ! CommandGoodResponse(s"$locked of ${entries.size} zones locked\n", data)

    case default => log.error(s"Unexpected message $default")
  }
}
