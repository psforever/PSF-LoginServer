// Copyright (c) 2019 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Information about a specific squad member.
  * @param char_id the character's unique identifier
  * @param health the character's health value percentage, divided into 64 units
  * @param armor the character's armor value percentage, divided into 64 units
  * @param pos the world coordinates of the character
  * @param unk4 na;
  *             usually, 2
  * @param unk5 na;
  *             usually, 2
  * @param unk6 na;
  *             usually, false
  * @param unk7 na
  * @param unk8 na;
  *             if defined, will be defined with unk9
  * @param unk9 na;
  *             if defined, will be defined with unk8
  */
final case class SquadStateInfo(char_id : Long,
                                health : Int,
                                armor : Int,
                                pos : Vector3,
                                unk4 : Int,
                                unk5 : Int,
                                unk6 : Boolean,
                                unk7 : Int,
                                unk8 : Option[Int],
                                unk9 : Option[Boolean])

/**
  * Dispatched by the server to update a squad member's representative icons on the continental maps and the interstellar map.<br>
  * <br>
  * This packet must be preceded by the correct protocol
  * to assign any character who is defined by `char_id` in `info_list`
  * as a member of this client's player's assigned squad by means of associating that said `char_id`.
  * The said preceding protocol also assigns the player's current zone (continent) and their ordinal position in the squad.
  * @see `SquadMemberEvent`
  * @param guid the squad's unique identifier;
  *             must be consistent per packet on a given client;
  *             does not have to be the global uid of the squad as according to the server
  * @param info_list information about the members in this squad who will be updated
  */
final case class SquadState(guid : PlanetSideGUID,
                            info_list : List[SquadStateInfo])
  extends PlanetSideGamePacket {
  type Packet = SquadState
  def opcode = GamePacketOpcode.SquadState
  def encode = SquadState.encode(this)
}

object SquadStateInfo {
  def apply(unk1 : Long, unk2 : Int, unk3 : Int, pos : Vector3, unk4 : Int, unk5 : Int, unk6 : Boolean, unk7 : Int) : SquadStateInfo =
    SquadStateInfo(unk1, unk2, unk3, pos, unk4, unk5, unk6, unk7, None, None)

  def apply(unk1 : Long, unk2 : Int, unk3 : Int, pos : Vector3, unk4 : Int, unk5 : Int, unk6 : Boolean, unk7 : Int, unk8 : Int, unk9 : Boolean) : SquadStateInfo =
    SquadStateInfo(unk1, unk2, unk3, pos, unk4, unk5, unk6, unk7, Some(unk8), Some(unk9))
}

object SquadState extends Marshallable[SquadState] {
  private val info_codec : Codec[SquadStateInfo] = (
    ("char_id" | uint32L) ::
      ("health" | uint(7)) ::
      ("armor" | uint(7)) ::
      ("pos" | Vector3.codec_pos) ::
      ("unk4" | uint2) ::
      ("unk5" | uint2) ::
      ("unk6" | bool) ::
      ("unk7" | uint16L) ::
      (bool >>:~ { out =>
        conditional(out, "unk8" | uint16L) ::
          conditional(out, "unk9" | bool)
      })
    ).exmap[SquadStateInfo] (
    {
      case char_id :: health :: armor :: pos :: u4 :: u5 :: u6 :: u7 :: _ :: u8 :: u9 :: HNil =>
        Attempt.Successful(SquadStateInfo(char_id, health, armor, pos, u4, u5, u6, u7, u8, u9))
    },
    {
      case SquadStateInfo(char_id, health, armor, pos, u4, u5, u6, u7, Some(u8), Some(u9)) =>
        Attempt.Successful(char_id :: health :: armor :: pos :: u4 :: u5 :: u6 :: u7 :: true :: Some(u8) :: Some(u9) :: HNil)
      case SquadStateInfo(char_id, health, armor, pos, u4, u5, u6, u7, None, None) =>
        Attempt.Successful(char_id :: health :: armor :: pos :: u4 :: u5 :: u6 :: u7 :: false :: None :: None :: HNil)
      case data @ (SquadStateInfo(_, _, _, _, _, _, _, _, Some(_), None) | SquadStateInfo(_, _, _, _, _, _, _, _, None, Some(_))) =>
        Attempt.Failure(Err(s"SquadStateInfo requires both unk8 and unk9 to be either defined or undefined at the same time - $data"))
    }
  )

  implicit val codec : Codec[SquadState] = (
    ("guid" | PlanetSideGUID.codec) ::
      ("info_list" | listOfN(uint4, info_codec))
    ).as[SquadState]
}
