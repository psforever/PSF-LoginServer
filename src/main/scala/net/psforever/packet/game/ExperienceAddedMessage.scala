// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.bits.BitVector
import scodec.{Attempt, Codec}
import scodec.codecs._

/**
  * Displays a message about being awarded experience points in the events chat.<br>
  * <br>
  * This packet does not actually award any experience points.
  * It merely generates the message:<br>
  *   `"You have been awarded x experience points."`<br>
  * ... where `x` is the number of experience points that have been promised.
  * @param exp the number of (Command) experience points earned
  * @param cmd if `true`, the message will be tailored for "Command" experience;
  *            if `false`, the number of experience points and the "Command" flair will be blanked
  */
final case class ExperienceAddedMessage(exp: Int, cmd: Boolean) extends PlanetSideGamePacket {
  type Packet = ExperienceAddedMessage
  def opcode: GamePacketOpcode.Value = GamePacketOpcode.ExperienceAddedMessage
  def encode: Attempt[BitVector] = ExperienceAddedMessage.encode(this)
}

object ExperienceAddedMessage extends Marshallable[ExperienceAddedMessage] {
  /**
   * Produce a packet whose message to the event chat is
   * "You have been awarded experience points."
   * @return `ExperienceAddedMessage` packet
   */
  def apply(): ExperienceAddedMessage = ExperienceAddedMessage(0, cmd = false)

  /**
   * Produce a packet whose message to the event chat is
   * "You have been awarded 'exp' Command experience points."
   * @param exp the number of Command experience points earned
   * @return `ExperienceAddedMessage` packet
   */
  def apply(exp: Int): ExperienceAddedMessage = ExperienceAddedMessage(exp, cmd = true)

  implicit val codec: Codec[ExperienceAddedMessage] = (
    ("exp" | uintL(bits = 15)) :: ("unk" | bool)
  ).as[ExperienceAddedMessage]
}
