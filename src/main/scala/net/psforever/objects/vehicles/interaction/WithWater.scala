// Copyright (c) 2024 PSForever
package net.psforever.objects.vehicles.interaction

import net.psforever.objects.{GlobalDefinitions, Vehicle}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.environment.interaction.{InteractionWith, RespondsToZoneEnvironment}
import net.psforever.objects.serverobject.environment.interaction.common.Watery
import net.psforever.objects.serverobject.environment.interaction.common.Watery.OxygenStateTarget
import net.psforever.objects.serverobject.environment.{PieceOfEnvironment, interaction}
import net.psforever.objects.zones.InteractsWithZone
import net.psforever.types.OxygenState

import scala.concurrent.duration._

class WithWater()
  extends InteractionWith
    with Watery {
  /**
    * Water causes vehicles to become disabled if they dive off too far, too deep.
    * Flying vehicles do not display progress towards being waterlogged.
    * They just disable outright.
    * @param obj the target
    * @param body the environment
    * @param data additional interaction information, if applicable
    */
  def doInteractingWith(
                         obj: InteractsWithZone,
                         body: PieceOfEnvironment,
                         data: Option[Any]
                       ): Unit = {
    obj match {
      case vehicle: Vehicle =>
        val (effect: Boolean, time: Long, percentage: Float) = {
          val (a, b, c) = Watery.drowningInWateryConditions(obj, condition.map(_.state), waterInteractionTime)
          if (a && GlobalDefinitions.isFlightVehicle(vehicle.Definition)) {
            (true, 0L, 0f) //no progress bar
          } else {
            (a, b, c)
          }
        }
        val cond = OxygenStateTarget(obj.GUID, body, OxygenState.Suffocation, percentage)
        if (effect) {
          condition = Some(cond)
          waterInteractionTime = System.currentTimeMillis() + time
          doInteractingWithTargets(
            obj,
            percentage,
            body,
            vehicle.Seats.values
              .flatMap {
                case seat if seat.isOccupied => seat.occupants
                case _ => Nil
              }
              .filter { p => p.isAlive && (p.Zone eq obj.Zone) }
          )
        }
      case _ => ()
    }
  }

  /**
    * When out of water, the vehicle no longer risks becoming disabled.
    * It does have to endure a recovery period to get back to full dehydration
    * Flying vehicles are exempt from this process due to the abrupt disability they experience.
    * @param obj the target
    * @param body the environment
    * @param data additional interaction information, if applicable
    */
  override def stopInteractingWith(
                                    obj: InteractsWithZone,
                                    body: PieceOfEnvironment,
                                    data: Option[Any]
                                  ): Unit = {
    obj match {
      case vehicle: Vehicle =>
        val (effect: Boolean, time: Long, percentage: Float) =
          Watery.recoveringFromWateryConditions(obj, condition.map(_.state), waterInteractionTime)
        val cond = OxygenStateTarget(obj.GUID, body, OxygenState.Recovery, percentage)
        if (effect) {
          condition = Some(cond)
          waterInteractionTime = System.currentTimeMillis() + time
          obj.Actor ! RespondsToZoneEnvironment.Timer(attribute, delay = time milliseconds, obj.Actor, interaction.RecoveredFromEnvironmentInteraction(attribute))
          stopInteractingWithTargets(
            obj,
            percentage,
            body,
            vehicle.Seats.values
              .flatMap {
                case seat if seat.isOccupied => seat.occupants
                case _ => Nil
              }
              .filter { p => p.isAlive && (p.Zone eq obj.Zone) }
          )
        }
      case _ => ()
    }
  }

  override def recoverFromInteracting(obj: InteractsWithZone): Unit = {
    super.recoverFromInteracting(obj)
    waterInteractionTime = 0L
    condition = None
  }

  /**
    * Tell the given targets that water causes vehicles to become disabled if they dive off too far, too deep.
    * @see `InteractingWithEnvironment`
    * @see `OxygenState`
    * @see `OxygenStateTarget`
    * @param obj the target
    * @param percentage the progress bar completion state
    * @param body the environment
    * @param targets recipients of the information
    */
  def doInteractingWithTargets(
                                   obj: PlanetSideServerObject,
                                   percentage: Float,
                                   body: PieceOfEnvironment,
                                   targets: Iterable[PlanetSideServerObject]
                                 ): Unit = {
    val state = Some(OxygenStateTarget(obj.GUID, body, OxygenState.Suffocation, percentage))
    targets.foreach(_.Actor ! interaction.InteractingWithEnvironment(body, state))
  }

  /**
    * Tell the given targets that, when out of water, the vehicle no longer risks becoming disabled.
    * @see `EscapeFromEnvironment`
    * @see `OxygenState`
    * @see `OxygenStateTarget`
    * @param obj the target
    * @param percentage the progress bar completion state
    * @param body the environment
    * @param targets recipients of the information
    */
  def stopInteractingWithTargets(
                                    obj: PlanetSideServerObject,
                                    percentage: Float,
                                    body: PieceOfEnvironment,
                                    targets: Iterable[PlanetSideServerObject]
                                  ): Unit = {
    val state = Some(OxygenStateTarget(obj.GUID, body, OxygenState.Recovery, percentage))
    targets.foreach(_.Actor ! interaction.EscapeFromEnvironment(body, state))
  }
}
