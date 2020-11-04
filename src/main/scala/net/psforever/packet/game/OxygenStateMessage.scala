// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * The progress state of being a drowning victim.
  * `Suffocation` means being too far under water.
  * In terms of percentage, progress proceeds towards 0.
  * `Recovery` means emerging from being too far under water.
  * In terms of percentage, progress proceeds towards 100.
  */
object Drowning extends Enumeration {
  val Recovery = Value(0)
  val Suffocation = Value(1)

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint(bits = 1))
}

/**
  * Infomation about the progress bar displayed for a certain target's drowning condition.
  * @param guid the target
  * @param progress the remaining countdown
  * @param condition in what state of drowning the target is progressing
  */
final case class DrowningTarget(guid: PlanetSideGUID, progress: Float, condition: Drowning.Value)

/**
  * Dispatched by the server to cause the player to slowly drown.
  * If the player is mounted in a vehicle at the time, alert the player that the vehicle may be disabled.<br>
  * <br>
  * When a player walks too far underwater, a borderless red progress bar with a countdown from 100 (98) is displayed across the screen.
  * The flavor text reads "Oxygen level".
  * The countdown proceeds to zero at a fixed rate - it takes approximately 60s - and is timed with the depleting progress bar.
  * When it reaches zero, the player will be killed.
  * If the player is in a vehicle after a certain depth, a blue bar and countdown pair will superimpose the red indicators.
  * It depletes much more rapidly than the red indicators - it takes approximately 5s.
  * When it reaches zero, the vehicle will become disabled.
  * All players in the vehicle's seats will be kicked and they will not be allowed back in.<br>
  * <br>
  * Normally, the countdowns should be set to begin at 100 (100.0).
  * This is the earliest the drowning GUI will appear for either blue or red indicators.
  * Greater intervals - up to 204.8 - will start the countdown silently but the GUI will be hidden until 100.0.
  * (The progress indicators will actually appear to start counting from 98.)
  * @param player the player's oxygen state
  * @param vehicle optional oxygen state of the vehicle the player is driving;
  *                the player must be mounted in the vehicle (at start time)
  */
final case class OxygenStateMessage(
                                     player: DrowningTarget,
                                     vehicle: Option[DrowningTarget]
                                   ) extends PlanetSideGamePacket {
  type Packet = OxygenStateMessage
  def opcode = GamePacketOpcode.OxygenStateMessage
  def encode = OxygenStateMessage.encode(this)
}

object DrowningTarget {
  def apply(guid: PlanetSideGUID): DrowningTarget =
    DrowningTarget(guid, 100, Drowning.Suffocation)

  def apply(guid: PlanetSideGUID, progress: Float): DrowningTarget =
    DrowningTarget(guid, progress, Drowning.Suffocation)

  def recover(guid: PlanetSideGUID, progress: Float): DrowningTarget =
    DrowningTarget(guid, progress, Drowning.Recovery)
}

object OxygenStateMessage extends Marshallable[OxygenStateMessage] {
  def apply(
             player_guid: PlanetSideGUID
           ): OxygenStateMessage =
    OxygenStateMessage(DrowningTarget(player_guid), None)

  def apply(
             player_guid: PlanetSideGUID,
             progress: Float
           ): OxygenStateMessage =
    OxygenStateMessage(DrowningTarget(player_guid, progress), None)

  def apply(
             player: DrowningTarget
           ): OxygenStateMessage =
    OxygenStateMessage(player, None)

  def apply(
             player_guid: PlanetSideGUID,
             player_progress: Float,
             vehicle_guid: PlanetSideGUID,
             vehicle_progress: Float
           ): OxygenStateMessage =
    OxygenStateMessage(
      DrowningTarget(player_guid, player_progress),
      Some(DrowningTarget(vehicle_guid, vehicle_progress))
    )

  def apply(
             player_guid: PlanetSideGUID,
             player_progress: Float,
             vehicle_guid: PlanetSideGUID
           ): OxygenStateMessage =
    OxygenStateMessage(
      DrowningTarget(player_guid, player_progress),
      Some(DrowningTarget(vehicle_guid))
    )

  def recover(
                  player_guid: PlanetSideGUID,
                  progress: Float
                ): OxygenStateMessage =
    OxygenStateMessage(DrowningTarget.recover(player_guid, progress), None)

  def recoverVehicle(
                   player_guid: PlanetSideGUID,
                   player_progress: Float,
                   vehicle_guid: PlanetSideGUID,
                   vehicle_progress: Float
                 ): OxygenStateMessage =
    OxygenStateMessage(
      DrowningTarget(player_guid, player_progress),
      Some(DrowningTarget.recover(vehicle_guid, vehicle_progress))
    )

  def recover(
               player_guid: PlanetSideGUID,
               player_progress: Float,
               vehicle_guid: PlanetSideGUID,
               vehicle_progress: Float
          ): OxygenStateMessage =
    OxygenStateMessage(
      DrowningTarget.recover(player_guid, player_progress),
      Some(DrowningTarget.recover(vehicle_guid, vehicle_progress))
    )

  /**
    * A `Codec` for the repeated processing of three values.
    * This `Codec` is the basis for the packet's data.
    */
  private val oxygen_deprivation_codec: Codec[DrowningTarget] = (
    PlanetSideGUID.codec ::
      newcodecs.q_float(
        0.0f,
        204.8f,
        11
      ) :: //hackish: 2^11 == 2047, so it should be 204.7; but, 204.8 allows decode == encode
    Drowning.codec
    ).as[DrowningTarget]

  implicit val codec: Codec[OxygenStateMessage] = (
    ("player" | oxygen_deprivation_codec) ::
      optional(bool, "vehicle" | oxygen_deprivation_codec)
    ).as[OxygenStateMessage]
}
