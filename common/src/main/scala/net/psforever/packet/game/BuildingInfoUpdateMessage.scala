// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import scodec.bits._

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
                                           hacking_empire : PlanetSideEmpire.Value,
                                           hack_time_remaining : Long,
                                           owning_empire : PlanetSideEmpire.Value,
                                           unk1 : Long,
                                           generator_state : PlanetSideGeneratorState.Value,
                                           tubes_not_destroyed : Boolean,
                                           unk2 : Boolean,
                                           lattice_benefits : Int,
                                           unk3 : Int,
                                           unk4 : Int,
                                           unk5 : Long,
                                           unk6 : Boolean,
                                           unk7 : Int,
                                           unk8 : Boolean,
                                           unk9 : Boolean)
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
      ("hacking_empire" | PlanetSideEmpire.codec) ::
      ("hack_time_remaining" | uint32L ) :: //In milliseconds
      ("owning_empire" | PlanetSideEmpire.codec) ::
      ("unk1" | uint32L) :: //TODO: string, uint16L, and uint32L follow if unk1 != 0
      
      ("generator_state" | PlanetSideGeneratorState.codec) ::
      ("tubes_not_destroyed" | bool) ::
      ("unk2" | bool) ::
      ("lattice_benefits" | uintL(5)) :: //5 possible benefits, bitwise combination. (MSB)5:Tech 4:Inter 3:Bio 2:Drop 1:Amp(LSB)
      ("unk3" | uintL(10)) :: //module and cavern lock benefit, not sure how encoded. Probably bitwise given 6 modules and 4 caverns. Modules probably just also need timer.
      ("unk4" | uint4L) :: //TODO: additional fields if unk4 > 0
      
      ("unk5" | uint32L) ::
      ("unk6" | bool) ::
      ("unk7" | uint4L) :: //TODO: bool and uintL(2) follow if unk7 != 8

      ("unk8" | bool) ::
      ("unk9" | bool)
    ).as[BuildingInfoUpdateMessage]
}
