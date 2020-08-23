// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.types.Vector3

class FacilityTurret(tDef: FacilityTurretDefinition) extends Amenity with WeaponTurret with JammableUnit {
  WeaponTurret.LoadDefinition(this)

  def MountPoints: Map[Int, Int] = Definition.MountPoints.toMap

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

  final case class RechargeAmmo()
  final case class WeaponDischarged()

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
