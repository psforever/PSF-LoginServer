// Copyright (c) 2017 PSForever
import akka.actor.ActorContext
import net.psforever.objects.zones.Zone

object Zones {
  val z1 = new Zone("z1", Maps.map1, 1)

  val z2 = new Zone("z2", Maps.map2, 2)

  val z3 = new Zone("z3", Maps.map3, 3)

  val z4 = new Zone("z4", Maps.map4, 4) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)

      import net.psforever.types.PlanetSideEmpire
      Base(21).get.Faction = PlanetSideEmpire.VS //Irkalla
      Base(30).get.Faction = PlanetSideEmpire.TR //Hanish
      Base(48).get.Faction = PlanetSideEmpire.NC //Girru
    }
  }

  val z5 = new Zone("z5", Maps.map5, 5)

  val z6 = new Zone("z6", Maps.map6, 6)

  val z7 = new Zone("z7", Maps.map7, 7)

  val z8 = new Zone("z8", Maps.map8, 8)

  val z9 = new Zone("z9", Maps.map9, 9)

  val z10 = new Zone("z10", Maps.map10, 10)

  val home1 = new Zone("home1", Maps.map11, 11)

  val home2 = new Zone("home2", Maps.map12, 12)

  val home3 = new Zone("home3", Maps.map13, 13) {
    override def Init(implicit context : ActorContext) : Unit = {
      super.Init(context)

      import net.psforever.types.PlanetSideEmpire
      Base(2).get.Faction = PlanetSideEmpire.VS //HART building C
      Base(29).get.Faction = PlanetSideEmpire.NC //South Villa Gun Tower
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

  val i1 = new Zone("i1", Maps.map96, 29)

  val i2 = new Zone("i2", Maps.map97, 30)

  val i3 = new Zone("i3", Maps.map98, 31)

  val i4 = new Zone("i4", Maps.map99, 32)
}
