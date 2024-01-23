// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.serverobject.structures.{Amenity, AmenityOwner, Building}
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminalAware
import net.psforever.objects.serverobject.turret.auto.AutomatedTurret
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.types.Vector3

class FacilityTurret(tDef: FacilityTurretDefinition)
  extends Amenity
    with AutomatedTurret
    with JammableUnit
    with CaptureTerminalAware {
  WeaponTurret.LoadDefinition(turret = this)

  def TurretOwner: SourceEntry = {
    Seats
      .headOption
      .collect { case (_, a) => a }
      .flatMap(_.occupant)
      .map(SourceEntry(_))
      .getOrElse(SourceEntry(Owner))
  }

  override def Owner: AmenityOwner = {
    if (Zone.map.cavern) {
      Building.NoBuilding
    } else {
      super.Owner
    }
  }

  def Definition: FacilityTurretDefinition = tDef
}

object FacilityTurret {

  /**
    * Overloaded constructor.
    * @param tDef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    * @return a `FacilityTurret` object
    */
  def apply(tDef: FacilityTurretDefinition): FacilityTurret = {
    new FacilityTurret(tDef)
  }

  import akka.actor.ActorContext

  /**
    * Instantiate and configure a `FacilityTurret` object
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `MannedTurret` object
    */
  def Constructor(tdef: FacilityTurretDefinition)(id: Int, context: ActorContext): FacilityTurret = {
    import akka.actor.Props
    val obj = FacilityTurret(tdef)
    obj.Actor = context.actorOf(Props(classOf[FacilityTurretControl], obj), s"${tdef.Name}_$id")
    obj
  }

  def Constructor(pos: Vector3, tdef: FacilityTurretDefinition)(id: Int, context: ActorContext): FacilityTurret = {
    import akka.actor.Props
    val obj = FacilityTurret(tdef)
    obj.Position = pos
    obj.Actor = context.actorOf(Props(classOf[FacilityTurretControl], obj), s"${tdef.Name}_$id")
    obj
  }
}
