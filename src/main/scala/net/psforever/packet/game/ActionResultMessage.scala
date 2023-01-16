// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
 * Is sent by the server when the client has performed an action from a menu item
 * (i.e create character, delete character, etc...).
 * Error messages usually are accompanied by an angry beep.<br>
 * Error 0 is a common code but it doesn't do anything specific on its own.<br>
 * Error 1 generates the message box: a character with that name already exists.<br>
 * Error 2 generates the message box: something to do with the word filter.<br>
 * Other errors during the character login screen generate a generic error message box and list the code.
 */
final case class ActionResultMessage(errorCode: Option[Long]) extends PlanetSideGamePacket {
  type Packet = ActionResultMessage
  def opcode = GamePacketOpcode.ActionResultMessage
  def encode = ActionResultMessage.encode(this)
}

object ActionResultMessage extends Marshallable[ActionResultMessage] {
  /**
    * A message where the result is always a pass.
    * @return an `ActionResultMessage` object
    */
  def Pass: ActionResultMessage = ActionResultMessage(None)

  /**
    * A message where the result is always a failure.
    * @param error the error code
    * @return an `ActionResultMessage` object
    */
  def Fail(error: Long): ActionResultMessage = ActionResultMessage(Some(error))

  implicit val codec: Codec[ActionResultMessage] =
    ("error_code" | optional(bool.xmap[Boolean](state => !state, state => !state), uint32L)).as[ActionResultMessage]
}
