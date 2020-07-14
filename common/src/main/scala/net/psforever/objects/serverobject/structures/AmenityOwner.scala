// Copyright (c) 2019 PSForever
package net.psforever.objects.serverobject.structures

import net.psforever.objects.serverobject.PlanetSideServerObject

/**
  * Amenities are elements of the game that belong to other elements of the game.
  * Their owners are also elements of the game, ones that understand that they belong to a specific `Zone` object.
  * @see `PlanetSideServerObject`
  */
abstract class AmenityOwner extends PlanetSideServerObject {
  private var amenities: List[Amenity] = List.empty

  def Amenities: List[Amenity] = amenities

  def Amenities_=(obj: Amenity): List[Amenity] = {
    amenities = amenities :+ obj
    obj.Owner = this
    amenities
  }
}
