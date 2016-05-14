// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class ConnectToWorldMessage(server : String, serverIp : String, port : Int)
  extends PlanetSideGamePacket {
  type Packet = ConnectToWorldMessage
  def opcode = GamePacketOpcode.ConnectToWorldMessage
  def encode = ConnectToWorldMessage.encode(this)
}

object ConnectToWorldMessage extends Marshallable[ConnectToWorldMessage] {
  implicit val codec : Codec[ConnectToWorldMessage] = (
      ("server_name" | PacketHelpers.encodedString) ::
      ("server_ip" | PacketHelpers.encodedString) ::
      ("server_port" | uint16L)
    ).as[ConnectToWorldMessage]
}