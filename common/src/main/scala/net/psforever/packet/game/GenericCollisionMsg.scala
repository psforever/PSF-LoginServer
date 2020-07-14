// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the client when the player has encountered a physical interaction that would cause damage.<br>
  * <br>
  * Collision information reports about two subjects who were involved in an altercation.
  * The first is the `player`, that is, the client's avatar.
  * The second is the `target` with respect to the `player` - whatever the avatar ran into, or whatever ran into the avatar.
  * In the case of isolated forms of collision such as fall damage the `target` fields are blank or zero'd.
  * @param unk1 na
  * @param player the player or player-controlled vehicle
  * @param target the other party in the collision
  * @param player_health the player's health
  * @param target_health the target's health
  * @param player_velocity the player's velocity
  * @param target_velocity the target's velocity
  * @param player_pos the player's world coordinates
  * @param target_pos the target's world coordinates
  * @param unk2 na
  * @param unk3 na
  * @param unk4 na
  */
final case class GenericCollisionMsg(
    unk1: Int,
    player: PlanetSideGUID,
    target: PlanetSideGUID,
    player_health: Int,
    target_health: Int,
    player_velocity: Vector3,
    target_velocity: Vector3,
    player_pos: Vector3,
    target_pos: Vector3,
    unk2: Long,
    unk3: Long,
    unk4: Long
) extends PlanetSideGamePacket {
  type Packet = GenericCollisionMsg
  def opcode = GamePacketOpcode.GenericCollisionMsg
  def encode = GenericCollisionMsg.encode(this)
}

object GenericCollisionMsg extends Marshallable[GenericCollisionMsg] {
  implicit val codec: Codec[GenericCollisionMsg] = (
    ("unk1" | uint2) ::
      ("p" | PlanetSideGUID.codec) ::
      ("t" | PlanetSideGUID.codec) ::
      ("p_health" | uint16L) ::
      ("t_health" | uint16L) ::
      ("p_vel" | Vector3.codec_float) ::
      ("t_vel" | Vector3.codec_float) ::
      ("p_pos" | Vector3.codec_pos) ::
      ("t_pos" | Vector3.codec_pos) ::
      ("unk2" | uint32L) ::
      ("unk3" | uint32L) ::
      ("unk4" | uint32L)
  ).as[GenericCollisionMsg]
}
