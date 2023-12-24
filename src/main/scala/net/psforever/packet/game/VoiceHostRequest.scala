// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Used by PlanetSide in conjunction with wiredred/pscs.exe to establish local platoon/squad voice chat.
  * We are not focusing on implementation of this feature.
  * At the most, we will merely record data about who requested it.
  *
  * @param remote_host true if the player provides info for a remote host (remote_ip)
  * @param port        the port to connect to
  * @param bandwidth   the bandwidth set by the player (valid values are 3, 201, 203)
  * @param remote_ip   the IP of the remote voice server, only set if remote_host == true
  */
final case class VoiceHostRequest(
    remote_host: Boolean,
    port: Int,
    bandwidth: Int,
    remote_ip: String
) extends PlanetSideGamePacket {
  require(port > 0)
  require(port <= 65535)
  require(bandwidth == 3 || bandwidth == 201 || bandwidth == 203)
  require(remote_host == (remote_ip != ""))

  def opcode = GamePacketOpcode.VoiceHostRequest
  def encode = VoiceHostRequest.encode(this)
}

object VoiceHostRequest extends Marshallable[VoiceHostRequest] {
  implicit val codec: Codec[VoiceHostRequest] = (
    ("remote_host" | bool) ::
      ("port" | uint16L) ::
      ("bandwidth" | uint8L) ::
      ("remote_ip" | PacketHelpers.encodedStringAligned(7))
    ).as[VoiceHostRequest]
}
