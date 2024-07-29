package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.Codec
import scodec.codecs._

final case class ClientHotStart(clientNonce: Long, ServerNonce: Long) extends PlanetSideControlPacket {
  type Packet = ClientHotStart
  def opcode = ControlPacketOpcode.ClientHotStart
  def encode = ClientHotStart.encode(this)
}

object ClientHotStart extends Marshallable[ClientHotStart] {
  implicit val codec: Codec[ClientHotStart] = (
    ("client_nonce" | uint32L) ::
      ("server_nonce" | uint32L)
    ).as[ClientHotStart]
}
