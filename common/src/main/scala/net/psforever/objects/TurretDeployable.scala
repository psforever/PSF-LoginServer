// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ce.{ComplexDeployable, Deployable, DeployedItem, LargeDeployableDefinition}
import net.psforever.objects.definition.DeployableDefinition
import net.psforever.objects.definition.converter.SmallTurretConverter
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.mount.MountableBehavior
import net.psforever.objects.serverobject.turret.{TurretDefinition, WeaponTurret}

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

class TurretDeployableDefinition(private val objectId : Int) extends TurretDefinition(objectId)
  with LargeDeployableDefinition {
  private val item = DeployedItem(objectId) //let throw NoSuchElementException
  Name = "turret_deployable"
  Packet = new SmallTurretConverter

  def Item : DeployedItem.Value = item

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
