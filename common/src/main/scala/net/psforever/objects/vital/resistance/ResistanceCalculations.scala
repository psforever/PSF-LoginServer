// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.resistance

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.ballistics._
import net.psforever.objects.definition.ExoSuitDefinition
import net.psforever.objects.vital.projectile.ProjectileCalculations
import net.psforever.types.ExoSuitType

import scala.util.{Failure, Success, Try}

/**
  * The base class for function literal description related to calculating resistance information.<br>
  * <br>
  * Implementing functionality of the children is the product of two user-defined processes
  * and information for the calculation is extracted from the to-be-provided weapon discharge information.
  * Specifically, the information is found as the `target` object which is a member of the said information.
  * The specific functions passed into this object typically operate simultaneously normally
  * and are related to the target and the kind of interaction the weapon discharge had with the target.
  * @param validate determine if a more generic `target` object is actually an expected type;
  *                 cast to and return that type of object
  * @param extractor recover the resistance values from an approved type of object
  * @tparam TargetType an internal type that converts between `validate`'s output and `extractor`'s input;
  *                    in essence, should match the type of object container to which these resistances belong;
  *                    never has to be defined explicitly, but will be checked upon object definition
  */
abstract class ResistanceCalculations[TargetType](validate : ResolvedProjectile=>Try[TargetType],
                                                  extractor : TargetType=>Int) extends ProjectileCalculations {
  /**
    * Get resistance valuess.
    * @param data the historical `ResolvedProjectile` information
    * @return the damage value
    */
  def Calculate(data : ResolvedProjectile) : Int = {
    validate(data) match {
      case Success(target) =>
        extractor(target)
      case _ =>
        0
    }
  }
}

object ResistanceCalculations {
  private def failure(typeName : String) = Failure(new Exception(s"can not match expected target $typeName"))

  //target identification
  def InvalidTarget(data : ResolvedProjectile) : Try[SourceEntry] = failure(s"invalid ${data.target.Definition.Name}")

  def ValidInfantryTarget(data : ResolvedProjectile) : Try[PlayerSource] = {
    data.target match {
      case target : PlayerSource =>
        if(target.ExoSuit != ExoSuitType.MAX) { //max is not counted as an official infantry exo-suit type
          Success(target)
        }
        else {
          failure("infantry")
        }
      case _ =>
        failure("infantry")
    }
  }

  def ValidMaxTarget(data : ResolvedProjectile) : Try[PlayerSource] = {
    data.target match {
      case target : PlayerSource =>
        if(target.ExoSuit == ExoSuitType.MAX) {
          Success(target)
        }
        else {
          failure("max")
        }
      case _ =>
        failure("max")
    }
  }

  def ValidVehicleTarget(data : ResolvedProjectile) : Try[VehicleSource] = {
    data.target match {
      case target : VehicleSource =>
        if(!GlobalDefinitions.isFlightVehicle(target.Definition)) {
          Success(target)
        }
        else {
          failure("vehicle")
        }
      case _ =>
        failure("vehicle")
    }
  }

  def ValidAircraftTarget(data : ResolvedProjectile) : Try[VehicleSource] = {
    data.target match {
      case target : VehicleSource =>
        if(GlobalDefinitions.isFlightVehicle(target.Definition)) {
          Success(target)
        }
        else {
          failure("aircraft")
        }
      case _ =>
        failure("aircraft")
    }
  }

  //extractors
  def NoResistExtractor(target : SourceEntry) : Int = 0

  def ExoSuitDirectExtractor(target : PlayerSource) : Int = ExoSuitDefinition.Select(target.ExoSuit, target.Faction).ResistanceDirectHit

  def ExoSuitSplashExtractor(target : PlayerSource) : Int = ExoSuitDefinition.Select(target.ExoSuit, target.Faction).ResistanceSplash

  def ExoSuitAggravatedExtractor(target : PlayerSource) : Int = ExoSuitDefinition.Select(target.ExoSuit, target.Faction).ResistanceAggravated

  def ExoSuitRadiationExtractor(target : PlayerSource) : Float = ExoSuitDefinition.Select(target.ExoSuit, target.Faction).RadiationShielding

  def VehicleDirectExtractor(target : VehicleSource) : Int = target.Definition.ResistanceDirectHit

  def VehicleSplashExtractor(target : VehicleSource) : Int = target.Definition.ResistanceSplash

  def VehicleAggravatedExtractor(target : VehicleSource) : Int = target.Definition.ResistanceAggravated

  def VehicleRadiationExtractor(target : VehicleSource) : Float = target.Definition.RadiationShielding

  def MaximumResistance(target : SourceEntry) : Int = Integer.MAX_VALUE
}
