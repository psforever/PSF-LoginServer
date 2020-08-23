// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched from the server, sending a "priority message" to the given client's avatar.
  * The messaging inbox is generally accessible through the use of `alt`+`i`.
  * It is also made accessible through use of an icon in the lower right corner when there is an outstanding message.<br>
  * <br>
  * Exploration:<br>
  * How does the PlanetSide Classic mail system work?
  * At the moment, it only seems possible to receive and read mail from the server.
  * @param sender the name of the player who sent the mail
  * @param subject the subject
  * @param message the message
  */
final case class MailMessage(sender: String, subject: String, message: String) extends PlanetSideGamePacket {
  type Packet = MailMessage
  def opcode = GamePacketOpcode.MailMessage
  def encode = MailMessage.encode(this)
}

object MailMessage extends Marshallable[MailMessage] {
  implicit val codec: Codec[MailMessage] = (
    ("sender" | PacketHelpers.encodedString) ::
      ("subject" | PacketHelpers.encodedString) ::
      ("message" | PacketHelpers.encodedString)
  ).as[MailMessage]
}
