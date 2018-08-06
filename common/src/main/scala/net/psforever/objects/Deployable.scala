// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.definition.converter.{ShieldGeneratorConverter, SmallDeployableConverter, SmallTurretConverter}
import net.psforever.objects.equipment.{CItem, Equipment}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.turret.{TurretDefinition, WeaponTurret}
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.PlanetSideEmpire

/** super classes */

object DeployableCategory extends Enumeration {
  type Type = Value

  val
  Boomers,
  Mines,
  SmallTurrets,
  Sensors,
  TankTraps,
  FieldTurrets,
  ShieldGenerators,
  Telepads
    = Value
}

trait Deployable extends FactionAffinity {
  private var faction : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  private var owner : Option[PlanetSideGUID] = None

  def Faction : PlanetSideEmpire.Value = faction

  override def Faction_=(toFaction : PlanetSideEmpire.Value) : PlanetSideEmpire.Value = {
    faction = toFaction
    Faction
  }

  def Owner : Option[PlanetSideGUID] = owner

  def Owner_=(owner : PlanetSideGUID) : Option[PlanetSideGUID] = Owner_=(Some(owner))

  def Owner_=(owner : Player) : Option[PlanetSideGUID] = Owner_=(Some(owner.GUID))

  def Owner_=(owner : Option[PlanetSideGUID]) : Option[PlanetSideGUID] = {
    owner match {
      case Some(_) =>
        this.owner = owner
      case None =>
        this.owner = None
    }
    Owner
  }

  def Definition : DeployableDefinition
}

trait LargeDeployable extends Deployable {
  def Health : Int
  def Health_=(toHealth : Int) : Int
}

abstract class SimpleDeployable(cdef : SimpleDeployableDefinition) extends PlanetSideGameObject
  with Deployable {

  def Definition = cdef
}

abstract class ComplexDeployable(cdef : ObjectDefinition with LargeDeployableDefinition) extends PlanetSideServerObject
  with LargeDeployable {
  private var health : Int = 1
  Health = cdef.MaxHealth

  def Health : Int = health

  def Health_=(toHealth : Int) : Int = {
    health = toHealth
    Health
  }

  def MaxHealth : Int = Definition.MaxHealth

  def Definition = cdef
}

/** definitions */

trait DeployableDefinition {
  private var category : DeployableCategory.Value = DeployableCategory.Boomers

  def Item : CItem.DeployedItem.Value

  def DeployCategory : DeployableCategory.Value = category

  def DeployCategory_=(cat : DeployableCategory.Value) : DeployableCategory.Value = {
    category = cat
    DeployCategory
  }
}

trait LargeDeployableDefinition extends DeployableDefinition {
  private var maxHealth : Int = 1

  def MaxHealth : Int = maxHealth

  def MaxHealth_=(toHealth : Int) : Int = {
    maxHealth = toHealth
    MaxHealth
  }
}

class SimpleDeployableDefinition(private val objectId : Int) extends ObjectDefinition(objectId)
  with DeployableDefinition {
  private val item = CItem.DeployedItem(objectId) //let throw NoSuchElementException
  Packet = new SmallDeployableConverter

  def Item : CItem.DeployedItem.Value = item
}

object SimpleDeployableDefinition {
  def apply(dtype : CItem.DeployedItem.Value) : SimpleDeployableDefinition = {
    new SimpleDeployableDefinition(dtype.id)
  }
}

class ComplexDeployableDefinition(private val objectId : Int) extends ObjectDefinition(objectId)
  with LargeDeployableDefinition {
  private val item = CItem.DeployedItem(objectId) //let throw NoSuchElementException

  def Item : CItem.DeployedItem.Value = item
}

object ComplexDeployableDefinition {
  def apply(dtype : CItem.DeployedItem.Value) : ComplexDeployableDefinition = {
    new ComplexDeployableDefinition(dtype.id)
  }
}

class ShieldGeneratorDefinition extends ComplexDeployableDefinition(240) {
  Packet = new ShieldGeneratorConverter
  DeployCategory = DeployableCategory.ShieldGenerators
}

class TurretDeployableDefinition(private val objectId : Int) extends TurretDefinition(objectId)
  with LargeDeployableDefinition {
  private val item = CItem.DeployedItem(objectId) //let throw NoSuchElementException
  item match {
    case _ @ (CItem.DeployedItem.spitfire_turret | CItem.DeployedItem.spitfire_cloaked | CItem.DeployedItem.spitfire_aa |
         CItem.DeployedItem.portable_manned_turret | CItem.DeployedItem.portable_manned_turret_tr |
         CItem.DeployedItem.portable_manned_turret_nc | CItem.DeployedItem.portable_manned_turret_vs) => ;
    case _ =>
      throw new IllegalArgumentException(s"turret deployable object type must be defined - $item ($objectId)")
  }
  Name = "turret_deployable"
  Packet = new SmallTurretConverter

  def Item : CItem.DeployedItem.Value = item

  //override to clarify inheritance conflict
  override def MaxHealth : Int = super[LargeDeployableDefinition].MaxHealth
  //override to clarify inheritance conflict
  override def MaxHealth_=(toHealth : Int) : Int = super[LargeDeployableDefinition].MaxHealth_=(toHealth)
}

object TurretDeployableDefinition {
  def apply(dtype : CItem.DeployedItem.Value) : TurretDeployableDefinition = {
    new TurretDeployableDefinition(dtype.id)
  }
}

/** implementing classes */

class ExplosiveDeployable(cdef : SimpleDeployableDefinition) extends SimpleDeployable(cdef)

class BoomerDeployable(cdef : SimpleDeployableDefinition) extends SimpleDeployable(cdef) {
  private var trigger : Option[Equipment] = None

  def Trigger : Option[Equipment] = trigger

  def Trigger_=(item : Equipment) : Option[Equipment] = {
    if(trigger.isEmpty) { //can only set trigger once
      trigger = Some(item)
    }
    Trigger
  }
}

class SensorDeployable(cdef : SimpleDeployableDefinition) extends SimpleDeployable(cdef)
  with Hackable

class TurretDeployable(tdef : TurretDeployableDefinition) extends ComplexDeployable(tdef)
  with WeaponTurret
  with Hackable {
  WeaponTurret.LoadDefinition(this)

  def MountPoints : Map[Int, Int] = Definition.MountPoints.toMap

  //override to clarify inheritance conflict
  override def Health : Int = super[ComplexDeployable].Health
  //override to clarify inheritance conflict
  override def Health_=(toHealth : Int) : Int = super[ComplexDeployable].Health_=(toHealth)

  override def Definition : TurretDeployableDefinition = tdef
}

class TrapDeployable(cdef : ComplexDeployableDefinition) extends ComplexDeployable(cdef)

class ShieldGeneratorDeployable(cdef : ShieldGeneratorDefinition) extends ComplexDeployable(cdef)
  with Hackable
