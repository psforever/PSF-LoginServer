// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Created by SouNourS on 10/08/2016.
  */

final case class BuildingInfoUpdateMessage(continent_guid : PlanetSideGUID,
                                           building_guid : PlanetSideGUID,
                                           ntu_lvl : Int,
                                           unk1 : Int,
                                           unk2 : Int,
                                           building_Empire : Int,
                                           unk3 : Int,
                                           building_Status : Int,
                                           unk4 : Int,
                                           unk5 : Int,
                                           unk6 : Int)
  extends PlanetSideGamePacket {
  type Packet = BuildingInfoUpdateMessage
  def opcode = GamePacketOpcode.BuildingInfoUpdateMessage
  def encode = BuildingInfoUpdateMessage.encode(this)
}

object BuildingInfoUpdateMessage extends Marshallable[BuildingInfoUpdateMessage] {
  implicit val codec : Codec[BuildingInfoUpdateMessage] = (
    ("continent_guid" | PlanetSideGUID.codec) ::
      ("building_guid" | PlanetSideGUID.codec) ::
      ("ntu_lvl" | uint8L) ::
      ("unk1" | uint8L) ::
      ("unk2" | uint8L) ::
      ("building_Empire" | uint24L) ::
      ("unk3" | uint16L) ::
      ("building_Status" | uint16L) ::
      ("unk4" | uint24L) ::
      ("unk5" | uint24L) ::
      ("unk6" | uint8L)

    ).as[BuildingInfoUpdateMessage]
}