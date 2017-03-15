// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched from the server to render the projectiles of one player's weapon on other players' clients.
  * @param projectile_guid the projectile
  * @param shot_origin a spawning position for the projectile
  * @param shot_vector a directional heading for the projectile
  * @param unk1 na;
  *             usually 0
  * @param unk2 na
  * @param unk3 na
  * @param unk4 na;
  *             usually false
  * @param unk5 na
  */
final case class ProjectileStateMessage(projectile_guid : PlanetSideGUID,
                                        shot_origin : Vector3,
                                        shot_vector : Vector3,
                                        unk1 : Int,
                                        unk2 : Int,
                                        unk3 : Int,
                                        unk4 : Boolean,
                                        unk5 : Int)
  extends PlanetSideGamePacket {
  type Packet = ProjectileStateMessage
  def opcode = GamePacketOpcode.ProjectileStateMessage
  def encode = ProjectileStateMessage.encode(this)
}

object ProjectileStateMessage extends Marshallable[ProjectileStateMessage] {
  implicit val codec : Codec[ProjectileStateMessage] = (
    ("projectile_guid" | PlanetSideGUID.codec) ::
      ("shot_origin" | Vector3.codec_pos) ::
      ("shot_vector" | Vector3.codec_float) ::
      ("unk1" | uint8L) ::
      ("unk2" | uint8L) ::
      ("unk3" | uint8L) ::
      ("unk4" | bool) ::
      ("unk5" | uint16L)
    ).as[ProjectileStateMessage]
}
