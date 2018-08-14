// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, ActorRef, Props}
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.definition.converter.{ShieldGeneratorConverter, SmallDeployableConverter, SmallTurretConverter}
import net.psforever.objects.equipment.CItem
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.mount.MountableBehavior
import net.psforever.objects.serverobject.turret.{TurretDefinition, WeaponTurret}
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.PlanetSideEmpire

import scala.concurrent.duration._

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
  private var ownerName : Option[String] = None

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

  def OwnerName : Option[String] = ownerName

  def OwnerName_=(owner : String) : Option[String] = OwnerName_=(Some(owner))

  def OwnerName_=(owner : Player) : Option[String] = OwnerName_=(Some(owner.Name))

  def OwnerName_=(owner : Option[String]) : Option[String] = {
    owner match {
      case Some(_) =>
        ownerName = owner
      case None =>
        ownerName = None
    }
    OwnerName
  }

  def Definition : ObjectDefinition with DeployableDefinition
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
  private var deployTime : Long = (1 second).toMillis //ms

  def Item : CItem.DeployedItem.Value

  def DeployCategory : DeployableCategory.Value = category

  def DeployCategory_=(cat : DeployableCategory.Value) : DeployableCategory.Value = {
    category = cat
    DeployCategory
  }

  def DeployTime : Long = deployTime

  def DeployTime_=(time : FiniteDuration) : Long = DeployTime_=(time.toMillis)

  def DeployTime_=(time: Long) : Long = {
    deployTime = time
    DeployTime
  }

  def Initialize(obj : PlanetSideGameObject with Deployable, context : ActorContext) : Unit = { }

  def Initialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) : Unit = { }

  def Uninitialize(obj : PlanetSideGameObject with Deployable, context : ActorContext) : Unit = { }

  def Uninitialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) : Unit = { }
}

object DeployableDefinition {
  def SimpleUninitialize(obj : PlanetSideGameObject, context : ActorContext) : Unit = { }

  def SimpleUninitialize(obj : PlanetSideServerObject, context : ActorContext) : Unit = {
    obj.Actor ! akka.actor.PoisonPill
    obj.Actor = ActorRef.noSender
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

  override def Initialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) = {
    obj.Actor = context.actorOf(Props(classOf[TurretControl], obj), s"${obj.Definition.Name}_${obj.GUID.guid}")
  }

  override def Uninitialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) = {
    DeployableDefinition.SimpleUninitialize(obj, context)
  }
}

object TurretDeployableDefinition {
  def apply(dtype : CItem.DeployedItem.Value) : TurretDeployableDefinition = {
    new TurretDeployableDefinition(dtype.id)
  }
}

/** control actors */

class TurretControl(turret : TurretDeployable) extends Actor
  with FactionAffinityBehavior.Check
  with MountableBehavior.Mount
  with MountableBehavior.Dismount {
  def MountableObject = turret //do not add type!

  def FactionObject : FactionAffinity = turret

  def receive : Receive = checkBehavior
    .orElse(dismountBehavior)
    .orElse(turretMountBehavior)
    .orElse {
      case _ => ;
    }
}

/** implementing classes */

class ExplosiveDeployable(cdef : SimpleDeployableDefinition) extends SimpleDeployable(cdef) {
  private var exploded : Boolean = false

  def Exploded : Boolean = exploded

  def Exploded_=(fuse : Boolean) : Boolean = {
    exploded = fuse
    Exploded
  }
}

class BoomerDeployable(cdef : SimpleDeployableDefinition) extends ExplosiveDeployable(cdef) {
  private var trigger : Option[BoomerTrigger] = None

  def Trigger : Option[BoomerTrigger] = trigger

  def Trigger_=(item : BoomerTrigger) : Option[BoomerTrigger] = {
    if(trigger.isEmpty) { //can only set trigger once
      trigger = Some(item)
    }
    Trigger
  }

  def Trigger_=(item : Option[Any]) : Option[BoomerTrigger] = {
    if(item.isEmpty) {
      trigger = None
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
