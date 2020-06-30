// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._

/**
  * An entry of the target that was hit by splash damage.
  * @param uid the target's uid
  * @param pos the target's position (when hit)
  * @param unk1 na
  * @param unk2 na
  */
final case class SplashedTarget(uid: PlanetSideGUID, pos: Vector3, unk1: Long, unk2: Option[Int])

/**
  * Dispatched to the server when a type of effect that influence multiple targets activates.<br>
  * <br>
  * Splash does not refer to the effect upon an applicable target.
  * Splash denotes the fixed radius wherein a said effect exerts temporary influence.
  * Being damaged is the most common splash effect; the jammering effect is another.
  * A pain field does not count because it is an environmental constant.
  * Lashing is considered different because it is a type of inheritable influence.<br>
  * <br>
  * Valid targets for splash are all interactive game objects that maintain a GUID.
  * This includes: players, of course; vehicles, of course; doors; terminals; spawn tubes; and, such objects.
  * Not all targets listed will actually be influenced by the effect carried by splash.<br>
  * <br>
  * The effect commonly modifies the visual depiction of the splash.
  * Being able to "see" splash also does not necessarily mean that one will be influenced by it.
  * Visually and spatially, it may seem to bleed through surfaces on occasion.
  * The effect will not be carried, however.
  * Splash will also respect the game's internal zoning and not pass through temporary obstacles like closed doors.
  * Not being able to see splash also does not stop a target from being affected.
  * The radius of influence is typically a bit larger than the visual indication.<br>
  * <br>
  * All sources of splash damage herein will be called "grenades" for simplicity.
  * @param unk1 na
  * @param projectile_uid the grenade's object
  * @param projectile_pos the position where the grenade landed (where it is)
  * @param unk2 na;
  *             frequently 42
  * @param unk3 na;
  *             frequently 0
  * @param projectile_vel the velocity of the grenade when it landed
  * @param unk4 na
  * @param targets a `List` of all targets influenced by the splash
  */
final case class SplashHitMessage(
    unk1: Int,
    projectile_uid: PlanetSideGUID,
    projectile_pos: Vector3,
    unk2: Int,
    unk3: Int,
    projectile_vel: Option[Vector3],
    unk4: Option[Int],
    targets: List[SplashedTarget]
) extends PlanetSideGamePacket {
  type Packet = SplashHitMessage
  def opcode = GamePacketOpcode.SplashHitMessage
  def encode = SplashHitMessage.encode(this)
}

object SplashedTarget extends Marshallable[SplashedTarget] {
  implicit val codec: Codec[SplashedTarget] = (
    ("uid" | PlanetSideGUID.codec) ::
      ("pos" | Vector3.codec_pos) ::
      ("unk1" | uint32L) ::
      optional(bool, "unk2" | uint16L)
  ).as[SplashedTarget]
}

object SplashHitMessage extends Marshallable[SplashHitMessage] {
  implicit val codec: Codec[SplashHitMessage] = (
    ("unk1" | uintL(10)) ::
      ("projectile_uid" | PlanetSideGUID.codec) ::
      ("projectile_pos" | Vector3.codec_pos) ::
      ("unk2" | uint16L) ::
      ("unk3" | uintL(3)) ::
      optional(bool, "projectile_vel" | Vector3.codec_vel) ::
      optional(bool, "unk4" | uint16L) ::
      ("targets" | PacketHelpers.listOfNAligned(uint32L, 0, SplashedTarget.codec))
  ).as[SplashHitMessage]
}
