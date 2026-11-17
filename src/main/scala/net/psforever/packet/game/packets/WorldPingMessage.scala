// Copyright (c) 2026 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

package net.psforever.packet.game.packets

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/** The WorldPingMessage packet is a raw 9 byte UDP datagram sent by the client
  * from the server select screen (LoginComm::SendWorldPingProbesRaw @ 0x005fdb60).
  *
  * It is sent "out-of-band" directly to the game world endpoint(s) advertised in
  * VNLWorldStatusMessage.worlds(index).connections without SOE crypto or session wrappers.
  *
  * @param worldIndex index of the server in the world list (0 indexed)
  * @param sendTimestamp high-resolution timer tick value of the client - needs to be reflected back
  */
final case class WorldPingMessage(worldIndex: Long, sendTimestamp: Long) extends PlanetSideGamePacket {
  type Packet = WorldPingMessage
  def opcode = GamePacketOpcode.WorldPingMessage
  def encode = WorldPingMessage.encode(this)
}

object WorldPingMessage extends Marshallable[WorldPingMessage] {
  implicit val codec: Codec[WorldPingMessage] = (
      ("worldIndex" | uint32L) ::
      ("sendTimestamp" | uint32L)
  ).as[WorldPingMessage]
}
