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

      BuildingByMapId(1).get.asInstanceOf[WarpGate].BroadcastFor = PlanetSideEmpire.TR
      BuildingByMapId(2).get.asInstanceOf[WarpGate].BroadcastFor = PlanetSideEmpire.TR
      BuildingByMapId(3).get.asInstanceOf[WarpGate].BroadcastFor = PlanetSideEmpire.TR
      BuildingByMapId(4).get.asInstanceOf[WarpGate].BroadcastFor = PlanetSideEmpire.TR
    }
  }

  val z2 = new Zone("z2", Maps.map2, 2)

  val z3 = new Zone("z3", Maps.map3, 3)

  val z4 = new Zone("z4", Maps.map4, 4) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)

      Buildings.values.flatMap {
        _.Amenities.collect {
          case amenity if amenity.Definition == GlobalDefinitions.resource_silo =>
            val silo = amenity.asInstanceOf[ResourceSilo]
            silo.ChargeLevel = silo.MaximumCharge
        }
      }
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

  val z5 = new Zone("z5", Maps.map5, 5)

  val z6 = new Zone("z6", Maps.map6, 6) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)

      GUID(2094) match {
        case Some(silo : ResourceSilo) =>
          silo.ChargeLevel = silo.MaximumCharge
        case _ => ;
      }
      import net.psforever.types.PlanetSideEmpire
      BuildingByMapId(2).get.Faction = PlanetSideEmpire.VS
      BuildingByMapId(10).get.asInstanceOf[WarpGate].BroadcastFor = PlanetSideEmpire.VS
      BuildingByMapId(11).get.asInstanceOf[WarpGate].BroadcastFor = PlanetSideEmpire.VS
      BuildingByMapId(12).get.asInstanceOf[WarpGate].BroadcastFor = PlanetSideEmpire.VS
      BuildingByMapId(13).get.asInstanceOf[WarpGate].BroadcastFor = PlanetSideEmpire.VS
      BuildingByMapId(48).get.Faction = PlanetSideEmpire.VS
      BuildingByMapId(49).get.Faction = PlanetSideEmpire.VS
      BuildingByMapId(18657).get.asInstanceOf[WarpGate].Active = false
      BuildingByMapId(18658).get.asInstanceOf[WarpGate].Active = false
    }
  }

  val z7 = new Zone("z7", Maps.map7, 7)

  val z8 = new Zone("z8", Maps.map8, 8)

  val z9 = new Zone("z9", Maps.map9, 9)

  val z10 = new Zone("z10", Maps.map10, 10)

  val home1 = new Zone("home1", Maps.map11, 11){
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)

      import net.psforever.types.PlanetSideEmpire
      Buildings.values.foreach { _.Faction = PlanetSideEmpire.NC }
    }
  }

  val home2 = new Zone("home2", Maps.map12, 12){
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)

      import net.psforever.types.PlanetSideEmpire
      Buildings.values.foreach { _.Faction = PlanetSideEmpire.TR }
      BuildingByMapId(1).get.asInstanceOf[WarpGate].Broadcast = true
      BuildingByMapId(2).get.asInstanceOf[WarpGate].Broadcast = true
      BuildingByMapId(3).get.asInstanceOf[WarpGate].Broadcast = true
    }
  }

  val home3 = new Zone("home3", Maps.map13, 13) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)

      import net.psforever.types.PlanetSideEmpire
      Buildings.values.foreach { _.Faction = PlanetSideEmpire.VS }
      BuildingByMapId(60).get.Faction = PlanetSideEmpire.NC //South Villa Gun Tower
      BuildingByMapId(1).get.asInstanceOf[WarpGate].Broadcast = true
      BuildingByMapId(3).get.asInstanceOf[WarpGate].Broadcast = true
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

  val c1 = new Zone("c1", Maps.ugd01, 23)

  val c2 = new Zone("c2", Maps.ugd02, 24)

  val c3 = new Zone("c3", Maps.ugd03, 25)

  val c4 = new Zone("c4", Maps.ugd04, 26)

  val c5 = new Zone("c5", Maps.ugd05, 27)

  val c6 = new Zone("c6", Maps.ugd06, 28)

  val i1 = new Zone("i1", Maps.map99, 29)

  val i2 = new Zone("i2", Maps.map98, 30)

  val i3 = new Zone("i3", Maps.map97, 31)

  val i4 = new Zone("i4", Maps.map96, 32)

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
}
