package net.psforever.zones

import akka.actor.ActorContext
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.WarpGate
import net.psforever.objects.zones.Zone
import net.psforever.types.PlanetSideEmpire
import scala.concurrent.duration._
import scala.collection.immutable.HashMap
import scala.concurrent.Await

object Zones {
  val zones: HashMap[String, Zone] = HashMap(
    (
      "z1",
      new Zone("z1", Await.result(Maps.map01, 60 seconds), 1) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "z2",
      new Zone("z2", Await.result(Maps.map02, 60 seconds), 2) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "z3",
      new Zone("z3", Await.result(Maps.map03, 60 seconds), 3) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "z4",
      new Zone("z4", Await.result(Maps.map04, 60 seconds), 4) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "z5",
      new Zone("z5", Await.result(Maps.map05, 60 seconds), 5) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "z6",
      new Zone("z6", Await.result(Maps.map06, 60 seconds), 6) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "z7",
      new Zone("z7", Await.result(Maps.map07, 60 seconds), 7) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "z8",
      new Zone("z8", Await.result(Maps.map08, 60 seconds), 8) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "z9",
      new Zone("z9", Await.result(Maps.map09, 60 seconds), 9) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "z10",
      new Zone("z10", Await.result(Maps.map10, 60 seconds), 10) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "home1",
      new Zone("home1", Await.result(Maps.map11, 60 seconds), 11) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)

          import net.psforever.types.PlanetSideEmpire
          Buildings.values.foreach {
            _.Faction = PlanetSideEmpire.NC
          }

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "home2",
      new Zone("home2", Await.result(Maps.map12, 60 seconds), 12) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)

          import net.psforever.types.PlanetSideEmpire
          Buildings.values.foreach {
            _.Faction = PlanetSideEmpire.TR
          }

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "home3",
      new Zone("home3", Await.result(Maps.map13, 60 seconds), 13) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)

          import net.psforever.types.PlanetSideEmpire
          Buildings.values.foreach {
            _.Faction = PlanetSideEmpire.VS
          }

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "tzshtr",
      new Zone("tzshtr", Maps.map14, 14)
    ),
    (
      "tzdrtr",
      new Zone("tzsdrtr", Maps.map15, 15)
    ),
    (
      "tzcotr",
      new Zone("tzcotr", Maps.map16, 16)
    ),
    (
      "tzshnc",
      new Zone("tzshnc", Maps.map14, 17)
    ),
    (
      "tzdrnc",
      new Zone("tzdrnc", Maps.map15, 18)
    ),
    (
      "tzconc",
      new Zone("tzconc", Maps.map16, 19)
    ),
    (
      "tzshvs",
      new Zone("tzshvs", Maps.map14, 20)
    ),
    (
      "tzdrvs",
      new Zone("tzdrvs", Maps.map15, 21)
    ),
    (
      "tzcovs",
      new Zone("tzcovs", Maps.map16, 22)
    ),
    (
      "c1",
      new Zone("c1", Await.result(Maps.ugd01, 60 seconds), 23) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "c2",
      new Zone("c2", Await.result(Maps.ugd02, 60 seconds), 24) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "c3",
      new Zone("c3", Await.result(Maps.ugd03, 60 seconds), 25) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "c4",
      new Zone("c4", Await.result(Maps.ugd04, 60 seconds), 26) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "c5",
      new Zone("c5", Await.result(Maps.ugd05, 60 seconds), 27) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "c6",
      new Zone("c6", Await.result(Maps.ugd06, 60 seconds), 28) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "i1",
      new Zone("i1", Await.result(Maps.map99, 60 seconds), 29) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "i2",
      new Zone("i2", Await.result(Maps.map98, 60 seconds), 30) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "i3",
      new Zone("i3", Await.result(Maps.map97, 60 seconds), 31) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    ),
    (
      "i4",
      new Zone("i4", Await.result(Maps.map96, 60 seconds), 32) {
        override def init(implicit context: ActorContext): Unit = {
          super.init(context)
          HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(map.Scale, 80, 80)
          HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

          InitZoneAmenities(zone = this)
        }
      }
    )
  )

  def InitZoneAmenities(zone: Zone): Unit = {
    InitResourceSilos(zone)
    InitWarpGates(zone)

    def InitWarpGates(zone: Zone): Unit = {
      // todo: work out which faction owns links to this warpgate and if they should be marked as broadcast or not
      // todo: enable geowarps to go to the correct cave
      zone.Buildings.values.collect {
        case wg: WarpGate
            if wg.Definition == GlobalDefinitions.warpgate || wg.Definition == GlobalDefinitions.warpgate_small =>
          wg.Active = true
          wg.Faction = PlanetSideEmpire.NEUTRAL
          wg.Broadcast = true
        case geowarp: WarpGate
            if geowarp.Definition == GlobalDefinitions.warpgate_cavern || geowarp.Definition == GlobalDefinitions.hst =>
          geowarp.Faction = PlanetSideEmpire.NEUTRAL
          geowarp.Active = false
      }
    }

    def InitResourceSilos(zone: Zone): Unit = {
      // todo: load silo charge from database
      zone.Buildings.values.flatMap {
        _.Amenities.collect {
          case silo : ResourceSilo =>
            silo.Actor ! ResourceSilo.UpdateChargeLevel(silo.MaxNtuCapacitor)
        }
      }
    }
  }

  /**
    * Get the zone identifier name for the sanctuary continent of a given empire.
    * @param faction the empire
    * @return the zone id, with a blank string as an invalidating result
    */
  def SanctuaryZoneId(faction: PlanetSideEmpire.Value): String = {
    faction match {
      case PlanetSideEmpire.NC      => "home1"
      case PlanetSideEmpire.TR      => "home2"
      case PlanetSideEmpire.VS      => "home3"
      case PlanetSideEmpire.NEUTRAL => "" //invalid, not black ops
    }
  }

  /**
    * Get the zone number for the sanctuary continent of a given empire.
    * @param faction the empire
    * @return the zone number, within the sequence 1-32, and with 0 as an invalidating result
    */
  def SanctuaryZoneNumber(faction: PlanetSideEmpire.Value): Int = {
    faction match {
      case PlanetSideEmpire.NC      => 11
      case PlanetSideEmpire.TR      => 12
      case PlanetSideEmpire.VS      => 13
      case PlanetSideEmpire.NEUTRAL => 0 //invalid, not black ops
    }
  }

  /**
    * Given a zone identification string, provide that zone's ordinal number.
    * As zone identification naming is extremely formulaic,
    * just being able to poll the zone's identifier by its first few letters will produce its ordinal position.
    * @param id a zone id string
    * @return a zone number
    */
  def NumberFromId(id: String): Int = {
    if (id.startsWith("z")) { //z2 -> 2
      id.substring(1).toInt
    } else if (id.startsWith("home")) { //home2 -> 2 + 10 = 12
      id.substring(4).toInt + 10
    } else if (id.startsWith("tz")) { //tzconc -> (14 + (3 * 1) + 2) -> 19
      (List("tr", "nc", "vs").indexOf(id.substring(4)) * 3) + List("sh", "dr", "co").indexOf(id.substring(2, 4)) + 14
    } else if (id.startsWith("c")) { //c2 -> 2 + 21 = 23
      id.substring(1).toInt + 21
    } else if (id.startsWith("i")) { //i2 -> 2 + 28 = 30
      id.substring(1).toInt + 28
    } else {
      0
    }
  }

  object HotSpots {
    import net.psforever.objects.ballistics.SourceEntry
    import net.psforever.objects.zones.MapScale
    import net.psforever.types.Vector3

    import scala.concurrent.duration._

    /**
      * Produce hotspot coordinates based on map coordinates.
      * @see `FindClosestDivision`
      * @param scale the map's scale (width and height)
      * @param longDivNum the number of division lines spanning the width of the `scale`
      * @param latDivNum the number of division lines spanning the height of the `scale`
      * @param pos the absolute position of the activity reported
      * @return the position for a hotspot
      */
    def StandardRemapping(scale: MapScale, longDivNum: Int, latDivNum: Int)(pos: Vector3): Vector3 = {
      Vector3(
        //x
        FindClosestDivision(pos.x, scale.width, longDivNum.toFloat),
        //y
        FindClosestDivision(pos.y, scale.height, latDivNum.toFloat),
        //z is always zero - maps are flat 2D planes
        0
      )
    }

    /**
      * Produce hotspot coordinates based on map coordinates.<br>
      * <br>
      * Transform a reported number by mapping it
      * into a division from a regular pattern of divisions
      * defined by the scale divided evenly a certain number of times.
      * The depicted number of divisions is actually one less than the parameter number
      * as the first division is used to represent everything before that first division (there is no "zero").
      * Likewise, the last division occurs before the farther edge of the scale is counted
      * and is used to represent everything after that last division.
      * This is not unlike rounding.
      * @param coordinate the point to scale
      * @param scale the map's scale (width and height)
      * @param divisions the number of division lines spanning across the `scale`
      * @return the closest regular division
      */
    private def FindClosestDivision(coordinate: Float, scale: Float, divisions: Float): Float = {
      val divLength: Float = scale / divisions
      if (coordinate >= scale - divLength) {
        scale - divLength
      } else if (coordinate >= divLength) {
        val sector: Float     = (coordinate * divisions / scale).toInt * divLength
        val nextSector: Float = sector + divLength
        if (coordinate - sector < nextSector - coordinate) {
          sector
        } else {
          nextSector
        }
      } else {
        divLength
      }
    }

    /**
      * Determine a duration for which the hotspot will be displayed on the zone map.
      * Friendly fire is not recognized.
      * @param defender the defending party
      * @param attacker the attacking party
      * @return the duration
      */
    def StandardTimeRules(defender: SourceEntry, attacker: SourceEntry): FiniteDuration = {
      import net.psforever.objects.GlobalDefinitions
      import net.psforever.objects.ballistics._
      if (attacker.Faction == defender.Faction) {
        0 seconds
      } else {
        //TODO is target occupy-able and occupied, or jammer-able and jammered?
        defender match {
          case _: PlayerSource =>
            60 seconds
          case _: VehicleSource =>
            60 seconds
          case t: ObjectSource if t.Definition == GlobalDefinitions.manned_turret =>
            60 seconds
          case _: DeployableSource =>
            60 seconds
          case _: ComplexDeployableSource =>
            60 seconds
          case _ =>
            0 seconds
        }
      }
    }
  }
}
