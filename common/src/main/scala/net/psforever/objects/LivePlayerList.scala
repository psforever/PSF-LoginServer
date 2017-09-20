// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.packet.game.PlanetSideGUID

import scala.annotation.tailrec
import scala.collection.concurrent.{Map, TrieMap}

/**
  * See the companion object for class and method documentation.
  * `LivePlayerList` is a singleton and this private class lacks exposure.
  */
private class LivePlayerList {
  /** key - the session id; value - a `Player` object */
  private val sessionMap : Map[Long, Player] = new TrieMap[Long, Player]
  /** the index of the List corresponds to zone number 1-32 with 0 being "Nowhere" */
  /** each mapping: key - the global unique identifier; value - the session id */
  private val zoneMap : List[Map[Int, Long]] = List.fill(33)(new TrieMap[Int,Long])

  def WorldPopulation(predicate : ((_, Player)) => Boolean) : List[Player] = {
    sessionMap.filter(predicate).values.toList
  }

  def ZonePopulation(zone : Int, predicate : ((_, Player)) => Boolean) : List[Player] = {
    zoneMap.lift(zone) match {
      case Some(map) =>
        val list = map.values.toList
        sessionMap.filter({ case ((sess, _)) => list.contains(sess) }).filter(predicate).values.toList
      case None =>
        Nil
    }
  }

  def Add(sessionId : Long, player : Player) : Boolean = {
    sessionMap.values.find(char => char.equals(player)) match {
      case None =>
        sessionMap.putIfAbsent(sessionId, player).isEmpty
      case Some(_) =>
        false
    }
  }

  def Remove(sessionId : Long) : Option[Player] = {
    sessionMap.remove(sessionId) match {
      case Some(char) =>
        zoneMap.foreach(zone => {
          recursiveRemoveSession(zone.iterator, sessionId) match {
            case Some(guid) =>
              zone.remove(guid)
            case None => ;
          }
        })
        Some(char)
      case None =>
        None
    }
  }

  @tailrec private def recursiveRemoveSession(iter : Iterator[(Int, Long)], sessionId : Long) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      val (guid : Int, sess : Long) = iter.next
      if(sess == sessionId) {
        Some(guid)
      }
      else {
        recursiveRemoveSession(iter, sessionId)
      }
    }
  }

  def Get(zone : Int, guid : PlanetSideGUID) : Option[Player] = {
    Get(zone, guid.guid)
  }

  def Get(zone : Int, guid : Int) : Option[Player] = {
    zoneMap.lift(zone) match {
      case Some(map) =>
        map.get(guid) match {
          case Some(sessionId) =>
            sessionMap.get(sessionId)
          case _ =>
            None
        }
      case None =>
        None
    }
  }

  def Assign(zone: Int, sessionId : Long, guid : PlanetSideGUID) : Boolean = Assign(zone, sessionId, guid.guid)

  def Assign(zone : Int, sessionId : Long, guid : Int) : Boolean = {
    sessionMap.get(sessionId) match {
      case Some(_) =>
        zoneMap.lift(zone) match {
          case Some(zn) =>
            AssignToZone(zn, sessionId, guid)
          case None =>
            false
        }

      case None =>
        false
    }
  }

  private def AssignToZone(zone : Map[Int, Long], sessionId : Long, guid : Int) : Boolean = {
    zone.get(guid) match {
      case Some(_) =>
        false
      case None =>
        zone(guid) = sessionId
        true
    }
  }

  def Drop(zone : Int, guid : PlanetSideGUID) : Option[Player] = Drop(zone, guid.guid)

  def Drop(zone : Int, guid : Int) : Option[Player] = {
    zoneMap.lift(zone) match {
      case Some(map) =>
        map.remove(guid) match {
          case Some(sessionId) =>
            sessionMap.get(sessionId)
          case None =>
            None
        }
      case None =>
        None
    }
  }

  def Shutdown : List[Player] = {
    val list = sessionMap.values.toList
    sessionMap.clear
    zoneMap.foreach(map => map.clear())
    list
  }
}

/**
  * A class for storing `Player` mappings for users that are currently online.
  * The mapping system is tightly coupled between the `Player` class and to an instance of `WorldSessionActor`.
  * Looser couplings exist between the instance of `WorldSessionActor` and a given `Player`'s globally unique id.
  * These looser couplings are zone-specific.
  * Though the user may have local knowledge of the zone they inhabit on their `Player` object,
  * it should not be trusted.<br>
  * <br>
  * Use:<br>
  * 1) When a users logs in during `WorldSessionActor`, associate that user's session id and the character.<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`LivePlayerList.Add(session, player)`<br>
  * 2) When that user's chosen character is declared his avatar using `SetCurrentAvatarMessage`,
  * also associate the user's session with their current GUID.<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`LivePlayerList.Assign(zone, session, guid)`<br>
  * 3) Repeat the previous step for as many times the user's GUID changes, especially during the aforementioned condition.<br>
  * 4a) In between the previous two steps, a user's character may be referenced by their current GUID.<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`LivePlayerList.Get(zone, guid)`<br>
  * 4b) Also in between those same previous steps, a range of characters may be queried based on provided statistics.<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`LivePlayerList.WorldPopulation(...)`<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`LivePlayerList.ZonePopulation(zone, ...)`<br>
  * 5) When the user navigates away from a region completely, their entry is forgotten.<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`LivePlayerList.Drop(zone, guid)`<br>
  * 6) When the user leaves the game entirely, his character's entries are removed from the mappings.<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`LivePlayerList.Remove(session)`
  */
object LivePlayerList {
  /** As `LivePlayerList` is a singleton, an object of `LivePlayerList` is automatically instantiated. */
  private val Instance : LivePlayerList = new LivePlayerList

  /**
    * Given some criteria, examine the mapping of user characters and find the ones that fulfill the requirements.<br>
    * <br>
    * Note the signature carefully.
    * A two-element tuple is checked, but only the second element of that tuple - a `Player` - is eligible for being queried.
    * The first element is ignored.
    * Even a predicate as simple as `{ case ((x : Long, _)) => x > 0 }` will not work for that reason.
    * @param predicate the conditions for filtering the live `Player`s
    * @return a list of users's `Player`s that fit the criteria
    */
  def WorldPopulation(predicate : ((_, Player)) => Boolean) : List[Player] = Instance.WorldPopulation(predicate)

  /**
    * Given some criteria, examine the mapping of user characters for a zone and find the ones that fulfill the requirements.<br>
    * <br>
    * Note the signature carefully.
    * A two-element tuple is checked, but only the second element of that tuple - a `Player` - is eligible for being queried.
    * The first element is ignored.
    * Even a predicate as simple as `{ case ((x : Long, _)) => x > 0 }` will not work for that reason.
    * @param zone the number of the zone
    * @param predicate the conditions for filtering the live `Player`s
    * @return a list of users's `Player`s that fit the criteria
    */
  def ZonePopulation(zone : Int, predicate : ((_, Player)) => Boolean) : List[Player] = Instance.ZonePopulation(zone, predicate)

  /**
    * Create a mapped entry between the user's session and a user's character.
    * Neither the player nor the session may exist in the current mappings if this is to work.
    * @param sessionId the session
    * @param player the character
    * @return `true`, if the session was association was made; `false`, otherwise
    */
  def Add(sessionId : Long, player : Player) : Boolean = Instance.Add(sessionId, player)

  /**
    * Remove all entries related to the given session identifier from the mappings.
    * The player no longer counts as "online."
    * This function cleans up __all__ associations - those created by `Add`, and those created by `Assign`.
    * @param sessionId the session
    * @return any character that was afffected by the mapping removal
    */
  def Remove(sessionId : Long) : Option[Player] = Instance.Remove(sessionId)

  /**
    * Get a user's character from the mappings.
    * @param zone the number of the zone
    * @param guid the current GUID of the character
    * @return the character, if it can be found using the GUID
    */
  def Get(zone : Int, guid : PlanetSideGUID) : Option[Player] = Instance.Get(zone, guid)

  /**
    * Get a user's character from the mappings.
    * @param zone the number of the zone
    * @param guid the current GUID of the character
    * @return the character, if it can be found using the GUID
    */
  def Get(zone : Int, guid : Int) : Option[Player] = Instance.Get(zone, guid)

  /**
    * Given a session that maps to a user's character, create a mapping between the character's current GUID and the session.
    * If the user already has a GUID in the mappings, remove it and assert the new one.
    * @param zone the number of the zone
    * @param sessionId the session
    * @param guid the GUID to associate with the character;
    *             technically, it has already been assigned and should be findable using `{character}.GUID.guid`
    * @return `true`, if the mapping was created;
    *        `false`, if the session can not be found or if the character's GUID doesn't match the one provided
    */
  def Assign(zone : Int, sessionId : Long, guid : PlanetSideGUID) : Boolean = Instance.Assign(zone, sessionId, guid)

  /**
    * Given a session that maps to a user's character, create a mapping between the character's current GUID and the session.
    * If the user already has a GUID in the mappings, remove it and assert the new one.
    * @param zone the number of the zone
    * @param sessionId the session
    * @param guid the GUID to associate with the character;
    *             technically, it has already been assigned and should be findable using `{character}.GUID.guid`
    * @return `true`, if the mapping was created;
    *        `false`, if the session can not be found or if the character's GUID doesn't match the one provided
    */
  def Assign(zone : Int, sessionId : Long, guid : Int) : Boolean = Instance.Assign(zone, sessionId, guid)

  /**
    * Given a GUID, remove any record of it.
    * @param zone the number of the zone
    * @param guid a GUID associated with the character;
    *             it does not have to be findable using `{character}.GUID.guid`
    * @return any `Player` that may have been associated with this GUID
    */
  def Drop(zone : Int, guid : PlanetSideGUID) : Option[Player] = Instance.Drop(zone, guid)

  /**
    * Given a GUID, remove any record of it.
    * @param zone the number of the zone
    * @param guid a GUID associated with the character;
    *             it does not have to be findable using `{character}.GUID.guid`
    * @return any `Player` that may have been associated with this GUID
    */
  def Drop(zone : Int, guid : Int) : Option[Player] = Instance.Drop(zone, guid)

  /**
    * Hastily remove all mappings and ids.
    * @return an unsorted list of the characters that were still online
    */
  def Shutdown : List[Player] = Instance.Shutdown
}
