// Copyright (c) 2016 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, Vector3}
import scodec.bits._

class ObjectCreateMessageTest extends Specification {
  val packet = hex"18 CF 13 00 00 BC 87 00  0A F0 16 C3 43 A1 30 90 00 02 C0 40 00 08 70 43  00 68 00 6F 00 72 00 64 00 54 00 52 00 82 65 1F  F5 9E 80 80 00 00 00 00 00 3F FF C0 00 00 00 20  00 00 00 20 27 03 FF FF FF FF FF FF FF FF FF FF  FF FF FF FF FF FC CC 10 00 03 20 00 00 00 00 00  00 00 00 00 00 00 00 00 00 01 90 01 90 00 00 00  00 01 00 7E C8 00 C8 00 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00 00 00 01 C0 00 42 C5 46  86 C7 00 00 02 A0 00 00 12 60 78 70 65 5F 77 61  72 70 5F 67 61 74 65 5F 75 73 61 67 65 92 78 70  65 5F 69 6E 73 74 61 6E 74 5F 61 63 74 69 6F 6E  92 78 70 65 5F 73 61 6E 63 74 75 61 72 79 5F 68  65 6C 70 91 78 70 65 5F 62 61 74 74 6C 65 5F 72  61 6E 6B 5F 32 8E 78 70 65 5F 66 6F 72 6D 5F 73  71 75 61 64 8E 78 70 65 5F 74 68 5F 6E 6F 6E 73  61 6E 63 8B 78 70 65 5F 74 68 5F 61 6D 6D 6F 90  78 70 65 5F 74 68 5F 66 69 72 65 6D 6F 64 65 73  8F 75 73 65 64 5F 63 68 61 69 6E 62 6C 61 64 65  9A 76 69 73 69 74 65 64 5F 62 72 6F 61 64 63 61  73 74 5F 77 61 72 70 67 61 74 65 8E 76 69 73 69  74 65 64 5F 6C 6F 63 6B 65 72 8D 75 73 65 64 5F  70 75 6E 69 73 68 65 72 88 75 73 65 64 5F 72 65  6B 8D 75 73 65 64 5F 72 65 70 65 61 74 65 72 9F  76 69 73 69 74 65 64 5F 64 65 63 6F 6E 73 74 72  75 63 74 69 6F 6E 5F 74 65 72 6D 69 6E 61 6C 8F  75 73 65 64 5F 73 75 70 70 72 65 73 73 6F 72 96  76 69 73 69 74 65 64 5F 6F 72 64 65 72 5F 74 65  72 6D 69 6E 61 6C 85 6D 61 70 31 35 85 6D 61 70  31 34 85 6D 61 70 31 32 85 6D 61 70 30 31 00 00  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00 00 00 00 01 0A 36 13 88  04 00 40 00 00 10 00 04 00 00 4D 6E 40 10 41 00  00 00 40 00 18 08 38 1C C0 20 32 00 00 07 80 15  E1 D0 02 10 20 00 00 08 00 03 01 07 13 A8 04 06  40 00 00 10 03 20 BB 00 42 E4 00 00 01 00 0E 07  70 08 6C 80 00 06 40 01 C0 F0 01 13 90 00 00 C8  00 38 1E 40 23 32 00 00 19 00 07 03 D0 05 0E 40  00 03 20 00 E8 7B 00 A4 C8 00 00 64 00 DA 4F 80  14 E1 00 00 00 40 00 18 08 38 1F 40 20 32 00 00  0A 00 08 " //fake data?
  val packet2 = hex"18 F8 00 00 00 BC 8C 10 90 3B 45 C6 FA 94 00 9F F0 00 00 40 00 08 C0 44 00 69 00 66 00 66 00 45" //fake data
  //val packet2Rest = packet2.bits.drop(8 + 32 + 1 + 11 + 16)
  var string_inventoryItem = hex"46 04 C0 08 08 80 00 00 20 00 0C 04 10 29 A0 10 19 00 00 04 00 00"
  val string_9mm = hex"18 7C000000 2580 0E0 0005 A1 C8000064000"
  val string_gauss = hex"18 DC000000 2580 2C9 B905 82 480000020000C04 1C00C0B0190000078000"
  val string_punisher = hex"18 27010000 2580 612 a706 82 080000020000c08 1c13a0d01900000780 13a4701a072000000800"
  val string_rek = hex"18 97000000 2580 6C2 9F05 81 48000002000080000"
  val string_testchar = hex"18 570C0000 BC8 4B00 6C2D7 65535 CA16 0 00 01 34 40 00 0970 49006C006C006C004900490049006C006C006C0049006C0049006C006C0049006C006C006C0049006C006C004900 84 52 70 76 1E 80 80 00 00 00 00 00 3FFFC 0 00 00 00 20 00 00 0F F6 A7 03 FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FC 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 90 01 90 00 64 00 00 01 00 7E C8 00 C8 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 C0 00 42 C5 46  86 C7 00 00 00 80 00 00 12 40 78 70 65 5F 73 61 6E 63 74 75 61 72 79 5F 68 65 6C 70 90 78 70 65 5F 74 68 5F 66 69 72 65 6D 6F 64 65 73 8B 75 73 65 64 5F 62 65 61 6D 65 72 85 6D 61 70 31 33 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 0A 23 02 60 04 04 40 00 00 10 00 06 02 08 14 D0 08 0C 80 00 02 00 02 6B 4E 00 82 88 00 00 02 00 00 C0 41 C0 9E 01 01 90 00 00 64 00 44 2A 00 10 91 00 00 00 40 00 18 08 38 94 40 20 32 00 00 00 80 19 05 48 02 17 20 00 00 08 00 70 29 80 43 64 00 00 32 00 0E 05 40 08 9C 80 00 06 40 01 C0 AA 01 19 90 00 00 C8 00 3A 15 80 28 72 00 00 19 00 04 0A B8 05 26 40 00 03 20 06 C2 58 00 A7 88 00 00 02 00 00 80 00 00"

  "decode (2)" in {
    //an invalid bit representation will fail to turn into an object
    PacketCoding.DecodePacket(packet2).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 248
        cls mustEqual 121
        guid mustEqual PlanetSideGUID(2497)
        parent mustEqual None
        data.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "decode (9mm)" in {
    PacketCoding.DecodePacket(string_9mm).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 124
        cls mustEqual 28
        guid mustEqual PlanetSideGUID(1280)
        parent.isDefined mustEqual true
        parent.get.guid mustEqual PlanetSideGUID(75)
        parent.get.slot mustEqual 33
        data.isDefined mustEqual true
        data.get.asInstanceOf[AmmoBoxData].magazine mustEqual 50
      case _ =>
        ko
    }
  }

  "decode (gauss)" in {
    PacketCoding.DecodePacket(string_gauss).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 220
        cls mustEqual 345
        guid mustEqual PlanetSideGUID(1465)
        parent.isDefined mustEqual true
        parent.get.guid mustEqual PlanetSideGUID(75)
        parent.get.slot mustEqual 2
        data.isDefined mustEqual true
        val obj_wep = data.get.asInstanceOf[WeaponData]
        obj_wep.unk mustEqual 4
        val obj_ammo = obj_wep.ammo
        obj_ammo.objectClass mustEqual 28
        obj_ammo.guid mustEqual PlanetSideGUID(1286)
        obj_ammo.parentSlot mustEqual 0
        obj_ammo.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 30
      case _ =>
        ko
    }
  }

  "decode (punisher)" in {
    PacketCoding.DecodePacket(string_punisher).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 295
        cls mustEqual 706
        guid mustEqual PlanetSideGUID(1703)
        parent.isDefined mustEqual true
        parent.get.guid mustEqual PlanetSideGUID(75)
        parent.get.slot mustEqual 2
        data.isDefined mustEqual true
        val obj_wep = data.get.asInstanceOf[ConcurrentFeedWeaponData]
        obj_wep.unk mustEqual 0
        val obj_ammo = obj_wep.ammo
        obj_ammo.size mustEqual 2
        obj_ammo.head.objectClass mustEqual 28
        obj_ammo.head.guid mustEqual PlanetSideGUID(1693)
        obj_ammo.head.parentSlot mustEqual 0
        obj_ammo.head.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 30
        obj_ammo(1).objectClass mustEqual 413
        obj_ammo(1).guid mustEqual PlanetSideGUID(1564)
        obj_ammo(1).parentSlot mustEqual 1
        obj_ammo(1).obj.asInstanceOf[AmmoBoxData].magazine mustEqual 1
      case _ =>
        ko
    }
  }

  "decode (rek)" in {
    PacketCoding.DecodePacket(string_rek).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 151
        cls mustEqual 0x2D8
        guid mustEqual PlanetSideGUID(1439)
        parent.isDefined mustEqual true
        parent.get.guid mustEqual PlanetSideGUID(75)
        parent.get.slot mustEqual 1
        data.isDefined mustEqual true
        data.get.asInstanceOf[REKData].unk mustEqual 4
      case _ =>
        ko
    }
  }

  "decode (character)" in {
    PacketCoding.DecodePacket(string_testchar).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 3159
        cls mustEqual 0x79
        guid mustEqual PlanetSideGUID(75)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true

        val char = data.get.asInstanceOf[CharacterData]
        char.appearance.pos.x mustEqual 3674.8438f
        char.appearance.pos.y mustEqual 2726.789f
        char.appearance.pos.z mustEqual 91.15625f
        char.appearance.objYaw mustEqual 19
        char.appearance.faction mustEqual PlanetSideEmpire.VS
        char.appearance.bops mustEqual false
        char.appearance.unk1 mustEqual 4
        char.appearance.name mustEqual "IlllIIIlllIlIllIlllIllI"
        char.appearance.exosuit mustEqual 4 //standard
        char.appearance.sex mustEqual 2 //female
        char.appearance.face1 mustEqual 2
        char.appearance.face2 mustEqual 9
        char.appearance.voice mustEqual 1 //female 1
        char.appearance.unk2 mustEqual 3
        char.appearance.unk3 mustEqual 118
        char.appearance.unk4 mustEqual 30
        char.appearance.unk5 mustEqual 0x8080
        char.appearance.unk6 mustEqual 0xFFFF
        char.appearance.unk7 mustEqual 2
        char.appearance.viewPitch mustEqual 0xFF
        char.appearance.viewYaw mustEqual 0x6A
        char.appearance.unk8 mustEqual 7
        char.appearance.ribbons.upper mustEqual 0xFFFFFFFFL //none
        char.appearance.ribbons.middle mustEqual 0xFFFFFFFFL //none
        char.appearance.ribbons.lower mustEqual 0xFFFFFFFFL //none
        char.appearance.ribbons.tos mustEqual 0xFFFFFFFFL //none
        char.healthMax mustEqual 100
        char.health mustEqual 100
        char.armor mustEqual 50 //standard exosuit value
        char.unk1 mustEqual 1
        char.unk2 mustEqual 7
        char.unk3 mustEqual 7
        char.staminaMax mustEqual 100
        char.stamina mustEqual 100
        char.unk4 mustEqual 28
        char.unk5 mustEqual 4
        char.unk6 mustEqual 44
        char.unk7 mustEqual 84
        char.unk8 mustEqual 104
        char.unk9 mustEqual 1900
        char.firstTimeEvents.size mustEqual 4
        char.firstTimeEvents.head mustEqual "xpe_sanctuary_help"
        char.firstTimeEvents(1) mustEqual "xpe_th_firemodes"
        char.firstTimeEvents(2) mustEqual "used_beamer"
        char.firstTimeEvents(3) mustEqual "map13"
        char.tutorials.size mustEqual 0
        char.inventory.unk1 mustEqual true
        char.inventory.unk2 mustEqual false
        char.inventory.contents.size mustEqual 10
        val inventory = char.inventory.contents
        //0
        inventory.head.item.objectClass mustEqual 0x8C //beamer
        inventory.head.item.guid mustEqual PlanetSideGUID(76)
        inventory.head.item.parentSlot mustEqual 0
        var wep = inventory.head.item.obj.asInstanceOf[WeaponData]
        wep.ammo.objectClass mustEqual 0x110 //plasma
        wep.ammo.guid mustEqual PlanetSideGUID(77)
        wep.ammo.parentSlot mustEqual 0
        wep.ammo.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 16
        //1
        inventory(1).item.objectClass mustEqual 0x34D //suppressor
        inventory(1).item.guid mustEqual PlanetSideGUID(78)
        inventory(1).item.parentSlot mustEqual 2
        wep = inventory(1).item.obj.asInstanceOf[WeaponData]
        wep.ammo.objectClass mustEqual 0x1C //9mm
        wep.ammo.guid mustEqual PlanetSideGUID(79)
        wep.ammo.parentSlot mustEqual 0
        wep.ammo.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 25
        //2
        inventory(2).item.objectClass mustEqual 0x144 //force blade
        inventory(2).item.guid mustEqual PlanetSideGUID(80)
        inventory(2).item.parentSlot mustEqual 4
        wep = inventory(2).item.obj.asInstanceOf[WeaponData]
        wep.ammo.objectClass mustEqual 0x21C //force blade ammo
        wep.ammo.guid mustEqual PlanetSideGUID(81)
        wep.ammo.parentSlot mustEqual 0
        wep.ammo.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 1
        //3
        inventory(3).item.objectClass mustEqual 0x1C8 //thing
        inventory(3).item.guid mustEqual PlanetSideGUID(82)
        inventory(3).item.parentSlot mustEqual 5
        inventory(3).item.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 1
        //4
        inventory(4).item.objectClass mustEqual 0x1C //9mm
        inventory(4).item.guid mustEqual PlanetSideGUID(83)
        inventory(4).item.parentSlot mustEqual 6
        inventory(4).item.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 50
        //5
        inventory(5).item.objectClass mustEqual 0x1C //9mm
        inventory(5).item.guid mustEqual PlanetSideGUID(84)
        inventory(5).item.parentSlot mustEqual 9
        inventory(5).item.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 50
        //6
        inventory(6).item.objectClass mustEqual 0x1C //9mm
        inventory(6).item.guid mustEqual PlanetSideGUID(85)
        inventory(6).item.parentSlot mustEqual 12
        inventory(6).item.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 50
        //7
        inventory(7).item.objectClass mustEqual 0x1D //9mm ap
        inventory(7).item.guid mustEqual PlanetSideGUID(86)
        inventory(7).item.parentSlot mustEqual 33
        inventory(7).item.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 50
        //8
        inventory(8).item.objectClass mustEqual 0x110 //plasma
        inventory(8).item.guid mustEqual PlanetSideGUID(87)
        inventory(8).item.parentSlot mustEqual 36
        inventory(8).item.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 50
        //9
        inventory(9).item.objectClass mustEqual 0x2D8 //rek
        inventory(9).item.guid mustEqual PlanetSideGUID(88)
        inventory(9).item.parentSlot mustEqual 39
      //the rek has data but none worth testing here
      case _ =>
        ko
    }
  }

  "encode (2)" in {
    //the lack of an object will fail to turn into a bad bitstream
    val msg = ObjectCreateMessage(0, 121, PlanetSideGUID(2497), None, None)
    PacketCoding.EncodePacket(msg).isFailure mustEqual true
  }

  "encode (9mm)" in {
    val obj = AmmoBoxData(50)
    val msg = ObjectCreateMessage(0, 28, PlanetSideGUID(1280), ObjectCreateMessageParent(PlanetSideGUID(75), 33), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_9mm
  }

  "encode (gauss)" in {
    val obj = WeaponData(4, 28, PlanetSideGUID(1286), 0, AmmoBoxData(30))
    val msg = ObjectCreateMessage(0, 345, PlanetSideGUID(1465), ObjectCreateMessageParent(PlanetSideGUID(75), 2), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_gauss
  }

  "encode (punisher)" in {
    val obj = ConcurrentFeedWeaponData(0, AmmoBoxData(28, PlanetSideGUID(1693), 0, AmmoBoxData(30)) :: AmmoBoxData(413, PlanetSideGUID(1564), 1, AmmoBoxData(1)) :: Nil)
    val msg = ObjectCreateMessage(0, 706, PlanetSideGUID(1703), ObjectCreateMessageParent(PlanetSideGUID(75), 2), obj)
    var pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_punisher
  }

  "encode (rek)" in {
    val obj = REKData(4)
    val msg = ObjectCreateMessage(0, 0x2D8, PlanetSideGUID(1439), ObjectCreateMessageParent(PlanetSideGUID(75), 1), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_rek
  }

  "encode (character)" in {
    val app = CharacterAppearanceData(
      Vector3(3674.8438f, 2726.789f, 91.15625f),
      19,
      PlanetSideEmpire.VS,
      false,
      4,
      "IlllIIIlllIlIllIlllIllI",
      4,
      2,
      2,9,
      1,
      3, 118,30, 0x8080, 0xFFFF, 2,
      255, 106, 7,
      RibbonBars()
    )
    val inv = InventoryItem(0x8C, PlanetSideGUID(76), 0, WeaponData(8, 0x110, PlanetSideGUID(77), 0, AmmoBoxData(16))) ::
      InventoryItem(0x34D, PlanetSideGUID(78), 2, WeaponData(8, 0x1C, PlanetSideGUID(79), 0, AmmoBoxData(25))) ::
      InventoryItem(0x144, PlanetSideGUID(80), 4, WeaponData(8, 0x21C, PlanetSideGUID(81), 0, AmmoBoxData(1))) ::
      InventoryItem(0x1C8, PlanetSideGUID(82), 5, AmmoBoxData(1)) ::
      InventoryItem(0x1C, PlanetSideGUID(83), 6, AmmoBoxData(50)) ::
      InventoryItem(0x1C, PlanetSideGUID(84), 9, AmmoBoxData(50)) ::
      InventoryItem(0x1C, PlanetSideGUID(85), 12, AmmoBoxData(50)) ::
      InventoryItem(0x1D, PlanetSideGUID(86), 33, AmmoBoxData(50)) ::
      InventoryItem(0x110, PlanetSideGUID(87), 36, AmmoBoxData(50)) ::
      InventoryItem(0x2D8, PlanetSideGUID(88), 39, REKData(8)) ::
      Nil
    val obj = CharacterData(
      app,
      100, 100,
      50,
      1, 7, 7,
      100, 100,
      28, 4, 44, 84, 104, 1900,
      "xpe_sanctuary_help" :: "xpe_th_firemodes" :: "used_beamer" :: "map13" :: Nil,
      List.empty,
      InventoryData(
        true, false, false, inv
      )
    )
    val msg = ObjectCreateMessage(0, 0x79, PlanetSideGUID(75), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_testchar
  }
}
