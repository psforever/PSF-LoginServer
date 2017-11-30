// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.{ImplantDefinition, Stance}
import net.psforever.types.{ExoSuitType, ImplantType}

/**
  * A slot "on the player" into which an implant is installed.
  * In total, players have three implant slots.<br>
  * <br>
  * All implants slots start as "locked" and must be "unlocked" through battle rank advancement.
  * Only after it is "unlocked" may an implant be "installed" into the slot.
  * Upon installation, it undergoes an initialization period and then, after which, it is ready for user activation.
  * Being jammed de-activates the implant, put it into a state of "not being ready," and causes the initialization to repeat.
  */
class ImplantSlot {
  /** is this slot available for holding an implant */
  private var unlocked : Boolean = false
  /** whether this implant is ready for use */
  private var initialized : Boolean = false
  /** is this implant active */
  private var active : Boolean = false
  /** what implant is currently installed in this slot; None if there is no implant currently installed */
  private var implant : Option[ImplantDefinition] = None

  def Unlocked : Boolean = unlocked

  def Unlocked_=(lock : Boolean) : Boolean = {
    unlocked = lock || unlocked //do not let re-lock
    Unlocked
  }

  def Initialized : Boolean = initialized

  def Initialized_=(init : Boolean) : Boolean = {
    initialized = Installed.isDefined && init
    Active = Active && initialized //can not be active just yet
    Initialized
  }

  def Active : Boolean = active

  def Active_=(state : Boolean) : Boolean = {
    active = Initialized && state
    Active
  }

  def Implant : ImplantType.Value = Installed match {
    case Some(idef) =>
      idef.Type
    case None =>
      Active = false
      Initialized = false
      ImplantType.None
  }

  def Implant_=(anImplant : ImplantDefinition) : ImplantType.Value = {
    Implant_=(Some(anImplant))
  }

  def Implant_=(anImplant : Option[ImplantDefinition]) : ImplantType.Value = {
    if(Unlocked) {
      anImplant match {
        case Some(_) =>
          implant = anImplant
        case None =>
          Active = false
          Initialized = false
          implant = None
      }
    }
    Implant
  }

  def Installed : Option[ImplantDefinition] = implant

  def MaxTimer : Long = Implant match {
    case ImplantType.None =>
      -1L
    case _ =>
      Installed.get.Initialization
  }

  def ActivationCharge : Int =  {
    if(Active) {
      Installed.get.ActivationCharge
    }
    else {
      0
    }
  }

  /**
    * Calculate the stamina consumption of the implant for any given moment of being active after its activation.
    * As implant energy use can be influenced by both exo-suit worn and general stance held, both are considered.
    * @param suit the exo-suit being worn
    * @param stance the player's stance
    * @return the amount of stamina (energy) that is consumed
    */
  def Charge(suit : ExoSuitType.Value, stance : Stance.Value) : Int = {
    if(Active) {
      val inst = Installed.get
      inst.DurationChargeBase + inst.DurationChargeByExoSuit(suit) + inst.DurationChargeByStance(stance)
    }
    else {
      0
    }
  }

  def Jammed() : Unit = {
    Active = false
    Initialized = false
  }
}

object ImplantSlot {
  def apply() : ImplantSlot = {
    new ImplantSlot()
  }
}
