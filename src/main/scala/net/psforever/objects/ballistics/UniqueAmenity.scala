// Copyright (c) 2023 PSForever
package net.psforever.objects.ballistics

import net.psforever.types.{PlanetSideGUID, Vector3}

final case class UniqueAmenity(
                                zoneNumber: Int,
                                guid: PlanetSideGUID,
                                position: Vector3
                              ) extends SourceUniqueness
