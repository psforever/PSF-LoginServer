// Copyright (c) 2017 PSForever
package objects

import akka.actor.ActorRef
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.LimitedNumberSource
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.objects.{GlobalDefinitions, Vehicle}
import org.specs2.mutable.Specification

class ZoneTest extends Specification {
  "ZoneMap" should {
    //TODO these are temporary tests as the current ZoneMap is a kludge
    "construct" in {
      new ZoneMap("map13")
      ok
    }

    "references bases by a positive building id (defaults to 0)" in {
      val map = new ZoneMap("map13")
      map.LocalBases mustEqual 0
      map.LocalBases = 10
      map.LocalBases mustEqual 10
      map.LocalBases = -1
      map.LocalBases mustEqual 10
    }

    "associates objects to bases (doesn't check numbers)" in {
      val map = new ZoneMap("map13")
      map.ObjectToBase mustEqual Nil
      map.ObjectToBase(1, 2)
      map.ObjectToBase mustEqual List((1, 2))
      map.ObjectToBase(3, 4)
      map.ObjectToBase mustEqual List((1, 2), (3, 4))
    }

    "associates doors to door locks (doesn't check numbers)" in {
      val map = new ZoneMap("map13")
      map.DoorToLock mustEqual Map.empty
      map.DoorToLock(1, 2)
      map.DoorToLock mustEqual Map(1 -> 2)
      map.DoorToLock(3, 4)
      map.DoorToLock mustEqual Map(1 -> 2, 3 -> 4)
    }
  }

  val map13 = new ZoneMap("map13")
  map13.LocalBases = 10
  class TestObject extends IdentifiableEntity

  "Zone" should {
    //TODO these are temporary tests as the current Zone is a kludge
    "construct" in {
      val zone = new Zone("home3", map13, 13)
      zone.GUID mustEqual ActorRef.noSender
      zone.Ground mustEqual ActorRef.noSender
      zone.Transport mustEqual ActorRef.noSender
      //zone also has a unique identifier system but it can't be accessed without its the Actor GUID being initialized
      zone.EquipmentOnGround mustEqual List.empty[Equipment]
      zone.Vehicles mustEqual List.empty[Vehicle]
    }

    "can have its unique identifier system changed if no objects were added to it" in {
      val zone = new Zone("home3", map13, 13)
      val guid1 : NumberPoolHub = new NumberPoolHub(new LimitedNumberSource(100))
      guid1.AddPool("pool1", (0 to 50).toList)
      guid1.AddPool("pool2", (51 to 75).toList)
      zone.GUID(guid1) mustEqual true

      val obj = new TestObject()
      guid1.register(obj, "pool2").isSuccess mustEqual true
      guid1.WhichPool(obj) mustEqual Some("pool2")

      val guid2 : NumberPoolHub = new NumberPoolHub(new LimitedNumberSource(150))
      guid2.AddPool("pool3", (0 to 50).toList)
      guid2.AddPool("pool4", (51 to 75).toList)
      zone.GUID(guid2) mustEqual false
    }

    "can keep track of Vehicles" in {
      val zone = new Zone("home3", map13, 13)
      val fury = Vehicle(GlobalDefinitions.fury)
      zone.Vehicles mustEqual List()
      zone.AddVehicle(fury)
      zone.Vehicles mustEqual List(fury)
    }

    "can forget specific vehicles" in {
      val zone = new Zone("home3", map13, 13)
      val fury = Vehicle(GlobalDefinitions.fury)
      val wraith = Vehicle(GlobalDefinitions.quadstealth)
      val basilisk = Vehicle(GlobalDefinitions.quadassault)
      zone.AddVehicle(wraith)
      zone.AddVehicle(fury)
      zone.AddVehicle(basilisk)
      zone.Vehicles mustEqual List(wraith, fury, basilisk)

      zone.RemoveVehicle(fury)
      zone.Vehicles mustEqual List(wraith, basilisk)
    }
  }
}
