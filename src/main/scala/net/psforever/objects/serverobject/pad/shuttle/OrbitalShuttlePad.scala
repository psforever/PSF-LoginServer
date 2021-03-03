// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.pad.shuttle

import net.psforever.objects.{GlobalDefinitions, Vehicle}
import net.psforever.objects.serverobject.structures.{Amenity, AmenityDefinition}
import net.psforever.objects.vehicles.AccessPermissionGroup

class OrbitalShuttlePad(spDef: AmenityDefinition) extends Amenity {
  val shuttle = new Vehicle(GlobalDefinitions.orbital_shuttle) {
    //the shuttle only has passenger seats
    override def SeatPermissionGroup(seatNumber : Int) : Option[AccessPermissionGroup.Value] = {
      Seat(seatNumber) match {
        case Some(_) => Some(AccessPermissionGroup.Passenger)
        case _       => None
      }
    }
  }

  def Definition: AmenityDefinition = spDef
}

object OrbitalShuttlePad {
  def apply(spDef: AmenityDefinition): OrbitalShuttlePad = {
    new OrbitalShuttlePad(spDef)
  }

  import akka.actor.ActorContext
  import net.psforever.types.Vector3

  /**
    * Instantiate and configure a `VehicleSpawnPad` object
    * @param pdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    * @param pos the position (used to determine spawn point)
    * @param orient the orientation (used to indicate spawn direction)
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `VehicleSpawnPad` object
    */
  def Constructor(pos: Vector3, pdef: AmenityDefinition, orient: Vector3)(
    id: Int,
    context: ActorContext
  ): OrbitalShuttlePad = {
    import akka.actor.Props

    val obj = OrbitalShuttlePad(pdef)
    obj.Position = pos
    obj.Orientation = orient
    obj.Actor = context.actorOf(Props(classOf[OrbitalShuttlePadControl], obj), s"${obj.Definition.Name}_$id")
    obj
  }
}
