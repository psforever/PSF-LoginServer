// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Displays a message about being awarded experience points in the events chat.<br>
  * <br>
  * This packet does not actually award any experience points.
  * It merely generates the message:<br>
  *   `"You have been awarded x experience points."`<br>
  * ... where `x` is the number of experience points that have been promised.
  * If the `Boolean` parameter is `true`, `x` will be equal to the number provided followed by the word "Command."
  * If the `Boolean` parameter is `false`, `x` will be represented as an obvious blank space character.
  * (Yes, it prints to the events chat like that.)
  * @param exp the number of (Command) experience points earned
  * @param unk defaults to `true` for effect;
  *            if `false`, the number of experience points in the message will be blanked
  */
final case class ExperienceAddedMessage(exp : Int,
                                        unk : Boolean = true)
  extends PlanetSideGamePacket {
  type Packet = ExperienceAddedMessage
  def opcode = GamePacketOpcode.ExperienceAddedMessage
  def encode = ExperienceAddedMessage.encode(this)
}

object ExperienceAddedMessage extends Marshallable[ExperienceAddedMessage] {
  implicit val codec : Codec[ExperienceAddedMessage] = (
    ("cep" | uintL(15)) ::
      ("unk" | bool)
    ).as[ExperienceAddedMessage]
}
