// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.Actor
import net.psforever.objects.Player
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.ce.Deployable
import net.psforever.objects.vehicles.MountedWeapons

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
      if (DeployableBuild(obj, deployableList)) {
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
          case _ => ;
        }
        obj.Definition.Initialize(obj, context)
        zone.actor ! ZoneActor.AddToBlockMap(obj, obj.Position)
        //obj.History(EntitySpawn(SourceEntry(obj), obj.Zone))
        obj.Actor ! Zone.Deployable.Setup()
      } else {
        log.warn(s"failed to build a ${obj.Definition.Name}")
        sender() ! Zone.Deployable.IsDismissed(obj)
      }

    case Zone.Deployable.BuildByOwner(obj, owner, tool) =>
      if (DeployableBuild(obj, deployableList)) {
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
          case _ => ;
        }
        obj.Definition.Initialize(obj, context)
        zone.actor ! ZoneActor.AddToBlockMap(obj, obj.Position)
        //obj.History(EntitySpawn(SourceEntry(obj), obj.Zone))
        owner.Actor ! Player.BuildDeployable(obj, tool)
      } else {
        log.warn(s"failed to build a ${obj.Definition.Name} belonging to ${obj.OwnerName.getOrElse("no one")}")
        sender() ! Zone.Deployable.IsDismissed(obj)
      }

    case Zone.Deployable.Dismiss(obj) =>
      if (DeployableDismiss(obj, deployableList)) {
        obj match {
          case _: MountedWeapons =>
            val dguid = obj.GUID.guid
            turretToMount.filterInPlace { case (_, guid) => guid != dguid }
          case _ => ;
        }
        obj.Actor ! Zone.Deployable.IsDismissed(obj)
        obj.Definition.Uninitialize(obj, context)
        obj.ClearHistory()
        zone.actor ! ZoneActor.RemoveFromBlockMap(obj)
      }

    case Zone.Deployable.IsBuilt(_) => ;

    case Zone.Deployable.IsDismissed(_) => ;

    case _ => ;
  }
}

object ZoneDeployableActor {
  def DeployableBuild(
      obj: Deployable,
      deployableList: ListBuffer[Deployable]
  ): Boolean = {
    deployableList.find(d => d == obj) match {
      case Some(_) =>
        false
      case None =>
        deployableList += obj
        true
    }
  }

  def DeployableDismiss(
      obj: Deployable,
      deployableList: ListBuffer[Deployable]
  ): Boolean = {
    recursiveFindDeployable(deployableList.iterator, obj) match {
      case None =>
        false
      case Some(index) =>
        deployableList.remove(index)
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
