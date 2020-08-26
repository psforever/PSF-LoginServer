// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched from the server to cause a damage reaction from a specific target.
  * Infantry targets should be the primary target of this packet, as indicated by their identifier.
  * Infantry targets display their flinch animation.
  * All targets yelp in agony.
  * Infantry targets use their assigned voice.
  * Non-infantry targets use the "grizzled"(?) voice.
  * @param guid the target entity's global unique identifier
  * @param damage the amount of damsge being simulated
  */
final case class AggravatedDamageMessage(guid : PlanetSideGUID,
                                         damage : Long)
  extends PlanetSideGamePacket {
  type Packet = AggravatedDamageMessage
  def opcode = GamePacketOpcode.AggravatedDamageMessage
  def encode = AggravatedDamageMessage.encode(this)
}

object AggravatedDamageMessage extends Marshallable[AggravatedDamageMessage] {
  implicit val codec : Codec[AggravatedDamageMessage] = (
    ("guid" | PlanetSideGUID.codec) ::
      ("damage" | uint32L)
    ).as[AggravatedDamageMessage]
}
