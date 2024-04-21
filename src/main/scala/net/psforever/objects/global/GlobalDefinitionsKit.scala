// Copyright (c) 2024 PSForever
package net.psforever.objects.global

import net.psforever.objects.GlobalDefinitions

object GlobalDefinitionsKit {
  import GlobalDefinitions._

  /**
   * Initialize `KitDefinition` globals.
   */
  def init(): Unit = {
    medkit.Name = "medkit"

    super_medkit.Name = "super_medkit"

    super_armorkit.Name = "super_armorkit"

    super_staminakit.Name = "super_staminakit"
  }
}
