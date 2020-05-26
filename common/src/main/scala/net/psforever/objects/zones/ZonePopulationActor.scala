// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.{Actor, ActorRef, Props}
import net.psforever.objects.avatar.PlayerControl
import net.psforever.objects.{Avatar, Default, Player}

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
      if(PopulationJoin(avatar, playerMap) && playerMap.size == 1) {
        zone.StartPlayerManagementSystems()
      }

    case Zone.Population.Leave(avatar) =>
      PopulationLeave(avatar, playerMap) match {
        case None => ;
        case player @ Some(tplayer) =>
          tplayer.Zone = Zone.Nowhere
          PlayerLeave(tplayer)
          sender ! Zone.Population.PlayerHasLeft(zone, player)
          if(playerMap.isEmpty) {
            zone.StopPlayerManagementSystems()
          }
      }

    case Zone.Population.Logout(avatar) =>
      PopulationLeave(avatar, playerMap) match {
        case None => ;
        case Some(tplayer) =>
          tplayer.Zone = Zone.Nowhere
          context.stop(tplayer.Actor)
          tplayer.Actor = Default.Actor
          if(playerMap.isEmpty) {
            zone.StopPlayerManagementSystems()
          }
      }

    case Zone.Population.Spawn(avatar, player) =>
      PopulationSpawn(avatar, player, playerMap) match {
        case Some((tplayer, newToZone)) =>
          tplayer.Zone = zone
          if(tplayer ne player) {
            sender ! Zone.Population.PlayerAlreadySpawned(zone, player)
          }
          else if(newToZone) {
            val control = context.actorOf(
              Props(classOf[PlayerControl], player),
              ZonePopulationActor.GetPlayerControlName(player, None)
            )
            player.Actor = control
            sender ! Zone.Population.PlayerHasSpawned(zone, player)
          }
          else {
            sender ! Zone.Population.PlayerHasSpawned(zone, player)
          }
        case None =>
          sender ! Zone.Population.PlayerCanNotSpawn(zone, player)
      }

    case Zone.Population.Release(avatar) =>
      PopulationRelease(avatar, playerMap) match {
        case Some(tplayer) =>
          PlayerLeave(tplayer)
        case None =>
          sender ! Zone.Population.PlayerHasLeft(zone, None)
      }

    case Zone.Corpse.Add(player) =>
      val (playerIsCorpse, playerInZone) = CorpseAdd(player, playerMap, corpseList)
      if(playerIsCorpse) {
        if(!playerInZone) {
          val name = ZonePopulationActor.GetPlayerControlName(player, None)
          player.Actor = context.actorOf(Props(classOf[PlayerControl], player), name = s"corpse_of_$name")
          player.Zone = zone
        }
      }

    case Zone.Corpse.Remove(player) =>
      if(CorpseRemove(player, corpseList)) {
        context.stop(player.Actor)
        player.Actor = Default.Actor
      }

    case _ => ;
  }

  def PlayerLeave(player : Player) : Unit = {
    player.Actor ! akka.actor.PoisonPill
    player.Actor = Default.Actor
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
    * @return a `Tuple` object of the `Player` object that is associated with the `Avatar` key
    *         and whether that player was added to the zone for the first time;
    *         `None`, if the player should not be introduced to this zone at this time
    */
  def PopulationSpawn(avatar : Avatar, player : Player, playerMap : TrieMap[Avatar, Option[Player]]) : Option[(Player, Boolean)] = {
    playerMap.get(avatar) match {
      case None =>
        None
      case Some(tplayer) =>
        tplayer match {
          case Some(aplayer) =>
            Some(aplayer, false)
          case None =>
            playerMap(avatar) = Some(player)
            Some(player, true)
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
    * Also, ensure that "this player" is not currently counted among the living.
    * @param player a `Player` object
    * @param playerMap the mapping of `Avatar` objects to `Player` objects
    * @param corpseList a list of `Player` objects
    * @return a `Tuple` of two flags;
    *         the first is whether the player was turned into a corpse or not;
    *         the second is whether the player was found in the zone before being turned into a corpse
    */
  def CorpseAdd(player : Player, playerMap : TrieMap[Avatar, Option[Player]], corpseList : ListBuffer[Player]) : (Boolean, Boolean) = {
    if(player.isBackpack) {
      val playerFoundInZone = playerMap.find {
        case (_, Some(p)) => p.CharId == player.CharId
        case (_, None) => false
      } match {
        case Some((a, _)) => PopulationRelease(a, playerMap).nonEmpty
        case _ => false
      }
      corpseList.find { _ eq player } match {
        case None => ;
          corpseList += player
          (true, playerFoundInZone)
        case _ =>
          (false, false)
      }
    }
    else {
      (false, false)
    }
  }

  /**
    * Remove the given `player` from the list.
    * @param player a `Player` object
    * @param corpseList a list of `Player` objects
    * @return `true`, if the corpse was found and removed;
    *        `false`, otherwise
    */
  def CorpseRemove(player : Player, corpseList : ListBuffer[Player]) : Boolean = {
    recursiveFindCorpse(corpseList.iterator, player) match {
      case None =>
        false
      case Some(index) =>
        corpseList.remove(index)
        true
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

  def GetPlayerControlName(player : Player, old : Option[ActorRef]) : String = {
    old match {
      case Some(control) =>
        val nameNumber = control.toString.split("/").last //split on '/'
        nameNumber.split("#").head //split on '#'
      case None => ;
        s"${player.CharId}_${player.GUID.guid}_${System.currentTimeMillis}" //new
    }
  }
}