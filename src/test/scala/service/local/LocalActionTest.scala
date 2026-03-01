// Copyright (c) 2026 PSForever
package service.local

import net.psforever.objects.{ExplosiveDeployable, GlobalDefinitions}
import net.psforever.packet.game.{ObjectCreateMessage, TriggeredEffect, TriggeredEffectLocation}
import net.psforever.services.Service
import net.psforever.services.base.message.SendResponse
import net.psforever.services.local.LocalAction
import net.psforever.types.{PlanetSideGUID, Vector3}
import org.specs2.mutable.Specification

class LocalActionTest extends Specification {
  "DeployItem" should {
    assert(GlobalDefinitions.boomer != null, "missing definition - Boomer does not exist")

    "respond" in {
      val obj = new ExplosiveDeployable(GlobalDefinitions.boomer)
      obj.GUID = PlanetSideGUID(1)
      val msg = LocalAction.DeployItem(obj)
      val definition = obj.Definition
      val objectData = definition.Packet.ConstructorData(obj).get
      msg.response() match {
        case SendResponse(packets) =>
          packets.head match {
            case ObjectCreateMessage(_, oid, guid, _, data) =>
              oid mustEqual definition.ObjectId
              guid mustEqual obj.GUID
              data mustEqual objectData
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }
  }

  "TriggerEffect" should {
    "respond" in {
      val msg = LocalAction.TriggerEffect("on", PlanetSideGUID(1))
      msg.response() match {
        case LocalAction.TriggerEffectAtLocation(PlanetSideGUID(1), "on", None, None) =>
          ok
        case _ =>
          ko
      }
    }
  }

  "TriggerEffectInfo" should {
    "respond" in {
      val msg = LocalAction.TriggerEffectInfo(PlanetSideGUID(1), "on", unk1 = true, unk2 = 10L)
      msg.response() match {
        case LocalAction.TriggerEffectAtLocation(PlanetSideGUID(1), "on", Some(TriggeredEffect(true, 10L)), None) =>
          ok
        case _ =>
          ko
      }
    }
  }

  "TriggerEffectLocation" should {
    "respond" in {
      val msg = LocalAction.TriggerEffectLocation("on", Vector3(1, 2, 3), Vector3(4, 5, 6))
      msg.response() match {
        case LocalAction.TriggerEffectAtLocation(Service.defaultPlayerGUID, "on", None, Some(TriggeredEffectLocation(Vector3(1, 2, 3), Vector3(4, 5, 6)))) =>
          ok
        case _ =>
          ko
      }
    }
  }
}
