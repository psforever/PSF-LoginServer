// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.{Actor, Props}
import net.psforever.objects.serverobject.structures.Building
import net.psforever.types.Vector3

import scala.annotation.tailrec
import scala.util.Random

/**
  * The root of the universe of one-continent planets, codified by the game's "Interstellar Map."
  * Constructs each zone and thus instigates the construction of every server object in the game world.
  * The nanite flow connecting all of these `Zone`s is called the "Intercontinental Lattice."<br>
  * <br>
  * The process of "construction" and "initialization" and "configuration" are referenced at this level.
  * These concepts are not the same thing;
  * the distinction is important.
  * "Construction" and "instantiation" of the cluster merely produces the "facade" of the different `Zone` entities.
  * In such a `List`, every built `Zone` is capable of being a destination on the "Intercontinental lattice."
  * "Initialization" and "configuration" of the cluster refers to the act of completing the "Intercontinental Lattice"
  * by connecting different terminus warp gates together.
  * Other activities involve event management and managing wide-reaching and factional attributes.
  * @param zones a `List` of continental `Zone` arenas
  */
class InterstellarCluster(zones : List[Zone]) extends Actor {
  private[this] val log = org.log4s.getLogger
  val recallRandom = new Random()
  log.info("Starting interplanetary cluster ...")

  /**
    * Create a `ZoneActor` for each `Zone`.
    * That `Actor` is sent a packet that would start the construction of the `Zone`'s server objects.
    * The process is maintained this way to allow every planet to be created and configured in separate stages.
    */
  override def preStart() : Unit = {
    super.preStart()
    for(zone <- zones) {
      log.info(s"Built continent ${zone.Id}")
      zone.Actor = context.actorOf(Props(classOf[ZoneActor], zone), s"${zone.Id}-actor")
      zone.Actor ! Zone.Init()
    }
  }

  def receive : Receive = {
    case InterstellarCluster.GetWorld(zoneId) =>
      log.info(s"Asked to find $zoneId")
      recursiveFindWorldInCluster(zones.iterator, _.Id == zoneId) match {
        case Some(continent) =>
          sender ! InterstellarCluster.GiveWorld(zoneId, continent)
        case None =>
          log.error(s"Requested zone $zoneId could not be found")
      }

    case InterstellarCluster.RequestClientInitialization() =>
      zones.foreach(zone => { sender ! Zone.ClientInitialization(zone.ClientInitialization()) })
      sender ! InterstellarCluster.ClientInitializationComplete() //will be processed after all Zones

    case msg @ Zone.Lattice.RequestSpawnPoint(zone_number, _, _, _) =>
      recursiveFindWorldInCluster(zones.iterator, _.Number == zone_number) match {
        case Some(zone) =>
          zone.Actor forward msg

        case None => //zone_number does not exist
          sender ! Zone.Lattice.NoValidSpawnPoint(zone_number, None)
      }

    case msg @ Zone.Lattice.RequestSpecificSpawnPoint(zone_number, _, _, _) =>
      recursiveFindWorldInCluster(zones.iterator, _.Number == zone_number) match {
        case Some(zone) =>
          zone.Actor forward msg

        case None => //zone_number does not exist
          sender ! Zone.Lattice.NoValidSpawnPoint(zone_number, None)
      }
    case InterstellarCluster.ZoneMapUpdate(zone_num: Int) =>
      val zone = zones.find(x => x.Number == zone_num).get
      zone.Buildings.values.foreach(b => b.Actor ! Building.SendMapUpdate(all_clients = true))


    case Zoning.InstantAction.Request(faction) =>
      val interests = zones.flatMap { zone =>
        //TODO zone.Locked.contains(faction)
        zone.HotSpotData
          .collect { case spot if zone.Players.nonEmpty => (zone, spot) }
      } /* ignore zones without existing population */
      if(interests.nonEmpty) {
        val (withAllies, onlyEnemies) = interests
          .map { case (zone, spot) =>
            (
              zone,
              spot,
              ZoneActor.FindLocalSpawnPointsInZone(zone, spot.DisplayLocation, faction, 0).getOrElse(Nil)
            )
          } /* pair hotspots and spawn points */
          .filter { case (_, _, spawns) => spawns.nonEmpty } /* faction spawns must exist */
          .sortBy({ case (_, spot, _) => spot.Activity.values.foldLeft(0)(_ + _.Heat) })(Ordering[Int].reverse) /* greatest > least */
          .partition { case (_, spot, _) => spot.ActivityBy().contains(faction) } /* us versus them */
        withAllies.headOption.orElse(onlyEnemies.headOption) match {
          case Some((zone, info, List(spawnPoint))) =>
            //one spawn
            val pos = info.DisplayLocation
            sender ! Zoning.InstantAction.Located(zone, pos, spawnPoint)
          case Some((zone, info, spawns)) =>
            //multiple spawn options
            val pos = info.DisplayLocation
            val spawnPoint = spawns.minBy(point => Vector3.DistanceSquared(point.Position, pos))
            sender ! Zoning.InstantAction.Located(zone, pos, spawnPoint)
          case None =>
            //no actionable hot spots
            sender ! Zoning.InstantAction.NotLocated()
        }
      }
      else {
        //never had any actionable hot spots
        sender ! Zoning.InstantAction.NotLocated()
      }

    case Zoning.Recall.Request(faction, sanctuary_id) =>
      recursiveFindWorldInCluster(zones.iterator, _.Id.equals(sanctuary_id)) match {
        case Some(zone) =>
          //TODO zone full
          val width = zone.Map.Scale.width
          val height = zone.Map.Scale.height
          //xy-coordinates indicate sanctuary spawn bias:
          val spot = math.abs(scala.util.Random.nextInt() % sender.toString.hashCode % 4) match {
            case 0 => Vector3(width, height, 0) //NE
            case 1 => Vector3(width, 0, 0) //SE
            case 2 => Vector3.Zero //SW
            case 3 => Vector3(0, height, 0) //NW
          }
          ZoneActor.FindLocalSpawnPointsInZone(zone, spot, faction, 7).getOrElse(Nil) match {
            case Nil =>
              //no spawns
              sender ! Zoning.Recall.Denied("unavailable")
            case List(spawnPoint) =>
              //one spawn
              sender ! Zoning.Recall.Located(zone, spawnPoint)
            case spawnPoints =>
              //multiple spawn options
              val spawnPoint = spawnPoints(recallRandom.nextInt(spawnPoints.length))
              sender ! Zoning.Recall.Located(zone, spawnPoint)
          }
        case None =>
          sender ! Zoning.Recall.Denied("unavailable")
      }

    case _ =>
      log.warn(s"InterstellarCluster received unknown message");
  }

  /**
    * Search through the `List` of `Zone` entities and find the one with the matching designation.
    * @param iter an `Iterator` of `Zone` entities
    * @param predicate a condition to check against to determine when the appropriate `Zone` is discovered
    * @return the discovered `Zone`
    */
  @tailrec private def recursiveFindWorldInCluster(iter : Iterator[Zone], predicate : Zone=>Boolean) : Option[Zone] = {
    if(!iter.hasNext) {
      None
    }
    else {
      val cont = iter.next
      if(predicate.apply(cont)) {
        Some(cont)
      }
      else {
        recursiveFindWorldInCluster(iter, predicate)
      }
    }
  }
}

object InterstellarCluster {

  /**
    * Request a hard reference to a `Zone`.
    * @param zoneId the name of the `Zone`
    */
  final case class GetWorld(zoneId : String)

  /**
    * Provide a hard reference to a `Zone`.
    * @param zoneId the name of the `Zone`
    * @param zone the `Zone`
    */
  final case class GiveWorld(zoneId : String, zone : Zone)

  /**
    * Signal to the cluster that a new client needs to be initialized for all listed `Zone` destinations.
    * @see `Zone`
    */
  final case class RequestClientInitialization()

  /**
    * Return signal intended to inform the original sender that all `Zone`s have finished being initialized.
    * @see `WorldSessionActor`
    */
  final case class ClientInitializationComplete()

  /**
    * Requests that all buildings within a zone send a map update for the purposes of refreshing lattice benefits, such as when a base is hacked, changes faction or loses power
    * @see `BuildingInfoUpdateMessage`
    * @param zone_num the zone number to request building map updates for
    */
  final case class ZoneMapUpdate(zone_num : Int)
}
