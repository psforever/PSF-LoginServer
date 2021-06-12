// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.environment

import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.types.{OxygenState, PlanetSideGUID}

/**
  * Related to the progress of interacting with a body of water deeper than you are tall or
  * deeper than your vehicle is off the ground.
  * @param guid the target
  * @param state whether they are recovering or suffocating
  * @param progress the percentage of completion towards the state
  */
final case class OxygenStateTarget(
                                    guid: PlanetSideGUID,
                                    state: OxygenState,
                                    progress: Float
                                  )

/**
  * The target has clipped into a critical region of a piece of environment.
  * @param obj the target
  * @param environment the terrain clipping region
  * @param mountedVehicle whether or not the target is mounted
  *                       (specifically, if the target is a `Player` who is mounted in a `Vehicle`)
  */
final case class InteractingWithEnvironment(
                                             obj: PlanetSideServerObject,
                                             environment: PieceOfEnvironment,
                                             mountedVehicle: Option[OxygenStateTarget]
                                           )

/**
  * The target has ceased to clip into a critical region of a piece of environment.
  * @param obj the target
  * @param environment the previous terrain clipping region
  * @param mountedVehicle whether or not the target is mounted
  *                       (specifically, if the target is a `Player` who is mounted in a `Vehicle`)
  */
final case class EscapeFromEnvironment(
                                        obj: PlanetSideServerObject,
                                        environment: PieceOfEnvironment,
                                        mountedVehicle: Option[OxygenStateTarget]
                                      )

/**
  * Completely reset any internal actions or processes related to environment clipping.
  */
final case class RecoveredFromEnvironmentInteraction()
