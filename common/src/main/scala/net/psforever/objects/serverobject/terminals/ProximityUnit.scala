// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.terminals.Terminal.TerminalMessage
import net.psforever.packet.game.PlanetSideGUID

/**
  * A server object that is a "terminal" that can be accessed for amenities and services,
  * triggered when a certain distance from the unit itself (proximity-based).<br>
  * <br>
  * Unlike conventional terminals, this structure is not necessarily structure-owned.
  * For example, the cavern crystals are considered owner-neutral elements that are not attached to a `Building` object.
  */
trait ProximityUnit {
  this : Terminal =>

  private var users : Set[PlanetSideGUID] = Set.empty

  def NumberUsers : Int = users.size

  def AddUser(player_guid : PlanetSideGUID) : Int = {
    users += player_guid
    NumberUsers
  }

  def RemoveUser(player_guid : PlanetSideGUID) : Int = {
    users -= player_guid
    NumberUsers
  }
}

object ProximityUnit {
  import akka.actor.Actor

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