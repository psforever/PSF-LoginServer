// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.doors

import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.Player
import net.psforever.packet.game.UseItemMessage

/**
  * na
  * @param ddef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  */
class Door(ddef : DoorDefinition) extends PlanetSideServerObject {
  private var openState : Boolean = false
  private var lockState : Boolean = false

  def Open : Boolean = openState

  def Open_=(open : Boolean) : Boolean = {
    openState = open
    Open
  }

  def Locked : Boolean = lockState

  def Locked_=(lock : Boolean) : Boolean = {
    lockState = lock
    Locked
  }

  def Use(player : Player, msg : UseItemMessage) : Door.Exchange = {
    if(!lockState && !openState) {
      openState = true
      Door.OpenEvent()
    }
    else if(openState) {
      Door.CloseEvent()
    }
    else {
      Door.NoEvent()
    }
  }

  def Definition : DoorDefinition = ddef
}

object Door {
  final case class Use(player : Player, msg : UseItemMessage)

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
