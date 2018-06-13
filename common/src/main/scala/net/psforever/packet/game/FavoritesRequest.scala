// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.LoadoutType
import scodec.Codec
import scodec.codecs._

object FavoritesAction extends Enumeration {
  type Type = Value

  val
  Unknown,
  Save,
  Delete
  = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint2L)
}

/**
  * na
  * @param player_guid the player
  * @param list na
  * @param action the behavior of this packet
  * @param line what line of the applicable loadout ("Saved Favorites") list is modified
  * @param label applicable when a load out is being saved;
  *              this is the string that will be displayed in the list of loadouts on that line
  */
final case class FavoritesRequest(player_guid : PlanetSideGUID,
                                  list : LoadoutType.Value,
                                  action : FavoritesAction.Value,
                                  line : Int,
                                  label : Option[String])
  extends PlanetSideGamePacket {
  type Packet = FavoritesRequest
  def opcode = GamePacketOpcode.FavoritesRequest
  def encode = FavoritesRequest.encode(this)
}

object FavoritesRequest extends Marshallable[FavoritesRequest] {
  implicit val codec : Codec[FavoritesRequest] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("list" | LoadoutType.codec) ::
      (("action" | FavoritesAction.codec) >>:~ { action =>
        ("line" | uint4L) ::
          conditional(action == FavoritesAction.Save, "label" | PacketHelpers.encodedWideString)
      })
    ).as[FavoritesRequest]
}
