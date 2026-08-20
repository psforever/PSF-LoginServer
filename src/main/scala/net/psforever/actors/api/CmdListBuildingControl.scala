package net.psforever.actors.api

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{Actor, ActorRef}
import net.psforever.services.{InterstellarClusterService, ServiceManager}
import net.psforever.types.PlanetSideEmpire

import scala.collection.mutable.Map

/**
  * Who currently holds each capturable facility, read from the LIVE zones.
  *
  * The portal used to answer this by selecting `faction_id` from the `building` table. That was wrong
  * in a way that showed: `BuildingActor.SetFaction` updates the live entity and the database, and the
  * database write is asynchronous -- which is exactly why `CmdSetBuildingFaction` waits before
  * recomputing the continental lock. A map drawn from the table could therefore paint a base for the
  * empire that held it a moment ago, while the world had already changed hands. Reading the zone the
  * server is actually enforcing removes the lag and the second source of truth at once.
  *
  * Only buildings with a capture terminal are reported: everything else (warpgates, bunkers, the
  * cavern furniture) has a faction field that never means anything to a viewer, and including them
  * only invites a caller to colour something that cannot be owned.
  *
  * args: `[<zoneId>]` -- one zone, or every loaded zone when omitted.
  *
  * Response `data`: `{ "zones": { "<zoneId>": { "<localId>": <factionId 0-3>, ... }, ... } }`,
  * keyed by string because that is what JSON gives back on the other side anyway.
  */
class CmdListBuildingControl(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  private val zoneFilter: Option[String] = args.headOption.filter(_.nonEmpty)

  override def preStart(): Unit = {
    ServiceManager.receptionist ! Receptionist.Find(
      InterstellarClusterService.InterstellarClusterServiceKey,
      context.self
    )
  }

  override def receive: Receive = {
    case InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings) =>
      listings.head ! InterstellarClusterService.FilterZones(
        z => zoneFilter.forall(_.equalsIgnoreCase(z.id)),
        context.self
      )

    case InterstellarClusterService.ZonesResponse(zones) =>
      if (zones.isEmpty && zoneFilter.nonEmpty) {
        context.parent ! CommandErrorResponse(s"no loaded zone with id '${zoneFilter.get}'\n", Map[String, Any]())
      } else {
        val byZone = zones.toSeq.map { zone =>
          val control = zone.Buildings.values
            .filter(_.CaptureTerminal.isDefined)
            .map(b => b.MapId.toString -> factionId(b.Faction))
            .toMap
          zone.id -> control
        }.toMap

        val data = Map[String, Any]()
        data("zones") = byZone
        context.parent ! CommandGoodResponse(s"control state for ${byZone.size} zone(s)\n", data)
      }
      context.stop(self)

    case default => log.error(s"Unexpected message $default")
  }

  /**
    * Faction as the portal's maps expect it.
    *
    * NEUTRAL is a real value in-world for an unowned base, and the portal already draws faction 3 as
    * unclaimed, so it is reported as 3 rather than dropped -- unlike a zone lock, where "neutral"
    * means "not locked at all" and reporting a faction would be a lie.
    */
  private def factionId(empire: PlanetSideEmpire.Value): Int = empire.id
}
