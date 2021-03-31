// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{ActorContext, Props}
import net.psforever.objects.ballistics.{PlayerSource, SourceEntry}
import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.guid.GUIDTask
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.vital.etc.TriggerUsedReason
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{DeployableIcon, DeployableInfo, DeploymentAction}
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

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

  override def receive: Receive =
    deployableBehavior
      .orElse(takesDamage)
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

  override def loseOwnership(): Unit = {
    super.loseOwnership()
    val guid           = mine.GUID
    val zone           = mine.Zone
    val localEvents    = zone.LocalEvents
    val factionChannel = mine.Faction.toString
    val formerOwner    = mine.OwnerName.getOrElse("")
    mine.OwnerName     = None
    mine.Faction       = PlanetSideEmpire.NEUTRAL
    zone.AvatarEvents ! AvatarServiceMessage(
      factionChannel,
      AvatarAction.SetEmpire(Service.defaultPlayerGUID, guid, PlanetSideEmpire.NEUTRAL)
    )
    localEvents ! LocalServiceMessage(
      factionChannel,
      LocalAction.DeployableMapIcon(
        Service.defaultPlayerGUID,
        DeploymentAction.Dismiss,
        DeployableInfo(guid, DeployableIcon.Boomer, mine.Position, PlanetSideGUID(0))
      )
    )
    zone.Players.find { _.name == formerOwner } match {
      case Some(_) => //in this zone
        localEvents ! LocalServiceMessage(
          formerOwner,
          LocalAction.AlertDestroyDeployable(Service.defaultPlayerGUID, mine)
        )
      case _ => ;
    }
  }

  override def gainOwnership(player: Player): Unit = {
    super.gainOwnership(player)
    val guid           = mine.GUID
    val zone           = mine.Zone
    val name           = player.Name
    val faction        = player.Faction
    val factionChannel = faction.toString
    val localEvents    = zone.LocalEvents
    mine.Faction       = faction
    zone.AvatarEvents ! AvatarServiceMessage(
      factionChannel,
      AvatarAction.SetEmpire(Service.defaultPlayerGUID, guid, faction)
    )
    localEvents ! LocalServiceMessage(
      factionChannel,
      LocalAction.DeployableMapIcon(
        Service.defaultPlayerGUID,
        DeploymentAction.Build,
        DeployableInfo(guid, DeployableIcon.Boomer, mine.Position, mine.Owner.get)
      )
    )
    zone.Players.find { _.name == name } match {
      case Some(_) =>
        localEvents ! LocalServiceMessage(
          name,
          LocalAction.AlertBuildDeployable(mine)
        )
      case _ => ;
    }
  }

//  override def finalizeDeployable(tool : ConstructionItem) : Unit = {
//    val zone = mine.Zone
//    val trigger = new BoomerTrigger
//    trigger.Companion = mine.GUID
//    mine.Trigger = trigger
//    zone.tasks ! GUIDTask.UnregisterEquipment(tool)(zone.GUID)
//    zone.AvatarEvents ! AvatarServiceMessage(zone.id, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, tool.GUID))
//    Zone.EquipmentIs.Where(tool, tool.GUID, zone) match {
//      case Some(Zone.EquipmentIs.InContainer(player: Player, index)) =>
//        player.Slot(index).Equipment = None
//        zone.tasks ! HoldNewEquipmentUp(player)(trigger, index)
//      case _ => ;
//        mine.Trigger = None
//        //don't know where boomer trigger "should" go
//        //zone.tasks ! PutNewEquipmentInInventoryOrDrop(player)(trigger)
//    }
//    super.finalizeDeployable(tool)
//  }

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
        zone.tasks ! GUIDTask.UnregisterObjectTask(trigger)(zone.GUID)
      case None => ;
    }
  }
}
