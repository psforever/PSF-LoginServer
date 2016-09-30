// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

object GrenadeState extends Enumeration {
  type Type = Value
  val unk0,
  PRIMED,
  THROWN,
  unk3
  = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uintL(1))
}

/**
  * na
  * @param player_guid the player
  * @param count the state
  */
//case msg @ AvatarGrenadeStateMessage(player_guid, state) =>
//log.info("AvatarGrenadeStateMessage: " + msg)
final case class AvatarGrenadeStateMessage(player_guid : PlanetSideGUID,
                                           count : GrenadeState.Value)
  extends PlanetSideGamePacket {
  type Packet = AvatarGrenadeStateMessage
  def opcode = GamePacketOpcode.AvatarGrenadeStateMessage
  def encode = AvatarGrenadeStateMessage.encode(this)
}

object AvatarGrenadeStateMessage extends Marshallable[AvatarGrenadeStateMessage] {
  implicit val codec : Codec[AvatarGrenadeStateMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("state" | GrenadeState.codec)
    ).as[AvatarGrenadeStateMessage]
}

