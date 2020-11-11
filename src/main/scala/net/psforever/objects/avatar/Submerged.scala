// Copyright (c) 2020 PSForever
package net.psforever.objects.avatar

import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.zones.PieceOfEnvironment
import net.psforever.types.{OxygenState, PlanetSideGUID}

final case class OxygenStateTarget(
                                    guid: PlanetSideGUID,
                                    state: OxygenState,
                                    progress: Float
                                  )

final case class Submerged(obj: PlanetSideServerObject, fluid: PieceOfEnvironment, mountedVehicle: Option[OxygenStateTarget])

final case class Surfaced(obj: PlanetSideServerObject, fluid: PieceOfEnvironment, mountedVehicle: Option[OxygenStateTarget])

final case class RecoveredFromSubmerging()
