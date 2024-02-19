// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.environment.interaction

import net.psforever.objects.serverobject.environment.{EnvironmentTrait, PieceOfEnvironment}

/**
  * The target has clipped into a critical region of a piece of environment.
  * @param environment the terrain clipping region
  * @param mountedVehicle whether or not the target is mounted
  *                       (specifically, if the target is a `Player` who is mounted in a `Vehicle`)
  */
final case class InteractingWithEnvironment(
                                             environment: PieceOfEnvironment,
                                             mountedVehicle: Option[Any]
                                           )

/**
  * The target has ceased to clip into a critical region of a piece of environment.
  * @param environment the previous terrain clipping region
  * @param mountedVehicle whether or not the target is mounted
  *                       (specifically, if the target is a `Player` who is mounted in a `Vehicle`)
  */
final case class EscapeFromEnvironment(
                                        environment: PieceOfEnvironment,
                                        mountedVehicle: Option[Any]
                                      )

/**
  * Completely reset internal actions or processes related to environment clipping.
  */
final case class RecoveredFromEnvironmentInteraction(attribute: EnvironmentTrait)

/**
 * Completely reset internal actions or processes related to environment clipping.
 */
case object ResetAllEnvironmentInteractions
