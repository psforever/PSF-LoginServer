// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.types.Vector3
import net.psforever.objects.vital.{DamageResistanceModel, StandardResistanceProfile, Vitality}

class FacilityTurret(tDef : TurretDefinition) extends Amenity
  with WeaponTurret
  with Vitality
  with StandardResistanceProfile {
  /** some turrets can be updated; they all start without updates */
  private var upgradePath : TurretUpgrade.Value = TurretUpgrade.None

  WeaponTurret.LoadDefinition(this)

  override def Health_=(toHealth : Int) = super.Health_=(math.max(1, toHealth)) //TODO properly handle destroyed facility turrets

  def MaxHealth : Int = Definition.MaxHealth

  def MountPoints : Map[Int, Int] = Definition.MountPoints.toMap

  def Upgrade : TurretUpgrade.Value = upgradePath

  def Upgrade_=(upgrade : TurretUpgrade.Value) : TurretUpgrade.Value = {
    upgradePath = upgrade
    //upgrade each weapon as long as that weapon has a valid option for that upgrade
    Definition.Weapons.foreach({ case(index, upgradePaths) =>
      if(upgradePaths.contains(upgrade)) {
        weapons(index).Equipment.get.asInstanceOf[TurretWeapon].Upgrade = upgrade
      }
    })
    Upgrade
  }

  def DamageModel = Definition.asInstanceOf[DamageResistanceModel]

  def Definition : TurretDefinition = tDef
}

object FacilityTurret {
  /**
    * Overloaded constructor.
    * @param tDef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    * @return a `FacilityTurret` object
    */
  def apply(tDef : TurretDefinition) : FacilityTurret = {
    new FacilityTurret(tDef)
  }

  import akka.actor.ActorContext
  /**
    * Instantiate and configure a `FacilityTurret` object
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `MannedTurret` object
    */
  def Constructor(tdef : TurretDefinition)(id : Int, context : ActorContext) : FacilityTurret = {
    import akka.actor.Props
    val obj = FacilityTurret(tdef)
    obj.Actor = context.actorOf(Props(classOf[FacilityTurretControl], obj), s"${tdef.Name}_$id")
    obj
  }

  def Constructor(tdef : TurretDefinition, pos: Vector3)(id : Int, context : ActorContext) : FacilityTurret = {
    import akka.actor.Props
    val obj = FacilityTurret(tdef)
    obj.Position = pos
    obj.Actor = context.actorOf(Props(classOf[FacilityTurretControl], obj), s"${tdef.Name}_$id")
    obj
  }
}
