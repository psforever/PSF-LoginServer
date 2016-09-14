// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * na
  * @param list na
  * @param player_guid the player
  * @param line na
  * @param subject na
  * @param armor the type of exo-suit
  * @param subtype the exo-suit subtype, if any
  */
final case class FavoritesMessage(list : Int,
                                  player_guid : PlanetSideGUID,
                                  line : Int,
                                  subject : String,
                                  armor : Option[Int] = None,
                                  subtype : Option[Int] = None)
  extends PlanetSideGamePacket {
  type Packet = FavoritesMessage
  def opcode = GamePacketOpcode.FavoritesMessage
  def encode = FavoritesMessage.encode(this)
}

object FavoritesMessage extends Marshallable[FavoritesMessage] {
  implicit val codec : Codec[FavoritesMessage] = (
    ("list" | uintL(2)) >>:~ { value =>
      ("player_guid" | PlanetSideGUID.codec) ::
        ("line" | uintL(4)) ::
        ("subject" | PacketHelpers.encodedWideStringAligned(2)) ::
        conditional(value == 0, "armor" | uintL(3)) ::
        conditional(value == 0, "subtype" | uintL(3))
    }).as[FavoritesMessage]
}
