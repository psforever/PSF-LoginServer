// Copyright (c) 2021 PSForever
package net.psforever.packet.game

import enumeratum.values.{IntEnum, IntEnumEntry}
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._

/**
  * The types of errors that can be reported when attempting to droppod into a zone.<br>
  * <br>
  * All codes show the preceding text in the events chat window.
  * The typo in the message from `BlockedBySOI` can not be resolved by populating any of the greater packet's fields.
  * `ZoneFullWarpQueue` utilizes the additional packet fields to establish the warp queue prompt
  * with the warp queue and the player's position in that queue.
  * The zone to which the player desires transportation is defined elsewhere in the greater packet.
  */
sealed abstract class DroppodError(val value: Int, val message: String) extends IntEnumEntry

object DroppodError extends IntEnum[DroppodError] {
  val values = findValues

  case object ContinentNotAvailable extends DroppodError(
    value = 1,
    message = "That continent is not available - please choose another one."
  )

  case object BlockedBySOI extends DroppodError(
    value = 2,
    message = "That location is within a 's Sphere of Influence (SOI). Please try another location." //typo intentional
  )

  case object InvalidLocation extends DroppodError(
    value = 3,
    message = "That is an invalid drop location - please try another location."
  )

  case object ZoneNotAvailable extends DroppodError(
    value = 4,
    message = "This zone is not available - try another zone."
  )

  case object ZoneFull extends DroppodError(
    value = 5,
    message = "That zone is already full of battle hungry people - try another one."
  )

  case object EnemyBase extends DroppodError(
    value = 6,
    message = "You can not drop onto an enemy home base - please choose a valid continent."
  )

  case object NotOnHart extends DroppodError(
    value = 7,
    message = "You are attempting to drop but are not on the HART - be warned you are being watched."
  )

  case object OwnFactionLocked extends DroppodError(
    value = 8,
    message = "You cannot drop onto a continent that is locked to your empire - please choose a valid continent."
  )

  case object ZoneFullWarpQueue extends DroppodError(
    value = 9,
    message = "The zone you are trying to warp to is currently full.  You have been placed in the warp queue."
  )
}

/**
  * Information displayed on the zone warp queue in terms of queue size and queue progression.
  * @param queue_size the number of players trying to warp to this zone in the queue ('b' if a/b)
  * @param place the player's spot in the queue ('a' if a/b)
  */
final case class WarpQueuePrompt(queue_size: Long, place: Long)

/**
  * Dispatched from the client to indicate the player wished to use an orbital droppod
  * but the player will be denied that request for a specific reason.
  * The reason manifests as text appended to the event chat window.
  * Occasionally, a supplemental window will open with additional information about a delayed action (warp queue).
  * @see `DroppodLaunchInfo`
  * @param error_code the error reporting why the zoning through droppod use failed
  * @param launch_info information related to this droppod event
  * @param queue_info if the error invokes the warp queue, the current information about the state of the queue
  * @throws AssertionError if the error code requires additional fields
  */
final case class DroppodLaunchResponseMessage(
                                               error_code: DroppodError,
                                               launch_info: DroppodLaunchInfo,
                                               queue_info: Option[WarpQueuePrompt]
                                             ) extends PlanetSideGamePacket {
  assert(
    error_code != DroppodError.ZoneFullWarpQueue || queue_info.isDefined,
    "ZoneFullWarpQueue requires queue information"
  )
  type Packet = DroppodLaunchResponseMessage
  def opcode = GamePacketOpcode.DroppodLaunchResponseMessage
  def encode = DroppodLaunchResponseMessage.encode(this)
}

object DroppodLaunchResponseMessage extends Marshallable[DroppodLaunchResponseMessage] {
  /**
    * Overloaded constructor for most errors.
    * @param error the error reporting why the zoning through droppod use failed
    * @param guid the player using the droppod
    * @return a `DroppodLaunchResponseMessage` packet
    */
  def apply(error: DroppodError, guid: PlanetSideGUID): DroppodLaunchResponseMessage = {
    DroppodLaunchResponseMessage(error, guid, 0, Vector3.Zero)
  }

  /**
    * Overloaded constructor for most errors.
    * @param error the error reporting why the zoning through droppod use failed
    * @param guid the player using the droppod
    * @param zoneNumber the zone to which the player desires transportation
    * @param xypos where in the zone (relative to the ground) the player will be placed
    * @return a `DroppodLaunchResponseMessage` packet
    */
  def apply(error: DroppodError, guid: PlanetSideGUID, zoneNumber: Int, xypos: Vector3): DroppodLaunchResponseMessage = {
    DroppodLaunchResponseMessage(error, DroppodLaunchInfo(guid, zoneNumber, xypos))
  }

  /**
    * Overloaded constructor for quickly reflecting errors.
    * @param error the error reporting why the zoning through droppod use failed
    * @param info information related to this droppod event
    * @return a `DroppodLaunchResponseMessage` packet
    */
  def apply(error: DroppodError, info: DroppodLaunchInfo): DroppodLaunchResponseMessage = {
    DroppodLaunchResponseMessage(error, info, None)
  }

  /**
    * Overloaded constructor for `ZoneFullWarpQueue` errors.
    * @param guid the player using the droppod
    * @param zoneNumber the zone to which the player desires transportation
    * @param queueSize the number of players trying to warp to this zone in the queue ('b' if a/b)
    * @param placeInQueue the player's spot in the queue ('a' if a/b)
    * @return a `DroppodLaunchResponseMessage` packet
    */
  def apply(guid: PlanetSideGUID, zoneNumber: Int, queueSize: Int, placeInQueue: Int): DroppodLaunchResponseMessage = {
    DroppodLaunchResponseMessage(
      DroppodLaunchInfo(guid, zoneNumber, Vector3.Zero),
      queueSize, placeInQueue
    )
  }

  /**
    * Overloaded constructor for quickly reflecting `ZoneFullWarpQueue` errors.
    * @param info information related to this droppod event
    * @param queueSize the number of players trying to warp to this zone in the queue ('b' if a/b)
    * @param placeInQueue the player's spot in the queue ('a' if a/b)
    * @return a `DroppodLaunchResponseMessage` packet
    */
  def apply(info: DroppodLaunchInfo, queueSize: Int, placeInQueue: Int): DroppodLaunchResponseMessage = {
    DroppodLaunchResponseMessage(
      DroppodError.ZoneFullWarpQueue,
      info,
      Some(WarpQueuePrompt(queueSize, placeInQueue))
    )
  }

  private val droppodErrorCodec: Codec[DroppodError] = PacketHelpers.createIntEnumCodec(DroppodError, uint4)

  private val extra_codec: Codec[WarpQueuePrompt] = (
    ("place" | uint32L) ::
    ("queue_size" | uint32L)
    ).as[WarpQueuePrompt]

  implicit val codec: Codec[DroppodLaunchResponseMessage] = (
    ("error_code" | droppodErrorCodec) >>:~ { ecode =>
      ("launch_info" | DroppodLaunchInfo.codec) ::
      ("queue_info" | conditional(ecode == DroppodError.ZoneFullWarpQueue, extra_codec))
    }
    ).as[DroppodLaunchResponseMessage]
}
