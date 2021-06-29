// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import enumeratum.values.{IntEnum, IntEnumEntry}
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._

sealed abstract class ProjectileCharacteristics(val value: Int) extends IntEnumEntry

/**
  * Characteristics about the projectile being produced by a `WeaponFireMessage` packet.
  * Not really useful outside of `WeaponFireMessage`?
  */
object ProjectileCharacteristics extends IntEnum[ProjectileCharacteristics] {
  val values = findValues

  /** most common characteristic;
    * utilized for both straight-fire and various arcing projectiles
    */
  case object Standard extends ProjectileCharacteristics(value = 0)
  /** some arcing explosive projectiles such as the Thumper's alternate fire and the Leviathan's fluxpod launcher;
    * exceptions include: Punisher grenade cartridges, Grenade alternate fire (see `Thrown`), Pounder alternate fire
    */
  case object DelayedExplosion extends ProjectileCharacteristics(value = 1)
  /** remote client projectiles (those constructed through packets) */
  case object Guided extends ProjectileCharacteristics(value = 2)
  /** grenades, and only grenades */
  case object Thrown extends ProjectileCharacteristics(value = 3)

  /** unused? */
  case object u4 extends ProjectileCharacteristics(value = 4)
  /** unused? */
  case object u5 extends ProjectileCharacteristics(value = 5)
  /** unused? */
  case object u6 extends ProjectileCharacteristics(value = 6)
  /** unused? */
  case object u7 extends ProjectileCharacteristics(value = 7)
}

/**
  *  Dispatched form the client each time a weapon discharges.
  *
  * @param seq_time see [[PlayerStateMessageUpstream]] for explanation of seq_time
  * @param weapon_guid the weapon of discharge;
  *                    when dispatched to a client, an unreferenced entity results in the projectile not being rendered
  * @param projectile_guid the (client-local) projectile unique identifier;
  *                        when dispatched to a client, can be unreferenced (or blanked)
  * @param shot_origin the position where the projectile is first spawned
  * @param unk1 na;
  *             always 0?
  * @param spread_a related to the spread of the discharge;
  *                 works with `spread_b` field in unknown way;
  *                 the unmodified value is high (65535) when accurate, low (0) when not
  * @param spread_b related to the spread of the discharge;
  *                 works with `spread_a` field in unknown way
  * @param max_distance maximum travel distance (m), with exceptions, e.g., decimator rockets are always 0
  * @param unk5 na;
  *             always 255?
  * @param projectile_type the sort of projectile produced
  * @param thrown_projectile_vel if a thrown projectile, its velocity
  */
final case class WeaponFireMessage(
                                    seq_time: Int,
                                    weapon_guid: PlanetSideGUID,
                                    projectile_guid: PlanetSideGUID,
                                    shot_origin: Vector3,
                                    unk1: Int,
                                    spread_a: Int,
                                    spread_b: Int,
                                    max_distance: Int,
                                    unk5: Int,
                                    projectile_type: ProjectileCharacteristics,
                                    thrown_projectile_vel: Option[Option[Vector3]]
                                  ) extends PlanetSideGamePacket {
  type Packet = WeaponFireMessage
  def opcode = GamePacketOpcode.WeaponFireMessage
  def encode = WeaponFireMessage.encode(this)
}

object WeaponFireMessage extends Marshallable[WeaponFireMessage] {
  implicit val codec: Codec[WeaponFireMessage] = (
    ("seq_time" | uintL(bits = 10)) ::
    ("weapon_guid" | PlanetSideGUID.codec) ::
    ("projectile_guid" | PlanetSideGUID.codec) ::
    ("shot_origin" | Vector3.codec_pos) ::
    ("unk1" | uint16L) ::
    ("spread_a" | uint16L.xmap[Int]( { x => 65535 - x }, { x => 65535 - x })) :: //low when accurate, high when not
    ("spread_b" | uint16L) ::
    ("max_distance" | uint16L) ::
    ("unk5" | uint8L) ::
    ("projectile_type" | PacketHelpers.createIntEnumCodec(ProjectileCharacteristics, uint(bits = 3)) >>:~ { ptype =>
      ("thrown_projectile_vel" | conditional(ptype == ProjectileCharacteristics.Thrown, optional(bool, Vector3.codec_vel))).hlist
    })
    ).as[WeaponFireMessage]
}
