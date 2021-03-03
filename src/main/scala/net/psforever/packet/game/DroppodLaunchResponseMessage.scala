// Copyright (c) 2021 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Attempt.Successful
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * The types of errors that can be reported when attempting to droppod into a zone.<br>
  * <br>
  * All codes show the preceding text in the events chat window.
  * The typo in the message from `BlockedBySOI` can not be resolved by populating any of the greater packet's fields.
  * `ZoneFullWarpQueue` utilizes the additional packet fields to establish the warp queue prompt
  * with the warp queue and the player's position in that queue.
  * The zone that the player desires transportation is defined elsewhere in the greater packet.
  */
object DroppodError extends Enumeration {
  type Type = Value

  val
  None,
  // "That continent is not available - please choose another one."
  ContinentNotAvailable,
  // "That location is within a 's Sphere of Influence (SOI). Please try another location." (typo)
  BlockedBySOI,
  // "That is an invalid drop location - please try another location."
  InvalidLocation,
  // "This zone is not available - try another zone."
  ZoneNotAvailable,
  // "That zone is already full of battle hungry people - try another one."
  ZoneFull,
  // "You can not drop onto an enemy home base - please choose a valid continent."
  EnemyBase,
  // "You are attempting to drop but are not on the HART - be warned you are being watched."
  NotOnHart,
  // "You cannot drop onto a continent that is locked to your empire - please choose a valid continent."
  OwnFactionLocked,
  // "The zone you are trying to warp to is currently full.  You have been placed in the warp queue."
  ZoneFullWarpQueue
  = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(enum = this, uint4)
}

final case class WarpQueuePrompt(place: Long, queue_size: Long)

final case class DroppodLaunchResponseMessage(
                                               error_code: DroppodError.Value,
                                               guid: PlanetSideGUID,
                                               zone_number: Int,
                                               xypos: Vector3,
                                               unk: Option[WarpQueuePrompt]
                                             ) extends PlanetSideGamePacket {
  type Packet = DroppodLaunchResponseMessage
  def opcode = GamePacketOpcode.DroppodLaunchResponseMessage
  def encode = DroppodLaunchResponseMessage.encode(this)
}

object DroppodLaunchResponseMessage extends Marshallable[DroppodLaunchResponseMessage] {
  def apply(error: DroppodError.Value, guid: PlanetSideGUID): DroppodLaunchResponseMessage = {
    val errorCode = if (error != DroppodError.ZoneFullWarpQueue) error else DroppodError.None
    DroppodLaunchResponseMessage(errorCode, guid, 0, Vector3.Zero, None)
  }

  def apply(error: DroppodError.Value, guid: PlanetSideGUID, zoneNumber: Int, xypos: Vector3): DroppodLaunchResponseMessage = {
    val errorCode = if (error != DroppodError.ZoneFullWarpQueue) error else DroppodError.None
    DroppodLaunchResponseMessage(errorCode, guid, zoneNumber, xypos, None)
  }

  def WarpQueue(guid: PlanetSideGUID, zoneNumber: Int, queueSize: Int, placeInQueue: Int): DroppodLaunchResponseMessage = {
    DroppodLaunchResponseMessage(
      DroppodError.ZoneFullWarpQueue,
      guid,
      zoneNumber,
      Vector3.Zero,
      Some(WarpQueuePrompt(queueSize, placeInQueue))
    )
  }

  private val extra_codec: Codec[WarpQueuePrompt] = (
    ("place" | uint32L) ::
    ("queue_size" | uint32L)
    ).as[WarpQueuePrompt]

  implicit val codec: Codec[DroppodLaunchResponseMessage] = (
    ("error_code" | DroppodError.codec) >>:~ { ecode =>
      ("guid" | PlanetSideGUID.codec) ::
      ("zone_number" | uint16L) ::
      (floatL :: floatL).narrow[Vector3](
        {
          case x :: y :: HNil => Successful(Vector3(x, y, 0))
        },
        {
          case Vector3(x, y, _) => x :: y :: HNil
        }
      ) ::
      ("unk" | conditional(ecode == DroppodError.ZoneFullWarpQueue, extra_codec))
    }
    ).as[DroppodLaunchResponseMessage]
}
