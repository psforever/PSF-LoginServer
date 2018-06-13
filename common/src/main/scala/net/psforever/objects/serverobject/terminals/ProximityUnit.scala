// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.terminals.Terminal.TerminalMessage
import net.psforever.packet.game.PlanetSideGUID

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
  private var targets : Set[PlanetSideGUID] = Set.empty

  def NumberUsers : Int = targets.size

  def AddUser(player_guid : PlanetSideGUID) : Int = {
    targets += player_guid
    NumberUsers
  }

  def RemoveUser(player_guid : PlanetSideGUID) : Int = {
    targets -= player_guid
    NumberUsers
  }
}

object ProximityUnit {
  import akka.actor.Actor

  /**
    * A mixin `trait` for an `Actor`'s `PartialFunction` that handles messages,
    * in this case handling messages that controls the telegraphed state of the `ProximityUnit` object as the number of users changes.
    */
  trait Use {
    this : Actor =>

    def TerminalObject : Terminal with ProximityUnit

    val proximityBehavior : Receive = {
      case CommonMessages.Use(player) =>
        val hadNoUsers = TerminalObject.NumberUsers == 0
        if(TerminalObject.AddUser(player.GUID) == 1 && hadNoUsers) {
          sender ! TerminalMessage(player, null, Terminal.StartProximityEffect(TerminalObject))
        }

      case CommonMessages.Unuse(player) =>
        val hadUsers = TerminalObject.NumberUsers > 0
        if(TerminalObject.RemoveUser(player.GUID) == 0 && hadUsers) {
          sender ! TerminalMessage(player, null, Terminal.StopProximityEffect(TerminalObject))
        }
    }
  }
}
