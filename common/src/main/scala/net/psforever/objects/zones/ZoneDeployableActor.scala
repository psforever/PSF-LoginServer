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
class ZoneDeployableActor(zone : Zone, deployableList : ListBuffer[PlanetSideGameObject with Deployable]) extends Actor {
  import ZoneDeployableActor._

  def receive : Receive = {
    case Zone.Deployable.Build(obj, tool) =>
      if(DeployableBuild(obj, deployableList)) {
        obj match {
          case o : PlanetSideServerObject =>
            obj.Definition.Initialize(o, context)
          case _ =>
            obj.Definition.Initialize(obj, context)
        }
        obj.Zone = zone
        sender ! Zone.Deployable.DeployableIsBuilt(obj, tool)
      }

    case Zone.Deployable.Dismiss(obj) =>
      if(DeployableDismiss(obj, deployableList)) {
        obj match {
          case o : PlanetSideServerObject =>
            obj.Definition.Uninitialize(o, context)
          case _ =>
            obj.Definition.Uninitialize(obj, context)
        }
        sender ! Zone.Deployable.DeployableIsDismissed(obj)
      }

    case _ => ;
  }
}

object ZoneDeployableActor {
  def DeployableBuild(obj : PlanetSideGameObject with Deployable, deployableList : ListBuffer[PlanetSideGameObject with Deployable]) : Boolean = {
    deployableList.find(d => d == obj) match {
      case Some(_) =>
        false
      case None =>
        deployableList += obj
        true
    }
  }


  def DeployableDismiss(obj : PlanetSideGameObject with Deployable, deployableList : ListBuffer[PlanetSideGameObject with Deployable]) : Boolean = {
    recursiveFindDeployable(deployableList.iterator, obj) match {
      case None =>
        false
      case Some(index) =>
        deployableList.remove(index)
        true
    }
  }

  @tailrec final def recursiveFindDeployable(iter : Iterator[PlanetSideGameObject with Deployable], target : PlanetSideGameObject with Deployable, index : Int = 0) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      if(iter.next == target) {
        Some(index)
      }
      else {
        recursiveFindDeployable(iter, target, index + 1)
      }
    }
  }

//  /**
//    * Add an `avatar` as the key of an `Avatar` to `Player` object pair in the given collection.
//    * @param avatar an `Avatar` object
//    * @param playerMap the mapping of `Avatar` objects to `Player` objects
//    * @return true, if the mapping is for a new key;
//    *         false, if the key already exists
//    */
//  def PopulationJoin(avatar : Avatar, playerMap : TrieMap[Avatar, Option[Player]]) : Boolean = {
//    playerMap.get(avatar) match {
//      case Some(_) =>
//        false
//      case None =>
//        playerMap += avatar -> None
//        true
//    }
//  }
//  /**
//    * Remove an `avatar` from the key of an `Avatar` to `Player` object pair in the given collection.
//    * If a `Player` object is associated at the time, return it safely.
//    * @param avatar an `Avatar` object
//    * @param playerMap the mapping of `Avatar` objects to `Player` objects
//    * @return any `Player` object that was associated at the time the `avatar` was removed
//    */
//  def PopulationLeave(avatar : Avatar, playerMap : TrieMap[Avatar, Option[Player]]) : Option[Player] = {
//    playerMap.remove(avatar) match {
//      case None =>
//        None
//      case Some(tplayer) =>
//        tplayer
//    }
//  }
//
//  /**
//    * Associate a `Player` object as a value to an existing `Avatar` object that will be its key.
//    * Do not overwrite players that are already associated.
//    * @param avatar an `Avatar` object
//    * @param player a `Player` object
//    * @param playerMap the mapping of `Avatar` objects to `Player` objects
//    * @return the `Player` object that is associated with the `Avatar` key
//    */
//  def PopulationSpawn(avatar : Avatar, player : Player, playerMap : TrieMap[Avatar, Option[Player]]) : Option[Player] = {
//    playerMap.get(avatar) match {
//      case None =>
//        None
//      case Some(tplayer) =>
//        tplayer match {
//          case Some(aplayer) =>
//            Some(aplayer)
//          case None =>
//            playerMap(avatar) = Some(player)
//            Some(player)
//        }
//    }
//  }
//
//  /**
//    * Disassociate a `Player` object from an existing `Avatar` object that was be its key.
//    * @param avatar an `Avatar` object
//    * @param playerMap the mapping of `Avatar` objects to `Player` objects
//    * @return any `Player` object that is associated at the time
//    */
//  def PopulationRelease(avatar : Avatar, playerMap : TrieMap[Avatar, Option[Player]]) : Option[Player] = {
//    playerMap.get(avatar) match {
//      case None =>
//        None
//      case Some(tplayer) =>
//        playerMap(avatar) = None
//        tplayer
//    }
//  }
//
//  /**
//    * If the given `player` passes a condition check, add it to the list.
//    * @param player a `Player` object
//    * @param corpseList a list of `Player` objects
//    * @return true, if the `player` was added to the list;
//    *         false, otherwise
//    */
//  def CorpseAdd(player : Player, corpseList : ListBuffer[Player]) : Boolean = {
//    if(player.isBackpack) {
//      corpseList += player
//      true
//    }
//    else {
//      false
//    }
//  }
//
//  /**
//    * Remove the given `player` from the list.
//    * @param player a `Player` object
//    * @param corpseList a list of `Player` objects
//    */
//  def CorpseRemove(player : Player, corpseList : ListBuffer[Player]) : Unit = {
//    recursiveFindCorpse(corpseList.iterator, player) match {
//      case None => ;
//      case Some(index) =>
//        corpseList.remove(index)
//    }
//  }
//
//  /**
//    * A recursive function that finds and removes a specific player from a list of players.
//    * @param iter an `Iterator` of `Player` objects
//    * @param player the target `Player`
//    * @param index the index of the discovered `Player` object
//    * @return the index of the `Player` object in the list to be removed;
//    *         `None`, otherwise
//    */
//  @tailrec final def recursiveFindCorpse(iter : Iterator[Player], player : Player, index : Int = 0) : Option[Int] = {
//    if(!iter.hasNext) {
//      None
//    }
//    else {
//      if(iter.next == player) {
//        Some(index)
//      }
//      else {
//        recursiveFindCorpse(iter, player, index + 1)
//      }
//    }
//  }
}
