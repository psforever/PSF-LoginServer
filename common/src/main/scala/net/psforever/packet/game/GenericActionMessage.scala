// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Reports that something has happened.<br>
  * <br>
  * When sent from the server to a client, there are twenty-seven individual actions caused by this packet.
  * They are only vaguely organized by behavior and some numbers may not be associated with an action.
  * When sent by the client to the server, an unknown number of actions are available (update as discovered).
  * The maximum known action is a server-sent 45, or `0xB4`.<br>
  *<br>
  * Actions (when sent from server):<br>
  * 03 - `0x0C` - symbol: show Mosquito radar<br>
  * 04 - `0x10` - symbol: hide Mosquito radar<br>
  * 07 - `0x1C` - warning: missile lock<br>
  * 08 - `0x20` - warning: Wasp missile lock<br>
  * 09 - `0x24` - warning: T-REK lock<br>
  * 12 - `0x30` - sound: base captured fanfare<br>
  * 14 - `0x38` - prompt: new character basic training<br>
  * 22 - `0x58` - message: awarded a cavern capture (updates cavern capture status)<br>
  * 23 - `0x5C` - award a cavern kill<br>
  * 24 - `0x60` - message: you have been imprinted (updates imprinted status; does it?)<br>
  * 25 - `0x64` - message: you are no longer imprinted (updates imprinted status; does it?)<br>
  * 27 - `0x6C` - event: purchase timers reset (does it?)<br>
  * 31 - `0x7C` - switch to first person view, attempt to deconstruct but fail;
  *               event: fail to deconstruct due to having a "parent vehicle"<br>
  * 32 - `0x80` - switch to first person view<br>
  * 33 - `0x84` - event: fail to deconstruct<br>
  * 43 - `0xAC` - prompt: friendly fire in virtual reality zone<br>
  * <br>
  * Actions (when sent from client):<br>
  * 36 - `0x90` - turn on "Looking for Squad"<br>
  * 37 - `0x94` - turn off "Looking for Squad"<br>
  * <br>
  * Exploration:<br>
  * Well, get to it. :P
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
