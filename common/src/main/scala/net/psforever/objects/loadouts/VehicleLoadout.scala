// Copyright (c) 2017 PSForever
package net.psforever.objects.loadouts

import net.psforever.objects.definition._

final case class VehicleLoadout(label : String,
                                visible_slots : List[Loadout.SimplifiedEntry],
                                inventory : List[Loadout.SimplifiedEntry],
                                vehicle_definition : VehicleDefinition) extends Loadout(label, visible_slots, inventory) {
  def Definition : VehicleDefinition = vehicle_definition
}