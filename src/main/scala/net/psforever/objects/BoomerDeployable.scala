// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{ActorContext, Props}
import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry}
import net.psforever.objects.vital.etc.TriggerUsedReason
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.zones.Zone
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.PlanetSideEmpire

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

class BoomerDeployableDefinition(private val objectId: Int) extends ExplosiveDeployableDefinition(objectId) {
  override def Initialize(obj: Deployable, context: ActorContext) = {
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
          mine.Destroyed = true
          ExplosiveDeployableControl.DamageResolution(
            mine,
            DamageInteraction(
              SourceEntry(mine),
              TriggerUsedReason(PlayerSource(player), trigger.GUID),
              mine.Position
            ).calculate()(mine),
            damage = 0
          )

        case _ => ;
      }

  override def loseOwnership(faction: PlanetSideEmpire.Value): Unit = {
    super.loseOwnership(PlanetSideEmpire.NEUTRAL)
    mine.OwnerName = None
  }

  override def gainOwnership(player: Player): Unit = {
    mine.Faction = PlanetSideEmpire.NEUTRAL //force map icon redraw
    super.gainOwnership(player, player.Faction)
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
          case _ => ;
        }
        zone.AvatarEvents! AvatarServiceMessage(
          zone.id,
          AvatarAction.ObjectDelete(Service.defaultPlayerGUID, trigger.GUID)
        )
        TaskWorkflow.execute(GUIDTask.unregisterObject(zone.GUID, trigger))
      case None => ;
    }
  }
}
