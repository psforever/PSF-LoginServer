// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.avatar.Avatar

import scala.collection.concurrent.{Map, TrieMap}

/**
  * See the companion object for class and method documentation.
  * `LivePlayerList` is a singleton and this private class lacks exposure.
  */
private class LivePlayerList {

  /** key - the session id; value - a `Player` object */
  private val sessionMap: Map[Long, Avatar] = new TrieMap[Long, Avatar]

  def WorldPopulation(predicate: ((_, Avatar)) => Boolean): List[Avatar] = {
    sessionMap.filter(predicate).values.toList
  }

  def Add(sessionId: Long, avatar: Avatar): Boolean = {
    sessionMap.values.find(char => char.equals(avatar)) match {
      case None =>
        sessionMap.putIfAbsent(sessionId, avatar).isEmpty
      case Some(_) =>
        false
    }
  }

  def Update(sessionId: Long, avatar: Avatar): Unit = {
    sessionMap.get(sessionId) match {
      case Some(_) =>
        sessionMap(sessionId) = avatar
      case None => ;
    }
  }

  def Remove(sessionId: Long): Option[Avatar] = {
    sessionMap.remove(sessionId)
  }

  def Shutdown: List[Avatar] = {
    val list = sessionMap.values.toList
    sessionMap.clear()
    list
  }
}

/**
  * A class for storing `Player` mappings for users that are currently online.
  * The mapping system is tightly coupled between the `Avatar` class and to an instance of `WorldSessionActor`.
  * <br>
  * Use:<br>
  * 1) When a users logs in during `WorldSessionActor`, associate that user's session id and their character (avatar).<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`LivePlayerList.Add(session, avatar)`<br>
  * 2) In between the previous two steps, a range of characters may be queried based on provided statistics.<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`LivePlayerList.WorldPopulation(...)`<br>
  * 3) When the user leaves the game entirely, his character's entry is removed from the mapping.<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`LivePlayerList.Remove(session)`
  */
object LivePlayerList {

  /** As `LivePlayerList` is a singleton, an object of `LivePlayerList` is automatically instantiated. */
  private val Instance: LivePlayerList = new LivePlayerList

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
  def WorldPopulation(predicate: ((_, Avatar)) => Boolean): List[Avatar] = Instance.WorldPopulation(predicate)

  /**
    * Create a mapped entry between the user's session and a user's character.
    * Neither the player nor the session may exist in the current mappings if this is to work.
    *
    * @param sessionId the session
    * @param avatar    the character
    * @return `true`, if the session was association was made; `false`, otherwise
    */
  def Add(sessionId: Long, avatar: Avatar): Boolean = Instance.Add(sessionId, avatar)

  def Update(sessionId: Long, avatar: Avatar): Unit = Instance.Update(sessionId, avatar)

  /**
    * Remove all entries related to the given session identifier from the mappings.
    * The character no longer counts as "online."
    *
    * @param sessionId the session
    * @return any character that was afffected by the mapping removal
    */
  def Remove(sessionId: Long): Option[Avatar] = Instance.Remove(sessionId)

  /**
    * Hastily remove all mappings and ids.
    *
    * @return an unsorted list of the characters that were still online
    */
  def Shutdown: List[Avatar] = Instance.Shutdown
}
