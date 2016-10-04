// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

/**
  * A mysterious packet that is dispatched from the server to the client during zone transitions.<br>
  * <br>
  * One common instance of this packet occurs during zone transport.
  * Specifically, a packet is dispatched after unloading the current zone but before beginning loading in the new zone.
  * It is preceded by all of the `ObjectDeleteMessage` packets and itself precedes the `LoadMapMessage` packet.<br>
  * <br>
  * Exploration 1:<br>
  * We will not have any clue how this packet truly works until we are at the point where we allow the player to change continents.
  * Before that, however, all evidence seems to indicate "standard" `BindPlayerMessage` data, as indicated below.<br>
  * <br>
  * Exploration 2:<br>
  * Find other bind descriptors.<br>
  * <br>
  * Bind Descriptors:<br>
  * `&#64;ams`<br>
  * `&#64;tech_plant`
  * @param unk1 na
  * @param bindDesc a description of the binding point
  * @param unk2 na
  * @param unk3 na
  * @param unk4 na
  * @param unk5 na
  * @param unk6 na
  * @param pos a position associated with the binding unit
  */
//TODO everything in between bindDesc and unk5 is probably wrong
final case class BindPlayerMessage(unk1 : Int,
                                   bindDesc : String,
                                   unk2 : Boolean,
                                   unk3 : Boolean,
                                   unk4 : Int,
                                   unk5 : Long,
                                   unk6 : Long,
                                   pos : Vector3)
  extends PlanetSideGamePacket {
  type Packet = BindPlayerMessage
  def opcode = GamePacketOpcode.BindPlayerMessage
  def encode = BindPlayerMessage.encode(this)
}

object BindPlayerMessage extends Marshallable[BindPlayerMessage] {
  /**
    * A common variant of this packet.
    * `16028004000000000000000000000000000000`
    */
  val STANDARD = BindPlayerMessage(2, "", false, false, 2, 0, 0, Vector3(0, 0, 0))

  implicit val codec : Codec[BindPlayerMessage] = (
    ("unk1" | uint8L) ::
      ("bindDesc" | PacketHelpers.encodedString) ::
      ("unk2" | bool) ::
      ("unk3" | bool) ::
      ignore(1) :: //remove this and double up the other `ignore`?
      ("unk4" | uint4L) ::
      ignore(1) ::
      ("unk5" | uint32L) ::
      ("unk6" | uint32L) ::
      ("pos" | Vector3.codec_pos)
    ).as[BindPlayerMessage]
}
