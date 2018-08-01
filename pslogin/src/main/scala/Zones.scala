// Copyright (c) 2017 PSForever
import akka.actor.ActorContext
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.pad.process._
import net.psforever.objects.zones.Zone
import net.psforever.types.PlanetSideEmpire

object Zones {
  val z1 = new Zone("z1", Maps.map1, 1)

  val z2 = new Zone("z2", Maps.map2, 2)

  val z3 = new Zone("z3", Maps.map3, 3)

  val z4 = new Zone("z4", Maps.map4, 4) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)

      import net.psforever.types.PlanetSideEmpire
      Building(5).get.Faction = PlanetSideEmpire.NEUTRAL //Akkan
      Building(5).get.ModelId = 24
      //      Building(6).get.Faction = PlanetSideEmpire.TR //Baal
      //      Building(6).get.ModelId = 42
      //      Building(7).get.Faction = PlanetSideEmpire.TR //Dagon
      //      Building(7).get.ModelId = 27
      Building(9).get.Faction = PlanetSideEmpire.NC //Girru
      Building(9).get.ModelId = 48
      Building(10).get.Faction = PlanetSideEmpire.TR //Hanish
      Building(10).get.ModelId = 30
      Building(11).get.Faction = PlanetSideEmpire.VS //Irkalla
      Building(11).get.ModelId = 21
      ////      Building(13).get.Faction = PlanetSideEmpire.VS //Lahar
      Building(25).get.Faction = PlanetSideEmpire.VS //Gate Outpost Watch Tower (North of Forseral Warpgate)
      Building(25).get.ModelId = 74
      Building(33).get.Faction = PlanetSideEmpire.TR //East Girru Gun Tower
      Building(33).get.ModelId = 62
      Building(34).get.Faction = PlanetSideEmpire.TR //SE Hanish Gun Tower
      Building(34).get.ModelId = 60
      Building(35).get.Faction = PlanetSideEmpire.TR //Northeast Akkan Watch tower
      Building(35).get.ModelId = 69
      Building(36).get.Faction = PlanetSideEmpire.VS //West Girru Air Tower
      Building(36).get.ModelId = 83
      Building(55).get.Faction = PlanetSideEmpire.NC //South Irkalla Air Tower
      Building(55).get.ModelId = 86
      Building(56).get.Faction = PlanetSideEmpire.NC //Southwest Hanish Air Tower
      Building(56).get.ModelId = 82
      Building(59).get.Faction = PlanetSideEmpire.NC //Gate Outpost Watch Tower (South of Cyssor Warpgate)
      Building(59).get.ModelId = 73
      Building(65).get.Faction = PlanetSideEmpire.TR //West Hanish Gun Tower
      Building(65).get.ModelId = 56
    }
  }

  val z5 = new Zone("z5", Maps.map5, 5)

  val z6 = new Zone("z6", Maps.map6, 6) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)

      import net.psforever.types.PlanetSideEmpire
      Building(2).get.Faction = PlanetSideEmpire.VS
      Building(2).get.ModelId = 20
      Building(38).get.ModelId = 0
      Building(42).get.ModelId = 0
      Building(48).get.Faction = PlanetSideEmpire.VS
      Building(48).get.ModelId = 59
      Building(49).get.Faction = PlanetSideEmpire.VS
      Building(49).get.ModelId = 69
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
    }
  }

  val home3 = new Zone("home3", Maps.map13, 13) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)

      import net.psforever.types.PlanetSideEmpire
      Buildings.values.foreach { _.Faction = PlanetSideEmpire.VS }
      Building(29).get.Faction = PlanetSideEmpire.NC //South Villa Gun Tower
      GUID(293).get.asInstanceOf[VehicleSpawnPad].Railed = false //building 52
      GUID(706).get.asInstanceOf[VehicleSpawnPad].Guide = List(AutoDriveControls.DistanceFromHere(50f)) //building 77
      GUID(710).get.asInstanceOf[VehicleSpawnPad].Railed = false //building 79
      GUID(712).get.asInstanceOf[VehicleSpawnPad].Railed = false //building 81
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
}
