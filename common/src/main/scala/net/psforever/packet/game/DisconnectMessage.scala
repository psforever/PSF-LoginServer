// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched to the client to force a disconnect.<br>
  * <br>
  * The client's view of the game world will fade and be centered with a PlanetSide textbox with the given message.
  * Using the button on the textbox will drop the current world session and return the player to the world select screen.
  * Technically, they're already disconnected by the time the textbox is visible.
  * Being disconnected like this has no client-based consequences on its own.<br>
  * <br>
  * Exploration:<br>
  * When do the other two messages appear, if at all?
  * @param msg the displayed message
  * @param unk2 na
  * @param unk3 na
  */
final case class DisconnectMessage(msg : String,
                                   unk2 : String = "",
                                   unk3 : String = "")
  extends PlanetSideGamePacket {
  type Packet = DisconnectMessage
  def opcode = GamePacketOpcode.DisconnectMessage
  def encode = DisconnectMessage.encode(this)
}

object DisconnectMessage extends Marshallable[DisconnectMessage] {
  implicit val codec : Codec[DisconnectMessage] = (
    ("msg" | PacketHelpers.encodedString) ::
      ("unk2" | PacketHelpers.encodedString) ::
      ("unk3" | PacketHelpers.encodedString)
    ).as[DisconnectMessage]
}
