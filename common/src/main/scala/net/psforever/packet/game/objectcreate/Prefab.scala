// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

/**
  * A compilation of the common `*Data` objects that would be used for stock game objects.
  * Each function is named after the `ObjectClass` name (internal name) it creates.
  * No `Prefab` assumes empire allegiance or initial health.
  */
object Prefab {
  import net.psforever.packet.game.PlanetSideGUID
  import net.psforever.types.PlanetSideEmpire

  object Vehicle {
    def ams(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, driveState : DriveState.Value, matrix_guid : PlanetSideGUID, respawn_guid : PlanetSideGUID, term_a_guid : PlanetSideGUID, term_b_guid : PlanetSideGUID) : AMSData = {
      AMSData(CommonFieldData(loc, faction, 0), health, driveState, matrix_guid, respawn_guid, term_a_guid, term_b_guid)
    }

    def ant(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, driveState : DriveState.Value) : ANTData = {
      ANTData(CommonFieldData(loc, faction, 0), health, driveState)
    }

    def aurora(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo11_guid : PlanetSideGUID, ammo12_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo21_guid : PlanetSideGUID, ammo22_guid : PlanetSideGUID) : VehicleData = {
      VehicleData(CommonFieldData(loc, faction, 0), 0, health, 0, DriveState.State7, true, 0,
        Some(
          MountItem(ObjectClass.aurora_weapon_systema, weapon1_guid, 5,
            WeaponData(0x6, 0x8, 0, ObjectClass.fluxpod_ammo, ammo11_guid, 0, AmmoBoxData(0x8), ObjectClass.fluxpod_ammo, ammo12_guid, 1, AmmoBoxData(0x8))
          ) ::
            MountItem(ObjectClass.aurora_weapon_systemb, weapon2_guid, 6,
              WeaponData(0x6, 0x8, 0, ObjectClass.fluxpod_ammo, ammo21_guid, 0, AmmoBoxData(0x8), ObjectClass.fluxpod_ammo, ammo22_guid, 1, AmmoBoxData(0x8))
            ) :: Nil
        )
      )(2)
    }

    def battlewagon(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID, weapon3_guid : PlanetSideGUID, ammo3_guid : PlanetSideGUID, weapon4_guid : PlanetSideGUID, ammo4_guid : PlanetSideGUID) : VehicleData = {
      VehicleData(CommonFieldData(loc, faction, 0), 0, health, 0, DriveState.State7, true, 0,
        Some(
          MountItem(ObjectClass.battlewagon_weapon_systema, weapon1_guid, 5,
            WeaponData(0x6, 0x8, 0, ObjectClass.bullet_15mm, ammo1_guid, 0, AmmoBoxData(0x8))
          ) ::
            MountItem(ObjectClass.battlewagon_weapon_systemb, weapon2_guid, 6,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_15mm, ammo2_guid, 0, AmmoBoxData(0x8))
            ) ::
            MountItem(ObjectClass.battlewagon_weapon_systemc, weapon3_guid, 7,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_15mm, ammo3_guid, 0, AmmoBoxData(0x8))
            ) ::
            MountItem(ObjectClass.battlewagon_weapon_systemd, weapon4_guid, 8,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_15mm, ammo4_guid, 0, AmmoBoxData(0x8))
            ) :: Nil
        )
      )(4)
    }

    def fury(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon_guid : PlanetSideGUID, ammo_guid : PlanetSideGUID) : VehicleData = {
      VehicleData(CommonFieldData(loc, faction, 0), health,
        MountItem(ObjectClass.fury_weapon_systema, weapon_guid, 1,
          WeaponData(0x4, 0x8, ObjectClass.hellfire_ammo, ammo_guid, 0, AmmoBoxData(0x8))
        )
      )
    }

    def lightning(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID) : VehicleData = {
      VehicleData(CommonFieldData(loc, faction, 0), health,
        MountItem(ObjectClass.lightning_weapon_system, weapon_guid, 1,
          WeaponData(0x4, 0x8, 0, ObjectClass.bullet_75mm, ammo1_guid, 0, AmmoBoxData(0x0), ObjectClass.bullet_25mm, ammo2_guid, 1, AmmoBoxData(0x0))
        )
      )
    }

    def mediumtransport(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID): VehicleData = {
      VehicleData(CommonFieldData(loc, faction, 0), 0, health, 0, DriveState.State7, true, 0,
        Some(
          MountItem(ObjectClass.mediumtransport_weapon_systemA, weapon1_guid, 5,
            WeaponData(0x6, 0x8, ObjectClass.bullet_20mm, ammo1_guid, 0, AmmoBoxData(0x8))
          ) ::
            MountItem(ObjectClass.mediumtransport_weapon_systemB, weapon2_guid, 6,
              WeaponData(0x6, 0x8, ObjectClass.bullet_20mm, ammo2_guid, 0, AmmoBoxData(0x8))
            ) :: Nil
        )
      )(2)
    }

    def quadassault(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon_guid : PlanetSideGUID, ammo_guid : PlanetSideGUID) : VehicleData = {
      VehicleData(CommonFieldData(loc, faction, 0), health,
        MountItem(ObjectClass.quadassault_weapon_system, weapon_guid, 1,
          WeaponData(0x6, 0x8, ObjectClass.bullet_12mm, ammo_guid, 0, AmmoBoxData(0x8))
        )
      )
    }

    def quadstealth(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int) : VehicleData = {
      VehicleData(CommonFieldData(loc, faction, 0), 0, health, 0, DriveState.State7, false, 0)(0)
    }

    def switchblade(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, driveState : DriveState.Value, weapon_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID) : Vehicle2Data = {
      Vehicle2Data(CommonFieldData(loc, faction, 0), health, driveState,
        MountItem(ObjectClass.scythe, weapon_guid, 1,
          WeaponData(0x6, 0x8, 0, ObjectClass.ancient_ammo_vehicle, ammo1_guid, 0, AmmoBoxData(0x8), ObjectClass.ancient_ammo_vehicle, ammo2_guid, 1, AmmoBoxData(0x8))
        )
      )
    }

    def threemanheavybuggy(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID) : VehicleData = {
      VehicleData(CommonFieldData(loc, faction, 0), 0, health, 0, DriveState.State7, true, 0,
        Some(
          MountItem(ObjectClass.chaingun_p, weapon1_guid, 3,
            WeaponData(0x6, 0x8, 0, ObjectClass.bullet_12mm, ammo1_guid, 0, AmmoBoxData(0x8))
          ) ::
            MountItem(ObjectClass.grenade_launcher_marauder, weapon2_guid, 4,
              WeaponData(0x6, 0x8, 0, ObjectClass.heavy_grenade_mortar, ammo2_guid, 0, AmmoBoxData(0x8))
            ) :: Nil
        )
      )(2)
    }

    def thunderer(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID) : VehicleData = {
      VehicleData(CommonFieldData(loc, faction, 0), 0, health, 0, DriveState.State7, true, 0,
        Some(
          MountItem(ObjectClass.thunderer_weapon_systema, weapon1_guid, 5,
            WeaponData(0x6, 0x8, 0, ObjectClass.gauss_cannon_ammo, ammo1_guid, 0, AmmoBoxData(0x8))
          ) ::
            MountItem(ObjectClass.thunderer_weapon_systemb, weapon2_guid, 6,
              WeaponData(0x6, 0x8, 0, ObjectClass.gauss_cannon_ammo, ammo2_guid, 0, AmmoBoxData(0x8))
            ) :: Nil
        )
      )(2)
    }

    def two_man_assault_buggy(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon_guid : PlanetSideGUID, ammo_guid : PlanetSideGUID) : VehicleData = {
      VehicleData(CommonFieldData(loc, faction, 0), health,
        MountItem(ObjectClass.chaingun_p, weapon_guid, 2,
          WeaponData(0x6, 0x8, ObjectClass.bullet_12mm, ammo_guid, 0, AmmoBoxData(0x8))
        )
      )
    }

    def twomanheavybuggy(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon_guid : PlanetSideGUID, ammo_guid : PlanetSideGUID) : VehicleData = {
      VehicleData(CommonFieldData(loc, faction, 0), health,
        MountItem(ObjectClass.advanced_missile_launcher_t, weapon_guid, 2,
          WeaponData(0x6, 0x8, 0, ObjectClass.firebird_missile, ammo_guid, 0, AmmoBoxData(0x8))
        )
      )
    }

    def twomanhoverbuggy(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon_guid : PlanetSideGUID, ammo_guid : PlanetSideGUID) : VehicleData = {
      VehicleData(CommonFieldData(loc, faction, 0), health,
        MountItem(ObjectClass.flux_cannon_thresher, weapon_guid, 2,
          WeaponData(0x6, 0x8, 0, ObjectClass.flux_cannon_thresher_battery, ammo_guid, 0, AmmoBoxData(0x8))
        )
      )
    }
  }
}
