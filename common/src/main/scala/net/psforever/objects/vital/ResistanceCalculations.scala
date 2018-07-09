// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

import net.psforever.objects.{ExoSuitDefinition, GlobalDefinitions}
import net.psforever.objects.ballistics._
import net.psforever.types.ExoSuitType

import scala.util.{Failure, Success, Try}

abstract class ResistanceCalculations[TargetType](validate : (ResolvedProjectile)=>Try[TargetType],
                                                  extractor : (TargetType)=>Int) extends ProjectileCalculations {
  def Calculate(data : ResolvedProjectile) : Int = {
    validate(data) match {
      case Success(target) =>
        extractor(target)
      case _ =>
        0
    }
  }
}

object NoResistance extends ResistanceCalculations[SourceEntry](
  ResistanceCalculations.ValidInfantryTarget,
  ResistanceCalculations.NoResistExtractor
)

object InfantryHitResistance extends ResistanceCalculations[PlayerSource](
  ResistanceCalculations.ValidInfantryTarget,
  ResistanceCalculations.ExoSuitDirectExtractor
)

object InfantrySplashResistance extends ResistanceCalculations[PlayerSource](
  ResistanceCalculations.ValidInfantryTarget,
  ResistanceCalculations.ExoSuitSplashExtractor
)

object InfantryLashResistance extends ResistanceCalculations[PlayerSource](
  ResistanceCalculations.ValidInfantryTarget,
  ResistanceCalculations.NoResistExtractor
)

object InfantryAggravatedResistance extends ResistanceCalculations[PlayerSource](
  ResistanceCalculations.ValidInfantryTarget,
  ResistanceCalculations.ExoSuitAggravatedExtractor
)

object VehicleHitResistance extends ResistanceCalculations[VehicleSource](
  ResistanceCalculations.ValidVehicleTarget,
  ResistanceCalculations.VehicleDirectExtractor
)

object VehicleSplashResistance extends ResistanceCalculations[VehicleSource](
  ResistanceCalculations.ValidVehicleTarget,
  ResistanceCalculations.VehicleSplashExtractor
)

object VehicleLashResistance extends ResistanceCalculations[VehicleSource](
  ResistanceCalculations.ValidVehicleTarget,
  ResistanceCalculations.NoResistExtractor
)

object VehicleAggravatedResistance extends ResistanceCalculations[VehicleSource](
  ResistanceCalculations.ValidVehicleTarget,
  ResistanceCalculations.VehicleAggravatedExtractor
)

object ResistanceCalculations {
  private def failure(typeName : String) = Failure(new Exception(s"can not match expected target $typeName"))

  def InvalidTarget(data : ResolvedProjectile) : Try[SourceEntry] = failure(s"invalid ${data.target.Definition.Name}")

  def ValidInfantryTarget(data : ResolvedProjectile) : Try[PlayerSource] = {
    data.target match {
      case target : PlayerSource =>
        if(target.ExoSuit != ExoSuitType.MAX) {
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

  def NoResistExtractor(target : SourceEntry) : Int = 0

  def ExoSuitDirectExtractor(target : PlayerSource) : Int = ExoSuitDefinition(target.ExoSuit).ResistanceDirectHit

  def ExoSuitSplashExtractor(target : PlayerSource) : Int = ExoSuitDefinition(target.ExoSuit).ResistanceSplash

  def ExoSuitAggravatedExtractor(target : PlayerSource) : Int = ExoSuitDefinition(target.ExoSuit).ResistanceAggravated

  def ExoSuitRadiationExtractor(target : PlayerSource) : Float = ExoSuitDefinition(target.ExoSuit).RadiationShielding

  def VehicleDirectExtractor(target : VehicleSource) : Int = target.Definition.ResistanceDirectHit

  def VehicleSplashExtractor(target : VehicleSource) : Int = target.Definition.ResistanceSplash

  def VehicleAggravatedExtractor(target : VehicleSource) : Int = target.Definition.ResistanceAggravated

  def VehicleRadiationExtractor(target : VehicleSource) : Float = target.Definition.RadiationShielding
}

trait ResistanceSelection {
  final def None : ProjectileCalculations.Form = NoResistance.Calculate

  def Direct : ProjectileCalculations.Form
  def Splash : ProjectileCalculations.Form
  def Lash : ProjectileCalculations.Form
  def Aggravated : ProjectileCalculations.Form

  def apply(data : ResolvedProjectile) : ProjectileCalculations.Form = {
    data.resolution match {
      case ProjectileResolution.Hit => Direct
      case ProjectileResolution.Splash => Splash
      case ProjectileResolution.Lash => Lash
      case _ => None
    }
  }
}

object StandardInfantryResistance extends ResistanceSelection {
  def Direct : ProjectileCalculations.Form = InfantryHitResistance.Calculate
  def Splash : ProjectileCalculations.Form = InfantrySplashResistance.Calculate
  def Lash : ProjectileCalculations.Form = InfantryLashResistance.Calculate
  def Aggravated : ProjectileCalculations.Form = InfantryAggravatedResistance.Calculate
}

object StandardVehicleResistance extends ResistanceSelection {
  def Direct : ProjectileCalculations.Form = VehicleHitResistance.Calculate
  def Splash : ProjectileCalculations.Form = VehicleSplashResistance.Calculate
  def Lash : ProjectileCalculations.Form = VehicleLashResistance.Calculate
  def Aggravated : ProjectileCalculations.Form = VehicleAggravatedResistance.Calculate
}

//TODO temporary workaround?
object ResistanceSelectionByTarget {
  def apply(data : ResolvedProjectile) : ProjectileCalculations.Form = {
    (data.target match {
      case _ : PlayerSource =>
        StandardInfantryResistance
      case _ : VehicleSource =>
        StandardVehicleResistance
      case _ =>
        StandardVehicleResistance
    })(data)
  }
}


