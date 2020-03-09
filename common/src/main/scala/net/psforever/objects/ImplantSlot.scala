// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.Cancellable
import net.psforever.objects.definition.ImplantDefinition
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
  /** a cancellable timer that can be used to set an implant as initialized once complete */
  private var initializeTimer: Cancellable = DefaultCancellable.obj

  /** is this implant active */
  private var active : Boolean = false
  /** what implant is currently installed in this slot; None if there is no implant currently installed */
  private var implant : Option[ImplantDefinition] = None

  def InitializeTimer : Cancellable = initializeTimer

  def InitializeTimer_=(timer : Cancellable) : Cancellable = {
    initializeTimer = timer
    initializeTimer
  }

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
          implant = None
      }
      Active = false
      Initialized = false
    }
    Implant
  }

  def Installed : Option[ImplantDefinition] = implant

  def MaxTimer : Long = Implant match {
    case ImplantType.None =>
      -1L
    case _ =>
      Installed.get.InitializationDuration
  }

  def ActivationCharge : Int =  {
    if(Active) {
      Installed.get.ActivationStaminaCost
    }
    else {
      0
    }
  }

  /**
    * Calculate the stamina consumption of the implant for any given moment of being active after its activation.
    * @param suit the exo-suit being worn
    * @return the amount of stamina (energy) that is consumed
    */
  def Charge(suit : ExoSuitType.Value) : Int = {
    if(Active) {
      val inst = Installed.get
      inst.StaminaCost
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
