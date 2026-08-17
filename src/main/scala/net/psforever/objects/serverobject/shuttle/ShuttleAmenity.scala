// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.shuttle

import akka.actor.ActorRef
import net.psforever.objects.serverobject.structures.{Amenity, AmenityDefinition}
import net.psforever.types.PlanetSideGUID

/**
  * A pseudo-`Amenity` of the high-altitude rapid transport (HART) building
  * whose sole purpose is to allow the HART orbital shuttle to be initialized
  * as if it were a normal `Amenity`-level feature of the building.
  * This should not be considered an actual game object as defined by the game.
  * It should resemble the orbital shuttle that it wraps in most important measurable ways.
  * @see `OrbitalShuttleControl`
  * @throws `AssertionError` if the vehicle is not a `OrbitalShuttle`
  * @param shuttle the shuttle
  */
class ShuttleAmenity(shuttle: OrbitalShuttle) extends Amenity {
  override def GUID = shuttle.GUID

  override def GUID_=(guid: PlanetSideGUID) = GUID

  override def DamageModel = shuttle.DamageModel

  override def Actor = shuttle.Actor

  override def Actor_=(control: ActorRef) = Actor

  override def Health = shuttle.Health

  override def Faction = shuttle.Faction

  def Definition = ShuttleAmenity.definition
}

object ShuttleAmenity {
  final val definition = new AmenityDefinition(net.psforever.packet.game.objectcreate.ObjectClass.orbital_shuttle) {
    Name = "orbital_shuttle_fake"
    Damageable = false
    Repairable = false
  }
}
