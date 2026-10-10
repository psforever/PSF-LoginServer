package net.psforever.actors.api

import akka.actor.typed.receptionist.Receptionist
import akka.actor.{Actor, ActorRef}
import net.psforever.objects.serverobject.structures.Building
import net.psforever.services.{InterstellarClusterService, ServiceManager}
import scala.collection.mutable.Map
import akka.actor.typed.scaladsl.adapter._

private case class ZoneLattice(zone_id: String, zone_number: Int, links: Array[Array[String]])

/**
  * Report the facility lattice of every loaded zone.
  *
  * The links are read from the zones the cluster is actually running, so a caller sees the topology
  * this server is enforcing rather than having to re-read `zonemaps/lattice.json` and assume the two
  * agree. Each link is a pair of building names as the zone knows them.
  */
class CmdListLattice(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

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
          // The static zonemap lattice: intra-zone links plus the static intercontinental warpgate
          // links (already qualified as "zoneId/name").
          val staticLinks = zone.map.latticeLink.toArray.map { case (a, b) => Array(a, b) }
          // The DYNAMIC intercontinental links -- the continent<->cavern geowarp ties the cavern
          // rotation wires at runtime -- live only in the runtime graph, not the static map lattice.
          // Include the graph's cross-zone edges so those ties show while a cavern is open (intra-zone
          // graph edges are already covered by staticLinks above, so they're skipped).
          def nameOf(b: Building): String = if (b.Zone eq zone) b.Name else s"${b.Zone.id}/${b.Name}"
          val dynamicLinks: Array[Array[String]] = zone.Lattice.edges.iterator
            .map(_.toOuter)
            .collect { case o if o._1.Zone ne o._2.Zone => Array(nameOf(o._1), nameOf(o._2)) }
            .toArray
          ZoneLattice(zone.id, zone.Number, staticLinks ++ dynamicLinks)
        }
        .sortBy(_.zone_number)

      val data      = Map[String, Any]()
      val linkCount = entries.map(_.links.length).sum
      data("zone_count") = entries.size
      data("link_count") = linkCount
      data("lattice")    = entries.toArray

      context.parent ! CommandGoodResponse(s"$linkCount lattice links across ${entries.size} zones\n", data)

    case default => log.error(s"Unexpected message $default")
  }
}
