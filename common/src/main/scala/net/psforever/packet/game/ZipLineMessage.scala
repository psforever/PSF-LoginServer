// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * The player is interacting with a zipline.
  * @param player_guid the player
  * @param origin_side whether this corresponds with the "entry" or the "exit" of the zipline, as per the direction of the light pulse visuals
  * @param unk1 na; usually 0?
  * @param unk2 na; a number that is consistent to a zipline point but is not a building GUID
  * @param unk3 na; changes as the user moves
  * @param unk4 na; changes as the user moves
  * @param unk5 na; a number that is consistent to a zipline point
  */
final case class ZipLineMessage(player_guid : PlanetSideGUID,
                                origin_side : Boolean,
                                unk1 : Int,
                                unk2 : Long,
                                unk3 : Long,
                                unk4 : Long,
                                unk5 : Long)
  extends PlanetSideGamePacket {
  type Packet = ZipLineMessage
  def opcode = GamePacketOpcode.ZipLineMessage
  def encode = ZipLineMessage.encode(this)
}

object ZipLineMessage extends Marshallable[ZipLineMessage] {
  implicit val codec : Codec[ZipLineMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("origin_side" | bool) ::
      ("unk1" | uintL(2)) ::
      ("unk2" | uint32L) ::
      ("unk3" | uint32L) ::
      ("unk4" | uint32L) ::
      ("unk5" | uint32L)
    ).as[ZipLineMessage]
}
