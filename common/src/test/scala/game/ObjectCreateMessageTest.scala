// Copyright (c) 2017 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{CharacterGender, ExoSuitType, GrenadeState, PlanetSideEmpire, Vector3}
import org.specs2.mutable._
import scodec.bits._

class ObjectCreateMessageTest extends Specification {
  val string_striker_projectile = hex"17 C5000000 A4B 009D 4C129 0CB0A 9814 00 F5 E3 040000666686400"
  val string_implant_interface = hex"17 6C000000 01014C93304818000000"
  val string_order_terminala = hex"17 A5000000 B2AF30EACF1889F7A3D1200007D2000000"
  val string_ace_held = hex"17 76000000 0406900650C80480000000"
  val string_boomertrigger = hex"17 76000000 58084A8100E80C00000000" //reconstructed from an inventory entry
  val string_detonater_held = hex"17 76000000 1A886A8421080400000000"
  val string_lasher_held = hex"17 BB000000 1688569D90B83 880000008082077036032000000"
  val string_punisher_held = hex"17 F6000000 0A06612331083 88000000810381383E03200003793287C0E400000"
  val string_rek_held = hex"17 86000000 27086C2350F800800000000000"
  val string_captureflag = hex"17 E5000000 CE8EA10 04A47 B818A FE0E 00 00 0F 24000015000400160B09000"  //LLU for Qumu on Amerish
  val string_ace_dropped = hex"17 AF000000 90024113B329C5D5A2D1200005B440000000"
  val string_detonater_dropped = hex"17 AF000000 EA8620ED1549B4B6A741500001B000000000"
  val string_shotgunshell_dropped = hex"17 A5000000 F9A7D0D 5E269 BED5A F114 0000596000000"
  val string_lasher_dropped = hex"17 F4000000 D69020C 99299 85D0A 5F10 00 00 20 400000004041038819018000000"
  val string_punisher_dropped = hex"17 2F010000 E12A20B 915A9 28C9A 1412 00 00 33 200000004081C1901B01800001BCB5C2E07000000"
  val string_rek_dropped = hex"17 BF000000 EC20311 85219 7AC1A 2D12 00 00 4E 4000000001800"
  val string_boomer = hex"17 A5000000 CA0000F1630938D5A8F1400003F0031100"
  val string_spitfire_short = hex"17 BB000000 9D37010 E4F08 6AFCA 0312 00 7F 42 2C1F0F0000F00"
  val string_spitfire = hex"17 4F010000 9D3A910 D1D78 AE3FC 9111 00 00 69 4488107F80F2021DBF80B80C80000008086EDB83A03200000"
  val string_trap = hex"17 BB000000 A8B630A 39FA6 FD666 801C 00 00 00 44C6097F80F00"
  val string_aegis = hex"17 10010000 F80FC09 9DF96 0C676 801C 00 00 00 443E09FF0000000000000000000000000"
  val string_orion = hex"17 5E010000 D82640B 92F76 01D65 F611 00 00 5E 4400006304BFC1E4041826E1503900000010104CE704C06400000"
  val string_locker_container = hex"17 AF010000 E414C0C00000000000000000000600000818829DC2E030000000202378620D80C00000378FA0FADC000006F1FC199D800000"
  val string_character = hex"17 73070000 BC8 3E0F 6C2D7 65535 CA16 00 00 09 9741E4F804000000 234530063007200610077006E00790052006F006E006E0069006500 220B7 E67B540404001000000000022B50100 268042006C00610063006B002000420065007200650074002000410072006D006F007500720065006400200043006F00720070007300 1700E0030050040003BC00000234040001A004000 3FFF67A8F A0A5424E0E800000000080952A9C3A03000001081103E040000000A023782F1080C0000016244108200000000808382403A030000014284C3A0C0000000202512F00B80C00000578F80F840000000280838B3C320300000080"
  val string_character_backpack = hex"17 9C030000 BC8 340D F20A9 3956C AF0D 00 00 73 480000 87041006E00670065006C006C006F00 4A148 0000000000000000000000005C54200 24404F0072006900670069006E0061006C00200044006900730074007200690063007400 1740180181E8000000C202000042000000D202000000010A3C00"

  "deocde (striker projectile)" in {
    PacketCoding.DecodePacket(string_striker_projectile).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 197
        cls mustEqual ObjectClass.striker_missile_targeting_projectile
        guid mustEqual PlanetSideGUID(40192)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[TrackedProjectileData] mustEqual true
        val projectile = data.get.asInstanceOf[TrackedProjectileData]
        projectile.pos.coord.x mustEqual 4644.5938f
        projectile.pos.coord.y mustEqual 5472.0938f
        projectile.pos.coord.z mustEqual 82.375f
        projectile.pos.roll mustEqual 0
        projectile.pos.pitch mustEqual 245
        projectile.pos.yaw mustEqual 227
        projectile.unk1 mustEqual 0
        projectile.unk2 mustEqual TrackedProjectileData.striker_missile_targetting_projectile_data
      case _ =>
        ko
    }
  }

  "decode (implant interface)" in {
    PacketCoding.DecodePacket(string_implant_interface).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 108
        cls mustEqual 0x199
        guid mustEqual PlanetSideGUID(1075)
        parent.isDefined mustEqual true
        parent.get.guid mustEqual PlanetSideGUID(514)
        parent.get.slot mustEqual 1
        data.isDefined mustEqual true
        data.get.isInstanceOf[ImplantInterfaceData] mustEqual true
      case _ =>
        ko
    }
  }

  "decode (order terminal a)" in {
    PacketCoding.DecodePacket(string_order_terminala).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 165
        cls mustEqual ObjectClass.order_terminala
        guid mustEqual PlanetSideGUID(3827)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        val term = data.get.asInstanceOf[CommonTerminalData]
        term.pos.coord.x mustEqual 4579.3438f
        term.pos.coord.y mustEqual 5615.0703f
        term.pos.coord.z mustEqual 72.953125f
        term.pos.pitch mustEqual 0
        term.pos.roll mustEqual 0
        term.pos.yaw mustEqual 125
        ok
      case _ =>
        ko
    }
  }

  "decode (ace, held)" in {
    PacketCoding.DecodePacket(string_ace_held).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 118
        cls mustEqual ObjectClass.ace
        guid mustEqual PlanetSideGUID(3173)
        parent.isDefined mustEqual true
        parent.get.guid mustEqual PlanetSideGUID(3336)
        parent.get.slot mustEqual 0
        data.isDefined mustEqual true
        data.get.isInstanceOf[ACEData] mustEqual true
        val ace = data.get.asInstanceOf[ACEData]
        ace.unk1 mustEqual 4
        ace.unk2 mustEqual 8
        ace.unk3 mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (boomer trigger, held)" in {
    PacketCoding.DecodePacket(string_boomertrigger).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 118
        cls mustEqual ObjectClass.boomer_trigger
        guid mustEqual PlanetSideGUID(3600)
        parent.isDefined mustEqual true
        parent.get.guid mustEqual PlanetSideGUID(4272)
        parent.get.slot mustEqual 0
        data.isDefined mustEqual true
        data.get.isInstanceOf[BoomerTriggerData] mustEqual true
        data.get.asInstanceOf[BoomerTriggerData].unk mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (detonator, held)" in {
    PacketCoding.DecodePacket(string_detonater_held).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 118
        cls mustEqual ObjectClass.command_detonater
        guid mustEqual PlanetSideGUID(4162)
        parent.isDefined mustEqual true
        parent.get.guid mustEqual PlanetSideGUID(4149)
        parent.get.slot mustEqual 0
        data.isDefined mustEqual true
        data.get.isInstanceOf[CommandDetonaterData] mustEqual true
        val cud = data.get.asInstanceOf[CommandDetonaterData]
        cud.unk1 mustEqual 4
        cud.unk2 mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (lasher, held)" in {
    PacketCoding.DecodePacket(string_lasher_held).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 187
        cls mustEqual ObjectClass.lasher
        guid mustEqual PlanetSideGUID(3033)
        parent.isDefined mustEqual true
        parent.get.guid mustEqual PlanetSideGUID(4141)
        parent.get.slot mustEqual 3
        data.isDefined mustEqual true
        data.get.isInstanceOf[WeaponData] mustEqual true
        val wep = data.get.asInstanceOf[WeaponData]
        wep.unk1 mustEqual 8
        wep.unk2 mustEqual 8
        wep.fire_mode mustEqual 0
        wep.ammo.objectClass mustEqual ObjectClass.energy_cell
        wep.ammo.guid mustEqual PlanetSideGUID(3548)
        wep.ammo.parentSlot mustEqual 0
        wep.ammo.obj.isInstanceOf[AmmoBoxData] mustEqual true
        val ammo = wep.ammo.obj.asInstanceOf[AmmoBoxData]
        ammo.unk mustEqual 8
      case _ =>
        ko
    }
  }

  "decode (punisher, held)" in {
    PacketCoding.DecodePacket(string_punisher_held).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 246
        cls mustEqual ObjectClass.punisher
        guid mustEqual PlanetSideGUID(4147)
        parent.isDefined mustEqual true
        parent.get.guid mustEqual PlanetSideGUID(3092)
        parent.get.slot mustEqual 3
        data.isDefined mustEqual true
        data.get.isInstanceOf[ConcurrentFeedWeaponData] mustEqual true
        val wep = data.get.asInstanceOf[ConcurrentFeedWeaponData]
        wep.unk1 mustEqual 8
        wep.unk2 mustEqual 8
        wep.fire_mode mustEqual 0
        val ammo = wep.ammo
        ammo.size mustEqual 2
        //0
        ammo.head.objectClass mustEqual ObjectClass.bullet_9mm
        ammo.head.guid mustEqual PlanetSideGUID(3918)
        ammo.head.parentSlot mustEqual 0
        ammo.head.obj.isInstanceOf[AmmoBoxData] mustEqual true
        ammo.head.obj.asInstanceOf[AmmoBoxData].unk mustEqual 8
        //1
        ammo(1).objectClass mustEqual ObjectClass.rocket
        ammo(1).guid mustEqual PlanetSideGUID(3941)
        ammo(1).parentSlot mustEqual 1
        ammo(1).obj.isInstanceOf[AmmoBoxData] mustEqual true
        ammo(1).obj.asInstanceOf[AmmoBoxData].unk mustEqual 8
      case _ =>
        ko
    }
  }

  "decode (REK, held)" in {
    PacketCoding.DecodePacket(string_rek_held).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 134
        cls mustEqual ObjectClass.remote_electronics_kit
        guid mustEqual PlanetSideGUID(3893)
        parent.isDefined mustEqual true
        parent.get.guid mustEqual PlanetSideGUID(4174)
        parent.get.slot mustEqual 0
        data.isDefined mustEqual true
        data.get.isInstanceOf[REKData] mustEqual true
        val rek = data.get.asInstanceOf[REKData]
        rek.unk1 mustEqual 0
        rek.unk2 mustEqual 8
        rek.unk3 mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (capture flag)" in {
    PacketCoding.DecodePacket(string_captureflag).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 229
        cls mustEqual ObjectClass.capture_flag
        guid mustEqual PlanetSideGUID(4330)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[CaptureFlagData] mustEqual true
        val flag = data.get.asInstanceOf[CaptureFlagData]
        flag.pos.coord.x mustEqual 3912.0312f
        flag.pos.coord.y mustEqual 5169.4375f
        flag.pos.coord.z mustEqual 59.96875f
        flag.pos.roll mustEqual 0
        flag.pos.pitch mustEqual 0
        flag.pos.yaw mustEqual 15
        flag.faction mustEqual PlanetSideEmpire.NC
        flag.unk1 mustEqual 21
        flag.unk2 mustEqual 4
        flag.unk3 mustEqual 2838
        flag.unk4 mustEqual 9
      case _ =>
        ko
    }
  }
  "decode (ace, dropped)" in {
    PacketCoding.DecodePacket(string_ace_dropped).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 175
        cls mustEqual ObjectClass.ace
        guid mustEqual PlanetSideGUID(4388)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[DroppedItemData[_]] mustEqual true
        val drop = data.get.asInstanceOf[DroppedItemData[_]]
        drop.pos.coord.x mustEqual 4708.461f
        drop.pos.coord.y mustEqual 5547.539f
        drop.pos.coord.z mustEqual 72.703125f
        drop.pos.roll mustEqual 0
        drop.pos.pitch mustEqual 0
        drop.pos.yaw mustEqual 91
        drop.obj.isInstanceOf[ACEData] mustEqual true
        val ace = drop.obj.asInstanceOf[ACEData]
        ace.unk1 mustEqual 8
        ace.unk2 mustEqual 8
      case _ =>
        ko
    }
  }

  "decode (detonator, dropped)" in {
    PacketCoding.DecodePacket(string_detonater_dropped).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 175
        cls mustEqual ObjectClass.command_detonater
        guid mustEqual PlanetSideGUID(3682)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[DroppedItemData[_]] mustEqual true
        val drop = data.get.asInstanceOf[DroppedItemData[_]]
        drop.pos.coord.x mustEqual 4777.633f
        drop.pos.coord.y mustEqual 5485.4062f
        drop.pos.coord.z mustEqual 85.8125f
        drop.pos.roll mustEqual 0
        drop.pos.pitch mustEqual 0
        drop.pos.yaw mustEqual 27
        drop.obj.isInstanceOf[CommandDetonaterData] mustEqual true
      case _ =>
        ko
    }
  }

  "decode (shotgun shells, dropped)" in {
    PacketCoding.DecodePacket(string_shotgunshell_dropped).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 165
        cls mustEqual ObjectClass.shotgun_shell
        guid mustEqual PlanetSideGUID(3453)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[DroppedItemData[_]] mustEqual true
        val drop = data.get.asInstanceOf[DroppedItemData[_]]
        drop.pos.coord.x mustEqual 4684.7344f
        drop.pos.coord.y mustEqual 5547.4844f
        drop.pos.coord.z mustEqual 83.765625f
        drop.pos.roll mustEqual 0
        drop.pos.pitch mustEqual 0
        drop.pos.yaw mustEqual 89
        drop.obj.isInstanceOf[AmmoBoxData] mustEqual true
        val box = drop.obj.asInstanceOf[AmmoBoxData]
        box.unk mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (lasher, dropped)" in {
    PacketCoding.DecodePacket(string_lasher_dropped).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 244
        cls mustEqual ObjectClass.lasher
        guid mustEqual PlanetSideGUID(3074)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[DroppedItemData[_]] mustEqual true
        val drop = data.get.asInstanceOf[DroppedItemData[_]]
        drop.pos.coord.x mustEqual 4691.1953f
        drop.pos.coord.y mustEqual 5537.039f
        drop.pos.coord.z mustEqual 65.484375f
        drop.pos.roll mustEqual 0
        drop.pos.pitch mustEqual 0
        drop.pos.yaw mustEqual 32
        drop.obj.isInstanceOf[WeaponData] mustEqual true
        val wep = drop.obj.asInstanceOf[WeaponData]
        wep.unk1 mustEqual 8
        wep.unk2 mustEqual 0
        wep.fire_mode mustEqual 0
        wep.ammo.objectClass mustEqual ObjectClass.energy_cell
        wep.ammo.guid mustEqual PlanetSideGUID(3268)
        wep.ammo.parentSlot mustEqual 0
        wep.ammo.obj.isInstanceOf[AmmoBoxData] mustEqual true
        val ammo = wep.ammo.obj.asInstanceOf[AmmoBoxData]
        ammo.unk mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (punisher, dropped)" in {
    PacketCoding.DecodePacket(string_punisher_dropped).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 303
        cls mustEqual ObjectClass.punisher
        guid mustEqual PlanetSideGUID(2978)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[DroppedItemData[_]] mustEqual true
        val drop = data.get.asInstanceOf[DroppedItemData[_]]
        drop.pos.coord.x mustEqual 4789.133f
        drop.pos.coord.y mustEqual 5522.3125f
        drop.pos.coord.z mustEqual 72.3125f
        drop.pos.roll mustEqual 0
        drop.pos.pitch mustEqual 0
        drop.pos.yaw mustEqual 51
        drop.obj.isInstanceOf[ConcurrentFeedWeaponData] mustEqual true
        val wep = drop.obj.asInstanceOf[ConcurrentFeedWeaponData]
        wep.unk1 mustEqual 4
        wep.unk2 mustEqual 0
        wep.fire_mode mustEqual 0
        val ammo = wep.ammo
        ammo.size mustEqual 2
        //0
        ammo.head.objectClass mustEqual ObjectClass.bullet_9mm
        ammo.head.guid mustEqual PlanetSideGUID(3528)
        ammo.head.parentSlot mustEqual 0
        ammo.head.obj.isInstanceOf[AmmoBoxData] mustEqual true
        ammo.head.obj.asInstanceOf[AmmoBoxData].unk mustEqual 0
        //1
        ammo(1).objectClass mustEqual ObjectClass.rocket
        ammo(1).guid mustEqual PlanetSideGUID(3031)
        ammo(1).parentSlot mustEqual 1
        ammo(1).obj.isInstanceOf[AmmoBoxData] mustEqual true
        ammo(1).obj.asInstanceOf[AmmoBoxData].unk mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (REK, dropped)" in {
    PacketCoding.DecodePacket(string_rek_dropped).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 191
        cls mustEqual ObjectClass.remote_electronics_kit
        guid mustEqual PlanetSideGUID(4355)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[DroppedItemData[_]] mustEqual true
        val dropped = data.get.asInstanceOf[DroppedItemData[_]]
        dropped.pos.coord.x mustEqual 4675.039f
        dropped.pos.coord.y mustEqual 5506.953f
        dropped.pos.coord.z mustEqual 72.703125f
        dropped.pos.roll mustEqual 0
        dropped.pos.pitch mustEqual 0
        dropped.pos.yaw mustEqual 78
        dropped.obj.isInstanceOf[REKData] mustEqual true
        val rek = dropped.obj.asInstanceOf[REKData]
        rek.unk1 mustEqual 8
        rek.unk2 mustEqual 0
        rek.unk3 mustEqual 3
      case _ =>
        ko
    }
  }

  "decode (boomer)" in {
    PacketCoding.DecodePacket(string_boomer).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 165
        cls mustEqual ObjectClass.boomer
        guid mustEqual PlanetSideGUID(3840)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[SmallDeployableData] mustEqual true
        val boomer = data.get.asInstanceOf[SmallDeployableData]
        boomer.deploy.pos.coord.x mustEqual 4704.172f
        boomer.deploy.pos.coord.y mustEqual 5546.4375f
        boomer.deploy.pos.coord.z mustEqual 82.234375f
        boomer.deploy.pos.roll mustEqual 0
        boomer.deploy.pos.pitch mustEqual 0
        boomer.deploy.pos.yaw mustEqual 63
        boomer.deploy.unk mustEqual 0
        boomer.deploy.player_guid mustEqual PlanetSideGUID(4145)
      case _ =>
        ko
    }
  }

  "decode (spitfire, short)" in {
    PacketCoding.DecodePacket(string_spitfire_short).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 187
        cls mustEqual ObjectClass.spitfire_turret
        guid mustEqual PlanetSideGUID(4208)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[SmallTurretData] mustEqual true
        val turret = data.get.asInstanceOf[SmallTurretData]
        turret.deploy.pos.coord.x mustEqual 4577.7812f
        turret.deploy.pos.coord.y mustEqual 5624.828f
        turret.deploy.pos.coord.z mustEqual 72.046875f
        turret.deploy.pos.roll mustEqual 0
        turret.deploy.pos.pitch mustEqual 127
        turret.deploy.pos.yaw mustEqual 66
        turret.deploy.unk mustEqual 44
        turret.deploy.player_guid mustEqual PlanetSideGUID(3871)
        turret.health mustEqual 0
        turret.internals.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "decode (spitfire)" in {
    PacketCoding.DecodePacket(string_spitfire).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 335
        cls mustEqual ObjectClass.spitfire_turret
        guid mustEqual PlanetSideGUID(4265)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[SmallTurretData] mustEqual true
        val turret = data.get.asInstanceOf[SmallTurretData]
        turret.deploy.pos.coord.x mustEqual 4527.633f
        turret.deploy.pos.coord.y mustEqual 6271.3594f
        turret.deploy.pos.coord.z mustEqual 70.265625f
        turret.deploy.pos.roll mustEqual 0
        turret.deploy.pos.pitch mustEqual 0
        turret.deploy.pos.yaw mustEqual 105
        turret.deploy.unk mustEqual 68
        turret.deploy.player_guid mustEqual PlanetSideGUID(4232)
        turret.health mustEqual 255
        turret.internals.isDefined mustEqual true
        val internals = turret.internals.get
        internals.objectClass mustEqual ObjectClass.spitfire_weapon
        internals.guid mustEqual PlanetSideGUID(3064)
        internals.parentSlot mustEqual 0
        internals.obj.isInstanceOf[WeaponData] mustEqual true
        val wep = internals.obj.asInstanceOf[WeaponData]
        wep.unk1 mustEqual 0xC
        wep.unk2 mustEqual 0x8
        wep.fire_mode mustEqual 0
        val ammo = wep.ammo
        ammo.objectClass mustEqual ObjectClass.spitfire_ammo
        ammo.guid mustEqual PlanetSideGUID(3694)
        ammo.parentSlot mustEqual 0
        ammo.obj.isInstanceOf[AmmoBoxData] mustEqual true
        ammo.obj.asInstanceOf[AmmoBoxData].unk mustEqual 8
      case _ =>
        ko
    }
  }

  "decode (trap)" in {
    PacketCoding.DecodePacket(string_trap).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 187
        cls mustEqual ObjectClass.tank_traps
        guid mustEqual PlanetSideGUID(2659)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[TRAPData] mustEqual true
        val trap = data.get.asInstanceOf[TRAPData]
        trap.deploy.pos.coord.x mustEqual 3572.4453f
        trap.deploy.pos.coord.y mustEqual 3277.9766f
        trap.deploy.pos.coord.z mustEqual 114.0f
        trap.deploy.pos.roll mustEqual 0
        trap.deploy.pos.pitch mustEqual 0
        trap.deploy.pos.yaw mustEqual 0
        trap.deploy.unk mustEqual 68
        trap.health mustEqual 255
        trap.deploy.player_guid mustEqual PlanetSideGUID(2502)
      case _ =>
        ko
    }
  }

  "decode (aegis)" in {
    PacketCoding.DecodePacket(string_aegis).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 272
        cls mustEqual ObjectClass.deployable_shield_generator
        guid mustEqual PlanetSideGUID(2556)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[AegisShieldGeneratorData] mustEqual true
        val aegis = data.get.asInstanceOf[AegisShieldGeneratorData]
        aegis.deploy.pos.coord.x mustEqual 3571.2266f
        aegis.deploy.pos.coord.y mustEqual 3278.0938f
        aegis.deploy.pos.coord.z mustEqual 114.0f
        aegis.deploy.pos.roll mustEqual 0
        aegis.deploy.pos.pitch mustEqual 0
        aegis.deploy.pos.yaw mustEqual 0
        aegis.deploy.unk mustEqual 68
        aegis.health mustEqual 255
        aegis.deploy.player_guid mustEqual PlanetSideGUID(2366)
      case _ =>
        ko
    }
  }

  "decode (orion)" in {
    PacketCoding.DecodePacket(string_orion).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 350
        cls mustEqual ObjectClass.portable_manned_turret_vs
        guid mustEqual PlanetSideGUID(2916)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[OneMannedFieldTurretData] mustEqual true
        val omft = data.get.asInstanceOf[OneMannedFieldTurretData]
        omft.deploy.pos.coord.x mustEqual 3567.1406f
        omft.deploy.pos.coord.y mustEqual 2988.0078f
        omft.deploy.pos.coord.z mustEqual 71.84375f
        omft.deploy.pos.roll mustEqual 0
        omft.deploy.pos.pitch mustEqual 0
        omft.deploy.pos.yaw mustEqual 94
        omft.deploy.unk mustEqual 68
        omft.deploy.player_guid mustEqual PlanetSideGUID(0)
        omft.player_guid mustEqual PlanetSideGUID(2502)
        omft.health mustEqual 255
        omft.internals.isDefined mustEqual true
        val internals = omft.internals.get
        internals.objectClass mustEqual ObjectClass.energy_gun_vs
        internals.guid mustEqual PlanetSideGUID(2615)
        internals.parentSlot mustEqual 1
        internals.obj.isInstanceOf[WeaponData] mustEqual true
        val wep = internals.obj.asInstanceOf[WeaponData]
        wep.unk1 mustEqual 0xC
        wep.unk2 mustEqual 0x8
        wep.fire_mode mustEqual 0
        val ammo = wep.ammo
        ammo.objectClass mustEqual ObjectClass.energy_gun_ammo
        ammo.guid mustEqual PlanetSideGUID(2510)
        ammo.parentSlot mustEqual 0
        ammo.obj.isInstanceOf[AmmoBoxData] mustEqual true
        ammo.obj.asInstanceOf[AmmoBoxData].unk mustEqual 8
      case _ =>
        ko
    }
  }

  "decode (locker container)" in {
    PacketCoding.DecodePacket(string_locker_container).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 431
        cls mustEqual ObjectClass.locker_container
        guid mustEqual PlanetSideGUID(3148)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[LockerContainerData] mustEqual true
        val locker = data.get.asInstanceOf[LockerContainerData]
        locker.inventory.unk1 mustEqual false
        locker.inventory.unk2 mustEqual false
        val contents = locker.inventory.contents
        contents.size mustEqual 3
        //0
        contents.head.item.objectClass mustEqual ObjectClass.nano_dispenser
        contents.head.item.guid mustEqual PlanetSideGUID(2935)
        contents.head.item.parentSlot mustEqual 0
        contents.head.item.obj.isInstanceOf[WeaponData] mustEqual true
        val dispenser = contents.head.item.obj.asInstanceOf[WeaponData]
        dispenser.unk1 mustEqual 0xC
        dispenser.unk2 mustEqual 0x0
        dispenser.ammo.objectClass mustEqual ObjectClass.armor_canister
        dispenser.ammo.guid mustEqual PlanetSideGUID(3426)
        dispenser.ammo.parentSlot mustEqual 0
        dispenser.ammo.obj.isInstanceOf[AmmoBoxData] mustEqual true
        dispenser.ammo.obj.asInstanceOf[AmmoBoxData].unk mustEqual 0
        //1
        contents(1).item.objectClass mustEqual ObjectClass.armor_canister
        contents(1).item.guid mustEqual PlanetSideGUID(4090)
        contents(1).item.parentSlot mustEqual 45
        contents(1).item.obj.isInstanceOf[AmmoBoxData] mustEqual true
        contents(1).item.obj.asInstanceOf[AmmoBoxData].unk mustEqual 0
        //2
        contents(2).item.objectClass mustEqual ObjectClass.armor_canister
        contents(2).item.guid mustEqual PlanetSideGUID(3326)
        contents(2).item.parentSlot mustEqual 78
        contents(2).item.obj.isInstanceOf[AmmoBoxData] mustEqual true
        contents(2).item.obj.asInstanceOf[AmmoBoxData].unk mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (character, alive)" in {
    PacketCoding.DecodePacket(string_character).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 1907
        cls mustEqual ObjectClass.avatar
        guid mustEqual PlanetSideGUID(3902)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[CharacterData] mustEqual true
        val pc = data.get.asInstanceOf[CharacterData]
        pc.appearance.pos.coord.x mustEqual 3674.8438f
        pc.appearance.pos.coord.y mustEqual 2726.789f
        pc.appearance.pos.coord.z mustEqual 91.15625f
        pc.appearance.pos.roll mustEqual 0
        pc.appearance.pos.pitch mustEqual 0
        pc.appearance.pos.yaw mustEqual 9
        pc.appearance.pos.init_move.isDefined mustEqual true
        pc.appearance.pos.init_move.get.x mustEqual 1.4375f
        pc.appearance.pos.init_move.get.y mustEqual -0.4375f
        pc.appearance.pos.init_move.get.z mustEqual 0f
        pc.appearance.basic_appearance.name mustEqual "ScrawnyRonnie"
        pc.appearance.basic_appearance.faction mustEqual PlanetSideEmpire.TR
        pc.appearance.basic_appearance.sex mustEqual CharacterGender.Male
        pc.appearance.basic_appearance.head mustEqual 5
        pc.appearance.basic_appearance.voice mustEqual 5
        pc.appearance.voice2 mustEqual 3
        pc.appearance.black_ops mustEqual false
        pc.appearance.jammered mustEqual false
        pc.appearance.exosuit mustEqual ExoSuitType.Reinforced
        pc.appearance.outfit_name mustEqual "Black Beret Armoured Corps"
        pc.appearance.outfit_logo mustEqual 23
        pc.appearance.facingPitch mustEqual 7
        pc.appearance.facingYawUpper mustEqual 0
        pc.appearance.lfs mustEqual false
        pc.appearance.grenade_state mustEqual GrenadeState.None
        pc.appearance.is_cloaking mustEqual false
        pc.appearance.charging_pose mustEqual false
        pc.appearance.on_zipline mustEqual false
        pc.appearance.ribbons.upper mustEqual 276L
        pc.appearance.ribbons.middle mustEqual 239L
        pc.appearance.ribbons.lower mustEqual 397L
        pc.appearance.ribbons.tos mustEqual 360L
        pc.health mustEqual 255
        pc.armor mustEqual 253
        pc.uniform_upgrade mustEqual UniformStyle.ThirdUpgrade
        pc.command_rank mustEqual 5
        pc.implant_effects.isDefined mustEqual true
        pc.implant_effects.get mustEqual ImplantEffects.NoEffects
        pc.cosmetics.isDefined mustEqual true
        pc.cosmetics.get.no_helmet mustEqual true
        pc.cosmetics.get.beret mustEqual true
        pc.cosmetics.get.sunglasses mustEqual true
        pc.cosmetics.get.earpiece mustEqual true
        pc.cosmetics.get.brimmed_cap mustEqual false
        //short test of inventory items
        pc.inventory.isDefined mustEqual true
        val contents = pc.inventory.get.contents
        contents.size mustEqual 5
        //0
        contents.head.item.objectClass mustEqual ObjectClass.plasma_grenade
        contents.head.item.guid mustEqual PlanetSideGUID(3662)
        contents.head.item.parentSlot mustEqual 0
        contents.head.item.obj.asInstanceOf[WeaponData].fire_mode mustEqual 0
        contents.head.item.obj.asInstanceOf[WeaponData].ammo.objectClass mustEqual ObjectClass.plasma_grenade_ammo
        contents.head.item.obj.asInstanceOf[WeaponData].ammo.guid mustEqual PlanetSideGUID(3751)
        //1
        contents(1).item.objectClass mustEqual ObjectClass.bank
        contents(1).item.guid mustEqual PlanetSideGUID(3908)
        contents(1).item.parentSlot mustEqual 1
        contents(1).item.obj.asInstanceOf[WeaponData].fire_mode mustEqual 1
        contents(1).item.obj.asInstanceOf[WeaponData].ammo.objectClass mustEqual ObjectClass.armor_canister
        contents(1).item.obj.asInstanceOf[WeaponData].ammo.guid mustEqual PlanetSideGUID(4143)
        //2
        contents(2).item.objectClass mustEqual ObjectClass.mini_chaingun
        contents(2).item.guid mustEqual PlanetSideGUID(4164)
        contents(2).item.parentSlot mustEqual 2
        contents(2).item.obj.asInstanceOf[WeaponData].fire_mode mustEqual 0
        contents(2).item.obj.asInstanceOf[WeaponData].ammo.objectClass mustEqual ObjectClass.bullet_9mm
        contents(2).item.obj.asInstanceOf[WeaponData].ammo.guid mustEqual PlanetSideGUID(3728)
        //3
        contents(3).item.objectClass mustEqual ObjectClass.phoenix //actually, a decimator
        contents(3).item.guid mustEqual PlanetSideGUID(3603)
        contents(3).item.parentSlot mustEqual 3
        contents(3).item.obj.asInstanceOf[WeaponData].fire_mode mustEqual 0
        contents(3).item.obj.asInstanceOf[WeaponData].ammo.objectClass mustEqual ObjectClass.phoenix_missile
        contents(3).item.obj.asInstanceOf[WeaponData].ammo.guid mustEqual PlanetSideGUID(3056)
        //4
        contents(4).item.objectClass mustEqual ObjectClass.chainblade
        contents(4).item.guid mustEqual PlanetSideGUID(4088)
        contents(4).item.parentSlot mustEqual 4
        contents(4).item.obj.asInstanceOf[WeaponData].fire_mode mustEqual 1
        contents(4).item.obj.asInstanceOf[WeaponData].ammo.objectClass mustEqual ObjectClass.melee_ammo
        contents(4).item.obj.asInstanceOf[WeaponData].ammo.guid mustEqual PlanetSideGUID(3279)
        pc.drawn_slot mustEqual DrawnSlot.Rifle1
      case _ =>
        ko
    }
  }

  "decode (character, backpack)" in {
    PacketCoding.DecodePacket(string_character_backpack).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 924L
        cls mustEqual ObjectClass.avatar
        guid mustEqual PlanetSideGUID(3380)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[CharacterData] mustEqual true
        val pc = data.get.asInstanceOf[CharacterData]
        pc.appearance.pos.coord.x mustEqual 4629.8906f
        pc.appearance.pos.coord.y mustEqual 6316.4453f
        pc.appearance.pos.coord.z mustEqual 54.734375f
        pc.appearance.pos.roll mustEqual 0
        pc.appearance.pos.pitch mustEqual 0
        pc.appearance.pos.yaw mustEqual 115
        pc.appearance.pos.init_move.isDefined mustEqual false
        pc.appearance.basic_appearance.name mustEqual "Angello"
        pc.appearance.basic_appearance.faction mustEqual PlanetSideEmpire.VS
        pc.appearance.basic_appearance.sex mustEqual CharacterGender.Male
        pc.appearance.basic_appearance.head mustEqual 10
        pc.appearance.basic_appearance.voice mustEqual 2
        pc.appearance.voice2 mustEqual 0
        pc.appearance.black_ops mustEqual false
        pc.appearance.jammered mustEqual false
        pc.appearance.exosuit mustEqual ExoSuitType.MAX
        pc.appearance.outfit_name mustEqual "Original District"
        pc.appearance.outfit_logo mustEqual 23
        pc.appearance.facingPitch mustEqual 0
        pc.appearance.facingYawUpper mustEqual 192
        pc.appearance.lfs mustEqual false
        pc.appearance.grenade_state mustEqual GrenadeState.None
        pc.appearance.is_cloaking mustEqual false
        pc.appearance.charging_pose mustEqual false
        pc.appearance.on_zipline mustEqual false
        pc.appearance.ribbons.upper mustEqual 244L
        pc.appearance.ribbons.middle mustEqual 353L
        pc.appearance.ribbons.lower mustEqual 33L
        pc.appearance.ribbons.tos mustEqual 361L
        pc.health mustEqual 0
        pc.armor mustEqual 0
        pc.uniform_upgrade mustEqual UniformStyle.ThirdUpgrade
        pc.command_rank mustEqual 2
        pc.implant_effects.isDefined mustEqual false
        pc.cosmetics.isDefined mustEqual true
        pc.cosmetics.get.no_helmet mustEqual true
        pc.cosmetics.get.beret mustEqual true
        pc.cosmetics.get.sunglasses mustEqual true
        pc.cosmetics.get.earpiece mustEqual true
        pc.cosmetics.get.brimmed_cap mustEqual false
        pc.inventory.isDefined mustEqual false
        pc.drawn_slot mustEqual DrawnSlot.Pistol1
      case _ =>
        ko
    }
  }

  "encode (striker projectile)" in {
    val obj = TrackedProjectileData.striker(
      PlacementData(4644.5938f, 5472.0938f, 82.375f, 0, 245, 227),
      0
    )
    val msg = ObjectCreateMessage(ObjectClass.striker_missile_targeting_projectile, PlanetSideGUID(40192), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_striker_projectile
  }

  "encode (implant interface)" in {
    val obj = ImplantInterfaceData()
    val msg = ObjectCreateMessage(0x199, PlanetSideGUID(1075), ObjectCreateMessageParent(PlanetSideGUID(514), 1), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_implant_interface
  }

  "encode (order terminal a)" in {
    val obj = CommonTerminalData(PlacementData(Vector3(4579.3438f, 5615.0703f, 72.953125f), 0, 0, 125))
    val msg = ObjectCreateMessage(ObjectClass.order_terminala, PlanetSideGUID(3827), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_order_terminala
  }

  "encode (ace, held)" in {
    val obj = ACEData(4, 8)
    val msg = ObjectCreateMessage(ObjectClass.ace, PlanetSideGUID(3173), ObjectCreateMessageParent(PlanetSideGUID(3336), 0), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_ace_held
  }

  "encode (boomer trigger, held)" in {
    val obj = BoomerTriggerData(0)
    val msg = ObjectCreateMessage(ObjectClass.boomer_trigger, PlanetSideGUID(3600), ObjectCreateMessageParent(PlanetSideGUID(4272), 0), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_boomertrigger
  }

  "encode (detonater, held)" in {
    val obj = CommandDetonaterData(4)
    val msg = ObjectCreateMessage(ObjectClass.command_detonater, PlanetSideGUID(4162), ObjectCreateMessageParent(PlanetSideGUID(4149), 0), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_detonater_held
  }

  "encode (lasher, held)" in {
    val obj = WeaponData(8, 8, ObjectClass.energy_cell, PlanetSideGUID(3548), 0, AmmoBoxData(8))
    val msg = ObjectCreateMessage(ObjectClass.lasher, PlanetSideGUID(3033), ObjectCreateMessageParent(PlanetSideGUID(4141), 3), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_lasher_held
  }

  "encode (punisher, held)" in {
    val obj = ConcurrentFeedWeaponData(8, 8, 0,
      AmmoBoxData(ObjectClass.bullet_9mm, PlanetSideGUID(3918), 0, AmmoBoxData(8)) ::
        AmmoBoxData(ObjectClass.rocket, PlanetSideGUID(3941), 1, AmmoBoxData(8)) ::
        Nil
    )
    val msg = ObjectCreateMessage(ObjectClass.punisher, PlanetSideGUID(4147), ObjectCreateMessageParent(PlanetSideGUID(3092), 3), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_punisher_held
  }

  "encode (REK, held)" in {
    val obj = REKData(0, 8)
    val msg = ObjectCreateMessage(ObjectClass.remote_electronics_kit, PlanetSideGUID(3893), ObjectCreateMessageParent(PlanetSideGUID(4174), 0), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_rek_held
  }

  "encode (capture flag)" in {
    val obj = CaptureFlagData(PlacementData(3912.0312f, 5169.4375f, 59.96875f, 0, 0, 15), PlanetSideEmpire.NC, 21, 4, 2838, 9)
    val msg = ObjectCreateMessage(ObjectClass.capture_flag, PlanetSideGUID(4330), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_captureflag
  }

  "encode (ace, dropped)" in {
    val obj = DroppedItemData(
      PlacementData(Vector3(4708.461f, 5547.539f, 72.703125f), 0, 0, 91),
      ACEData(8, 8)
    )
    val msg = ObjectCreateMessage(ObjectClass.ace, PlanetSideGUID(4388), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_ace_dropped
  }

  "encode (detonator, dropped)" in {
    val obj = DroppedItemData(
      PlacementData(Vector3(4777.633f, 5485.4062f, 85.8125f), 0, 0, 27),
      CommandDetonaterData()
    )
    val msg = ObjectCreateMessage(ObjectClass.command_detonater, PlanetSideGUID(3682), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_detonater_dropped
  }

  "encode (shotgun shells, dropped)" in {
    val obj = DroppedItemData(
      PlacementData(Vector3(4684.7344f, 5547.4844f, 83.765625f), 0, 0, 89),
      AmmoBoxData()
    )
    val msg = ObjectCreateMessage(ObjectClass.shotgun_shell, PlanetSideGUID(3453), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_shotgunshell_dropped
  }

  "encode (lasher, dropped)" in {
    val obj = DroppedItemData(
      PlacementData(Vector3(4691.1953f, 5537.039f, 65.484375f), 0, 0, 32),
      WeaponData(8, 0, ObjectClass.energy_cell, PlanetSideGUID(3268), 0, AmmoBoxData())
    )
    val msg = ObjectCreateMessage(ObjectClass.lasher, PlanetSideGUID(3074), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_lasher_dropped
  }

  "encode (punisher, dropped)" in {
    val obj = DroppedItemData(
      PlacementData(Vector3(4789.133f, 5522.3125f, 72.3125f), 0, 0, 51),
      ConcurrentFeedWeaponData(4, 0, 0,
        AmmoBoxData(ObjectClass.bullet_9mm, PlanetSideGUID(3528), 0, AmmoBoxData()) ::
          AmmoBoxData(ObjectClass.rocket, PlanetSideGUID(3031), 1, AmmoBoxData()) ::
          Nil
      )
    )
    val msg = ObjectCreateMessage(ObjectClass.punisher, PlanetSideGUID(2978), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_punisher_dropped
  }

  "encode (REK, dropped)" in {
    val obj = DroppedItemData(
      PlacementData(Vector3(4675.039f, 5506.953f, 72.703125f), 0, 0, 78),
      REKData(8, 0, 3)
    )
    val msg = ObjectCreateMessage(ObjectClass.remote_electronics_kit, PlanetSideGUID(4355), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_rek_dropped
  }

  "encode (boomer)" in {
    val obj = SmallDeployableData(
      ACEDeployableData(
        PlacementData(Vector3(4704.172f, 5546.4375f, 82.234375f), 0, 0, 63),
        0, PlanetSideGUID(4145)
      )
    )
    val msg = ObjectCreateMessage(ObjectClass.boomer, PlanetSideGUID(3840), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_boomer
  }

  "encode (spitfire, short)" in {
    val obj = SmallTurretData(
      ACEDeployableData(
      PlacementData(Vector3(4577.7812f, 5624.828f, 72.046875f), 0, 127, 66),
      44,
      PlanetSideGUID(3871)
      ),
      255 //sets to 0
    )
    val msg = ObjectCreateMessage(ObjectClass.spitfire_turret, PlanetSideGUID(4208), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    val pkt_bitv = pkt.toBitVector
    val ori_bitv = string_spitfire_short.toBitVector
    pkt_bitv.take(173) mustEqual ori_bitv.take(173)
    pkt_bitv.drop(185) mustEqual ori_bitv.drop(185)
    //TODO work on SmallTurretData to make this pass as a single stream
  }

  "encode (spitfire)" in {
    val obj = SmallTurretData(
      ACEDeployableData(
        PlacementData(Vector3(4527.633f, 6271.3594f, 70.265625f), 0, 0, 105),
        68,
        PlanetSideGUID(4232)
      ),
      255,
      SmallTurretData.spitfire(PlanetSideGUID(3064), 0xC, 0x8, PlanetSideGUID(3694), 8)
    )
    val msg = ObjectCreateMessage(ObjectClass.spitfire_turret, PlanetSideGUID(4265), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    val pkt_bitv = pkt.toBitVector
    val ori_bitv = string_spitfire.toBitVector
    pkt_bitv.take(173) mustEqual ori_bitv.take(173)
    pkt_bitv.drop(185) mustEqual ori_bitv.drop(185)
    //TODO work on SmallTurretData to make this pass as a single stream
  }

  "encode (trap)" in {
    val obj = TRAPData(
      ACEDeployableData(
        PlacementData(Vector3(3572.4453f, 3277.9766f, 114.0f), 0, 0, 0),
        68,
        PlanetSideGUID(2502)
      ),
      255
    )
    val msg = ObjectCreateMessage(ObjectClass.tank_traps, PlanetSideGUID(2659), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    val pkt_bitv = pkt.toBitVector
    val ori_bitv = string_trap.toBitVector
    pkt_bitv.take(173) mustEqual ori_bitv.take(173)
    pkt_bitv.drop(185) mustEqual ori_bitv.drop(185)
    //TODO work on TRAPData to make this pass as a single stream
  }

  "encode (aegis)"  in {
    val obj = AegisShieldGeneratorData(
      ACEDeployableData(
        PlacementData(Vector3(3571.2266f, 3278.0938f, 114.0f), 0, 0, 0),
        68,
        PlanetSideGUID(2366)
      ),
      255
    )
    val msg = ObjectCreateMessage(ObjectClass.deployable_shield_generator, PlanetSideGUID(2556), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_aegis
  }

  "encode (orion)" in {
    val obj = OneMannedFieldTurretData(
      ACEDeployableData(
        PlacementData(Vector3(3567.1406f, 2988.0078f, 71.84375f), 0, 0, 94),
        68,
        PlanetSideGUID(0)
      ),
      PlanetSideGUID(2502),
      255,
      OneMannedFieldTurretData.orion(PlanetSideGUID(2615), 0xC, 0x8, PlanetSideGUID(2510), 8)
    )
    val msg = ObjectCreateMessage(ObjectClass.portable_manned_turret_vs, PlanetSideGUID(2916), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    val pkt_bitv = pkt.toBitVector
    val ori_bitv = string_orion.toBitVector
    pkt_bitv.take(189) mustEqual ori_bitv.take(189)
    pkt_bitv.drop(200) mustEqual ori_bitv.drop(200)
    //TODO work on OneMannedFieldTurretData to make this pass as a single stream
  }

  "encode (locker container)" in {
    val obj = LockerContainerData(
      InventoryData(
        InventoryItem(ObjectClass.nano_dispenser, PlanetSideGUID(2935), 0, WeaponData(0xC, 0x0, ObjectClass.armor_canister, PlanetSideGUID(3426), 0, AmmoBoxData())) ::
          InventoryItem(ObjectClass.armor_canister, PlanetSideGUID(4090), 45, AmmoBoxData()) ::
          InventoryItem(ObjectClass.armor_canister, PlanetSideGUID(3326), 78, AmmoBoxData()) ::
          Nil
      )
    )
    val msg = ObjectCreateMessage(ObjectClass.locker_container, PlanetSideGUID(3148), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_locker_container
  }

  "encode (character, alive)" in {
    val obj = CharacterData(
      CharacterAppearanceData(
        PlacementData(
          Vector3(3674.8438f, 2726.789f, 91.15625f),
          0, 0, 9,
          Some(Vector3(1.4375f, -0.4375f, 0f))
        ),
        BasicCharacterData(
          "ScrawnyRonnie",
          PlanetSideEmpire.TR,
          CharacterGender.Male,
          5,
          5
        ),
        3,
        false,
        false,
        ExoSuitType.Reinforced,
        "Black Beret Armoured Corps",
        23,
        false,
        7, 0,
        false,
        GrenadeState.None,
        false, false, false,
        RibbonBars(276L, 239L, 397L, 360L)
      ),
      255, 253,
      UniformStyle.ThirdUpgrade,
      5,
      Some(ImplantEffects.NoEffects),
      Some(Cosmetics(true, true, true, true, false)),
      InventoryData(
        InventoryItem(ObjectClass.plasma_grenade, PlanetSideGUID(3662), 0, WeaponData(0, 0, ObjectClass.plasma_grenade_ammo, PlanetSideGUID(3751), 0, AmmoBoxData())) ::
          InventoryItem(ObjectClass.bank, PlanetSideGUID(3908), 1, WeaponData(0, 0, 1, ObjectClass.armor_canister, PlanetSideGUID(4143), 0, AmmoBoxData())) ::
          InventoryItem(ObjectClass.mini_chaingun, PlanetSideGUID(4164), 2, WeaponData(0, 0, ObjectClass.bullet_9mm, PlanetSideGUID(3728), 0, AmmoBoxData())) ::
          InventoryItem(ObjectClass.phoenix, PlanetSideGUID(3603), 3, WeaponData(0, 0, ObjectClass.phoenix_missile, PlanetSideGUID(3056), 0, AmmoBoxData())) ::
          InventoryItem(ObjectClass.chainblade, PlanetSideGUID(4088), 4, WeaponData(0, 0, 1, ObjectClass.melee_ammo, PlanetSideGUID(3279), 0, AmmoBoxData())) ::
          Nil
      ),
      DrawnSlot.Rifle1
    )
    val msg = ObjectCreateMessage(ObjectClass.avatar, PlanetSideGUID(3902), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    val pkt_bitv = pkt.toBitVector
    val ori_bitv = string_character.toBitVector
    pkt_bitv.take(452) mustEqual ori_bitv.take(452) //skip 126
    pkt_bitv.drop(578).take(438) mustEqual ori_bitv.drop(578).take(438) //skip 2
    pkt_bitv.drop(1018).take(17) mustEqual ori_bitv.drop(1018).take(17) //skip 11
    pkt_bitv.drop(1046).take(147) mustEqual ori_bitv.drop(1046).take(147) //skip 3
    pkt_bitv.drop(1196) mustEqual ori_bitv.drop(1196)
    //TODO work on CharacterData to make this pass as a single stream
  }

  "encode (character, backpack)" in {
    val obj = CharacterData(
      CharacterAppearanceData(
        PlacementData(4629.8906f, 6316.4453f, 54.734375f, 0, 0, 115),
        BasicCharacterData(
          "Angello",
          PlanetSideEmpire.VS,
          CharacterGender.Male,
          10,
          2
        ),
        0,
        false,
        false,
        ExoSuitType.MAX,
        "Original District",
        23,
        true, //backpack
        0, 192,
        false,
        GrenadeState.None,
        false, false, false,
        RibbonBars(244L, 353L, 33L, 361L)
      ),
      0, 0,
      UniformStyle.ThirdUpgrade,
      2,
      None,
      Some(Cosmetics(true, true, true, true, false)),
      None,
      DrawnSlot.Pistol1
    )
    val msg = ObjectCreateMessage(ObjectClass.avatar, PlanetSideGUID(3380), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    val pkt_bitv = pkt.toBitVector
    val ori_bitv = string_character_backpack.toBitVector
    pkt_bitv.take(300) mustEqual ori_bitv.take(300) //skip 2
    pkt_bitv.drop(302).take(14) mustEqual ori_bitv.drop(302).take(14) //skip 126
    pkt_bitv.drop(442).take(317) mustEqual ori_bitv.drop(442).take(317) //skip 2
    pkt_bitv.drop(761).take(155) mustEqual ori_bitv.drop(761).take(155) //skip 1
    pkt_bitv.drop(917) mustEqual ori_bitv.drop(917)
    //TODO work on CharacterData to make this pass as a single stream
  }
}
