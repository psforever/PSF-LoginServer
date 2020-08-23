// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Is sent by the server when the client has performed an action from a menu item
  * (i.e create character, delete character, etc...)
  */
final case class ActionResultMessage(successful: Boolean, errorCode: Option[Long]) extends PlanetSideGamePacket {
  type Packet = ActionResultMessage
  def opcode = GamePacketOpcode.ActionResultMessage
  def encode = ActionResultMessage.encode(this)
}

object ActionResultMessage extends Marshallable[ActionResultMessage] {

  /**
    * A message where the result is always a pass.
    * @return an `ActionResultMessage` object
    */
  def Pass: ActionResultMessage = ActionResultMessage(true, None)

  /**
    * A message where the result is always a failure.
    * @param error the error code
    * @return an `ActionResultMessage` object
    */
  def Fail(error: Long): ActionResultMessage = ActionResultMessage(false, Some(error))

  implicit val codec: Codec[ActionResultMessage] = (
    ("successful" | bool) >>:~ { res =>
      // if not successful, look for an error code
      conditional(!res, "error_code" | uint32L).hlist
    }
  ).as[ActionResultMessage]
}
