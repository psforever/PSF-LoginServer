// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideEmpire
import scodec.Codec
import scodec.codecs._

/**
 * The object_guid space for SetEmpireMessage is continent specific.
 * SetEmpireMessage is usually followed by HackMessage (indicating the hack disposition of the same object_guid)
 */
final case class SetEmpireMessage(object_guid : PlanetSideGUID,
                                   empire : PlanetSideEmpire.Value)
  extends PlanetSideGamePacket {
  type Packet = SetEmpireMessage
  def opcode = GamePacketOpcode.SetEmpireMessage
  def encode = SetEmpireMessage.encode(this)
}

object SetEmpireMessage extends Marshallable[SetEmpireMessage] {
  implicit val codec : Codec[SetEmpireMessage] = (
      ("object_guid" | PlanetSideGUID.codec) ::
        ("empire" | PlanetSideEmpire.codec)
    ).as[SetEmpireMessage]
}
