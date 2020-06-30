// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{Angular, PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Dispatched to deliberately control certain projectiles of a weapon on other players' clients.<br>
  * <br>
  * This packet should be generated by firing specific weapons in specific fire modes.
  * For example, the Phoenix (`hunterseeker`) discharged in its primary fire mode generates this packet;
  * but, the Phoenix in secondary fire mode does not.
  * The Striker (`striker`) discharged in its primary fire mode generates this packet;
  * but, the Striker in secondary fire mode does not.
  * The chosen fire mode(s) are not a straight-fire projectile but one that has special control asserted over it.
  * For the Phoenix, it is user operated (camera-guided).
  * For the Striker, it tracks towards a valid target while the weapon's reticle hovers over that target.<br>
  * <br>
  * This packet will continue to be dispatched by the client for as long as the projectile being tracked is in the air.
  * All projectiles have a maximum lifespan before they will lose control and either despawn and/or explode.
  * This number is tracked in the packet for simplicity.
  * <br>
  * This control can not be demonstrated until that projectile is physically constructed on the other clients
  * in the same way that a player or a vehicle is constructed.
  * A projectile that exhibits intentional construction behavior is flagged using the property `exists_on_remote_client`.
  * The model comes with a number of caveats,
  * some that originate from the object construction process itself,
  * but also some from this packet.
  * For example,
  * as indicated by the static `shot_original_orient` values reported by this packet.
  * a discharged controlled projectile will not normally rotate.
  * A minor loss of lifespan may be levied.
  * @see `ProjectileDefinition`
  * @see `RemoteProjectileData`
  * @param projectile_guid when dispatched by the client, the client-specific local unique identifier of the projectile;
  *                        when dispatched by the server, the global unique identifier for the synchronized projectile object
  * @param shot_pos the position of the projectile
  * @param shot_vel the velocity of the projectile
  * @param shot_original_orient the orientation of the projectile when it was discharged
  * @param sequence_num an incrementing index of the packet in this projectile's lifetime;
  *                     suggests the "time alive" and indicates a place in packet ordering
  * @param end indicates the projectile has reached the end of its lifespan;
  *            usually, it should explode
  * @param hit_target_guid the global unique identifier of the object the projwectile collided with;
  *                        will be 0 if it reached the end of its life naturally, without colliding with anything
  */
final case class ProjectileStateMessage(
    projectile_guid: PlanetSideGUID,
    shot_pos: Vector3,
    shot_vel: Vector3,
    shot_original_orient: Vector3,
    sequence_num: Int,
    end: Boolean,
    hit_target_guid: PlanetSideGUID
) extends PlanetSideGamePacket {
  type Packet = ProjectileStateMessage
  def opcode = GamePacketOpcode.ProjectileStateMessage
  def encode = ProjectileStateMessage.encode(this)
}

object ProjectileStateMessage extends Marshallable[ProjectileStateMessage] {
  implicit val codec: Codec[ProjectileStateMessage] = (
    ("projectile_guid" | PlanetSideGUID.codec) ::
      ("shot_pos" | Vector3.codec_pos) ::
      ("shot_vel" | Vector3.codec_float) ::
      ("roll" | Angular.codec_roll) ::
      ("pitch" | Angular.codec_pitch) ::
      ("yaw" | Angular.codec_yaw()) ::
      ("sequence_num" | uint8) ::
      ("end" | bool) ::
      ("hit_target" | PlanetSideGUID.codec)
  ).xmap[ProjectileStateMessage](
    {
      case guid :: pos :: vel :: roll :: pitch :: yaw :: sequence_num :: explode :: unk :: HNil =>
        ProjectileStateMessage(guid, pos, vel, Vector3(roll, pitch, yaw), sequence_num, explode, unk)
    },
    {
      case ProjectileStateMessage(guid, pos, vel, Vector3(roll, pitch, yaw), sequence_num, explode, unk) =>
        guid :: pos :: vel :: roll :: pitch :: yaw :: sequence_num :: explode :: unk :: HNil
    }
  )
}
