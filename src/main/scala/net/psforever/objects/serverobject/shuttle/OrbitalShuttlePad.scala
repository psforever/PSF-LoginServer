// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.shuttle

import akka.actor.ActorRef
import net.psforever.objects.Vehicle
import net.psforever.objects.serverobject.structures.{Amenity, AmenityDefinition}
import net.psforever.types.PlanetSideGUID

/**
  * The orbital shuttle pad which is the primary component of the high altitude rapid transport (HART) system.<br>
  * <br>
  * The orbital shuttle pad is a type of flat called an `obbasemesh`.
  * The shuttle component of the HART casually perches on top of the pad and
  * adjusts its states to control animation and passenger access.
  * The shuttle that is visible to the player and flies in and out of the zone is actually a hologram
  * of the real shuttle that is an invisible, intangible vehicle
  * forever stationary on top of the building.
  * @param spDef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  */
class OrbitalShuttlePad(spDef: AmenityDefinition) extends Amenity {
  private var _shuttle: Option[PlanetSideGUID] = None

  def shuttle: Option[PlanetSideGUID] = _shuttle

  def shuttle_=(orbitalShuttle: Vehicle): Option[PlanetSideGUID] = {
    _shuttle = _shuttle.orElse(Some(orbitalShuttle.GUID))
    _shuttle
  }

  def Definition: AmenityDefinition = spDef
}

object OrbitalShuttlePad {
  final case class GetShuttle(giveTo: ActorRef)

  final case class GiveShuttle(shuttle: Vehicle)

  /**
    * Overloaded constructor.
    * @param spDef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    * @return an `OrbitalShuttlePad` object
    */
  def apply(spDef: AmenityDefinition): OrbitalShuttlePad = {
    new OrbitalShuttlePad(spDef)
  }

  import akka.actor.ActorContext
  import net.psforever.types.Vector3

  /**
    * Instantiate and configure an `OrbitalShuttlePad` object
    * @param pdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    * @param pos the position (used to determine spawn point)
    * @param orient the orientation (used to indicate spawn direction)
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `OrbitalShuttlePad` object
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
