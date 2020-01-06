// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}
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
          data.isInstanceOf[LockerContainerData] mustEqual true
          val locker = data.asInstanceOf[LockerContainerData]
          locker.inventory match {
            case Some(InventoryData(contents)) =>
              contents.size mustEqual 3
              //0
              contents.head.objectClass mustEqual ObjectClass.nano_dispenser
              contents.head.guid mustEqual PlanetSideGUID(2935)
              contents.head.parentSlot mustEqual 0
              contents.head.obj match {
                case WeaponData(CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid), _, _, _) =>
                  faction mustEqual PlanetSideEmpire.NEUTRAL
                  bops mustEqual false
                  alternate mustEqual false
                  v1 mustEqual false
                  v2.isEmpty mustEqual true
                  v3 mustEqual false
                  v4.isEmpty mustEqual true
                  v5.isEmpty mustEqual true
                  fguid mustEqual PlanetSideGUID(0)
                case _ =>
                  ko
              }
              //1
              contents(1).objectClass mustEqual ObjectClass.armor_canister
              contents(1).guid mustEqual PlanetSideGUID(4090)
              contents(1).parentSlot mustEqual 45
              contents(1).obj.isInstanceOf[CommonFieldData] mustEqual true
              //2
              contents(2).objectClass mustEqual ObjectClass.armor_canister
              contents(2).guid mustEqual PlanetSideGUID(3326)
              contents(2).parentSlot mustEqual 78
              contents(2).obj.isInstanceOf[CommonFieldData] mustEqual true
            case None =>
              ko
          }

        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = LockerContainerData(
        InventoryData(List(
          InventoryItemData(ObjectClass.nano_dispenser, PlanetSideGUID(2935), 0,
            WeaponData(
              CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, false, None, false, None, None, PlanetSideGUID(0)),
              0,
              List(InternalSlot(ObjectClass.armor_canister, PlanetSideGUID(3426), 0, CommonFieldData()(false)))
            )
          ),
          InventoryItemData(ObjectClass.armor_canister, PlanetSideGUID(4090), 45, CommonFieldData()(false)),
          InventoryItemData(ObjectClass.armor_canister, PlanetSideGUID(3326), 78, CommonFieldData()(false))
        ))
      )
      val msg = ObjectCreateMessage(ObjectClass.locker_container, PlanetSideGUID(3148), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_locker_container
    }
  }
}
