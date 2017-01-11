// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

object PlanetSideGeneratorState extends Enumeration {
  type Type = Value
  val Normal,
      Critical,
      Destroyed,
      Unk3
       = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uintL(2))
}

/**
 * BuildingInfoUpdateMessage is sent for all bases, towers, and warpgates from all continents upon login.
 */
final case class BuildingInfoUpdateMessage(continent_guid : PlanetSideGUID,
                                           building_guid : PlanetSideGUID,
                                           ntu_level : Int,
                                           is_hacked : Boolean,
                                           empire_hack : PlanetSideEmpire.Value,
                                           hack_time_remaining : Long,
                                           empire_own : PlanetSideEmpire.Value,
                                           unk1 : Long,
                                           generator_state : PlanetSideGeneratorState.Value,
                                           spawn_tubes_normal : Boolean,
                                           force_dome_active : Boolean,
                                           lattice_benefit : Int,
                                           unk3 : Int,
                                           unk4 : Int,
                                           unk5 : Long,
                                           unk6 : Boolean,
                                           unk7 : Int,
                                           boost_spawn_pain : Boolean,
                                           boost_generator_pain : Boolean)
  extends PlanetSideGamePacket {
  type Packet = BuildingInfoUpdateMessage
  def opcode = GamePacketOpcode.BuildingInfoUpdateMessage
  def encode = BuildingInfoUpdateMessage.encode(this)
}

object BuildingInfoUpdateMessage extends Marshallable[BuildingInfoUpdateMessage] {
  implicit val codec : Codec[BuildingInfoUpdateMessage] = (
      ("continent_guid" | PlanetSideGUID.codec) ::
      ("building_guid" | PlanetSideGUID.codec) ::
      ("ntu_level" | uint4L) ::
      ("is_hacked" | bool ) ::
      ("empire_hack" | PlanetSideEmpire.codec) ::
      ("hack_time_remaining" | uint32L ) :: //In milliseconds
      ("empire_own" | PlanetSideEmpire.codec) ::
      ("unk1" | uint32L) :: //TODO: string, uint16L, and uint32L follow if unk1 != 0
      
      ("generator_state" | PlanetSideGeneratorState.codec) ::
      ("spawn_tubes_normal" | bool) ::
      ("force_dome_active" | bool) ::
      ("lattice_benefit" | uintL(5)) :: //5 possible benefits, bitwise combination. (MSB)5:Tech 4:Inter 3:Bio 2:Drop 1:Amp(LSB)
      ("unk3" | uintL(10)) :: //Module related. 0x3FF gives all modules with no timer. Unclear how this works.
      ("unk4" | uint4L) :: //TODO: additional fields if unk4 > 0
      
      ("unk5" | uint32L) ::
      ("unk6" | bool) ::
      ("unk7" | uint4L) :: //TODO: bool and uintL(2) follow if unk7 != 8
      ("boost_spawn_pain" | bool) ::
      ("boost_generator_pain" | bool)
    ).as[BuildingInfoUpdateMessage]
}
