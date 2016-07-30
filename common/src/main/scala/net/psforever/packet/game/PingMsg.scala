// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/** Sent periodically by the PlanetSide client when connected to the Login server. Not encrypted
  *
  * @param unk1
  * @param unk2
  */
final case class PingMsg(unk1 : Long, unk2 : Long) extends PlanetSideGamePacket {
  type Packet = PingMsg
  def opcode = GamePacketOpcode.PingMsg
  def encode = PingMsg.encode(this)
}

object PingMsg extends Marshallable[PingMsg] {
  implicit val codec : Codec[PingMsg] = (
    ("unk1" | uint32L) ::
      ("unk2" | uint32L)
    ).as[PingMsg]
}