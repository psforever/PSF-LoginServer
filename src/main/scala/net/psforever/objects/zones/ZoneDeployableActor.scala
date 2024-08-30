// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.Actor
import net.psforever.objects.Player
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.ce.Deployable
import net.psforever.objects.serverobject.deploy.Interference
import net.psforever.objects.sourcing.ObjectSource
import net.psforever.objects.vehicles.MountedWeapons
import net.psforever.objects.vital.SpawningActivity
import net.psforever.packet.game.ChatMsg
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.ChatMessageType

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * na
  * @param zone the `Zone` object
  */
class ZoneDeployableActor(
                           zone: Zone,
                           deployableList: ListBuffer[Deployable],
                           turretToMount: mutable.HashMap[Int, Int]
                         ) extends Actor {
  import ZoneDeployableActor._

  private[this] val log = org.log4s.getLogger

  def receive: Receive = {
    case Zone.Deployable.Build(obj) =>
      if (DeployableBuild(zone, obj, deployableList)) {
        obj.Zone = zone
        obj match {
          case mounting: MountedWeapons =>
            val dguid = obj.GUID.guid
            mounting
              .Weapons
              .values
              .flatten { _.Equipment.map { _.GUID.guid } }
              .foreach { guid =>
                turretToMount.put(guid, dguid)
              }
          case _ => ()
        }
        obj.Definition.Initialize(obj, context)
        obj.LogActivity(SpawningActivity(ObjectSource(obj), zone.Number, None))
        obj.Actor ! Zone.Deployable.Setup()
      } else {
        log.warn(s"failed to build a ${obj.Definition.Name}")
        sender() ! Zone.Deployable.IsDismissed(obj)
      }

    case Zone.Deployable.BuildByOwner(obj, owner, tool) =>
      if (DeployableBuild(zone, obj, deployableList)) {
        obj.Zone = zone
        obj match {
          case mounting: MountedWeapons =>
            val dguid = obj.GUID.guid
            mounting
              .Weapons
              .values
              .flatten { _.Equipment.map { _.GUID.guid } }
              .foreach { guid =>
                turretToMount.put(guid, dguid)
              }
          case _ => ()
        }
        obj.Definition.Initialize(obj, context)
        obj.LogActivity(SpawningActivity(ObjectSource(obj), zone.Number, None))
        owner.Actor ! Player.BuildDeployable(obj, tool)
      } else {
        log.warn(s"failed to build a ${obj.Definition.Name} belonging to ${obj.OwnerName.getOrElse("no one")}")
        sender() ! Zone.Deployable.IsDismissed(obj)
      }

    case Zone.Deployable.Dismiss(obj) =>
      if (DeployableDismiss(zone, obj, deployableList)) {
        obj match {
          case _: MountedWeapons =>
            val dguid = obj.GUID.guid
            turretToMount.filterInPlace { case (_, guid) => guid != dguid }
          case _ => ()
        }
        obj.Actor ! Zone.Deployable.IsDismissed(obj)
        obj.Definition.Uninitialize(obj, context)
        obj.ClearHistory()
      }

    case Zone.Deployable.IsBuilt(_) => ()

    case Zone.Deployable.IsDismissed(_) => ()

    case _ => ()
  }
}

object ZoneDeployableActor {
  def DeployableBuild(
                       zone: Zone,
                       obj: Deployable,
                       deployableList: ListBuffer[Deployable]
                     ): Boolean = {
    val position = obj.Position
    deployableList.find(_ eq obj) match {
      case _ if Interference.Test(zone, obj).nonEmpty =>
        zone.LocalEvents ! LocalServiceMessage(
          obj.OwnerName.getOrElse(""),
          LocalAction.SendResponse(ChatMsg(ChatMessageType.UNK_227, "@nomove_intersecting"))
        ) //may not be the correct message but is sufficient at explaining why the deployable can not be built
        false
      case None =>
        deployableList += obj
        zone.actor ! ZoneActor.AddToBlockMap(obj, position)
        true
      case _ =>
        false
    }
  }

  def DeployableDismiss(
                         zone: Zone,
                         obj: Deployable,
                         deployableList: ListBuffer[Deployable]
                       ): Boolean = {
    recursiveFindDeployable(deployableList.iterator, obj) match {
      case None =>
        false
      case Some(index) =>
        deployableList.remove(index)
        zone.actor ! ZoneActor.RemoveFromBlockMap(obj)
        true
    }
  }

  @tailrec final def recursiveFindDeployable(
      iter: Iterator[Deployable],
      target: Deployable,
      index: Int = 0
  ): Option[Int] = {
    if (!iter.hasNext) {
      None
    } else {
      if (iter.next() == target) {
        Some(index)
      } else {
        recursiveFindDeployable(iter, target, index + 1)
      }
    }
  }
}
