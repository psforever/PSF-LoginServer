// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.{Attempt, Codec}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * na
  * @param vehicle_guid the player's mounted vehicle
  * @param time the countdown's time upon start
  * @param active show a new countdown if `true` (resets any active countdown);
  *               clear any active countdowns if `false`
  */
final case class WaterloggedVehicleState(vehicle_guid : PlanetSideGUID,
                                         time : Double,
                                         active : Boolean)

/**
  * na
  * @param player_guid the player
  * @param time the countdown's time upon start
  * @param active show a new countdown if `true` (resets any active countdown);
  *               clear any active countdowns if `false`
  * @param vehicle_state optional state of the vehicle the player is driving
  */
final case class OxygenStateMessage(player_guid : PlanetSideGUID,
                                    time : Double,
                                    active : Boolean,
                                    vehicle_state : Option[WaterloggedVehicleState] = None)
  extends PlanetSideGamePacket {
  type Packet = OxygenStateMessage
  def opcode = GamePacketOpcode.OxygenStateMessage
  def encode = OxygenStateMessage.encode(this)
}

object OxygenStateMessage extends Marshallable[OxygenStateMessage] {
  /**
    * Overloaded constructor that removes the optional state of the `WaterloggedVehicleState` parameter.
    * @param player_guid the player
    * @param time the countdown's time upon start
    * @param active show or clear the countdown
    * @param vehicle_state state of the vehicle the player is driving
    * @return
    */
  def apply(player_guid : PlanetSideGUID, time : Double, active : Boolean, vehicle_state : WaterloggedVehicleState) : OxygenStateMessage =
    OxygenStateMessage(player_guid, time, active, Some(vehicle_state))

  /**
    * A simple pattern that expands the datatypes of the packet's basic `Codec`.
    */
  private type basePattern = PlanetSideGUID :: Double :: Boolean :: HNil

  /**
    * A `Codec` for the repeated processing of three values.
    * This `Codec` is the basis for the packet's data.
    */
  private val base_codec : Codec[basePattern] =
    PlanetSideGUID.codec ::
      newcodecs.q_double(0.0, 204.8, 11) :: //hackish: 2^11 == 2047, so it should be 204.7; but, 204.8 allows the decode == encode
      bool

  implicit val codec : Codec[OxygenStateMessage] = (
    base_codec.exmap[basePattern] (
      {
        case guid :: time :: active :: HNil =>
          Attempt.successful(guid :: time :: active :: HNil)
      },
      {
        case guid :: time :: active :: HNil =>
          Attempt.successful(guid :: time :: active :: HNil)
      }
    ) :+
      optional(bool,
        "vehicle_state" | base_codec.exmap[WaterloggedVehicleState] (
          {
            case guid :: time :: active :: HNil =>
              Attempt.successful(WaterloggedVehicleState(guid, time, active))
          },
          {
            case WaterloggedVehicleState(guid, time, active) =>
              Attempt.successful(guid :: time :: active :: HNil)
          }
        ).as[WaterloggedVehicleState]
      )
    ).as[OxygenStateMessage]
}
