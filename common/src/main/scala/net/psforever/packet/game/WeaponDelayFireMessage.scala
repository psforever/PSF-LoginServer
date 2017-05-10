// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/** WeaponDelayFireMessage seems to be sent when a weapon has a delayed projectile after firing, such as the knife.
  *
  * See [[PlayerStateMessageUpstream]] for explanation of seq_time.
  */
final case class WeaponDelayFireMessage(seq_time : Int,
                                        weapon_guid : PlanetSideGUID)
  extends PlanetSideGamePacket {
  type Packet = WeaponDelayFireMessage
  def opcode = GamePacketOpcode.WeaponDelayFireMessage
  def encode = WeaponDelayFireMessage.encode(this)
}

object WeaponDelayFireMessage extends Marshallable[WeaponDelayFireMessage] {
  implicit val codec : Codec[WeaponDelayFireMessage] = (
      ("seq_time" | uintL(10)) ::
        ("weapon_guid" | PlanetSideGUID.codec)
    ).as[WeaponDelayFireMessage]
}
