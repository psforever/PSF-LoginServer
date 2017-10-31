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
  * @param action
  *               0 : add implant
  *                   with status = 0 to 9 (from ImplantType)
  *               1 : remove implant
  *                   seems work with any value in status
  *               2 : init implant
  *                   status : 0 to "uninit"
  *                   status : 1 to init
  *               3 : activate implant
  *                   status : 0 to desactivate
  *                   status : 1 to activate
  *               4 : number of implant slots unlocked
  *                   status : 0 = no implant slot
  *                   status : 1 = first implant slot + "implant message"
  *                   status : 2 or 3 = unlock second & third slots
  *               5 : out of stamina message
  *                   status : 0 to stop the lock
  *                   status : 1 to active the lock
  * @param implantSlot : from 0 to 2
  * @param status : see action
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
