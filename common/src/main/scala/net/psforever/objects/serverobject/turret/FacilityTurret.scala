// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.types.Vector3
import net.psforever.objects.vital.{DamageResistanceModel, StandardResistanceProfile, Vitality}

class FacilityTurret(tDef : FacilityTurretDefinition) extends Amenity
  with WeaponTurret
  with JammableUnit
  with Vitality
  with StandardResistanceProfile {
  /** some turrets can be updated; they all start without updates */
  private var upgradePath : TurretUpgrade.Value = TurretUpgrade.None
  private var middleOfUpgrade : Boolean = false

  WeaponTurret.LoadDefinition(this)

  override def Health_=(toHealth : Int) = super.Health_=(math.max(1, toHealth)) //TODO properly handle destroyed facility turrets

  def MaxHealth : Int = Definition.MaxHealth

  def MountPoints : Map[Int, Int] = Definition.MountPoints.toMap

  def Upgrade : TurretUpgrade.Value = upgradePath

  def Upgrade_=(upgrade : TurretUpgrade.Value) : TurretUpgrade.Value = {
    middleOfUpgrade = true //blocking flag; block early
    var updated = false
    //upgrade each weapon as long as that weapon has a valid option for that upgrade
    Definition.Weapons.foreach({ case(index, upgradePaths) =>
      if(upgradePaths.contains(upgrade)) {
        updated = true
        weapons(index).Equipment.get.asInstanceOf[TurretWeapon].Upgrade = upgrade
      }
    })
    if(updated) {
      upgradePath = upgrade
    }
    else {
      middleOfUpgrade = false //reset
    }
    Upgrade
  }

  def ConfirmUpgrade(upgrade : TurretUpgrade.Value) : TurretUpgrade.Value = {
    if(middleOfUpgrade && upgradePath == upgrade) {
      middleOfUpgrade = false
    }
    upgradePath
  }

  def isUpgrading : Boolean = middleOfUpgrade

  def DamageModel = Definition.asInstanceOf[DamageResistanceModel]

  def Definition : FacilityTurretDefinition = tDef
}

object FacilityTurret {
  /**
    * Overloaded constructor.
    * @param tDef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    * @return a `FacilityTurret` object
    */
  def apply(tDef : FacilityTurretDefinition) : FacilityTurret = {
    new FacilityTurret(tDef)
  }

  import akka.actor.ActorContext
  /**
    * Instantiate and configure a `FacilityTurret` object
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `MannedTurret` object
    */
  def Constructor(tdef : FacilityTurretDefinition)(id : Int, context : ActorContext) : FacilityTurret = {
    import akka.actor.Props
    val obj = FacilityTurret(tdef)
    obj.Actor = context.actorOf(Props(classOf[FacilityTurretControl], obj), s"${tdef.Name}_$id")
    obj
  }

  def Constructor(pos: Vector3, tdef : FacilityTurretDefinition)(id : Int, context : ActorContext) : FacilityTurret = {
    import akka.actor.Props
    val obj = FacilityTurret(tdef)
    obj.Position = pos
    obj.Actor = context.actorOf(Props(classOf[FacilityTurretControl], obj), s"${tdef.Name}_$id")
    obj
  }
}
