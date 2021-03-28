// Copyright (c) 2021 PSForever
package net.psforever.objects.ce

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.guid.GUIDTask
import net.psforever.objects.{ConstructionItem, Default, GlobalDefinitions, Player}
import net.psforever.objects.zones.Zone
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game._
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.PlanetSideGUID

import scala.concurrent.duration._

trait DeployableBehavior {
  _: Actor =>
  def DeployableObject: Deployable

  var deletionType: Int = 2
  var constructed : Boolean = false
  var setup: Cancellable = Default.Cancellable
  var decayTimer : Cancellable = Default.Cancellable

  def deployableBehaviorPostStop(): Unit = {
    setup.cancel()
    decayTimer.cancel()
  }

  val deployableBehavior: Receive = {
    case Zone.Deployable.Setup(tool)
      if !constructed && setup.isCancelled =>
      setupDeployable(tool)

    case DeployableBehavior.Finalize(tool) =>
      finalizeDeployable(tool)

    case Deployable.Ownership(None)
      if constructed =>
      loseOwnership()

    case Deployable.Ownership(Some(player))
      if constructed && !DeployableObject.Destroyed =>
      gainOwnership(player)

    case Deployable.Deconstruct(time)
      if constructed && decayTimer.isCancelled =>
      deconstructDeployable(time)

    case DeployableBehavior.FinalizeElimination() =>
      dismissDeployable()
  }

  def loseOwnership(): Unit = {
    DeployableObject.Owner = None //OwnerName should remain set
    if (decayTimer.isCancelled) {
      import scala.concurrent.ExecutionContext.Implicits.global
      decayTimer = context.system.scheduler.scheduleOnce(Deployable.decay, self, Deployable.Deconstruct())
    }
  }

  def gainOwnership(player: Player): Unit = {
    decayTimer.cancel()
    DeployableObject.AssignOwnership(player)
  }

  def handleConstructionTool(obj: Deployable, tool: ConstructionItem): Unit = {
    val zone = obj.Zone
    val owner = obj.Owner.getOrElse(Service.defaultPlayerGUID)
    tool.Definition match {
      case GlobalDefinitions.ace =>
        zone.LocalEvents ! LocalServiceMessage(
          zone.id,
          LocalAction.TriggerEffectLocation(owner, "spawn_object_effect", obj.Position, obj.Orientation)
        )
      case GlobalDefinitions.advanced_ace =>
        if (owner != Service.defaultPlayerGUID) {
          //put fdu down; it will be removed from the holster
          zone.AvatarEvents ! AvatarServiceMessage(zone.id, AvatarAction.PutDownFDU(owner))
        } else {
          zone.LocalEvents ! LocalServiceMessage(
            zone.id,
            LocalAction.TriggerEffectLocation(owner, "spawn_object_effect", obj.Position, obj.Orientation)
          )
        }
      case GlobalDefinitions.router_telepad => ; //no special animation
      case _ =>
        org.log4s.getLogger(name = "Deployables").warn(
          s"not sure what kind of construction item to animate - ${tool.Definition}"
        )
    }
  }

  def setupDeployable(tool: ConstructionItem): Unit = {
    val obj = DeployableObject
    handleConstructionTool(obj, tool)
    obj.Zone.LivePlayers.find { p => obj.OwnerName.contains(p.Name) } match {
      case Some(p) => p.Actor ! Zone.Deployable.Build(obj, tool) //owner is trying to put it down
      case None => //obj.Actor ! Zone.Deployable.Setup(tool) //strong and independent deployable
    }
    import scala.concurrent.ExecutionContext.Implicits.global
    setup = context.system.scheduler.scheduleOnce(
      obj.Definition.DeployTime milliseconds,
      self,
      DeployableBehavior.Finalize(tool)
    )
  }

  def finalizeDeployable(tool: ConstructionItem): Unit = {
    setup.cancel()
    constructed = true
    val obj = DeployableObject
    obj.Zone.LivePlayers.find { p => obj.OwnerName.contains(p.Name) } match {
      case Some(p) => p.Actor ! Zone.Deployable.IsBuilt(obj, tool)
      case None => ;
    }
    DeployableBehavior.DeployableBuildActivity(obj, tool)
  }

  def deconstructDeployable(time: Option[FiniteDuration]): Unit = {
    val duration = time.getOrElse(Deployable.cleanup)
    import scala.concurrent.ExecutionContext.Implicits.global
    setup.cancel()
    decayTimer.cancel()
    setup = context.system.scheduler.scheduleOnce(duration, self, DeployableBehavior.FinalizeElimination())
  }

  def unregisterDeployable(zone: Zone, obj: Deployable): Unit = {
    zone.tasks ! GUIDTask.UnregisterObjectTask(obj)(zone.GUID)
  }

  def dismissDeployable(): Unit = {
    constructed = false
    setup.cancel()
    decayTimer.cancel()
    val obj = DeployableObject
    val zone = obj.Zone
    zone.Deployables ! Zone.Deployable.Dismiss(obj)
    unregisterDeployable(zone, obj)
    zone.LocalEvents ! LocalServiceMessage(
      zone.id,
      LocalAction.EliminateDeployable(obj, obj.GUID, obj.Position, deletionType)
    )
    obj.OwnerName match {
      case Some(name) =>
        zone.LocalEvents ! LocalServiceMessage(
          name,
          LocalAction.AlertDestroyDeployable(Service.defaultPlayerGUID, obj)
        )
      case None => ;
    }
  }
}

object DeployableBehavior {
  private case class Finalize(tool: ConstructionItem)

  private case class FinalizeElimination()

  /**
    * Common actions related to constructing a new `Deployable` object in the game environment.<br>
    * <br>
    * Besides the standard `ObjectCreateMessage` packet that produces the model and game object on the client,
    * the map icon for the deployable just introduced is also created on the clients of all faction-affiliated players.
    * This icon is important as, short of destroying it,
    * the owner has no other means of controlling the created object that it is associated with.
    * @param obj the `Deployable` object to be built
    */
  def DeployableBuildActivity(obj: Deployable, tool: ConstructionItem): Unit = {
    val zone       = obj.Zone
    val guid       = obj.GUID
    val owner      = obj.Owner.getOrElse(Service.defaultPlayerGUID)
    val ownerName  = obj.OwnerName.getOrElse("")
    val channel    = ownerName
    val definition = obj.Definition
    val item       = definition.Item
    zone.AvatarEvents ! AvatarServiceMessage(zone.id, AvatarAction.DeployItem(Service.defaultPlayerGUID, obj))
    //map icon
    val deployInfo = DeployableInfo(guid, Deployable.Icon(item), obj.Position, obj.Owner.getOrElse(PlanetSideGUID(0)))
    sendResponse(zone, channel, DeployableObjectsInfoMessage(DeploymentAction.Build, deployInfo))
    zone.LocalEvents ! LocalServiceMessage(
      channel,
      LocalAction.DeployableMapIcon(owner, DeploymentAction.Build, deployInfo)
    )
  }

  private def sendResponse(zone: Zone, channel: String, msg: PlanetSideGamePacket): Unit = {
    zone.AvatarEvents ! AvatarServiceMessage(channel, AvatarAction.SendResponse(Service.defaultPlayerGUID, msg))
  }
}
