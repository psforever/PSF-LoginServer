// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.Actor
import net.psforever.objects.ce.Deployable

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

/**
  * na
  * @param zone the `Zone` object
  */
class ZoneDeployableActor(zone: Zone, deployableList: ListBuffer[Deployable]) extends Actor {

  import ZoneDeployableActor._

  private[this] val log = org.log4s.getLogger

  def receive: Receive = {
    case msg @ Zone.Deployable.Build(obj, tool) =>
      if (DeployableBuild(obj, deployableList)) {
        obj.Zone = zone
        obj.Definition.Initialize(obj, context)
        zone.LivePlayers.find { p => obj.OwnerName.contains(p.Name) } match {
          case Some(p) => p.Actor ! msg //owner is trying to put it down
          case None => obj.Actor ! Zone.Deployable.Setup(tool) //strong and independent deployable
        }
      } else {
        log.warn(s"failed to build deployable $obj from $tool")
      }

    case Zone.Deployable.Dismiss(obj) =>
      if (DeployableDismiss(obj, deployableList)) {
        obj.Definition.Uninitialize(obj, context)
        sender() ! Zone.Deployable.DeployableIsDismissed(obj)
      }

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
