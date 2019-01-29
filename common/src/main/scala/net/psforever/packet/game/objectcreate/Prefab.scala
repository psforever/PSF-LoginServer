// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{DriveState, PlanetSideEmpire}

/**
  * A compilation of the common `*Data` objects that would be used for stock game objects.
  * Each function is named after the `ObjectClass` name (internal name) it creates.
  * No `Prefab` assumes empire allegiance or initial health.
  * This file is more useful for reference, rather than application.
  */
object Prefab {
  object Vehicle {
    def ams(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, driveState : DriveState.Value, matrix_guid : PlanetSideGUID, respawn_guid : PlanetSideGUID, term_a_guid : PlanetSideGUID, term_b_guid : PlanetSideGUID) : VehicleData = {
      VehicleData(loc, CommonFieldData(faction, 0), health, driveState, false, UtilityVehicleData(0),
        Some(InventoryData(List(
          InternalSlot(ObjectClass.matrix_terminalc, matrix_guid, 1, CommonFieldData(faction)(false)),
          InternalSlot(ObjectClass.ams_respawn_tube, respawn_guid, 2, CommonFieldData(faction)(false)),
          InternalSlot(ObjectClass.order_terminala, term_a_guid, 3, CommonFieldData(faction)(false)),
          InternalSlot(ObjectClass.order_terminalb, term_b_guid, 4, CommonFieldData(faction)(false))
        )))
      )
    }

    def ant(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, driveState : DriveState.Value) : VehicleData = {
      VehicleData(loc, CommonFieldData(faction, 0), health, driveState, false, UtilityVehicleData(0), None)
    }

    def apc_nc(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID, weapon3_guid : PlanetSideGUID, ammo3_guid : PlanetSideGUID, weapon4_guid : PlanetSideGUID, ammo4_guid : PlanetSideGUID, weapon5_guid : PlanetSideGUID, ammo5_guid : PlanetSideGUID, weapon6_guid : PlanetSideGUID, ammo6_guid : PlanetSideGUID) : VehicleData = {
        //VehicleData(loc, CommonFieldData(faction, 2), 0, health, false, false, DriveState.State7, true, false, false, None,
      VehicleData(loc, CommonFieldData(faction, 2), health, DriveState.State7, false,
        Some(InventoryData(
          InventoryItemData(ObjectClass.apc_weapon_systemc_nc, weapon1_guid, 11,
            WeaponData(0x6, 0x8, 0, ObjectClass.bullet_20mm, ammo1_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) ::
            InventoryItemData(ObjectClass.apc_weapon_systemb, weapon2_guid, 12,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_75mm, ammo2_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.apc_weapon_systema, weapon3_guid, 13,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_75mm, ammo3_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.apc_weapon_systemd_nc, weapon4_guid, 14,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_20mm, ammo4_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.apc_ballgun_r, weapon5_guid, 15,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_12mm, ammo5_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.apc_ballgun_l, weapon6_guid, 16,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_12mm, ammo6_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) :: Nil
        ))
      )
    }

    def apc_tr(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID, weapon3_guid : PlanetSideGUID, ammo3_guid : PlanetSideGUID, weapon4_guid : PlanetSideGUID, ammo4_guid : PlanetSideGUID, weapon5_guid : PlanetSideGUID, ammo5_guid : PlanetSideGUID, weapon6_guid : PlanetSideGUID, ammo6_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 2), 0, health, false, false, DriveState.State7, true, false, false, None,
      VehicleData(loc, CommonFieldData(faction, 2), health, DriveState.State7, false,
        Some(InventoryData(
          InventoryItemData(ObjectClass.apc_weapon_systemc_tr, weapon1_guid, 11,
            WeaponData(0x6, 0x8, 0, ObjectClass.bullet_15mm, ammo1_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) ::
            InventoryItemData(ObjectClass.apc_weapon_systemb, weapon2_guid, 12,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_75mm, ammo2_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.apc_weapon_systema, weapon3_guid, 13,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_75mm, ammo3_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.apc_weapon_systemd_tr, weapon4_guid, 14,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_15mm, ammo4_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.apc_ballgun_r, weapon5_guid, 15,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_12mm, ammo5_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.apc_ballgun_l, weapon6_guid, 16,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_12mm, ammo6_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) :: Nil
        ))
      )
    }

    def apc_vs(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID, weapon3_guid : PlanetSideGUID, ammo3_guid : PlanetSideGUID, weapon4_guid : PlanetSideGUID, ammo4_guid : PlanetSideGUID, weapon5_guid : PlanetSideGUID, ammo5_guid : PlanetSideGUID, weapon6_guid : PlanetSideGUID, ammo6_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 2), 0, health, false, false, DriveState.State7, true, false, false, None,
      VehicleData(loc, CommonFieldData(faction, 2), health, DriveState.State7, false,
        Some(InventoryData(
          InventoryItemData(ObjectClass.apc_weapon_systemc_vs, weapon1_guid, 11,
            WeaponData(0x6, 0x8, 0, ObjectClass.flux_cannon_thresher_battery, ammo1_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) ::
            InventoryItemData(ObjectClass.apc_weapon_systemb, weapon2_guid, 12,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_75mm, ammo2_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.apc_weapon_systema, weapon3_guid, 13,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_75mm, ammo3_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.apc_weapon_systemd_vs, weapon4_guid, 14,
              WeaponData(0x6, 0x8, 0, ObjectClass.flux_cannon_thresher_battery, ammo4_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.apc_ballgun_r, weapon5_guid, 15,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_12mm, ammo5_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.apc_ballgun_l, weapon6_guid, 16,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_12mm, ammo6_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) :: Nil
        ))
      )
    }

    def aurora(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo11_guid : PlanetSideGUID, ammo12_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo21_guid : PlanetSideGUID, ammo22_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 0), 0, health, false, false, DriveState.State7, true, false, false, None,
        VehicleData(loc, CommonFieldData(faction, 2), health, DriveState.State7, false,
          Some(InventoryData(
          InventoryItemData(ObjectClass.aurora_weapon_systema, weapon1_guid, 5,
            WeaponData(0x6, 0x8, 0, ObjectClass.fluxpod_ammo, ammo11_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) ::
            InventoryItemData(ObjectClass.aurora_weapon_systemb, weapon2_guid, 6,
              WeaponData(0x6, 0x8, 0, ObjectClass.fluxpod_ammo, ammo21_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) :: Nil
        ))
      )
    }

    def battlewagon(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID, weapon3_guid : PlanetSideGUID, ammo3_guid : PlanetSideGUID, weapon4_guid : PlanetSideGUID, ammo4_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 0), 0, health, false, false, DriveState.State7, true, false, false, None,
      VehicleData(loc, CommonFieldData(faction, 2), health, DriveState.State7, false,
        Some(InventoryData(
          InventoryItemData(ObjectClass.battlewagon_weapon_systema, weapon1_guid, 5,
            WeaponData(0x6, 0x8, 0, ObjectClass.bullet_15mm, ammo1_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) ::
            InventoryItemData(ObjectClass.battlewagon_weapon_systemb, weapon2_guid, 6,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_15mm, ammo2_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.battlewagon_weapon_systemc, weapon3_guid, 7,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_15mm, ammo3_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.battlewagon_weapon_systemd, weapon4_guid, 8,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_15mm, ammo4_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) :: Nil
        ))
      )
    }

    def dropship(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID, weapon3_guid : PlanetSideGUID, ammo3_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 2), 0, health, false, false, DriveState.State7, true, false, false, Some(VariantVehicleData(0)),
      VehicleData(loc, CommonFieldData(faction, 2), health, DriveState.State7, false, VariantVehicleData(0),
        Some(InventoryData(
          InventoryItemData(ObjectClass.cannon_dropship_20mm, weapon1_guid, 12,
            WeaponData(0x6, 0x8, 0, ObjectClass.bullet_20mm, ammo1_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) ::
            InventoryItemData(ObjectClass.cannon_dropship_20mm, weapon2_guid, 13,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_20mm, ammo2_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.dropship_rear_turret, weapon3_guid, 14,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_20mm, ammo3_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) :: Nil
        ))
      )
    }

    def flail(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon_guid : PlanetSideGUID, ammo_guid : PlanetSideGUID, terminal_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 2), 0, health, false, false, DriveState.Mobile, false, false, false, Some(VariantVehicleData(0)),
      VehicleData(loc, CommonFieldData(faction, 2), health, DriveState.Mobile, false, VariantVehicleData(0),
        Some(InventoryData(
          InventoryItemData(ObjectClass.flail_weapon, weapon_guid, 1,
            WeaponData(0x6, 0x8, 0, ObjectClass.ancient_ammo_vehicle, ammo_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) ::
            InventoryItemData(ObjectClass.targeting_laser_dispenser, terminal_guid, 2, CommonFieldData(faction, 2)(false)) :: Nil
        ))
      )
    }

    def fury(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon_guid : PlanetSideGUID, ammo_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 0), 0, health, false, false, DriveState.State7, true, false, false, None,
      VehicleData(loc, CommonFieldData(faction, 0), health, DriveState.State7, false,
        Some(InventoryData(
          InventoryItemData(ObjectClass.fury_weapon_systema, weapon_guid, 1,
            WeaponData(0x4, 0x8, ObjectClass.hellfire_ammo, ammo_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) :: Nil
        ))
      )
    }

    def galaxy_gunship(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID, weapon3_guid : PlanetSideGUID, ammo3_guid : PlanetSideGUID, weapon4_guid : PlanetSideGUID, ammo4_guid : PlanetSideGUID, weapon5_guid : PlanetSideGUID, ammo5_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 2), 0, health, false, false, DriveState.State7, true, false, false, Some(VariantVehicleData(0)),
      VehicleData(loc, CommonFieldData(faction, 2), health, DriveState.State7, false, VariantVehicleData(0),
        Some(InventoryData(
          InventoryItemData(ObjectClass.galaxy_gunship_cannon, weapon1_guid, 6,
            WeaponData(0x6, 0x8, 0, ObjectClass.heavy_grenade_mortar, ammo1_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) ::
            InventoryItemData(ObjectClass.galaxy_gunship_cannon, weapon2_guid, 7,
              WeaponData(0x6, 0x8, 0, ObjectClass.heavy_grenade_mortar, ammo2_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.galaxy_gunship_tailgun, weapon3_guid, 8,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_35mm, ammo3_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.galaxy_gunship_gun, weapon4_guid, 9,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_35mm, ammo4_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.galaxy_gunship_gun, weapon5_guid, 10,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_35mm, ammo5_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) :: Nil
        ))
      )
    }

    def liberator(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID, ammo3_guid : PlanetSideGUID, weapon3_guid : PlanetSideGUID, ammo4_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 2), 0, health, false, false, DriveState.State7, true, false, false, Some(VariantVehicleData(0)),
      VehicleData(loc, CommonFieldData(faction, 2), health, DriveState.State7, false, VariantVehicleData(0),
        Some(InventoryData(
          InventoryItemData(ObjectClass.liberator_weapon_system, weapon1_guid, 3,
            WeaponData(0x6, 0x8, 0, ObjectClass.bullet_35mm, ammo1_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) ::
            InventoryItemData(ObjectClass.liberator_bomb_bay, weapon2_guid, 4,
              WeaponData(0x6, 0x8, 0, ObjectClass.liberator_bomb, ammo2_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2), ObjectClass.liberator_bomb, ammo3_guid, 1, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.liberator_25mm_cannon, weapon3_guid, 5,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_25mm, ammo4_guid, 0 ,CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) :: Nil
        ))
      )
    }

    def lightgunship(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 2), 0, health, false, false, DriveState.Mobile, false, false, false, Some(VariantVehicleData(0)),
      VehicleData(loc, CommonFieldData(faction, 2), health, DriveState.Mobile, false, VariantVehicleData(0),
        Some(InventoryData(
          InventoryItemData(ObjectClass.lightgunship_weapon_system, weapon_guid, 1,
            WeaponData(0x6, 0x8, 0, ObjectClass.bullet_20mm, ammo1_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2), ObjectClass.reaver_rocket, ammo2_guid,1, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) :: Nil
        ))
      )
    }

    def lightning(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 0), 0, health, false, false, DriveState.State7, true, false, false, None,
      VehicleData(loc, CommonFieldData(faction, 0), health, DriveState.State7, false,
        Some(InventoryData(
          InventoryItemData(ObjectClass.lightning_weapon_system, weapon_guid, 1,
            WeaponData(0x4, 0x8, 0, ObjectClass.bullet_75mm, ammo1_guid, 0, CommonFieldData()(false), ObjectClass.bullet_12mm, ammo2_guid, 1, CommonFieldData()(false))
          ) :: Nil)
        )
      )
    }

    def lodestar(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, repair1_guid : PlanetSideGUID, repair2_guid : PlanetSideGUID, veh_rearm1_guid : PlanetSideGUID, veh_rearm2_guid : PlanetSideGUID, bfr_rearm1_guid : PlanetSideGUID, bfr_rearm2_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 2), 0, health, false, false, DriveState.State7, true, false, false, Some(VariantVehicleData(0)),
      VehicleData(loc, CommonFieldData(faction, 2), health, DriveState.State7, false, VariantVehicleData(0),
        Some(InventoryData(List(
          InternalSlot(ObjectClass.lodestar_repair_terminal, repair1_guid, 2, CommonFieldData(faction, 2)(false)),
          InternalSlot(ObjectClass.lodestar_repair_terminal, repair2_guid, 3, CommonFieldData(faction, 2)(false)),
          InternalSlot(ObjectClass.multivehicle_rearm_terminal, veh_rearm1_guid, 4, CommonFieldData(faction, 2)(false)),
          InternalSlot(ObjectClass.multivehicle_rearm_terminal, veh_rearm2_guid, 5, CommonFieldData(faction, 2)(false)),
          InternalSlot(ObjectClass.bfr_rearm_terminal, bfr_rearm1_guid, 6, CommonFieldData(faction, 2)(false)),
          InternalSlot(ObjectClass.bfr_rearm_terminal, bfr_rearm2_guid, 7, CommonFieldData(faction, 2)(false))
        )))
      )
    }

    def magrider(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 2), 0, health, false, false, DriveState.State7, true, false, false, None,
      VehicleData(loc, CommonFieldData(faction, 2), health, DriveState.State7, false,
        Some(InventoryData(
          InventoryItemData(ObjectClass.particle_beam_magrider, weapon1_guid, 2,
            WeaponData(0x6, 0x8, 0, ObjectClass.pulse_battery, ammo1_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) ::
            InventoryItemData(ObjectClass.heavy_rail_beam_magrider, weapon2_guid, 3,
              WeaponData(0x6, 0x8, 0, ObjectClass.heavy_rail_beam_battery, ammo2_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) :: Nil
        ))
      )
    }

    def mediumtransport(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID): VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 0), 0, health, false, false, DriveState.State7, true, false, false, None,
      VehicleData(loc, CommonFieldData(faction, 0), health, DriveState.State7, false,
        Some(InventoryData(
          InventoryItemData(ObjectClass.mediumtransport_weapon_systemA, weapon1_guid, 5,
            WeaponData(0x6, 0x8, ObjectClass.bullet_20mm, ammo1_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) ::
            InventoryItemData(ObjectClass.mediumtransport_weapon_systemB, weapon2_guid, 6,
              WeaponData(0x6, 0x8, ObjectClass.bullet_20mm, ammo2_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) :: Nil
        ))
      )
    }

    def mosquito(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon_guid : PlanetSideGUID, ammo_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 0), 0, health, false, false, DriveState.State7, true, false, false, Some(VariantVehicleData(0)),
      VehicleData(loc, CommonFieldData(faction, 0), health, DriveState.State7, false, VariantVehicleData(0),
        Some(InventoryData(
          InventoryItemData(ObjectClass.rotarychaingun_mosquito, weapon_guid, 1,
            WeaponData(0x6, 0x8, ObjectClass.bullet_12mm, ammo_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) :: Nil
        ))
      )
    }

    def phantasm(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 2), 0, health, false, false, DriveState.State7, true, false, false, Some(VariantVehicleData(0)), None)(VehicleFormat.Variant)
      VehicleData(loc, CommonFieldData(faction, 2), health, DriveState.State7, false, VariantVehicleData(0), None)
    }

    def prowler(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 2), 0, health, false, false, DriveState.State7, true, false, false, None,
      VehicleData(loc, CommonFieldData(faction, 2), health, DriveState.State7, false,
        Some(InventoryData(
          InventoryItemData(ObjectClass.prowler_weapon_systemA, weapon1_guid, 3,
            WeaponData(0x6, 0x8, 0, ObjectClass.bullet_105mm, ammo1_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) ::
            InventoryItemData(ObjectClass.prowler_weapon_systemB, weapon2_guid, 4,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_15mm, ammo2_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) :: Nil
        ))
      )
    }

    def quadassault(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon_guid : PlanetSideGUID, ammo_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 0), 0, health, false, false, DriveState.State7, true, false, false, None,
      VehicleData(loc, CommonFieldData(faction, 0), health, DriveState.State7, false,
        Some(InventoryData(
          InventoryItemData(ObjectClass.quadassault_weapon_system, weapon_guid, 1,
            WeaponData(0x6, 0x8, ObjectClass.bullet_12mm, ammo_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) :: Nil
        ))
      )
    }

    def quadstealth(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 0), 0, health, false, false, DriveState.State7, false, false, false, None, None)(VehicleFormat.Normal)
      VehicleData(loc, CommonFieldData(faction, 0), health, DriveState.State7, false, None)
    }

    def router(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, terminal_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 2), 0, health, false, false, DriveState.Mobile, false, false, false, Some(VariantVehicleData(0)),
      VehicleData(loc, CommonFieldData(faction, 2), health, DriveState.Mobile, false, VariantVehicleData(0),
        Some(InventoryData(
          InventoryItemData(ObjectClass.teleportpad_terminal, terminal_guid, 1, CommonFieldData(faction, 2)(false)) :: Nil
        ))
      )
    }

    def skyguard(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 2), 0, health, false, false, DriveState.State7, true, false, false, None,
      VehicleData(loc, CommonFieldData(faction, 2), health, DriveState.State7, false,
        Some(InventoryData(
          InventoryItemData(ObjectClass.skyguard_weapon_system, weapon_guid, 2,
            WeaponData(0x6, 0x8, 0, ObjectClass.skyguard_flak_cannon_ammo, ammo1_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2), ObjectClass.bullet_12mm, ammo2_guid, 1, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) :: Nil
        ))
      )
    }

    def switchblade(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, driveState : DriveState.Value, weapon_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 0), 0, health, false, false, DriveState.State7, true, false, false, Some(VariantVehicleData(0)),
      VehicleData(loc, CommonFieldData(faction, 0), health, DriveState.State7, false, VariantVehicleData(0),
        Some(InventoryData(
          InventoryItemData(ObjectClass.scythe, weapon_guid, 1,
            WeaponData(0x6, 0x8, 0, ObjectClass.ancient_ammo_vehicle, ammo1_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2), ObjectClass.ancient_ammo_vehicle, ammo2_guid, 1, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) :: Nil
        ))
      )
    }

    def threemanheavybuggy(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 0), 0, health, false, false, DriveState.State7, true, false, false, None,
      VehicleData(loc, CommonFieldData(faction, 0), health, DriveState.State7, false,
        Some(InventoryData(
          InventoryItemData(ObjectClass.chaingun_p, weapon1_guid, 3,
            WeaponData(0x6, 0x8, 0, ObjectClass.bullet_12mm, ammo1_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) ::
            InventoryItemData(ObjectClass.grenade_launcher_marauder, weapon2_guid, 4,
              WeaponData(0x6, 0x8, 0, ObjectClass.heavy_grenade_mortar, ammo2_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) :: Nil
        ))
      )
    }

    def thunderer(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 0), 0, health, false, false, DriveState.State7, true, false, false, None,
      VehicleData(loc, CommonFieldData(faction, 0), health, DriveState.State7, false,
        Some(InventoryData(
          InventoryItemData(ObjectClass.thunderer_weapon_systema, weapon1_guid, 5,
            WeaponData(0x6, 0x8, 0, ObjectClass.gauss_cannon_ammo, ammo1_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) ::
            InventoryItemData(ObjectClass.thunderer_weapon_systemb, weapon2_guid, 6,
              WeaponData(0x6, 0x8, 0, ObjectClass.gauss_cannon_ammo, ammo2_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) :: Nil
        ))
      )
    }

    def two_man_assault_buggy(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon_guid : PlanetSideGUID, ammo_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 0), 0, health, false, false, DriveState.State7, true, false, false, None,
      VehicleData(loc, CommonFieldData(faction, 0), health, DriveState.State7, false,
        Some(InventoryData(
          InventoryItemData(ObjectClass.chaingun_p, weapon_guid, 2,
            WeaponData(0x6, 0x8, ObjectClass.bullet_12mm, ammo_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) :: Nil
        ))
      )
    }

    def twomanheavybuggy(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon_guid : PlanetSideGUID, ammo_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 0), 0, health, false, false, DriveState.State7, true, false, false, None,
      VehicleData(loc, CommonFieldData(faction, 0), health, DriveState.State7, false,
        Some(InventoryData(
          InventoryItemData(ObjectClass.advanced_missile_launcher_t, weapon_guid, 2,
            WeaponData(0x6, 0x8, 0, ObjectClass.firebird_missile, ammo_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) :: Nil
        ))
      )
    }

    def twomanhoverbuggy(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon_guid : PlanetSideGUID, ammo_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 0), 0, health, false, false, DriveState.State7, true, false, false, None,
      VehicleData(loc, CommonFieldData(faction, 0), health, DriveState.State7, false,
        Some(InventoryData(
          InventoryItemData(ObjectClass.flux_cannon_thresher, weapon_guid, 2,
            WeaponData(0x6, 0x8, 0, ObjectClass.flux_cannon_thresher_battery, ammo_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) :: Nil
        ))
      )
    }

    def vanguard(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 2), 0, health, false, false, DriveState.State7, true, false, false, None,
      VehicleData(loc, CommonFieldData(faction, 2), health, DriveState.State7, false,
        Some(InventoryData(
          InventoryItemData(ObjectClass.vanguard_weapon_system, weapon_guid, 2,
            WeaponData(0x6, 0x8, 0, ObjectClass.bullet_150mm, ammo1_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2), ObjectClass.bullet_20mm, ammo2_guid, 1, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) :: Nil
        ))
      )
    }

    def vulture(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon1_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, weapon2_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID, weapon3_guid : PlanetSideGUID, ammo3_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 2), 0, health, false, false, DriveState.State7, true, false, false, Some(VariantVehicleData(0)),
      VehicleData(loc, CommonFieldData(faction, 2), health, DriveState.State7, false, VariantVehicleData(0),
        Some(InventoryData(
          InventoryItemData(ObjectClass.vulture_nose_weapon_system, weapon1_guid, 3,
            WeaponData(0x6, 0x8, 0, ObjectClass.bullet_35mm, ammo1_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) ::
            InventoryItemData(ObjectClass.vulture_bomb_bay, weapon2_guid, 4,
              WeaponData(0x6, 0x8, 0, ObjectClass.liberator_bomb, ammo2_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) ::
            InventoryItemData(ObjectClass.vulture_tail_cannon, weapon3_guid, 5,
              WeaponData(0x6, 0x8, 0, ObjectClass.bullet_25mm, ammo3_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
            ) :: Nil
        ))
      )
    }

    def wasp(loc : PlacementData, faction : PlanetSideEmpire.Value, health : Int, weapon_guid : PlanetSideGUID, ammo1_guid : PlanetSideGUID, ammo2_guid : PlanetSideGUID) : VehicleData = {
      //VehicleData(loc, CommonFieldData(faction, 0), 0, health, false, false, DriveState.Mobile, false, false, false, Some(VariantVehicleData(0)),
      VehicleData(loc, CommonFieldData(faction, 0), health, DriveState.Mobile, false, VariantVehicleData(0),
        Some(InventoryData(
          InventoryItemData(ObjectClass.wasp_weapon_system, weapon_guid, 1,
            WeaponData(0x6, 0x8, 0, ObjectClass.wasp_gun_ammo, ammo1_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2), ObjectClass.wasp_rocket_ammo, ammo2_guid, 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
          ) :: Nil
        ))
      )
    }
  }
}
