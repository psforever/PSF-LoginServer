// Copyright (c) 2017 PSForever
import akka.actor.ActorContext
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.WarpGate
import net.psforever.objects.zones.Zone
import net.psforever.types.PlanetSideEmpire


object Zones {
  val z1 = new Zone("z1", Maps.map1, 1) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)
    }
  }

  val z2 = new Zone("z2", Maps.map2, 2) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)
    }
  }

  val z3 = new Zone("z3", Maps.map3, 3) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)
    }
  }

  val z4 = new Zone("z4", Maps.map4, 4) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)

      BuildingByMapId(5).get.Faction = PlanetSideEmpire.TR //Akkan
      BuildingByMapId(6).get.Faction = PlanetSideEmpire.TR //Baal
      BuildingByMapId(7).get.Faction = PlanetSideEmpire.TR //Dagon
      BuildingByMapId(8).get.Faction = PlanetSideEmpire.NC //Enkidu
      BuildingByMapId(9).get.Faction = PlanetSideEmpire.VS //Girru
      BuildingByMapId(10).get.Faction = PlanetSideEmpire.VS //Hanish
      BuildingByMapId(11).get.Faction = PlanetSideEmpire.VS //Irkalla
      BuildingByMapId(12).get.Faction = PlanetSideEmpire.VS //Kusag
      BuildingByMapId(13).get.Faction = PlanetSideEmpire.VS //Lahar
      BuildingByMapId(14).get.Faction = PlanetSideEmpire.NC //Marduk
      BuildingByMapId(15).get.Faction = PlanetSideEmpire.NC //Neti
      BuildingByMapId(16).get.Faction = PlanetSideEmpire.NC //Zaqar
      BuildingByMapId(17).get.Faction = PlanetSideEmpire.NC //S_Marduk_Tower
      BuildingByMapId(18).get.Faction = PlanetSideEmpire.NC //W_Neti_Tower
      BuildingByMapId(19).get.Faction = PlanetSideEmpire.NC //W_Zaqar_Tower
      BuildingByMapId(20).get.Faction = PlanetSideEmpire.NC //E_Zaqar_Tower
      BuildingByMapId(21).get.Faction = PlanetSideEmpire.NC //NE_Neti_Tower
      BuildingByMapId(22).get.Faction = PlanetSideEmpire.NC //SE_Ceryshen_Warpgate_Tower
      BuildingByMapId(23).get.Faction = PlanetSideEmpire.VS //S_Kusag_Tower
      BuildingByMapId(24).get.Faction = PlanetSideEmpire.VS //NW_Kusag_Tower
      BuildingByMapId(25).get.Faction = PlanetSideEmpire.VS //N_Ceryshen_Warpgate_Tower
      BuildingByMapId(26).get.Faction = PlanetSideEmpire.VS //SE_Irkalla_Tower
      BuildingByMapId(27).get.Faction = PlanetSideEmpire.VS //S_Irkalla_Tower
      BuildingByMapId(28).get.Faction = PlanetSideEmpire.TR //NE_Enkidu_Tower
      BuildingByMapId(29).get.Faction = PlanetSideEmpire.NC //SE_Akkan_Tower
      BuildingByMapId(30).get.Faction = PlanetSideEmpire.NC //SW_Enkidu_Tower
      BuildingByMapId(31).get.Faction = PlanetSideEmpire.TR //E_Searhus_Warpgate_Tower
      BuildingByMapId(32).get.Faction = PlanetSideEmpire.TR //N_Searhus_Warpgate_Tower
      BuildingByMapId(33).get.Faction = PlanetSideEmpire.VS //E_Girru_Tower
      BuildingByMapId(34).get.Faction = PlanetSideEmpire.VS //SE_Hanish_Tower
      BuildingByMapId(35).get.Faction = PlanetSideEmpire.TR //SW_Hanish_Tower
      BuildingByMapId(36).get.Faction = PlanetSideEmpire.VS //W_Girru_Tower
      BuildingByMapId(37).get.Faction = PlanetSideEmpire.TR //E_Dagon_Tower
      BuildingByMapId(38).get.Faction = PlanetSideEmpire.TR //NE_Baal_Tower
      BuildingByMapId(39).get.Faction = PlanetSideEmpire.TR //SE_Baal_Tower
      BuildingByMapId(40).get.Faction = PlanetSideEmpire.TR //S_Dagon_Tower
      BuildingByMapId(41).get.Faction = PlanetSideEmpire.NC //W_Ceryshen_Warpgate_Tower
      BuildingByMapId(42).get.Faction = PlanetSideEmpire.NEUTRAL //dagon bunker
      BuildingByMapId(43).get.Faction = PlanetSideEmpire.NEUTRAL //Akkan North Bunker
      BuildingByMapId(44).get.Faction = PlanetSideEmpire.NEUTRAL //Enkidu East Bunker
      BuildingByMapId(45).get.Faction = PlanetSideEmpire.NEUTRAL //Neti bunker
      BuildingByMapId(46).get.Faction = PlanetSideEmpire.NEUTRAL //Hanish West Bunker
      BuildingByMapId(47).get.Faction = PlanetSideEmpire.NEUTRAL //Irkalla East Bunker
      BuildingByMapId(48).get.Faction = PlanetSideEmpire.NEUTRAL //Zaqar bunker
      BuildingByMapId(49).get.Faction = PlanetSideEmpire.NEUTRAL //Kusag West Bunker
      BuildingByMapId(50).get.Faction = PlanetSideEmpire.NEUTRAL //marduk bunker
      BuildingByMapId(51).get.Faction = PlanetSideEmpire.TR //baal bunker
      BuildingByMapId(52).get.Faction = PlanetSideEmpire.NEUTRAL //girru bunker
      BuildingByMapId(53).get.Faction = PlanetSideEmpire.NEUTRAL //lahar bunker
      BuildingByMapId(54).get.Faction = PlanetSideEmpire.NEUTRAL //akkan bunker
      BuildingByMapId(55).get.Faction = PlanetSideEmpire.VS //Irkalla_Tower
      BuildingByMapId(56).get.Faction = PlanetSideEmpire.VS //Hanish_Tower
      BuildingByMapId(57).get.Faction = PlanetSideEmpire.VS //E_Ceryshen_Warpgate_Tower
      BuildingByMapId(58).get.Faction = PlanetSideEmpire.VS //Lahar_Tower
      BuildingByMapId(59).get.Faction = PlanetSideEmpire.VS //VSSanc_Warpgate_Tower
      BuildingByMapId(60).get.Faction = PlanetSideEmpire.TR //Akkan_Tower
      BuildingByMapId(61).get.Faction = PlanetSideEmpire.NC //TRSanc_Warpgate_Tower
      BuildingByMapId(62).get.Faction = PlanetSideEmpire.NC //Marduk_Tower
      BuildingByMapId(63).get.Faction = PlanetSideEmpire.TR //NW_Dagon_Tower
      BuildingByMapId(64).get.Faction = PlanetSideEmpire.NEUTRAL //E7 East Bunker (at north from bridge)
      BuildingByMapId(65).get.Faction = PlanetSideEmpire.VS //W_Hanish_Tower
    }
  }

  val z5 = new Zone("z5", Maps.map5, 5) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)
    }
  }

  val z6 = new Zone("z6", Maps.map6, 6) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      import net.psforever.types.PlanetSideEmpire
      BuildingByMapId(2).get.Faction = PlanetSideEmpire.VS
      BuildingByMapId(48).get.Faction = PlanetSideEmpire.VS
      BuildingByMapId(49).get.Faction = PlanetSideEmpire.VS

      InitZoneAmenities(zone = this)
    }
  }

  val z7 = new Zone("z7", Maps.map7, 7) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)
    }
  }

  val z8 = new Zone("z8", Maps.map8, 8) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)
    }
  }

  val z9 = new Zone("z9", Maps.map9, 9) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)
    }
  }

  val z10 = new Zone("z10", Maps.map10, 10) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)
    }
  }

  val home1 = new Zone("home1", Maps.map11, 11){
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)

      import net.psforever.types.PlanetSideEmpire
      Buildings.values.foreach { _.Faction = PlanetSideEmpire.NC }

      InitZoneAmenities(zone = this)
    }
  }

  val home2 = new Zone("home2", Maps.map12, 12){
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)

      import net.psforever.types.PlanetSideEmpire
      Buildings.values.foreach { _.Faction = PlanetSideEmpire.TR }

      InitZoneAmenities(zone = this)
    }
  }

  val home3 = new Zone("home3", Maps.map13, 13) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)

      import net.psforever.types.PlanetSideEmpire
      Buildings.values.foreach { _.Faction = PlanetSideEmpire.VS }

      InitZoneAmenities(zone = this)
    }
  }

  val tzshtr = new Zone("tzshtr", Maps.map14, 14)

  val tzdrtr = new Zone("tzsdrtr", Maps.map15, 15)

  val tzcotr = new Zone("tzcotr", Maps.map16, 16)

  val tzshnc = new Zone("tzshnc", Maps.map14, 17)

  val tzdrnc = new Zone("tzdrnc", Maps.map15, 18)

  val tzconc = new Zone("tzconc", Maps.map16, 19)

  val tzshvs = new Zone("tzshvs", Maps.map14, 20)

  val tzdrvs = new Zone("tzdrvs", Maps.map15, 21)

  val tzcovs = new Zone("tzcovs", Maps.map16, 22)

  val c1 = new Zone("c1", Maps.ugd01, 23) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)
    }
  }

  val c2 = new Zone("c2", Maps.ugd02, 24) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)
    }
  }

  val c3 = new Zone("c3", Maps.ugd03, 25) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)
    }
  }

  val c4 = new Zone("c4", Maps.ugd04, 26) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)
    }
  }

  val c5 = new Zone("c5", Maps.ugd05, 27) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)
    }
  }

  val c6 = new Zone("c6", Maps.ugd06, 28) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)
    }
  }

  val i1 = new Zone("i1", Maps.map99, 29) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)
    }
  }

  val i2 = new Zone("i2", Maps.map98, 30) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)
    }
  }

  val i3 = new Zone("i3", Maps.map97, 31) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)
    }
  }

  val i4 = new Zone("i4", Maps.map96, 32) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)
      HotSpotCoordinateFunction = Zones.HotSpots.StandardRemapping(Map.Scale, 80, 80)
      HotSpotTimeFunction = Zones.HotSpots.StandardTimeRules

      InitZoneAmenities(zone = this)
    }
  }

  def InitZoneAmenities(zone: Zone): Unit = {
    InitResourceSilos(zone)
    InitWarpGates(zone)

    def InitWarpGates(zone: Zone): Unit = {
      // todo: work out which faction owns links to this warpgate and if they should be marked as broadcast or not
      // todo: enable geowarps to go to the correct cave
      zone.Buildings.values.collect {
        case wg : WarpGate if wg.Definition == GlobalDefinitions.warpgate || wg.Definition == GlobalDefinitions.warpgate_small =>
          wg.Active = true
          wg.Faction = PlanetSideEmpire.NEUTRAL
          wg.Broadcast = true
        case geowarp : WarpGate if geowarp.Definition == GlobalDefinitions.warpgate_cavern || geowarp.Definition == GlobalDefinitions.hst =>
          geowarp.Faction = PlanetSideEmpire.NEUTRAL
          geowarp.Active = false
      }
  }

    def InitResourceSilos(zone: Zone): Unit = {
      // todo: load silo charge from database
      zone.Buildings.values.flatMap {
        _.Amenities.collect {
          case silo : ResourceSilo =>
            silo.Actor ! ResourceSilo.UpdateChargeLevel(silo.MaximumCharge)
        }
      }
    }
  }

  /**
    * Get the zone identifier name for the sanctuary continent of a given empire.
    * @param faction the empire
    * @return the zone id, with a blank string as an invalidating result
    */
  def SanctuaryZoneId(faction : PlanetSideEmpire.Value) : String = {
    faction match {
      case PlanetSideEmpire.NC => "home1"
      case PlanetSideEmpire.TR => "home2"
      case PlanetSideEmpire.VS => "home3"
      case PlanetSideEmpire.NEUTRAL => "" //invalid, not black ops
    }
  }

  /**
    * Get the zone number for the sanctuary continent of a given empire.
    * @param faction the empire
    * @return the zone number, within the sequence 1-32, and with 0 as an invalidating result
    */
  def SanctuaryZoneNumber(faction : PlanetSideEmpire.Value) : Int = {
    faction match {
      case PlanetSideEmpire.NC => 11
      case PlanetSideEmpire.TR => 12
      case PlanetSideEmpire.VS => 13
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
  def NumberFromId(id : String) : Int = {
    if(id.startsWith("z")) { //z2 -> 2
      id.substring(1).toInt
    }
    else if(id.startsWith("home")) { //home2 -> 2 + 10 = 12
      id.substring(4).toInt + 10
    }
    else if(id.startsWith("tz")) { //tzconc -> (14 + (3 * 1) + 2) -> 19
      (List("tr", "nc", "vs").indexOf(id.substring(4)) * 3) + List("sh", "dr", "co").indexOf(id.substring(2, 4)) + 14
    }
    else if(id.startsWith("c")) { //c2 -> 2 + 21 = 23
      id.substring(1).toInt + 21
    }
    else if(id.startsWith("i")) { //i2 -> 2 + 28 = 30
      id.substring(1).toInt + 28
    }
    else {
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
    def StandardRemapping(scale : MapScale, longDivNum : Int, latDivNum : Int)(pos : Vector3) : Vector3 = {
      Vector3(
        //x
        FindClosestDivision(pos.x, scale.width, longDivNum),
        //y
        FindClosestDivision(pos.y, scale.height, latDivNum),
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
    private def FindClosestDivision(coordinate : Float, scale : Float, divisions : Float) : Float = {
      val divLength : Float = scale / divisions
      if(coordinate >= scale - divLength) {
        scale - divLength
      }
      else if(coordinate >= divLength) {
        val sector : Float = (coordinate * divisions / scale).toInt * divLength
        val nextSector : Float = sector + divLength
        if(coordinate - sector < nextSector - coordinate) {
          sector
        }
        else {
          nextSector
        }
      }
      else {
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
    def StandardTimeRules(defender : SourceEntry, attacker : SourceEntry) : FiniteDuration = {
      import net.psforever.objects.ballistics._
      import net.psforever.objects.GlobalDefinitions
      if(attacker.Faction == defender.Faction) {
        0 seconds
      }
      else {
        //TODO is target occupy-able and occupied, or jammer-able and jammered?
        defender match {
          case _ : PlayerSource =>
            60 seconds
          case _ : VehicleSource =>
            60 seconds
          case t : ObjectSource if t.Definition == GlobalDefinitions.manned_turret =>
            60 seconds
          case _ : DeployableSource =>
            30 seconds
          case _ : ComplexDeployableSource =>
            30 seconds
          case _ =>
            0 seconds
        }
      }
    }
  }
}
