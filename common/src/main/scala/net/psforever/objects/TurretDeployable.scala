// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.definition.{BaseDeployableDefinition, DeployableDefinition}
import net.psforever.objects.definition.converter.SmallTurretConverter
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.mount.MountableBehavior
import net.psforever.objects.serverobject.turret.{TurretDefinition, WeaponTurret}

class TurretDeployable(tdef : TurretDeployableDefinition) extends PlanetSideServerObject
  with Deployable
  with WeaponTurret
  with Hackable {
  private var shields : Int = 0

  WeaponTurret.LoadDefinition(this) //calls the equivalent of Health = Definition.MaxHealth

  def MaxHealth : Int = Definition.MaxHealth

  def Shields : Int = shields

  def Shields_=(toShields : Int) : Int = {
    shields = math.min(math.max(0, toShields), MaxShields)
    Shields
  }

  def MaxShields : Int = {
    0//Definition.MaxShields
  }

  def MountPoints : Map[Int, Int] = Definition.MountPoints.toMap

  //override to clarify inheritance conflict
  override def Health : Int = super[Deployable].Health
  //override to clarify inheritance conflict
  override def Health_=(toHealth : Int) : Int = super[Deployable].Health_=(toHealth)

  override def Definition = tdef
}

class TurretDeployableDefinition(private val objectId : Int) extends TurretDefinition(objectId)
  with BaseDeployableDefinition {
  private val item = DeployedItem(objectId) //let throw NoSuchElementException
  Name = "turret_deployable"
  Packet = new SmallTurretConverter

  def Item : DeployedItem.Value = item

  //override to clarify inheritance conflict
  override def MaxHealth : Int = super[BaseDeployableDefinition].MaxHealth
  //override to clarify inheritance conflict
  override def MaxHealth_=(max : Int) : Int = super[BaseDeployableDefinition].MaxHealth_=(max)

  override def Initialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) = {
    obj.Actor = context.actorOf(Props(classOf[TurretControl], obj), s"${obj.Definition.Name}_${obj.GUID.guid}")
  }

  override def Uninitialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) = {
    DeployableDefinition.SimpleUninitialize(obj, context)
  }
}

object TurretDeployableDefinition {
  def apply(dtype : DeployedItem.Value) : TurretDeployableDefinition = {
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
