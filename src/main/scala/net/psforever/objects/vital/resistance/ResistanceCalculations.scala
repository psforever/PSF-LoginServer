// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.resistance

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.ballistics._
import net.psforever.objects.definition.ExoSuitDefinition
import net.psforever.objects.serverobject.structures.AmenityDefinition
import net.psforever.objects.sourcing.{ObjectSource, PlayerSource, SourceEntry, VehicleSource}
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.types.ExoSuitType

import scala.util.{Failure, Success, Try}

/**
  * The base for function literal description related to calculating resistance information.
  * This glue connects target validation to value extraction
  * to avoid the possibility of `NullPointerException` and `ClassCastException`.
  * Some different types of vital objects store their resistance values in different places.
  * @param validate determine if a more generic `target` object is actually an expected type;
  *                 cast to and return that type of object
  * @param extractor recover the resistance values from an approved type of object
  * @param default if the target does not match the validator, this is the constant resistance to return;
  *                the code really needs to be examined in this case;
  *                defaults to 0
  * @tparam TargetType an internal type that converts between `validate`'s output and `extractor`'s input;
  *                    in essence, should match the type of object container to which these resistances belong;
  *                    never has to be defined explicitly but will be checked at compile time
  */
abstract class ResistanceCalculations[TargetType](
    validate: DamageInteraction => Try[TargetType],
    extractor: TargetType => Int,
    default: Int = 0
) {

  /**
    * Get resistance values.
    * @param data the historical `DamageInteraction` information
    * @return the damage value
    */
  def Calculate(data: DamageInteraction): Int = {
    validate(data) match {
      case Success(target) =>
        extractor(target)
      case _ =>
        default
    }
  }
}

object ResistanceCalculations {
  private def failure(typeName: String) = Failure(new Exception(s"can not match expected target $typeName"))

  //target identification
  def InvalidTarget(data: DamageInteraction): Try[SourceEntry] = failure(s"invalid ${data.target.Definition.Name}")

  //target is always considered valid
  def AlwaysValidTarget(data: DamageInteraction): Try[SourceEntry] = Success(data.target)

  def ValidInfantryTarget(data: DamageInteraction): Try[PlayerSource] = {
    data.target match {
      case target: PlayerSource =>
        if (target.ExoSuit != ExoSuitType.MAX) { //max is not counted as an official infantry exo-suit type
          Success(target)
        } else {
          failure("infantry")
        }
      case _ =>
        failure("infantry")
    }
  }

  def ValidMaxTarget(data: DamageInteraction): Try[PlayerSource] = {
    data.target match {
      case target: PlayerSource =>
        if (target.ExoSuit == ExoSuitType.MAX) {
          Success(target)
        } else {
          failure("max")
        }
      case _ =>
        failure("max")
    }
  }

  def ValidVehicleTarget(data: DamageInteraction): Try[VehicleSource] = {
    data.target match {
      case target: VehicleSource =>
        if (!GlobalDefinitions.isFlightVehicle(target.Definition)) {
          Success(target)
        } else {
          failure("vehicle")
        }
      case _ =>
        failure("vehicle")
    }
  }

  def ValidAircraftTarget(data: DamageInteraction): Try[VehicleSource] = {
    data.target match {
      case target: VehicleSource =>
        if (GlobalDefinitions.isFlightVehicle(target.Definition)) {
          Success(target)
        } else {
          failure("aircraft")
        }
      case _ =>
        failure("aircraft")
    }
  }

  def ValidAmenityTarget(data: DamageInteraction): Try[ObjectSource] = {
    data.target match {
      case target: ObjectSource =>
        if (target.Definition.isInstanceOf[AmenityDefinition]) {
          Success(target)
        } else {
          failure(s"${target.Definition.Name} amenity")
        }
      case _ =>
        failure(s"amenity")
    }
  }

  //extractors
  def NoResistExtractor(target: SourceEntry): Int = 0

  def ExoSuitDirectExtractor(target: PlayerSource): Int =
    ExoSuitDefinition.Select(target.ExoSuit, target.Faction).ResistanceDirectHit

  def ExoSuitSplashExtractor(target: PlayerSource): Int =
    ExoSuitDefinition.Select(target.ExoSuit, target.Faction).ResistanceSplash

  def ExoSuitAggravatedExtractor(target: PlayerSource): Int =
    ExoSuitDefinition.Select(target.ExoSuit, target.Faction).ResistanceAggravated

  def ExoSuitRadiationExtractor(target: PlayerSource): Float =
    ExoSuitDefinition.Select(target.ExoSuit, target.Faction).RadiationShielding

  def VehicleDirectExtractor(target: VehicleSource): Int = target.Definition.ResistanceDirectHit

  def VehicleSplashExtractor(target: VehicleSource): Int = target.Definition.ResistanceSplash

  def VehicleAggravatedExtractor(target: VehicleSource): Int = target.Definition.ResistanceAggravated

  def VehicleRadiationExtractor(target: VehicleSource): Float = target.Definition.RadiationShielding

  def OtherDirectExtractor(target: ObjectSource): Int =
    target.Definition.asInstanceOf[ResistanceProfile].ResistanceDirectHit

  def OtherSplashExtractor(target: ObjectSource): Int =
    target.Definition.asInstanceOf[ResistanceProfile].ResistanceSplash

  def MaximumResistance(target: SourceEntry): Int = Integer.MAX_VALUE
}
