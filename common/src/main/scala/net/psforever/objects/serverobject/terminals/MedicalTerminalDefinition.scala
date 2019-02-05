// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player

import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * The definition for any `Terminal` that is of a type "medical_terminal".
  * This includes the functionality of the formal medical terminals and some of the cavern crystals.
  * Do not confuse the game's internal "medical_terminal" object category and the actual `medical_terminal` object (529).
  */
class MedicalTerminalDefinition(objectId : Int) extends ProximityTerminalDefinition(objectId) {
  private var interval : FiniteDuration = Duration(0, "seconds")
  private var healAmount : Int = 0
  private var armorAmount : Int = 0

  def Interval : FiniteDuration = interval

  def Interval_=(amount : Int) : FiniteDuration = {
    Interval_=(Duration(amount, "milliseconds"))
  }

  def Interval_=(amount : FiniteDuration) : FiniteDuration = {
    interval = amount
    Interval
  }

  def HealAmount : Int = healAmount

  def HealAmount_=(amount : Int) : Int = {
    healAmount = amount
    HealAmount
  }

  def ArmorAmount : Int = armorAmount

  def ArmorAmount_=(amount : Int) : Int = {
    armorAmount = amount
    ArmorAmount
  }

  override def Request(player : Player, msg : Any) : Terminal.Exchange = Terminal.NoDeal()
}
