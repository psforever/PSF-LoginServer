// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax}
import scodec.codecs._

/**
  * A mysterious packet that is dispatched from the server to the client during zone transitions.<br>
  * <br>
  * This packet is sent out after unloading the current zone but before beginning loading in the new zone.
  * Specifically, it is preceded by all of the `ObjectDeleteMessage` packets and itself precedes the `LoadMapMessage` packet.<br>
  * <br>
  * Exploration:<br>
  * We will not have any clue how this packet truly works until we are at the point where we allow the player to change continents.
  * Before that, however, all evidence seems to indicate "standard" `BindPlayerMessage` data, as indicated below.
  * @param unk1 na
  * @param bindDesc a description of the binding point ("@ams")
  * @param unk2 na
  * @param unk3 na
  * @param unk4 na
  */
//TODO except for bindDesc, this is all wrong
final case class BindPlayerMessage(unk1 : Int,
                                   bindDesc : String,
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
    * A common variant of this packet
    */
  val STANDARD = BindPlayerMessage(2, "", 4, 0, hex"00 00 00 00 00 00 00 00 00 00 00 00 00 00")

  implicit val codec : Codec[BindPlayerMessage] = (
    ("unk1" | uint8L) ::
      ("bindDesc" | PacketHelpers.encodedString) ::
      ("unk2" | uint8L) ::
      ("unk3" | uint8L) ::
      ("unk4" | bytes)
    ).as[BindPlayerMessage]
}
