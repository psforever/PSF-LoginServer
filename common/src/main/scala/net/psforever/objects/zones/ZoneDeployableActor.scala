// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.Actor
import net.psforever.objects.ce.Deployable
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.PlanetSideGameObject

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

/**
  * na
  * @param zone the `Zone` object
  */
class ZoneDeployableActor(zone: Zone, deployableList: ListBuffer[PlanetSideGameObject with Deployable]) extends Actor {

  import ZoneDeployableActor._

  private[this] val log = org.log4s.getLogger

  def receive: Receive = {
    case Zone.Deployable.Build(obj, tool) =>
      if (DeployableBuild(obj, deployableList)) {
        obj match {
          case o: PlanetSideServerObject =>
            obj.Definition.Initialize(o, context)
          case _ =>
            obj.Definition.Initialize(obj, context)
        }
        obj.Zone = zone
        sender() ! Zone.Deployable.DeployableIsBuilt(obj, tool)
      } else {
        log.warn(s"failed to build deployable ${obj} ${tool}")
      }

    case Zone.Deployable.Dismiss(obj) =>
      if (DeployableDismiss(obj, deployableList)) {
        obj match {
          case o: PlanetSideServerObject =>
            obj.Definition.Uninitialize(o, context)
          case _ =>
            obj.Definition.Uninitialize(obj, context)
        }
        sender() ! Zone.Deployable.DeployableIsDismissed(obj)
      }

    case _ => ;
  }
}

object ZoneDeployableActor {
  def DeployableBuild(
      obj: PlanetSideGameObject with Deployable,
      deployableList: ListBuffer[PlanetSideGameObject with Deployable]
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
      obj: PlanetSideGameObject with Deployable,
      deployableList: ListBuffer[PlanetSideGameObject with Deployable]
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
      iter: Iterator[PlanetSideGameObject with Deployable],
      target: PlanetSideGameObject with Deployable,
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
