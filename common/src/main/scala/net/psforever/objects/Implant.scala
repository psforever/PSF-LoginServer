// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.{ImplantDefinition, Stance}
import net.psforever.types.{ExoSuitType, ImplantType}

/**
  * A type of installable player utility that grants a perk, usually in exchange for stamina (energy).<br>
  * <br>
  * An implant starts with a never-to-initialized timer value of -1 and will not report as `Ready` until the timer is 0.
  * The `Timer`, however, will report to the user a time of 0 since negative time does not make sense.
  * Although the `Timer` can be manually set, using `Reset` is the better way to default the initialization timer to the correct amount.
  * An external script will be necessary to operate the actual initialization countdown.
  * An implant must be `Ready` before it can be `Active`.
  * The `Timer` must be set (or reset) (or countdown) to 0 to be `Ready` and then it must be activated.
  * @param implantDef the `ObjectDefinition` that constructs this item and maintains some of its immutable fields
  */
class Implant(implantDef : ImplantDefinition) {
  private var active : Boolean = false
  private var initTimer : Long = -1L

  def Name : String = implantDef.Name

  def Ready : Boolean = initTimer == 0L

  def Active : Boolean = active

  def Active_=(isActive : Boolean) : Boolean = {
    active = Ready && isActive
    Active
  }

  def Timer : Long = math.max(0, initTimer)

  def Timer_=(time : Long) : Long = {
    initTimer = math.max(0, time)
    Timer
  }

  def MaxTimer : Long = implantDef.Initialization

  def ActivationCharge : Int = Definition.ActivationCharge

  /**
    * Calculate the stamina consumption of the implant for any given moment of being active after its activation.
    * As implant energy use can be influenced by both exo-suit worn and general stance held, both are considered.
    * @param suit the exo-suit being worn
    * @param stance the player's stance
    * @return the amount of stamina (energy) that is consumed
    */
  def Charge(suit : ExoSuitType.Value, stance : Stance.Value) : Int = {
    if(active) {
      implantDef.DurationChargeBase + implantDef.DurationChargeByExoSuit(suit) + implantDef.DurationChargeByStance(stance)
    }
    else {
      0
    }
  }

  /**
    * Place an implant back in its initializing state.
    */
  def Reset() : Unit = {
    Active = false
    Timer = MaxTimer
  }

  /**
    * Place an implant back in its pre-initialization state.
    * The implant is inactive and can not proceed to a `Ready` condition naturally from this state.
    */
  def Jammed() : Unit = {
    Active = false
    Timer = -1
  }

  def Definition : ImplantDefinition = implantDef
}

object Implant {
  def default : Implant = new Implant(ImplantDefinition(ImplantType.RangeMagnifier))

  def apply(implantDef : ImplantDefinition) : Implant = {
    new Implant(implantDef)
  }
}
