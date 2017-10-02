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
  * @param action 2 to init, 3 to activate
  * @param implantSlot na
  * @param status 0 to uninit or unactivate, 1 to init or activate
  */
final case class AvatarImplantMessage(player_guid : PlanetSideGUID,
                                      action : Int,
                                      implantSlot : Int,
                                      status : Int)
  extends PlanetSideGamePacket {
  type Packet = AvatarImplantMessage
  def opcode = GamePacketOpcode.AvatarImplantMessage
  def encode = AvatarImplantMessage.encode(this)
}

object AvatarImplantMessage extends Marshallable[AvatarImplantMessage] {
  implicit val codec : Codec[AvatarImplantMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("action" | uintL(3)) ::
      ("implantSlot" | uint2L) ::
      ("status" | uint4L)
    ).as[AvatarImplantMessage]
}
