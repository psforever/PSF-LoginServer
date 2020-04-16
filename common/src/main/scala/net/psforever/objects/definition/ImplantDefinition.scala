// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.types.{ExoSuitType, ImplantType}

import scala.collection.mutable

/**
  * The definition for an installable player utility that grants a perk, usually in exchange for stamina (energy).<br>
  * <br>
  * Most of the definition deals with the costs of activation and operation.
  * When activated by the user, an `activationCharge` may be deducted form that user's stamina reserves.
  * This does not necessarily have to be a non-zero value.
  * Passive implants are always active and thus have no cost.
  * After being activated, a non-passive implant consumes a specific amount of stamina at regular intervals
  * Some implants will specify a different interval for consuming stamina based on the exo-suit the player is wearing
  * @param implantType the type of implant that is defined
  * @see `ImplantType`
  */
class ImplantDefinition(private val implantType : Int) extends BasicDefinition {
  ImplantType(implantType)
  /** how long it takes the implant to become ready for activation; is milliseconds */
  private var initializationDuration : Long = 0L
  /** a passive certification is activated as soon as it is ready (or other condition) */
  private var passive : Boolean = false
  /** how much turning on the implant costs */
  private var activationStaminaCost : Int = 0
  /** how much energy does this implant cost to remain activate per interval tick */
  private var staminaCost : Int = 0

  /**
    * How often in milliseconds the stamina cost will be applied, per exo-suit type
    * in game_objects.adb.lst each armour type is listed as a numeric identifier
    * stamina_consumption_interval = Standard
    * stamina_consumption_interval1 = Infil
    * stamina_consumption_interval2 = Agile
    * stamina_consumption_interval3 = Rexo
    * stamina_consumption_interval4 = MAX?
    */
  private var costIntervalDefault : Int = 0
  private val costIntervalByExoSuit = mutable.HashMap[ExoSuitType.Value, Int]().withDefaultValue(CostIntervalDefault)
  Name = "implant"

  def InitializationDuration : Long = initializationDuration

  def InitializationDuration_=(time : Long) : Long = {
    initializationDuration = math.max(0, time)
    InitializationDuration
  }

  def Passive : Boolean = passive

  def Passive_=(isPassive : Boolean) : Boolean = {
    passive = isPassive
    Passive
  }

  def ActivationStaminaCost : Int = activationStaminaCost

  def ActivationStaminaCost_=(charge : Int) : Int = {
    activationStaminaCost = math.max(0, charge)
    ActivationStaminaCost
  }

  def StaminaCost : Int = staminaCost

  def StaminaCost_=(charge : Int) : Int = {
    staminaCost = math.max(0, charge)
    StaminaCost
  }


  def CostIntervalDefault : Int = {
    costIntervalDefault
  }
  def CostIntervalDefault_=(interval : Int) : Int = {
    costIntervalDefault = interval
    CostIntervalDefault
  }

  def GetCostIntervalByExoSuit(exosuit : ExoSuitType.Value) : Int = costIntervalByExoSuit.getOrElse(exosuit, CostIntervalDefault)
  def CostIntervalByExoSuitHashMap : mutable.Map[ExoSuitType.Value, Int] = costIntervalByExoSuit

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
