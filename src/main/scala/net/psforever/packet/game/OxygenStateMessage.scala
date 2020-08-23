// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.{Attempt, Codec}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Alert the condition of a vehicle the player is using when going too far underwater.
  * The player must be mounted in/on this vehicle at start time for this countdown to display.
  * @param vehicle_guid the player's mounted vehicle
  * @param progress the remaining countdown;
  *                 for vehicle waterlog condition, the progress per second rate is very high
  * @param active show a new countdown if `true` (resets any active countdown);
  *               clear any active countdowns if `false`;
  *               defaults to `true`
  */
final case class WaterloggedVehicleState(vehicle_guid: PlanetSideGUID, progress: Float, active: Boolean = true)

/**
  * Dispatched by the server to cause the player to slowly drown.
  * If the player is mounted in a vehicle at the time, alert the player that the vehicle may be disabled.<br>
  * <br>
  * When a player walks too far underwater, a borderless red progress bar with a countdown from 100 (98) is displayed across the screen.
  * The countdown proceeds to zero at a fixed rate and is timed with the depleting progress bar.
  * When it reaches zero, the player will be killed.
  * If the player is in a vehicle after a certain depth, a blue bar and countdown pair will superimpose the red indicators.
  * It depletes much more rapidly than the red indicators.
  * When it reaches zero, the vehicle will become disabled.
  * All players in the vehicle's seats will be kicked and they will not be allowed back in.<br>
  * <br>
  * Normally, the countdowns should be set to begin at 100 (100.0).
  * This is the earliest the drowning GUI will appear for either blue or red indicators.
  * Passing greater intervals - up to 204.8 - will start the countdown silently but the GUI will be hidden until 100.0.
  * (The progress indicators will actually appear to start counting from 98.)
  * Managing the secondary vehicle countdown independent of the primary player countdown requires updating with the correct levels.
  * The countdown can be cancelled by instructing it to be `active = false`.<br>
  * <br>
  * Except for updating the indicators, all other functionality of "drowning" is automated by the server.
  * @param player_guid the player
  * @param progress the remaining countdown;
  *                 for character oxygen, the progress per second rate is about 1
  * @param active show a new countdown if `true` (resets any active countdown);
  *               clear any active countdowns if `false`
  * @param vehicle_state optional state of the vehicle the player is driving
  */
final case class OxygenStateMessage(
    player_guid: PlanetSideGUID,
    progress: Float,
    active: Boolean,
    vehicle_state: Option[WaterloggedVehicleState] = None
) extends PlanetSideGamePacket {
  type Packet = OxygenStateMessage
  def opcode = GamePacketOpcode.OxygenStateMessage
  def encode = OxygenStateMessage.encode(this)
}

object OxygenStateMessage extends Marshallable[OxygenStateMessage] {

  /**
    * Overloaded constructor that removes the optional state of the `WaterloggedVehicleState` parameter.
    * @param player_guid the player
    * @param progress the remaining countdown
    * @param active show or clear the countdown
    * @param vehicle_state state of the vehicle the player is driving
    * @return
    */
  def apply(
      player_guid: PlanetSideGUID,
      progress: Float,
      active: Boolean,
      vehicle_state: WaterloggedVehicleState
  ): OxygenStateMessage =
    OxygenStateMessage(player_guid, progress, active, Some(vehicle_state))

  /**
    * A simple pattern that expands the datatypes of the packet's basic `Codec`.
    */
  private type basePattern = PlanetSideGUID :: Float :: Boolean :: HNil

  /**
    * A `Codec` for the repeated processing of three values.
    * This `Codec` is the basis for the packet's data.
    */
  private val base_codec: Codec[basePattern] =
    PlanetSideGUID.codec ::
      newcodecs.q_float(
        0.0f,
        204.8f,
        11
      ) :: //hackish: 2^11 == 2047, so it should be 204.7; but, 204.8 allows decode == encode
      bool

  implicit val codec: Codec[OxygenStateMessage] = (
    base_codec.exmap[basePattern](
      {
        case guid :: time :: active :: HNil =>
          Attempt.successful(guid :: time :: active :: HNil)
      },
      {
        case guid :: time :: active :: HNil =>
          Attempt.successful(guid :: time :: active :: HNil)
      }
    ) :+
      optional(
        bool,
        "vehicle_state" | base_codec
          .exmap[WaterloggedVehicleState](
            {
              case guid :: time :: active :: HNil =>
                Attempt.successful(WaterloggedVehicleState(guid, time, active))
            },
            {
              case WaterloggedVehicleState(guid, time, active) =>
                Attempt.successful(guid :: time :: active :: HNil)
            }
          )
          .as[WaterloggedVehicleState]
      )
  ).as[OxygenStateMessage]
}
