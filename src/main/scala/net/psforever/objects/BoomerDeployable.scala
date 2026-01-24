// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{ActorContext, Props}
import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry}
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.etc.TriggerUsedReason
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.zones.Zone
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.PlanetSideEmpire

import scala.annotation.unused

class BoomerDeployable(cdef: ExplosiveDeployableDefinition)
  extends ExplosiveDeployable(cdef) {
  private var trigger: Option[BoomerTrigger] = None

  def Trigger: Option[BoomerTrigger] = trigger

  def Trigger_=(item: BoomerTrigger): Option[BoomerTrigger] = {
    if (trigger.isEmpty) { //can only set trigger once
      trigger = Some(item)
    }
    Trigger
  }

  def Trigger_=(item: Option[BoomerTrigger]): Option[BoomerTrigger] = {
    if (item.isEmpty) {
      trigger = None
    }
    Trigger
  }
}

class BoomerDeployableDefinition(private val objectId: Int)
  extends ExplosiveDeployableDefinition(objectId) {
  override def Initialize(obj: Deployable, context: ActorContext): Unit = {
    obj.Actor =
      context.actorOf(Props(classOf[BoomerDeployableControl], obj), PlanetSideServerObject.UniqueActorName(obj))
  }
}

object BoomerDeployableDefinition {
  def apply(dtype: DeployedItem.Value): BoomerDeployableDefinition = {
    new BoomerDeployableDefinition(dtype.id)
  }
}

class BoomerDeployableControl(mine: BoomerDeployable)
  extends ExplosiveDeployableControl(mine) {

  def receive: Receive =
    commonMineBehavior
      .orElse {
        case CommonMessages.Use(player, Some(trigger: BoomerTrigger)) if mine.Trigger.contains(trigger) =>
          // the trigger damages the mine, which sets it off, which causes an explosion
          // think of this as an initiator to the proper explosion
          HandleDamage(
            mine,
            DamageInteraction(
              SourceEntry(mine),
              TriggerUsedReason(PlayerSource(player), trigger.GUID),
              mine.Position
            ).calculate()(mine),
            damage = 0
          )
        case _ => ()
      }

  def loseOwnership(@unused faction: PlanetSideEmpire.Value): Unit = {
    super.loseOwnership(mine, PlanetSideEmpire.NEUTRAL)
    val guid = mine.OwnerGuid
    mine.AssignOwnership(None)
    mine.OwnerGuid = guid
  }

  override def gainOwnership(player: Player): Unit = {
    val originalOwner = mine.OwnerName
    super.gainOwnership(player, player.Faction)
    val events = mine.Zone.LocalEvents
    val msg = LocalAction.DeployItem(mine)
    originalOwner.collect { name =>
      events ! LocalServiceMessage(name, msg)
    }
    events ! LocalServiceMessage(player.Name, msg)
  }

  override def dismissDeployable() : Unit = {
    super.dismissDeployable()
    val zone = mine.Zone
    mine.Trigger match {
      case Some(trigger) =>
        mine.Trigger = None
        trigger.Companion = None
        val guid = trigger.GUID
        Zone.EquipmentIs.Where(trigger, guid, zone) match {
          case Some(Zone.EquipmentIs.InContainer(container, index)) =>
            container.Slot(index).Equipment = None
          case Some(Zone.EquipmentIs.OnGround()) =>
            zone.Ground ! Zone.Ground.RemoveItem(guid)
          case _ => ()
        }
        zone.AvatarEvents! AvatarServiceMessage(
          zone.id,
          AvatarAction.ObjectDelete(guid)
        )
        TaskWorkflow.execute(GUIDTask.unregisterObject(zone.GUID, trigger))
      case None => ()
    }
  }

  /**
   * Boomers are not bothered by explosive sympathy
   * but can still be affected by sources of jammering.
   * @param obj the entity being damaged
   * @param damage the amount of damage
   * @param data historical information about the damage
   *  @return `true`, if the target can be affected;
   *        `false`, otherwise
   */
  override def CanDetonate(obj: Vitality with FactionAffinity, damage: Int, data: DamageInteraction): Boolean = {
    super.CanDetonate(obj, damage, data) || data.cause.isInstanceOf[TriggerUsedReason]
  }
}
