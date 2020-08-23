// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.{Actor, ActorRef, Props}
import net.psforever.objects.avatar.{CorpseControl, PlayerControl}
import net.psforever.objects.{Default, Player}
import scala.collection.concurrent.TrieMap
import scala.collection.mutable.ListBuffer

/**
  * A support `Actor` that sequences adding and removing `Avatar` and `Player` objects to mappings and lists.
  * The former mapping is considered to represent every user connect to the `zone` (`as Avatar` objects)
  * and their current representation (as `Player` objects).
  * The latter list keeps track of a group of former user representations.
  *
  * @param zone       the `Zone` object
  * @param playerMap  the mapping of `Avatar` objects to `Player` objects
  * @param corpseList a list of `Player` objects
  */
class ZonePopulationActor(zone: Zone, playerMap: TrieMap[Int, Option[Player]], corpseList: ListBuffer[Player])
    extends Actor {

  import ZonePopulationActor._

  def receive: Receive = {
    case Zone.Population.Join(avatar) =>
      if (PopulationJoin(avatar.id, playerMap) && playerMap.size == 1) {
        zone.StartPlayerManagementSystems()
      }

    case Zone.Population.Leave(avatar) =>
      PopulationLeave(avatar.id, playerMap) match {
        case None => ;
        case player @ Some(tplayer) =>
          tplayer.Zone = Zone.Nowhere
          PlayerLeave(tplayer)
          sender() ! Zone.Population.PlayerHasLeft(zone, player)
          if (playerMap.isEmpty) {
            zone.StopPlayerManagementSystems()
          }
      }

    case Zone.Population.Spawn(avatar, player, avatarActor) =>
      PopulationSpawn(avatar.id, player, playerMap) match {
        case Some((tplayer, newToZone)) =>
          tplayer.Zone = zone
          if (tplayer ne player) {
            sender() ! Zone.Population.PlayerAlreadySpawned(zone, player)
          } else if (newToZone) {
            player.Actor = context.actorOf(
              Props(classOf[PlayerControl], player, avatarActor),
              name = GetPlayerControlName(player, None)
            )
            player.Zone = zone
          }
        case None =>
          sender() ! Zone.Population.PlayerCanNotSpawn(zone, player)
      }

    case Zone.Population.Release(avatar) =>
      PopulationRelease(avatar.id, playerMap) match {
        case Some(tplayer) =>
          PlayerLeave(tplayer)
          sender() ! Zone.Population.PlayerHasLeft(zone, Some(tplayer))
        case None =>
          sender() ! Zone.Population.PlayerHasLeft(zone, None)
      }

    case Zone.Corpse.Add(player) =>
      //player can be a corpse if they are in the current zone or are not in any zone
      //player is "found" if their avatar can be matched by name within this zone and it has a character
      val (canBeCorpse, control) = playerMap.find { case (id, _) => id == player.avatar.id } match {
        case Some((id, Some(p))) if p eq player =>
          PopulationRelease(id, playerMap)
          context.stop(player.Actor)
          (true, Some(player.Actor))
        case Some((_, None)) =>
          (true, None)
        case _ =>
          (player.Zone == Zone.Nowhere || player.Zone == zone, None)
      }
      if (canBeCorpse && CorpseAdd(player, corpseList)) {
        player.Actor = context.actorOf(
          Props(classOf[CorpseControl], player),
          name = s"corpse_of_${GetPlayerControlName(player, control)}"
        )
        player.Zone = zone
      }

    case Zone.Corpse.Remove(player) =>
      if (CorpseRemove(player, corpseList)) {
        PlayerLeave(player)
      }

    case _ => ;
  }
}

object ZonePopulationActor {

  /**
    * Add an `avatar` as the key of an `Avatar` to `Player` object pair in the given collection.
    *
    * @param id        an `Avatar` id
    * @param playerMap the mapping of `Avatar` objects to `Player` objects
    * @return true, if the mapping is for a new key;
    *         false, if the key already exists
    */
  def PopulationJoin(id: Int, playerMap: TrieMap[Int, Option[Player]]): Boolean = {
    playerMap.get(id) match {
      case Some(_) =>
        false
      case None =>
        playerMap += id -> None
        true
    }
  }

  /**
    * Remove an `avatar` from the key of an `Avatar` to `Player` object pair in the given collection.
    * If a `Player` object is associated at the time, return it safely.
    *
    * @param id        an `Avatar` id
    * @param playerMap the mapping of `Avatar` objects to `Player` objects
    * @return any `Player` object that was associated at the time the `avatar` was removed
    */
  def PopulationLeave(id: Int, playerMap: TrieMap[Int, Option[Player]]): Option[Player] = {
    playerMap.get(id) match {
      case None =>
        None
      case Some(player) =>
        playerMap.remove(id)
        player
    }
  }

  /**
    * Associate a `Player` object as a value to an existing `Avatar` object that will be its key.
    * Do not overwrite players that are already associated.
    *
    * @param id        an `Avatar` object
    * @param player    a `Player` object
    * @param playerMap the mapping of `Avatar` objects to `Player` objects
    * @return a `Tuple` object of the `Player` object that is associated with the `Avatar` key
    *         and whether that player was added to the zone for the first time;
    *         `None`, if the player should not be introduced to this zone at this time
    */
  def PopulationSpawn(
      id: Int,
      player: Player,
      playerMap: TrieMap[Int, Option[Player]]
  ): Option[(Player, Boolean)] = {
    playerMap.get(id) match {
      case None =>
        None
      case Some(tplayer) =>
        tplayer match {
          case Some(aplayer) =>
            Some(aplayer, false)
          case None =>
            playerMap(id) = Some(player)
            Some(player, true)
        }
    }
  }

  /**
    * Disassociate a `Player` object from an existing `Avatar` object that was be its key.
    *
    * @param id        an `Avatar` id
    * @param playerMap the mapping of `Avatar` objects to `Player` objects
    * @return any `Player` object that is associated at the time
    */
  def PopulationRelease(id: Int, playerMap: TrieMap[Int, Option[Player]]): Option[Player] = {
    playerMap.get(id) match {
      case None =>
        None
      case Some(value) =>
        playerMap(id) = None
        value
    }
  }

  /**
    * If the given `player` passes a condition check, add it to the list.
    * @param player a `Player` object
    * @param corpseList a list of `Player` objects
    * @return true, if the `player` was added to the list;
    *         false, otherwise
    */
  def CorpseAdd(player: Player, corpseList: ListBuffer[Player]): Boolean = {
    if (player.isBackpack) {
      corpseList += player
      true
    } else {
      false
    }
  }

  /**
    * Remove the given `player` from the list.
    * @param player a `Player` object
    * @param corpseList a list of `Player` objects
    */
  def CorpseRemove(player: Player, corpseList: ListBuffer[Player]): Boolean = {
    corpseList.indexOf(player) match {
      case -1 =>
        false
      case index =>
        corpseList.remove(index)
        true
    }
  }

  def PlayerLeave(player: Player): Unit = {
    if (player.Actor != null) player.Actor ! akka.actor.PoisonPill
    player.Actor = Default.Actor
  }

  def GetPlayerControlName(player: Player, old: Option[ActorRef]): String = {
    old match {
      case Some(control) =>
        val nameNumber = control.toString.split("/").last //split on '/'
        nameNumber.split("#").head //split on '#'
      case None => ;
        s"${player.CharId}_${player.GUID.guid}_${System.currentTimeMillis}" //new
    }
  }
}
