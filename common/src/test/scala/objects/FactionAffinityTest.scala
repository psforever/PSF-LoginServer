// Copyright (c) 2017 PSForever
package objects

import akka.actor.{Actor, ActorSystem, Props}
import base.ActorTest
import net.psforever.objects.{GlobalDefinitions, Vehicle}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.zones.Zone
import net.psforever.types.PlanetSideEmpire
import org.specs2.mutable.Specification

import scala.concurrent.duration.Duration

class FactionAffinityTest extends Specification {
  "FactionAffinity" should {
    "construct (basic)" in {
      val obj = new FactionAffinity { def Faction = PlanetSideEmpire.TR }
      obj.Faction mustEqual PlanetSideEmpire.TR
    }

    "construct (part of)" in {
      val obj = new Door(GlobalDefinitions.door)
      obj.Faction mustEqual PlanetSideEmpire.NEUTRAL
    }

    "can not change affinity directly (basic)" in {
      val obj = new FactionAffinity { def Faction = PlanetSideEmpire.TR }
      (obj.Faction = PlanetSideEmpire.NC) mustEqual PlanetSideEmpire.TR
    }

    "can not change affinity directly (part of)" in {
      val obj = new Door(GlobalDefinitions.door)
      (obj.Faction = PlanetSideEmpire.TR) mustEqual PlanetSideEmpire.NEUTRAL
    }

    "inherits affinity from owner 1" in {
      val obj = new Door(GlobalDefinitions.door)
      obj.Owner.Faction mustEqual PlanetSideEmpire.NEUTRAL
      (obj.Faction = PlanetSideEmpire.TR) mustEqual PlanetSideEmpire.NEUTRAL
    }

    "inherits affinity from owner 2" in {
      val obj = new Door(GlobalDefinitions.door)
      val bldg = new Building("Building", building_guid = 0, map_id = 1, Zone.Nowhere, StructureType.Building, GlobalDefinitions.building)
      obj.Owner = bldg
      obj.Faction mustEqual PlanetSideEmpire.NEUTRAL

      bldg.Faction = PlanetSideEmpire.TR
      obj.Faction mustEqual PlanetSideEmpire.TR

      bldg.Faction = PlanetSideEmpire.NC
      obj.Faction mustEqual PlanetSideEmpire.NC
    }
  }
}

class FactionAffinity1Test extends ActorTest {
  "FactionAffinity" should {
    "assert affinity on confirm request" in {
      val obj = FactionAffinityTest.SetUpAgent
      obj.Faction = PlanetSideEmpire.VS //object is a type that can be changed directly
      assert(obj.Faction == PlanetSideEmpire.VS)

      obj.Actor ! FactionAffinity.ConfirmFactionAffinity()
      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[FactionAffinity.AssertFactionAffinity])
      assert(reply.asInstanceOf[FactionAffinity.AssertFactionAffinity].obj == obj)
      assert(reply.asInstanceOf[FactionAffinity.AssertFactionAffinity].faction == PlanetSideEmpire.VS)
    }
  }
}

class FactionAffinity2Test extends ActorTest {
  "FactionAffinity" should {
    "assert affinity on assert request" in {
      val obj = FactionAffinityTest.SetUpAgent
      obj.Faction = PlanetSideEmpire.VS //object is a type that can be changed directly
      assert(obj.Faction == PlanetSideEmpire.VS)

      obj.Actor ! FactionAffinity.AssertFactionAffinity(obj, PlanetSideEmpire.NEUTRAL)
      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[FactionAffinity.AssertFactionAffinity])
      assert(reply.asInstanceOf[FactionAffinity.AssertFactionAffinity].obj == obj)
      assert(reply.asInstanceOf[FactionAffinity.AssertFactionAffinity].faction == PlanetSideEmpire.VS)
    }
  }
}

class FactionAffinity3Test extends ActorTest {
  "FactionAffinity" should {
    "convert and assert affinity on convert request" in {
      val obj = FactionAffinityTest.SetUpAgent
      obj.Faction = PlanetSideEmpire.VS //object is a type that can be changed directly
      assert(obj.Faction == PlanetSideEmpire.VS)

      obj.Actor ! FactionAffinity.ConvertFactionAffinity(PlanetSideEmpire.TR)
      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[FactionAffinity.AssertFactionAffinity])
      assert(reply.asInstanceOf[FactionAffinity.AssertFactionAffinity].obj == obj)
      assert(reply.asInstanceOf[FactionAffinity.AssertFactionAffinity].faction == PlanetSideEmpire.TR)
      assert(obj.Faction == PlanetSideEmpire.TR)
    }
  }
}

object FactionAffinityTest {
  import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior

  private class AffinityControl(obj : FactionAffinity) extends Actor
    with FactionAffinityBehavior.Check
    with FactionAffinityBehavior.Convert {
    override def FactionObject = obj
    def receive = checkBehavior.orElse(convertBehavior).orElse { case _ => }
  }

  def SetUpAgent(implicit system : ActorSystem) = {
    val obj = new Vehicle(GlobalDefinitions.quadstealth)
    obj.Actor = system.actorOf(Props(classOf[FactionAffinityTest.AffinityControl], obj), "test")
    obj
  }

  def FreeFactionObject : FactionAffinity = new FactionAffinity() {
    private var faction : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
    def Faction : PlanetSideEmpire.Value = faction
    override def Faction_=(fac : PlanetSideEmpire.Value) : PlanetSideEmpire.Value = {
      faction = fac
      faction
    }
  }
}