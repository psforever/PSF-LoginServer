// Copyright (c) 2021 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import shapeless.HNil
import scodec.codecs._

/**
  * A representation of a battle frame robotics vehicle.
  * @param pos where the vehicle is and how it is oriented in the game world
  * @param data common vehicle field data
  * @param health the amount of health the vehicle has, as a percentage of a filled bar (255)
  * @param shield the strength of the shield the vehicle has, as a percentage of a filled bar (255)
  * @param unk1 na
  * @param unk2 na
  * @param no_mount_points do not display entry points for the seats
  * @param driveState a representation for the current mobility state;
  *                   various vehicles also use this field to indicate "deployment," e.g., the advanced mobile spawn
  * @param proper_anim na;
  *                    I forget what this does
  * @param unk3 na
  * @param show_bfr_shield display the swirling shield of the battle frame
  * @param unk4 na
  * @param inventory the seats, mounted weapons, and utilities (such as terminals) that are currently included;
  *                  will also include trunk contents;
  *                  the driver is the only valid seat entry (more will cause the access permissions to act up)
  */
final case class BattleFrameRoboticsData(
                                          pos: PlacementData,
                                          data: CommonFieldData,
                                          health: Int,
                                          shield: Int,
                                          unk1: Int,
                                          unk2: Boolean,
                                          no_mount_points: Boolean,
                                          driveState: Int,
                                          proper_anim: Boolean,
                                          unk3: Int,
                                          show_bfr_shield: Boolean,
                                          unk4: Option[Boolean],
                                          inventory: Option[InventoryData] = None
                                        ) extends ConstructorData {
  override def bitsize: Long = {
    val posSize: Long = pos.bitsize
    val dataSize: Long = data.bitsize
    val unk4Size = unk4 match {
      case Some(_) => 1L
      case None => 0L
    }
    val inventorySize = inventory match {
      case Some(inv) => inv.bitsize
      case None => 0L
    }
    49L + posSize + dataSize + unk4Size + inventorySize
  }
}

object BattleFrameRoboticsData extends Marshallable[BattleFrameRoboticsData] {
  implicit val codec : Codec[BattleFrameRoboticsData] = {
    import shapeless.::
    (
      ("pos" | PlacementData.codec) >>:~ { pos =>
        ("data" | CommonFieldData.codec(extra = false)) ::
        ("health" | uint8L) ::
        ("shield" | uint8L) ::
        ("unk1" | uint16) :: //usually 0
        ("unk2" | bool) ::
        ("no_mount_points" | bool) ::
        ("driveState" | uint8L) :: //used for deploy state
        ("proper_anim" | bool) :: //when unflagged, bfr stands, even if unmanned
        ("unk3" | uint4) ::
        ("show_bfr_shield" | bool) ::
        optional(bool, target = "inventory" | MountableInventory.custom_inventory_codec(pos.vel.isDefined, VehicleFormat.Battleframe))
      }
      ).exmap[BattleFrameRoboticsData] (
      {
        case pos :: data :: health :: shield :: 0 :: u2 :: no_mount :: drive :: proper_anim :: u3 :: show_bfr_shield :: inv :: HNil =>
          Attempt.successful(BattleFrameRoboticsData(pos, data, health, shield, 0, u2, no_mount, drive, proper_anim, u3, show_bfr_shield, None, inv))

        case data =>
          Attempt.failure(Err(s"decoding invalid battleframe data - $data"))
      },
      {
        case BattleFrameRoboticsData(pos, data, health, shield, 0, u2, no_mount, drive, proper_anim, u3, show_bfr_shield, None, inv) =>
          Attempt.successful(pos :: data :: health :: shield :: 0 :: u2 :: no_mount :: drive :: proper_anim :: u3 :: show_bfr_shield :: inv :: HNil)

        case data =>
          Attempt.failure(Err(s"encoding invalid battleframe data - $data"))
      }
    )
  }

  val codec_flight: Codec[BattleFrameRoboticsData] = {
    import shapeless.::
    (
      ("pos" | PlacementData.codec) >>:~ { pos =>
        ("data" | CommonFieldData.codec(extra = false)) ::
        ("health" | uint8L) ::
        ("shield" | uint8L) ::
        ("unk1" | uint16) :: //usually 0
        ("unk2" | bool) ::
        ("no_mount_points" | bool) ::
        ("driveState" | uint8L) :: //used for deploy state
        ("proper_anim" | bool) :: //when unflagged, bfr stands, even if unmanned
        ("unk3" | uint4) ::
        ("show_bfr_shield" | bool) ::
        ("unk4" | bool) ::
        optional(bool, target = "inventory" | MountableInventory.custom_inventory_codec(pos.vel.isDefined, VehicleFormat.BattleframeFlight))
      }
      ).exmap[BattleFrameRoboticsData] (
      {
        case pos :: data :: health :: shield :: 0 :: u2 :: no_mount :: drive :: proper_anim :: u3 :: show_bfr_shield :: unk4 :: inv :: HNil =>
          Attempt.successful(BattleFrameRoboticsData(pos, data, health, shield, 0, u2, no_mount, drive, proper_anim, u3, show_bfr_shield, Some(unk4), inv))

        case data =>
          Attempt.failure(Err(s"decoding invalid battleframe data - $data"))
      },
      {
        case BattleFrameRoboticsData(pos, data, health, shield, 0, u2, no_mount, drive, proper_anim, u3, show_bfr_shield, Some(unk4), inv) =>
          Attempt.successful(pos :: data :: health :: shield :: 0 :: u2 :: no_mount :: drive :: proper_anim :: u3 :: show_bfr_shield :: unk4 :: inv :: HNil)

        case data =>
          Attempt.failure(Err(s"encoding invalid battleframe data - $data"))
      }
    )
  }
}
