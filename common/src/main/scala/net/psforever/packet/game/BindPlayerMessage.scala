// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax} //It's not unused! shut up. :(
import scodec.codecs._

/**
  * A mysterious packet that is dispatched from the server to the client during zone transitions.<br>
  * <br>
  * This packet is sent out after unloading the current zone but before beginning loading in the new zone.
  * Specifically, it is preceded by all of the `ObjectDeleteMessage` packets and itself precedes the `LoadMapMessage` packet.
  * The server does not attempt to "bind" during initial login.<br>
  * <br>
  * Exploration:<br>
  * We will not have any clue how this packet truly works until we are at the point where we left the player change continents.
  * Before that, however, all evidence seems to indicate "standard" `BindPlayerMessage` data, as indicated below.
  * @param unk1 na; always 2 (`02`)?
  * @param unk2 na; always 128 (`80`)?
  * @param unk3 na; always 4 (`04`)?
  * @param unk4 na; always a stream of fifteen `00` values?
  */
final case class BindPlayerMessage(unk1 : Int,
                                  unk2 : Int,
                                  unk3 : Int,
                                  unk4 : ByteVector)
  extends PlanetSideGamePacket {
  type Packet = BindPlayerMessage
  def opcode = GamePacketOpcode.BindPlayerMessage
  def encode = BindPlayerMessage.encode(this)
}

object BindPlayerMessage extends Marshallable[BindPlayerMessage] {
  /**
    * The most common (only?) variant of this packet created by the server and sent to the client
    */
  val STANDARD = BindPlayerMessage(2, 128, 4, hex"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")

  implicit val codec : Codec[BindPlayerMessage] = (
    ("unk1" | uint8L) ::
      ("unk2" | uint8L) ::
      ("unk3" | uint8L) ::
      ("unk4" | bytes)
    ).as[BindPlayerMessage]
}
