// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class SetEmpireMessage(continent_guid : PlanetSideGUID,
                                   empire : PlanetSideEmpire.Value)
  extends PlanetSideGamePacket {
  type Packet = SetEmpireMessage
  def opcode = GamePacketOpcode.SetEmpireMessage
  def encode = SetEmpireMessage.encode(this)
}

object SetEmpireMessage extends Marshallable[SetEmpireMessage] {
  implicit val codec : Codec[SetEmpireMessage] = (
      ("continent_guid" | PlanetSideGUID.codec) ::
        ("empire" | PlanetSideEmpire.codec)
    ).as[SetEmpireMessage]
}
