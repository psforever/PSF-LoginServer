// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.ImplantDefinition
import net.psforever.types.ImplantType

/**
  * A slot "on the player" into which an implant is installed.<br>
  * <br>
  * In total, players have three implant slots.
  * At battle rank one (BR1), however, all of those slots are locked.
  * The player earns implants at BR16, BR12, and BR18.
  * A locked implant slot can not be used.
  * (The code uses "not yet unlocked" logic.)
  * When unlocked, an implant may be installed into that slot.<br>
  * <br>
  * The default implant that the underlying slot utilizes is the "Range Magnifier."
  * Until the `Installed` condition is some value other than `None`, however, the implant in the slot will not work.
  */
class ImplantSlot {
  /** is this slot available for holding an implant */
  private var unlocked : Boolean = false
  /** what implant is currently installed in this slot; None if there is no implant currently installed */
  private var installed : Option[ImplantType.Value] = None
  /** the entry for that specific implant used by the a player; always occupied by some type of implant */
  private var implant : Implant = ImplantSlot.default

  def Unlocked : Boolean = unlocked

  def Unlocked_=(lock : Boolean) : Boolean = {
    unlocked = lock
    Unlocked
  }

  def Installed : Option[ImplantType.Value] = installed

  def Implant : Option[Implant] = if(Installed.isDefined) { Some(implant) } else { None }

  def Implant_=(anImplant : Option[Implant]) : Option[Implant] = {
    anImplant match {
      case Some(module) =>
        Implant = module
      case None =>
        installed = None
    }
    Implant
  }

  def Implant_=(anImplant : Implant) : Option[Implant] = {
    implant = anImplant
    installed = Some(anImplant.Definition.Type)
    Implant
  }
}

object ImplantSlot {
  private val default = new Implant(ImplantDefinition(ImplantType.None))

  def apply() : ImplantSlot = {
    new ImplantSlot()
  }
}
