// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.packet.game.PlanetSideGUID

import scala.collection.concurrent.{Map, TrieMap}

/**
  * See the companion object for class and method documentation.
  * `LivePlayerList` is a singleton and this private class lacks exposure.
  */
private class LivePlayerList {
  /** key - the session id; value - a `Player` object */
  private val sessionMap : Map[Long, Player] = new TrieMap[Long, Player]
  /** key - the global unique identifier; value - the session id */
  private val playerMap : Map[Int, Long] = new TrieMap[Int, Long]

  def WorldPopulation(predicate : ((_, Player)) => Boolean) : List[Player] = {
    sessionMap.filter(predicate).map({ case(_, char) => char }).toList
  }

  def Add(sessionId : Long, player : Player) : Boolean = {
    sessionMap.values.find(char => char.equals(player)) match {
      case None =>
        sessionMap.putIfAbsent(sessionId, player).isEmpty
        true
      case Some(_) =>
        false
    }
  }

  def Remove(sessionId : Long) : Option[Player] = {
    sessionMap.remove(sessionId) match {
      case Some(char) =>
        playerMap.find({ case(_, sess) => sess == sessionId }) match {
          case Some((guid, _)) =>
            playerMap.remove(guid)
          case None => ;
        }
        Some(char)
      case None =>
        None
    }
  }

  def Get(guid : PlanetSideGUID) : Option[Player] = {
    Get(guid.guid)
  }

  def Get(guid : Int) : Option[Player] = {
    playerMap.get(guid) match {
      case Some(sess) =>
        sessionMap.get(sess)
      case _ =>
        None
    }
  }

  def Assign(sessionId : Long, guid : PlanetSideGUID) : Boolean = Assign(sessionId, guid.guid)

  def Assign(sessionId : Long, guid : Int) : Boolean = {
    sessionMap.find({ case(sess, _) => sess == sessionId}) match {
      case Some((_, char)) =>
        if(char.GUID.guid == guid) {
          playerMap.find({ case(_, sess) => sess == sessionId }) match {
            case Some((id, _)) =>
              playerMap.remove(id)
            case None => ;
          }
          playerMap.put(guid, sessionId)
          true
        }
        else {
          false
        }

      case None =>
        false
    }
  }

  def Shutdown : List[Player] = {
    val list = sessionMap.values.toList
    sessionMap.clear
    playerMap.clear
    list
  }
}

/**
  * A class for storing `Player` mappings for users that are currently online.
  * The mapping system is tightly coupled between the `Player` class and to an instance of `WorldSessionActor`.
  * A loose coupling between the current globally unique identifier (GUID) and the user is also present.<br>
  * <br>
  * Use:<br>
  * 1) When a users logs in during `WorldSessionActor`, associate that user's session id and the character.<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`LivePlayerList.Add(session, player)`<br>
  * 2) When that user's chosen character is declared his avatar using `SetCurrentAvatarMessage`,
  * also associate the user's session with their current GUID.<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`LivePlayerList.Assign(session, guid)`<br>
  * 3) Repeat the previous step for as many times the user's GUID changes, especially during the aforementioned condition.<br>
  * 4a) In between the previous two steps, a user's character may be referenced by their current GUID.<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`LivePlayerList.Get(guid)`<br>
  * 4b) Also in between those same previous steps, a range of characters may be queried based on provided statistics.<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`LivePlayerList.WorldPopulation(...)`<br>
  * 5) When the user leaves the game, his character's entries are removed from the mappings.<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`LivePlayerList.Remove(session)`
  */
object LivePlayerList {
  /** As `LivePlayerList` is a singleton, an object of `LivePlayerList` is automatically instantiated. */
  private val Instance : LivePlayerList = new LivePlayerList

  /**
    * Given some criteria, examine the mapping of user characters and find the ones that fulfill the requirements.<br>
    * <br>
    * Note the signature carefully.
    * A two-element tuple is checked, but only the second element of that tuple - a character -  is eligible for being queried.
    * The first element is ignored.
    * Even a predicate as simple as `{ case ((x : Long, _)) => x > 0 }` will not work for that reason.
    * @param predicate the conditions for filtering the live `Player`s
    * @return a list of users's `Player`s that fit the criteria
    */
  def WorldPopulation(predicate : ((_, Player)) => Boolean) : List[Player] = Instance.WorldPopulation(predicate)

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
    * @param guid the current GUID of the character
    * @return the character, if it can be found using the GUID
    */
  def Get(guid : PlanetSideGUID) : Option[Player] = Instance.Get(guid)

  /**
    * Get a user's character from the mappings.
    * @param guid the current GUID of the character
    * @return the character, if it can be found using the GUID
    */
  def Get(guid : Int) : Option[Player] = Instance.Get(guid)

  /**
    * Given a session that maps to a user's character, create a mapping between the character's current GUID and the session.
    * If the user already has a GUID in the mappings, remove it and assert the new one.
    * @param sessionId the session
    * @param guid the GUID to associate with the character;
    *             technically, it has already been assigned and should be findable using `{character}.GUID.guid`
    * @return `true`, if the mapping was created;
    *        `false`, if the session can not be found or if the character's GUID doesn't match the one provided
    */
  def Assign(sessionId : Long, guid : PlanetSideGUID) : Boolean = Instance.Assign(sessionId, guid)

  /**
    * Given a session that maps to a user's character, create a mapping between the character's current GUID and the session.
    * If the user already has a GUID in the mappings, remove it and assert the new one.
    * @param sessionId the session
    * @param guid the GUID to associate with the character;
    *             technically, it has already been assigned and should be findable using `{character}.GUID.guid`
    * @return `true`, if the mapping was created;
    *        `false`, if the session can not be found or if the character's GUID doesn't match the one provided
    */
  def Assign(sessionId : Long, guid : Int) : Boolean = Instance.Assign(sessionId, guid)

  /**
    * Hastily remove all mappings and ids.
    * @return an unsorted list of the characters that were still online
    */
  def Shutdown : List[Player] = Instance.Shutdown
}
