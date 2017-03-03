// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._

/**
  * Used by PlanetSide in conjunction with wiredred/pscs.exe to establish local platoon/squad voice chat.
  * We are not focusing on implementation of this feature.
  * This packet should not be generated because `VoiceHostRequest` will be ignored.
  * @param player_guid the player who sent this info (the originator of voice chat?)
  * @param data everything else
  */
final case class VoiceHostInfo(player_guid : PlanetSideGUID,
                               data : ByteVector)
  extends PlanetSideGamePacket {
  type Packet = VoiceHostInfo
  def opcode = GamePacketOpcode.VoiceHostInfo
  def encode = VoiceHostInfo.encode(this)
}

object VoiceHostInfo extends Marshallable[VoiceHostInfo] {
  implicit val codec : Codec[VoiceHostInfo] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("data" | bytes)
    ).as[VoiceHostInfo]
}
