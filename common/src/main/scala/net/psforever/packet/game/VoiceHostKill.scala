// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec

/**
  * Used by PlanetSide in conjunction with wiredred/pscs.exe to establish local platoon/squad voice chat.
  * We are not focusing on implementation of this feature.
  * As a precaution, all attempts at sending `VoiceHostRequest` should be replied to with a `VoiceHostKill`.
  * This packet seems to publish no data.
  */
final case class VoiceHostKill()
  extends PlanetSideGamePacket {
  type Packet = VoiceHostKill
  def opcode = GamePacketOpcode.VoiceHostKill
  def encode = VoiceHostKill.encode(this)
}

object VoiceHostKill extends Marshallable[VoiceHostKill] {
  implicit val codec : Codec[VoiceHostKill] = PacketHelpers.emptyCodec(VoiceHostKill())
}
