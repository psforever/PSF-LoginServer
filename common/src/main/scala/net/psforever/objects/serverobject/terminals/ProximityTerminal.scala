// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.packet.game.PlanetSideGUID

class ProximityTerminal(tdef : TerminalDefinition) extends Terminal(tdef) {
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

object ProximityTerminal {
  final case class Use(player : Player)
  final case class Unuse(player : Player)

  /**
    * Overloaded constructor.
    * @param tdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    */
  def apply(tdef : TerminalDefinition) : ProximityTerminal = {
    new ProximityTerminal(tdef)
  }

  import akka.actor.ActorContext

  /**
    * Instantiate an configure a `Terminal` object
    * @param tdef    the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    * @param id      the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `Terminal` object
    */
  def Constructor(tdef : TerminalDefinition)(id : Int, context : ActorContext) : Terminal = {
    import akka.actor.Props
    val obj = ProximityTerminal(tdef)
    obj.Actor = context.actorOf(Props(classOf[ProximityTerminalControl], obj), s"${tdef.Name}_$id")
    obj
  }
}
