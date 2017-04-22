// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the client when its player is done using something.
  * The classic example is sifting through backpacks, an exclusive activity that only one player can do at a time.
  */
final case class UnuseItemMessage(guid1 : PlanetSideGUID,
                                  guid2 : PlanetSideGUID)
  extends PlanetSideGamePacket {
  type Packet = UnuseItemMessage
  def opcode = GamePacketOpcode.UnuseItemMessage
  def encode = UnuseItemMessage.encode(this)
}

object UnuseItemMessage extends Marshallable[UnuseItemMessage] {
  implicit val codec : Codec[UnuseItemMessage] = (
    ("guid1" | PlanetSideGUID.codec) ::
      ("guid2" | PlanetSideGUID.codec)
    ).as[UnuseItemMessage]
}
