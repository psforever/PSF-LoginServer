// Copyright (c) 2016 PSForever.net to present
package psforever.net

import scodec.Codec
import scodec.codecs._

final case class ConnectToWorldMessage(world : String)
  extends PlanetSideGamePacket {
  type Packet = ConnectToWorldMessage
  def opcode = GamePacketOpcode.ConnectToWorldMessage
  def encode = ConnectToWorldMessage.encode(this)
}

object ConnectToWorldMessage extends Marshallable[ConnectToWorldMessage] {
  implicit val codec : Codec[ConnectToWorldMessage] = ascii.as[ConnectToWorldMessage]
}