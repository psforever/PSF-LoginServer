// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.objects.definition.converter.TerminalConverter
import net.psforever.objects.vital._
import net.psforever.objects.vital.resistance.ResistanceProfileMutators

/**
  * The basic definition for any `Terminal` object.
  * @param objectId the object's identifier number
  */
abstract class TerminalDefinition(objectId : Int) extends net.psforever.objects.definition.ObjectDefinition(objectId)
  with ResistanceProfileMutators
  with DamageResistanceModel {
  Name = "terminal"
  Packet = new TerminalConverter
  Damage = StandardAmenityDamage
  Resistance = StandardAmenityResistance
  Model = StandardResolutions.Amenities

  private var maxHealth : Int = 0

  /**
    * The unimplemented functionality for the entry function of form of activity
    * processed by this terminal and codified by the input message (a "request").
    * @see `Terminal.Exchange`
    * @param player the player who made the request
    * @param msg the request message
    * @return a message that resolves the transaction
    */
  def Request(player : Player, msg : Any) : Terminal.Exchange

  def MaxHealth : Int = maxHealth

  def MaxHealth_=(health : Int) : Int = {
    maxHealth = health
    MaxHealth
  }
}
