// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player

/**
  * A server object that provides a service, triggered when a certain distance from the unit itself (proximity-based).
  * Unlike conventional terminals, this one is not necessarily structure-owned.
  * For example, the cavern crystals are considered owner-neutral elements that are not attached to a `Building` object.
  */
trait ProximityUnit {
  this : Terminal =>

  /**
    * A list of targets that are currently affected by this proximity unit.
    */
  private var targets : Set[Player] = Set.empty

  def Targets : Seq[Player] = targets toSeq

  def NumberUsers : Int = targets.size

  def AddUser(player : Player) : Int = {
    targets += player
    NumberUsers
  }

  def RemoveUser(player : Player) : Int = {
    targets -= player
    NumberUsers
  }
}
