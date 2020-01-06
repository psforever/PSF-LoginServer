// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._

/**
  * Used by PlanetSide in conjunction with wiredred/pscs.exe to establish local platoon/squad voice chat.
  * We are not focusing on implementation of this feature.
  * At the most, we will merely record data about who requested it.
  * @param unk na
  * @param player_guid the player who sent this request
  * @param data everything else
  */
final case class VoiceHostRequest(unk : Boolean,
                                  player_guid : PlanetSideGUID,
                                  data : ByteVector)
  extends PlanetSideGamePacket {
  type Packet = VoiceHostRequest
  def opcode = GamePacketOpcode.VoiceHostRequest
  def encode = VoiceHostRequest.encode(this)
}

object VoiceHostRequest extends Marshallable[VoiceHostRequest] {
  implicit val codec : Codec[VoiceHostRequest] = (
    ("unk" | bool) ::
      ("player_guid" | PlanetSideGUID.codec) ::
      ("data" | bytes)
    ).as[VoiceHostRequest]
}
