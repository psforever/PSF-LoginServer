// Copyright (c) 2024 PSForever
package net.psforever.objects.global

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.SpawnPoint
import net.psforever.types.LatticeBenefit

object GlobalDefinitionsBuilding {
  import GlobalDefinitions._

  /**
   * Initialize `BuildingDefinition` globals.
   */
  def init(): Unit = {
    amp_station.Name = "amp_station"
    amp_station.SOIRadius = 300
    amp_station.LatticeLinkBenefit = LatticeBenefit.AmpStation

    comm_station.Name = "comm_station"
    comm_station.SOIRadius = 300
    comm_station.LatticeLinkBenefit = LatticeBenefit.InterlinkFacility

    comm_station_dsp.Name = "comm_station_dsp"
    comm_station_dsp.SOIRadius = 300
    comm_station_dsp.LatticeLinkBenefit = LatticeBenefit.DropshipCenter

    cryo_facility.Name = "cryo_facility"
    cryo_facility.SOIRadius = 300
    cryo_facility.LatticeLinkBenefit = LatticeBenefit.BioLaboratory

    tech_plant.Name = "tech_plant"
    tech_plant.SOIRadius = 300
    tech_plant.LatticeLinkBenefit = LatticeBenefit.TechnologyPlant

    building.Name = "building"

    vanu_core.Name = "vanu_core"

    ground_bldg_a.Name = "ground_bldg_a"
    
    ground_bldg_b.Name = "ground_bldg_b"
    
    ground_bldg_c.Name = "ground_bldg_c"
    
    ground_bldg_d.Name = "ground_bldg_d"
    
    ground_bldg_e.Name = "ground_bldg_e"
    
    ground_bldg_f.Name = "ground_bldg_f"
    
    ground_bldg_g.Name = "ground_bldg_g"
    
    ground_bldg_h.Name = "ground_bldg_h"
    
    ground_bldg_i.Name = "ground_bldg_i"
    
    ground_bldg_j.Name = "ground_bldg_j"
    
    ground_bldg_z.Name = "ground_bldg_z"

    ceiling_bldg_a.Name = "ceiling_bldg_a"
    
    ceiling_bldg_b.Name = "ceiling_bldg_b"
    
    ceiling_bldg_c.Name = "ceiling_bldg_c"
    
    ceiling_bldg_d.Name = "ceiling_bldg_d"
    
    ceiling_bldg_e.Name = "ceiling_bldg_e"
    
    ceiling_bldg_f.Name = "ceiling_bldg_f"
    
    ceiling_bldg_g.Name = "ceiling_bldg_g"
    
    ceiling_bldg_h.Name = "ceiling_bldg_h"
    
    ceiling_bldg_i.Name = "ceiling_bldg_i"
    
    ceiling_bldg_j.Name = "ceiling_bldg_j"
    
    ceiling_bldg_z.Name = "ceiling_bldg_z"

    mainbase1.Name = "mainbase1"
    
    mainbase2.Name = "mainbase2"
    
    mainbase3.Name = "mainbase3"
    
    meeting_center_nc.Name = "meeting_center_nc"
    
    meeting_center_tr.Name = "meeting_center_tr"
    
    meeting_center_vs.Name = "meeting_center_vs"
    
    minibase1.Name = "minibase1"
    
    minibase2.Name = "minibase2"
    
    minibase3.Name = "minibase3"
    
    redoubt.Name = "redoubt"
    redoubt.SOIRadius = 187
    
    tower_a.Name = "tower_a"
    tower_a.SOIRadius = 50
    
    tower_b.Name = "tower_b"
    tower_b.SOIRadius = 50
    
    tower_c.Name = "tower_c"
    tower_c.SOIRadius = 50
    
    vanu_control_point.Name = "vanu_control_point"
    vanu_control_point.SOIRadius = 187
    
    vanu_vehicle_station.Name = "vanu_vehicle_station"
    vanu_vehicle_station.SOIRadius = 187
    
    hst.Name = "hst"
    hst.UseRadius = 44.96882005f
    hst.SOIRadius = 82
    hst.VehicleAllowance = true
    hst.NoWarp += dropship
    hst.NoWarp += galaxy_gunship
    hst.NoWarp += lodestar
    hst.NoWarp += aphelion_gunner
    hst.NoWarp += aphelion_flight
    hst.NoWarp += colossus_gunner
    hst.NoWarp += colossus_flight
    hst.NoWarp += peregrine_gunner
    hst.NoWarp += peregrine_flight
    hst.SpecificPointFunc = SpawnPoint.CavernGate(innerRadius = 6f)
    
    warpgate.Name = "warpgate"
    warpgate.UseRadius = 67.81070029f
    warpgate.SOIRadius = 302 //301.8713f
    warpgate.VehicleAllowance = true
    warpgate.SpecificPointFunc = SpawnPoint.Gate
    
    warpgate_cavern.Name = "warpgate_cavern"
    warpgate_cavern.UseRadius = 19.72639434f
    warpgate_cavern.SOIRadius = 41
    warpgate_cavern.VehicleAllowance = true
    warpgate_cavern.SpecificPointFunc = SpawnPoint.CavernGate(innerRadius = 4.5f)
    
    warpgate_small.Name = "warpgate_small"
    warpgate_small.UseRadius = 69.03687655f
    warpgate_small.SOIRadius = 103
    warpgate_small.VehicleAllowance = true
    warpgate_small.SpecificPointFunc = SpawnPoint.SmallGate(innerRadius = 27.60654127f, flightlessZOffset = 0.5f)

    bunker_gauntlet.Name = "bunker_gauntlet"
    
    bunker_lg.Name = "bunker_lg"
    
    bunker_sm.Name = "bunker_sm"

    orbital_building_nc.Name = "orbital_building_nc"
    
    orbital_building_tr.Name = "orbital_building_tr"
    
    orbital_building_vs.Name = "orbital_building_vs"
    
    VT_building_nc.Name = "VT_building_nc"
    
    VT_building_tr.Name = "VT_building_tr"
    
    VT_building_vs.Name = "VT_building_vs"
    
    vt_dropship.Name = "vt_dropship"
    
    vt_spawn.Name = "vt_spawn"
    
    vt_vehicle.Name = "vt_vehicle"
  }
}
