// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.types.{ExoSuitType, ImplantType}

import scala.collection.mutable

/**
  * An `Enumeration` of a variety of poses or generalized movement.
  */
object Stance extends Enumeration {
  val
  Crouching,
  CrouchWalking, //not used, but should still be defined
  Standing,
  Walking, //not used, but should still be defined
  Running
  = Value
}

/**
  * The definition for an installable player utility that grants a perk, usually in exchange for stamina (energy).<br>
  * <br>
  * Most of the definition deals with the costs of activation and operation.
  * When activated by the user, an `activationCharge` may be deducted form that user's stamina reserves.
  * This does not necessarily have to be a non-zero value.
  * Passive implants are always active and thus have no cost.
  * After being activated, a non-passive implant consumes a specific amount of stamina each second.
  * This cost is modified by how the user is standing and what type of exo-suit they are wearing.
  * The `durationChargeBase` is the lowest cost for an implant.
  * Modifiers for exo-suit type and stance type are then added onto this base cost.
  * For example: wearing `Reinforced` costs 2 stamina but costs only 1 stamina in all other cases.
  * Assuming that is the only cost, the definition would have a base charge of 1 and a `Reinforced` modifier of 1.
  * @param implantType the type of implant that is defined
  * @see `ImplantType`
  */
class ImplantDefinition(private val implantType : Int) extends BasicDefinition {
  ImplantType(implantType)
  /** how long it takes the implant to spin-up; is milliseconds */
  private var initialization : Long = 0L
  /** a passive certification is activated as soon as it is ready (or other condition) */
  private var passive : Boolean = false
  /** how much turning on the implant costs */
  private var activationCharge : Int = 0
  /** how much energy does this implant cost to remain active per second*/
  private var durationChargeBase : Int = 0
  /** how much more energy does the implant cost for this exo-suit */
  private val durationChargeByExoSuit = mutable.HashMap[ExoSuitType.Value, Int]().withDefaultValue(0)
  /** how much more energy does the implant cost for this stance */
  private val durationChargeByStance = mutable.HashMap[Stance.Value, Int]().withDefaultValue(0)
  Name = "implant"

  def Initialization : Long = initialization

  def Initialization_=(time : Long) : Long = {
    initialization = math.max(0, time)
    Initialization
  }

  def Passive : Boolean = passive

  def Passive_=(isPassive : Boolean) : Boolean = {
    passive = isPassive
    Passive
  }

  def ActivationCharge : Int = activationCharge

  def ActivationCharge_=(charge : Int) : Int = {
    activationCharge = math.max(0, charge)
    ActivationCharge
  }

  def DurationChargeBase : Int = durationChargeBase

  def DurationChargeBase_=(charge : Int) : Int = {
    durationChargeBase = math.max(0, charge)
    DurationChargeBase
  }

  def DurationChargeByExoSuit : mutable.Map[ExoSuitType.Value, Int] = durationChargeByExoSuit

  def DurationChargeByStance : mutable.Map[Stance.Value, Int] = durationChargeByStance

  def Type : ImplantType.Value = ImplantType(implantType)
}

object ImplantDefinition {
  def apply(implantType : Int) : ImplantDefinition = {
    new ImplantDefinition(implantType)
  }

  def apply(implantType : ImplantType.Value) : ImplantDefinition = {
    new ImplantDefinition(implantType.id)
  }
}
