// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * An `Enumeration` for all the actions that can be applied to implants and implant slots.
  */
object ImplantAction extends Enumeration {
  type Type = Value

  val
  Add,
  Remove,
  Initialization,
  Activation,
  UnlockMessage,
  OutOfStamina
  = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uintL(3))
}

/**
  * Change the state of the implant.
  * Spawn messages for certain implant-related events.<br>
  * <br>
  * The implant Second Wind is technically an invalid `ImplantType` for this packet.
  * This owes to the unique activation trigger for that implant - a near-death experience of ~0HP.
  * @see `ImplantType`
  * @param player_guid the player
  * @param action how to affect the implant or the slot
  * @param implantSlot : from 0 to 2
  * @param status : a value that depends on context from `ImplantAction`:<br>
  *                 `Add` - 0-9 depending on the `ImplantType`<br>
  *                 `Remove` - any valid value; field is not significant to this action<br>
  *                 `Initialization` - 0 to revoke slot; 1 to allocate implant slot<br>
  *                 `Activation` - 0 to deactivate implant; 1 to activate implant<br>
  *                 `UnlockMessage` - 0-3 as an unlocked implant slot; display a message<br>
  *                 `OutOfStamina` - lock implant; 0 to lock; 1 to unlock; display a message
  */
final case class AvatarImplantMessage(player_guid : PlanetSideGUID,
                                      action : ImplantAction.Value,
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
      ("action" | ImplantAction.codec) ::
      ("implantSlot" | uint2L) ::
      ("status" | uint4L)
    ).as[AvatarImplantMessage]
}
