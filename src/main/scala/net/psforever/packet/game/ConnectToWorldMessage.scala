// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Is sent in response to the PlanetSide client in order to give server information on the world server.
  * @param server The name of the server to connect to
  * @param serverAddress The IP or hostname of the server to connect to
  * @param port The 16-bit port number to connect on
  */
final case class ConnectToWorldMessage(server: String, serverAddress: String, port: Int) extends PlanetSideGamePacket {
  type Packet = ConnectToWorldMessage
  def opcode = GamePacketOpcode.ConnectToWorldMessage
  def encode = ConnectToWorldMessage.encode(this)
}

object ConnectToWorldMessage extends Marshallable[ConnectToWorldMessage] {
  implicit val codec: Codec[ConnectToWorldMessage] = (
    ("server_name" | PacketHelpers.encodedString) ::
      ("server_address" | PacketHelpers.encodedString) ::
      ("server_port" | uint16L)
  ).as[ConnectToWorldMessage]
}
