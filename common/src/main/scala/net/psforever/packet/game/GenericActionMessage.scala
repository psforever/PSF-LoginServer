// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Reports that something has happened, or makes something happen.<br>
  * <br>
  * When sent from the server to a client, there are twenty-seven individual actions caused by this packet.
  * They are only vaguely organized by behavior and some numbers may not be associated with an action.
  * When sent by the client to the server, an unknown number of actions are available.
  * The highest known action is a server-sent 45.<br>
  *<br>
  * Actions (when sent from server):<br>
  * 03 - symbol: show Mosquito radar<br>
  * 04 - symbol: hide Mosquito radar<br>
  * 07 - warning: missile lock<br>
  * 08 - warning: Wasp missile lock<br>
  * 09 - warning: T-REK lock<br>
  * 12 - sound: base captured fanfare<br>
  * 14 - prompt: new character basic training<br>
  * 22 - message: awarded a cavern capture (updates cavern capture status)<br>
  * 23 - award a cavern kill<br>
  * 24 - message: you have been imprinted (updates imprinted status; does it?)<br>
  * 25 - message: you are no longer imprinted (updates imprinted status; does it?)<br>
  * 27 - event: purchase timers reset (does it?)<br>
  * 31 - switch to first person view, attempt to deconstruct but fail;
  *      event: fail to deconstruct due to having a "parent vehicle"<br>
  * 32 - switch to first person view<br>
  * 33 - event: fail to deconstruct<br>
  * 43 - prompt: friendly fire in virtual reality zone<br>
  * 45 - ?<br>
  * <br>
  * Actions (when sent from client):<br>
  * 15 - Max anchor
  * 16 - Max unanchor
  * 20 - Client requests MAX special effect (NC shield and TR overdrive. VS jump jets are handled by the jump_thrust boolean on PlayerStateMessageUpstream)
  * 21 - Disable MAX special effect (NC shield)
  * 29 - AFK<br>
  * 30 - back in game<br>
  * 36 - turn on "Looking for Squad"<br>
  * 37 - turn off "Looking for Squad"
  *
  * @param action what this packet does
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
