// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Reports that something has happened.<br>
  * <br>
  * There are twenty-seven individual actions whose activities are changed by the client that are communicated by this packet.
  * They are not numbered sequentially and some numbers are not associated with an action.
  * The maximum known action is 45, or `0xB4`, and the lowest is 0, `0x0`.<br>
  *<br>
  * Actions:<br>
  * `0x90` - 36 - turn on "Looking for Squad"<br>
  * `0x94` - 37 - turn off "Looking for Squad"<br>
  * <br>
  * Exploration:<br>
  * It could take a long time to manually track down what each of these values does.
  * @param action what this packet is about
  */
final case class GenericActionMessage(action : Int)
  extends PlanetSideGamePacket {
  type Packet = GenericActionMessage
  def opcode = GamePacketOpcode.GenericActionMessage
  def encode = GenericActionMessage.encode(this)
}

object GenericActionMessage extends Marshallable[GenericActionMessage] {
  implicit val codec : Codec[GenericActionMessage] = (
    "action" | uint(6)
    ).as[GenericActionMessage]
}
