// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.{Actor, ActorRef, Props}
import net.psforever.objects.avatar.PlayerControl
import net.psforever.objects.vehicles.VehicleControl
import net.psforever.objects.{Avatar, Player}

import scala.annotation.tailrec
import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ListBuffer

/**
  * A support `Actor` that sequences adding and removing `Avatar` and `Player` objects to mappings and lists.
  * The former mapping is considered to represent every user connect to the `zone` (`as Avatar` objects)
  * and their current representation (as `Player` objects).
  * The latter list keeps track of a group of former user representations.
  * @param zone the `Zone` object
  * @param playerMap the mapping of `Avatar` objects to `Player` objects
  * @param corpseList a list of `Player` objects
  */
class ZonePopulationActor(zone : Zone, playerMap : TrieMap[Avatar, Option[Player]], corpseList : ListBuffer[Player]) extends Actor {
  import ZonePopulationActor._

  def receive : Receive = {
    case Zone.Population.Join(avatar) =>
      PopulationJoin(avatar, playerMap)

    case Zone.Population.Leave(avatar) =>
      PopulationLeave(avatar, playerMap) match {
        case None => ;
        case player @ Some(_) =>
          sender ! Zone.Population.PlayerHasLeft(zone, player)
      }

    case Zone.Population.Spawn(avatar, player) =>
      PopulationSpawn(avatar, player, playerMap) match {
        case Some(tplayer) =>
          tplayer.Zone = zone
          if(tplayer ne player) {
            sender ! Zone.Population.PlayerAlreadySpawned(zone, player)
          }
          else {
            player.Actor = context.actorOf(Props(classOf[PlayerControl], player), s"${player.Name}_${player.GUID.guid}")
          }
        case None =>
          sender ! Zone.Population.PlayerCanNotSpawn(zone, player)
      }

    case Zone.Population.Release(avatar) =>
      PopulationRelease(avatar, playerMap) match {
        case Some(tplayer) =>
          tplayer.Actor ! akka.actor.PoisonPill
          tplayer.Actor = ActorRef.noSender
        case None =>
          sender ! Zone.Population.PlayerHasLeft(zone, None)
      }

    case Zone.Corpse.Add(player) =>
      CorpseAdd(player, corpseList)

    case Zone.Corpse.Remove(player) =>
      CorpseRemove(player, corpseList)

    case _ => ;
  }
}

object ZonePopulationActor {
  /**
    * Add an `avatar` as the key of an `Avatar` to `Player` object pair in the given collection.
    * @param avatar an `Avatar` object
    * @param playerMap the mapping of `Avatar` objects to `Player` objects
    * @return true, if the mapping is for a new key;
    *         false, if the key already exists
    */
  def PopulationJoin(avatar : Avatar, playerMap : TrieMap[Avatar, Option[Player]]) : Boolean = {
    playerMap.get(avatar) match {
      case Some(_) =>
        false
      case None =>
        playerMap += avatar -> None
        true
    }
  }
  /**
    * Remove an `avatar` from the key of an `Avatar` to `Player` object pair in the given collection.
    * If a `Player` object is associated at the time, return it safely.
    * @param avatar an `Avatar` object
    * @param playerMap the mapping of `Avatar` objects to `Player` objects
    * @return any `Player` object that was associated at the time the `avatar` was removed
    */
  def PopulationLeave(avatar : Avatar, playerMap : TrieMap[Avatar, Option[Player]]) : Option[Player] = {
    playerMap.remove(avatar) match {
      case None =>
        None
      case Some(tplayer) =>
        tplayer
    }
  }

  /**
    * Associate a `Player` object as a value to an existing `Avatar` object that will be its key.
    * Do not overwrite players that are already associated.
    * @param avatar an `Avatar` object
    * @param player a `Player` object
    * @param playerMap the mapping of `Avatar` objects to `Player` objects
    * @return the `Player` object that is associated with the `Avatar` key
    */
  def PopulationSpawn(avatar : Avatar, player : Player, playerMap : TrieMap[Avatar, Option[Player]]) : Option[Player] = {
    playerMap.get(avatar) match {
      case None =>
        None
      case Some(tplayer) =>
        tplayer match {
          case Some(aplayer) =>
            Some(aplayer)
          case None =>
            playerMap(avatar) = Some(player)
            Some(player)
        }
    }
  }

  /**
    * Disassociate a `Player` object from an existing `Avatar` object that was be its key.
    * @param avatar an `Avatar` object
    * @param playerMap the mapping of `Avatar` objects to `Player` objects
    * @return any `Player` object that is associated at the time
    */
  def PopulationRelease(avatar : Avatar, playerMap : TrieMap[Avatar, Option[Player]]) : Option[Player] = {
    playerMap.get(avatar) match {
      case None =>
        None
      case Some(tplayer) =>
        playerMap(avatar) = None
        tplayer
    }
  }

  /**
    * If the given `player` passes a condition check, add it to the list.
    * @param player a `Player` object
    * @param corpseList a list of `Player` objects
    * @return true, if the `player` was added to the list;
    *         false, otherwise
    */
  def CorpseAdd(player : Player, corpseList : ListBuffer[Player]) : Boolean = {
    if(player.isBackpack) {
      corpseList += player
      true
    }
    else {
      false
    }
  }

  /**
    * Remove the given `player` from the list.
    * @param player a `Player` object
    * @param corpseList a list of `Player` objects
    */
  def CorpseRemove(player : Player, corpseList : ListBuffer[Player]) : Unit = {
    recursiveFindCorpse(corpseList.iterator, player) match {
      case None => ;
      case Some(index) =>
        corpseList.remove(index)
    }
  }

  /**
    * A recursive function that finds and removes a specific player from a list of players.
    * @param iter an `Iterator` of `Player` objects
    * @param player the target `Player`
    * @param index the index of the discovered `Player` object
    * @return the index of the `Player` object in the list to be removed;
    *         `None`, otherwise
    */
  @tailrec final def recursiveFindCorpse(iter : Iterator[Player], player : Player, index : Int = 0) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      if(iter.next == player) {
        Some(index)
      }
      else {
        recursiveFindCorpse(iter, player, index + 1)
      }
    }
  }
}
