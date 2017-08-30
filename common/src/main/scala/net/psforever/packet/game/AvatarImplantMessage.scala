// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.ImplantType
import scodec.Codec
import scodec.codecs._

/**
  * Change the state of the implant.<br>
  * <br>
  * The implant Second Wind is technically an invalid `ImplantType` for this packet.
  * This owes to the unique activation trigger for that implant - a near-death experience of ~0HP.
  * @param player_guid the player
  * @param unk1 na
  * @param unk2 na
  * @param implant the implant
  */
final case class AvatarImplantMessage(player_guid : PlanetSideGUID,
                                      unk1 : Int,
                                      unk2 : Int,
                                      implant : ImplantType.Value)
  extends PlanetSideGamePacket {
  type Packet = AvatarImplantMessage
  def opcode = GamePacketOpcode.AvatarImplantMessage
  def encode = AvatarImplantMessage.encode(this)
}

object AvatarImplantMessage extends Marshallable[AvatarImplantMessage] {
  implicit val codec : Codec[AvatarImplantMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("unk1" | uintL(3)) ::
      ("unk2" | uint2L) ::
      ("implant" | ImplantType.codec)
    ).as[AvatarImplantMessage]
}
