// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import enumeratum.values.{IntEnum, IntEnumEntry}
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

sealed abstract class CollisionIs(val value: Int) extends IntEnumEntry

object CollisionIs extends IntEnum[CollisionIs] {
  val values = findValues

  case object OfGroundVehicle extends CollisionIs(value = 0)

  case object OfAircraft extends CollisionIs(value = 1)

  case object OfInfantry extends CollisionIs(value = 2)

  case object BetweenThings extends CollisionIs(value = 3) //currently, invalid
}

/**
  * Dispatched by the client when the player has encountered a physical interaction that would cause damage.<br>
  * <br>
  * Collision information reports about two subjects who were involved in an altercation.
  * The first is the `player`, that is, the client's avatar.
  * The second is the `target` with respect to the `player` - whatever the avatar ran into, or whatever ran into the avatar.
  * In the case of isolated forms of collision such as fall damage the `target` fields are blank or zero'd.
  * @param collision_type a brief hint at the sort of interaction
  * @param player the player or player-controlled vehicle
  * @param target the other party in the collision
  * @param player_health the player's health
  * @param target_health the target's health
  * @param player_velocity the player's velocity
  * @param target_velocity the target's velocity
  * @param player_pos the player's world coordinates
  * @param target_pos the target's world coordinates
  * @param unk1 na
  * @param unk2 na
  * @param unk3 na
  */
final case class GenericCollisionMsg(
                                      collision_type: CollisionIs,
                                      player: PlanetSideGUID,
                                      player_health: Int,
                                      player_pos: Vector3,
                                      player_velocity: Vector3,
                                      target: PlanetSideGUID,
                                      target_health: Int,
                                      target_pos: Vector3,
                                      target_velocity: Vector3,
                                      unk1: Long,
                                      unk2: Long,
                                      unk3: Long
                                    ) extends PlanetSideGamePacket {
  type Packet = GenericCollisionMsg
  def opcode = GamePacketOpcode.GenericCollisionMsg
  def encode = GenericCollisionMsg.encode(this)
}

object GenericCollisionMsg extends Marshallable[GenericCollisionMsg] {
  implicit val codec: Codec[GenericCollisionMsg] = (
    ("collision_type" | PacketHelpers.createIntEnumCodec(CollisionIs, uint2)) ::
    ("p" | PlanetSideGUID.codec) ::
    ("t" | PlanetSideGUID.codec) ::
    ("p_health" | uint16L) ::
    ("t_health" | uint16L) ::
    ("p_vel" | Vector3.codec_float) ::
    ("t_vel" | Vector3.codec_float) ::
    ("p_pos" | Vector3.codec_pos) ::
    ("t_pos" | Vector3.codec_pos) ::
    ("unk1" | uint32L) ::
    ("unk2" | uint32L) ::
    ("unk3" | uint32L)
  ).xmap[GenericCollisionMsg](
    {
      case ct :: p :: t :: ph :: th :: pv :: tv :: pp :: tp :: u1 :: u2 :: u3 :: HNil =>
        GenericCollisionMsg(ct, p, ph, pp, pv, t, th, tp, tv, u1, u2, u3)
    },
    {
      case GenericCollisionMsg(ct, p, ph, pp, pv, t, th, tp, tv, u1, u2, u3) =>
        ct :: p :: t :: ph :: th :: pv :: tv :: pp :: tp :: u1 :: u2 :: u3 :: HNil
    }
  )
}
