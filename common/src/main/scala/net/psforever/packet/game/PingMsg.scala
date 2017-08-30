// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/** Sent periodically by the PlanetSide client when connected to the Login server. Not encrypted
  *
  * @param serverSlot Which server on the server display is the ping referring to
  * @param ticks The number of ticks. Usually just reflected back to the client
  */
final case class PingMsg(serverSlot : Long, ticks : Long) extends PlanetSideGamePacket {
  type Packet = PingMsg
  def opcode = GamePacketOpcode.PingMsg
  def encode = PingMsg.encode(this)
}

object PingMsg extends Marshallable[PingMsg] {
  implicit val codec : Codec[PingMsg] = (
    ("server_slot" | uint32L) ::
      ("ticks" | uint32L)
    ).as[PingMsg]
}