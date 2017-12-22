// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import org.specs2.mutable._
import scodec.bits._

class LockerContainerDataTest extends Specification {
  val string_locker_container = hex"17 AF010000 E414C0C00000000000000000000600000818829DC2E030000000202378620D80C00000378FA0FADC000006F1FC199D800000"

  "LockerContainerData" should {
    "decode" in {
      PacketCoding.DecodePacket(string_locker_container).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 431
          cls mustEqual ObjectClass.locker_container
          guid mustEqual PlanetSideGUID(3148)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[LockerContainerData] mustEqual true
          val locker = data.get.asInstanceOf[LockerContainerData]
          val contents = locker.inventory.contents
          contents.size mustEqual 3
          //0
          contents.head.objectClass mustEqual ObjectClass.nano_dispenser
          contents.head.guid mustEqual PlanetSideGUID(2935)
          contents.head.parentSlot mustEqual 0
          contents.head.obj.isInstanceOf[WeaponData] mustEqual true
          val dispenser = contents.head.obj.asInstanceOf[WeaponData]
          dispenser.unk1 mustEqual 0x6
          dispenser.unk2 mustEqual 0x0
          dispenser.ammo.head.objectClass mustEqual ObjectClass.armor_canister
          dispenser.ammo.head.guid mustEqual PlanetSideGUID(3426)
          dispenser.ammo.head.parentSlot mustEqual 0
          dispenser.ammo.head.obj.isInstanceOf[AmmoBoxData] mustEqual true
          dispenser.ammo.head.obj.asInstanceOf[AmmoBoxData].unk mustEqual 0
          //1
          contents(1).objectClass mustEqual ObjectClass.armor_canister
          contents(1).guid mustEqual PlanetSideGUID(4090)
          contents(1).parentSlot mustEqual 45
          contents(1).obj.isInstanceOf[AmmoBoxData] mustEqual true
          contents(1).obj.asInstanceOf[AmmoBoxData].unk mustEqual 0
          //2
          contents(2).objectClass mustEqual ObjectClass.armor_canister
          contents(2).guid mustEqual PlanetSideGUID(3326)
          contents(2).parentSlot mustEqual 78
          contents(2).obj.isInstanceOf[AmmoBoxData] mustEqual true
          contents(2).obj.asInstanceOf[AmmoBoxData].unk mustEqual 0
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = LockerContainerData(
        InventoryData(
          InventoryItemData(ObjectClass.nano_dispenser, PlanetSideGUID(2935), 0, WeaponData(0x6, 0x0, ObjectClass.armor_canister, PlanetSideGUID(3426), 0, AmmoBoxData())) ::
            InventoryItemData(ObjectClass.armor_canister, PlanetSideGUID(4090), 45, AmmoBoxData()) ::
            InventoryItemData(ObjectClass.armor_canister, PlanetSideGUID(3326), 78, AmmoBoxData()) ::
            Nil
        )
      )
      val msg = ObjectCreateMessage(ObjectClass.locker_container, PlanetSideGUID(3148), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_locker_container
    }
  }
}
