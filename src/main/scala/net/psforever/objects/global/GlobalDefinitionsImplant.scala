// Copyright (c) 2024 PSForever
package net.psforever.objects.global

import net.psforever.objects.GlobalDefinitions
import net.psforever.types.ExoSuitType

object GlobalDefinitionsImplant {
  import GlobalDefinitions._

  /**
   * Initialize `ImplantDefinition` globals.
   */
  def init(): Unit = {
    advanced_regen.Name = "advanced_regen"
    advanced_regen.InitializationDuration = 120
    advanced_regen.StaminaCost = 2
    advanced_regen.CostIntervalDefault = 500

    targeting.Name = "targeting"
    targeting.InitializationDuration = 60

    audio_amplifier.Name = "audio_amplifier"
    audio_amplifier.InitializationDuration = 60
    audio_amplifier.StaminaCost = 1
    audio_amplifier.CostIntervalDefault = 1000

    darklight_vision.Name = "darklight_vision"
    darklight_vision.InitializationDuration = 60
    darklight_vision.ActivationStaminaCost = 3
    darklight_vision.StaminaCost = 1
    darklight_vision.CostIntervalDefault = 500

    melee_booster.Name = "melee_booster"
    melee_booster.InitializationDuration = 120
    melee_booster.StaminaCost = 10

    personal_shield.Name = "personal_shield"
    personal_shield.InitializationDuration = 120
    personal_shield.StaminaCost = 1
    personal_shield.CostIntervalDefault = 600

    range_magnifier.Name = "range_magnifier"
    range_magnifier.InitializationDuration = 60

    second_wind.Name = "second_wind"
    second_wind.InitializationDuration = 180

    silent_run.Name = "silent_run"
    silent_run.InitializationDuration = 90
    silent_run.StaminaCost = 1
    silent_run.CostIntervalDefault = 333
    silent_run.CostIntervalByExoSuitHashMap(ExoSuitType.Infiltration) = 1000

    surge.Name = "surge"
    surge.InitializationDuration = 90
    surge.StaminaCost = 1
    surge.CostIntervalDefault = 1000
    surge.CostIntervalByExoSuitHashMap(ExoSuitType.Agile) = 500
    surge.CostIntervalByExoSuitHashMap(ExoSuitType.Reinforced) = 333
  }
}
