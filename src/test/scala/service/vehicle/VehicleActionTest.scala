// Copyright (c) 2026 PSForever
package service.vehicle

import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.objects.{GlobalDefinitions, Tool, Vehicle}
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.services.vehicle.VehicleAction
import net.psforever.types.{DriveState, PlanetSideGUID}
import org.specs2.mutable.Specification

object VehicleActionTest {
  assert(GlobalDefinitions.quadstealth != null, "missing definition - quadstealth does not exist")
  assert(GlobalDefinitions.ams_respawn_tube != null, "missing definition - ams_respawn_tube does not exist")
  assert(GlobalDefinitions.ams != null, "missing definition - AMS does not exist")

  final val testZone: Zone = {
    var i = 1
    val notAms = new Vehicle(GlobalDefinitions.quadstealth)
    notAms.GUID = PlanetSideGUID(i)
    notAms.Health = 1
    i += 1
    val ams = new Vehicle(GlobalDefinitions.ams)
    ams.Health = 1
    ams.GUID = PlanetSideGUID(i)
    i += 1
    ams.Utilities.values.foreach { utility =>
      utility.apply().GUID = PlanetSideGUID(i)
      i += 1
    }
    ams.DeploymentState = DriveState.Deployed
    new Zone(id = "test", new ZoneMap( name = "test"), zoneNumber = 1) {
      override def Vehicles: List[Vehicle] = List(notAms, ams)
    }
  }
}

class VehicleActionTest extends Specification {
  import VehicleActionTest._

  "EquipmentInSlot" should {
    assert(GlobalDefinitions.suppressor != null, "missing definition - Suppressor does not exist")

    "respond" in {
      var i = 1
      val obj = new Tool(GlobalDefinitions.suppressor)
      obj.GUID = PlanetSideGUID(i)
      i += 1
      obj.AmmoSlots.map(_.Box).foreach { box =>
        box.GUID = PlanetSideGUID(i)
        i += 1
      }
      val msg = VehicleAction.EquipmentInSlot(PlanetSideGUID(1), 2, obj)
      val definition = obj.Definition
      val objectData = definition.Packet.ConstructorData(obj).get
      msg.response() match {
        case VehicleAction.EquipmentCreatedInSlot(packets) =>
          packets match {
            case ObjectCreateMessage(_, oid, guid, pdata, data) =>
              oid mustEqual definition.ObjectId
              guid mustEqual obj.GUID
              pdata.contains(ObjectCreateMessageParent(PlanetSideGUID(1), 2)) mustEqual true
              data mustEqual objectData
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }
  }

  "UpdateAmsSpawnPoint" should {
    "respond" in {
      testZone.Vehicles.size mustEqual 2
      val msg = VehicleAction.UpdateAmsSpawnPoint(testZone)
      msg.response() match {
        case VehicleAction.UpdateAmsSpawnList(list) =>
          list.size mustEqual 1
          list.head.Definition mustEqual GlobalDefinitions.ams_respawn_tube
          list.head.Owner.Definition mustEqual GlobalDefinitions.ams
        case _ =>
          ko
      }
    }
  }

  "AMSDeploymentChange" should {
    "respond" in {
      testZone.Vehicles.size mustEqual 2
      val msg = VehicleAction.AMSDeploymentChange(testZone)
      msg.response() match {
        case VehicleAction.UpdateAmsSpawnList(list) =>
          list.size mustEqual 1
          list.head.Definition mustEqual GlobalDefinitions.ams_respawn_tube
          list.head.Owner.Definition mustEqual GlobalDefinitions.ams
        case _ =>
          ko
      }
    }
  }
}
