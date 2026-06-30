// Copyright (c) 2026 PSForever
package service.avatar

import net.psforever.objects.avatar.Avatar
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.objects.{GlobalDefinitions, Player, Tool}
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.packet.game.objectcreate.{BasicCharacterData, DroppedItemData, ObjectCreateMessageParent, PlacementData}
import net.psforever.services.avatar.AvatarAction
import net.psforever.services.base.message.ObjectDelete
import net.psforever.types._
import org.specs2.mutable.Specification

object AvatarActionTest {
  assert(GlobalDefinitions.suppressor != null, "missing definition - suppressor does not exist")
  assert(GlobalDefinitions.avatar != null, "missing definition - avatar does not exist")

  private var i: Int = 1
  val testSuppressor: Tool = {
    val obj = new Tool(GlobalDefinitions.suppressor)
    obj.Position = Vector3(1, 2, 3)
    obj.Orientation = Vector3(4, 5, 6)
    obj.GUID = PlanetSideGUID(i)
    i += 1
    obj.AmmoSlots.map(_.Box).foreach { box =>
      box.GUID = PlanetSideGUID(i)
      i += 1
    }
    obj
  }

  val testPlayer: Player = {
    val avatar = new Avatar(id = 1, BasicCharacterData("testPlayer", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
    avatar.locker.GUID = PlanetSideGUID(i)
    i += 1
    val obj = new Player(avatar)
    obj.Position = Vector3(1, 2, 3)
    obj.Orientation = Vector3(4, 5, 6)
    obj.GUID = PlanetSideGUID(i)
    obj.Spawn()
    i += 1
    obj
  }
}

class AvatarActionTest extends Specification {
  import AvatarActionTest._

  "DropItem" should {
    "respond" in {
      val obj = testSuppressor
      val msg = AvatarAction.DropItem(obj)
      val definition = obj.Definition
      val objectData = DroppedItemData(
        PlacementData(obj.Position, obj.Orientation),
        definition.Packet.ConstructorData(obj).get
      )
      msg.response() match {
        case AvatarAction.DropCreatedItem(pkt) =>
          pkt match {
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

  "EquipmentInHand" should {
    "respond" in {
      val obj = testSuppressor
      val msg = AvatarAction.EquipmentInHand(PlanetSideGUID(100), 2, obj)
      val definition = obj.Definition
      val objectData = definition.Packet.ConstructorData(obj).get
      msg.response() match {
        case AvatarAction.EquipmentCreatedInHand(pkt) =>
          pkt match {
            case ObjectCreateMessage(_, oid, guid, pdata, data) =>
              oid mustEqual definition.ObjectId
              guid mustEqual obj.GUID
              pdata.contains(ObjectCreateMessageParent(PlanetSideGUID(100), 2)) mustEqual true
              data mustEqual objectData
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }
  }

  "LoadPlayer (1)" should {
    "respond" in {
      val obj = testPlayer
      val definition = obj.Definition
      val id = definition.ObjectId
      val guid = obj.GUID
      val objectData = definition.Packet.ConstructorData(obj).get
      val msg = AvatarAction.LoadPlayer(id, guid, objectData, None)
      msg.response() match {
        case AvatarAction.LoadCreatedPlayer(pkt) =>
          pkt match {
            case ObjectCreateMessage(_, oid, guid, _, data) =>
              oid mustEqual id
              guid mustEqual guid
              data mustEqual objectData
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }
  }

  "LoadPlayer (2)" should {
    "respond" in {
      val obj = testPlayer
      val definition = obj.Definition
      val id = definition.ObjectId
      val guid = obj.GUID
      val parentData = ObjectCreateMessageParent(PlanetSideGUID(100), 2)
      val objectData = definition.Packet.ConstructorData(obj).get
      val msg = AvatarAction.LoadPlayer(id, guid, objectData, Some(parentData))
      msg.response() match {
        case AvatarAction.LoadCreatedPlayer(pkt) =>
          pkt match {
            case ObjectCreateMessage(_, oid, guid, pdata, data) =>
              oid mustEqual id
              guid mustEqual guid
              pdata.contains(parentData) mustEqual true
              data mustEqual objectData
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }
  }

  "LoadProjectile" should {
    assert(GlobalDefinitions.wasp_rocket_projectile != null, "missing definition - wasp_rocket_projectile does not exist")
    assert(GlobalDefinitions.wasp_weapon_system != null, "missing definition - wasp_weapon_system does not exist")

    "respond" in {
      val obj = new Projectile(
        GlobalDefinitions.wasp_rocket_projectile,
        GlobalDefinitions.wasp_weapon_system,
        GlobalDefinitions.wasp_weapon_system.FireModes.head,
        Some((1, SourceEntry.None)),
        SourceEntry.None,
        GlobalDefinitions.wasp_weapon_system.ObjectId,
        Vector3(1, 2, 3),
        Vector3(4, 5, 6),
        Some(Vector3(7, 8, 9))
      )
      obj.GUID = PlanetSideGUID(1)
      val definition = obj.Definition
      val id = definition.ObjectId
      val guid = obj.GUID
      val objectData = definition.Packet.ConstructorData(obj).get
      val msg = AvatarAction.LoadProjectile(id, guid, objectData)
      msg.response() match {
        case AvatarAction.LoadCreatedProjectile(pkt) =>
          pkt match {
            case ObjectCreateMessage(_, oid, guid, _, data) =>
              oid mustEqual id
              guid mustEqual guid
              data mustEqual objectData
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }
  }

  "PickupItem" should {
    "respond" in {
      val obj = testSuppressor
      val msg = AvatarAction.PickupItem(obj, 2)
      msg.response() match {
        case ObjectDelete(guid, slot) =>
          guid mustEqual obj.GUID
          slot mustEqual 2
        case _ =>
          ko
      }
    }
  }

  "Release" should {
    val testZone: Zone = new Zone(id = "test", new ZoneMap( name = "test"), zoneNumber = 1)

    "respond" in {
      val obj = testPlayer
      val msg = AvatarAction.Release(obj, testZone)
      msg.response() match {
        case AvatarAction.ReleasePlayer(player) =>
          (player == obj) mustEqual true
        case _ =>
          ko
      }
    }
  }
}
