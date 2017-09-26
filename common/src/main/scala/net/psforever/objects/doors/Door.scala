// Copyright (c) 2017 PSForever
package net.psforever.objects.doors

import akka.actor.{ActorContext, ActorRef, Props}
import net.psforever.objects.{PlanetSideGameObject, Player}
import net.psforever.packet.game.UseItemMessage
import net.psforever.types.PlanetSideEmpire

/**
  * na
  * @param ddef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  */
class Door(ddef : DoorDefinition) extends PlanetSideGameObject {
  /** Internal reference to the `Actor` for this `Door`, sets up by this `Door`. */
  private var actor = ActorRef.noSender

  def Actor(implicit context : ActorContext) : ActorRef =  {
    if(actor == ActorRef.noSender) {
      actor = context.actorOf(Props(classOf[DoorControl], this), s"${ddef.Name}_${GUID.guid}")
    }
    actor
  }

  private var openState : Boolean = false
  private var faction : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  private var hackedBy : Option[PlanetSideEmpire.Value] = None

  def Open : Boolean = openState

  def Faction : PlanetSideEmpire.Value = faction

  def Convert(toFaction : PlanetSideEmpire.Value) : Unit = {
    hackedBy = None
    faction = toFaction
  }

  def Request(player : Player, msg : UseItemMessage) : Door.Exchange = {
    if(!openState) {
      if(faction == PlanetSideEmpire.NEUTRAL || player.Faction == faction) {
        Door.OpenEvent()
      }
      else {
        Door.NoEvent()
      }
    }
    else {
      Door.NoEvent()
    }
  }

  def Definition : DoorDefinition = ddef
}

object Door {
  final case class Request(player : Player, msg : UseItemMessage)

  sealed trait Exchange

  final case class DoorMessage(player : Player, msg : UseItemMessage, response : Exchange)

  final case class OpenEvent() extends Exchange

  final case class CloseEvent() extends Exchange

  final case class NoEvent() extends Exchange

  def apply(tdef : DoorDefinition) : Door = {
    new Door(tdef)
  }

  import net.psforever.packet.game.PlanetSideGUID
  def apply(guid : PlanetSideGUID, ddef : DoorDefinition) : Door = {
    val obj = new Door(ddef)
    obj.GUID = guid
    obj
  }
}
