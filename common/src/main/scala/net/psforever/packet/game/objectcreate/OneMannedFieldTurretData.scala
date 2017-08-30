// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * A representation of the player-mountable large field turrets deployed using an advanced adaptive construction engine.<br>
  * <br>
  * Field turrets are divided into the turret base, the mounted turret weapon, and the turret's ammunition.
  * The ammunition is always the same regardless of which faction owns the turret.
  * Turret bases and turret weapons are generally paired by the faction.<br>
  * <br>
  * If the turret has no `health`, it is rendered as destroyed.
  * If the turret has no internal weapon, it is safest rendered as destroyed.
  * Trying to fire a turret with no internal weapon will soft-lock the PlanetSide client.
  * @param deploy data common to objects spawned by the (advanced) adaptive construction engine
  * @param health the amount of health the object has, as a percentage of a filled bar
  * @param internals data regarding the mountable weapon
  */
final case class OneMannedFieldTurretData(deploy : CommonFieldData,
                                          health : Int,
                                          internals : Option[InternalSlot] = None
                                         ) extends ConstructorData {
  override def bitsize : Long = {
    val deploySize = deploy.bitsize
    val internalSize = if(internals.isDefined) { CommonFieldData.internalWeapon_bitsize + internals.get.bitsize } else { 0L }
    38L + deploySize + internalSize //16u + 8u + 8u + 2u + 4u
  }
}

object OneMannedFieldTurretData extends Marshallable[OneMannedFieldTurretData] {
  /**
    * Overloaded constructor that mandates information about the internal weapon of the field turret.
    * @param deploy data common to objects spawned by the (advanced) adaptive construction engine
    * @param health the amount of health the object has, as a percentage of a filled bar
    * @param internals data regarding the mountable weapon
    * @return a `OneMannedFieldTurretData` object
    */
  def apply(deploy : CommonFieldData, health : Int, internals : InternalSlot) : OneMannedFieldTurretData =
    new OneMannedFieldTurretData(deploy, health, Some(internals))

  /**
    * Prefabricated weapon data for a weaponless field turret mount (`portable_manned_turret`).
    * @param wep_guid the uid to assign to the weapon
    * @param wep_unk1 na;
    *                used by `WeaponData`
    *
    * @param wep_unk2 na;
    *                used by `WeaponData`
    * @param ammo_guid the uid to assign to the ammo
    * @param ammo_unk na;
    *                 used by `AmmoBoxData`
    * @return an `InternalSlot` object
    */
  def generic(wep_guid : PlanetSideGUID, wep_unk1 : Int, wep_unk2 : Int, ammo_guid : PlanetSideGUID, ammo_unk : Int) : InternalSlot =
    InternalSlot(ObjectClass.energy_gun, wep_guid, 1,
      WeaponData(wep_unk1, wep_unk2, ObjectClass.energy_gun_ammo, ammo_guid, 0,
        AmmoBoxData(ammo_unk)
      )
    )

  /**
    * Prefabricated weapon data for the Terran Republic field turret, the Avenger (`portable_manned_turret_tr`).
    * @param wep_guid the uid to assign to the weapon
    * @param wep_unk1 na;
    *                used by `WeaponData`
    *
    * @param wep_unk2 na;
    *                used by `WeaponData`
    * @param ammo_guid the uid to assign to the ammo
    * @param ammo_unk na;
    *                 used by `AmmoBoxData`
    * @return an `InternalSlot` object
    */
  def avenger(wep_guid : PlanetSideGUID, wep_unk1 : Int, wep_unk2 : Int, ammo_guid : PlanetSideGUID, ammo_unk : Int) : InternalSlot =
    InternalSlot(ObjectClass.energy_gun_tr, wep_guid, 1,
      WeaponData(wep_unk1, wep_unk2, ObjectClass.energy_gun_ammo, ammo_guid, 0,
        AmmoBoxData(ammo_unk)
      )
  )

  /**
    * Prefabricated weapon data for the New Conglomerate field turret, the Osprey (`portable_manned_turret_vnc`).
    * @param wep_guid the uid to assign to the weapon
    * @param wep_unk1 na;
    *                used by `WeaponData`
    * @param wep_unk2 na;
    *                used by `WeaponData`
    * @param ammo_guid the uid to assign to the ammo
    * @param ammo_unk na;
    *                 used by `AmmoBoxData`
    * @return an `InternalSlot` object
    */
  def osprey(wep_guid : PlanetSideGUID, wep_unk1 : Int, wep_unk2 : Int, ammo_guid : PlanetSideGUID, ammo_unk : Int) : InternalSlot =
    InternalSlot(ObjectClass.energy_gun_nc, wep_guid, 1,
      WeaponData(wep_unk1, wep_unk2, ObjectClass.energy_gun_ammo, ammo_guid, 0,
        AmmoBoxData(ammo_unk)
      )
    )

  /**
    * Prefabricated weapon data for the Vanu Soveriegnty field turret, the Orion (`portable_manned_turret_vs`).
    * @param wep_guid the uid to assign to the weapon
    * @param wep_unk1 na;
    *                used by `WeaponData`
    * @param wep_unk2 na;
    *                used by `WeaponData`
    * @param ammo_guid the uid to assign to the ammo
    * @param ammo_unk na;
    *                 used by `AmmoBoxData`
    * @return an `InternalSlot` object
    */
  def orion(wep_guid : PlanetSideGUID, wep_unk1 : Int, wep_unk2 : Int, ammo_guid : PlanetSideGUID, ammo_unk : Int) : InternalSlot =
    InternalSlot(ObjectClass.energy_gun_vs, wep_guid, 1,
      WeaponData(wep_unk1, wep_unk2, ObjectClass.energy_gun_ammo, ammo_guid, 0,
        AmmoBoxData(ammo_unk)
      )
    )

  implicit val codec : Codec[OneMannedFieldTurretData] = (
    ("deploy" | CommonFieldData.codec) ::
      bool ::
      PlanetSideGUID.codec :: //hoist/extract with the CommonFieldData above
      bool ::
      ("health" | uint8L) ::
      uint2L ::
      uint8L ::
      bool ::
      optional(bool, "internals" | CommonFieldData.internalWeaponCodec)
    ).exmap[OneMannedFieldTurretData] (
    {
      case deploy :: false :: player :: false :: health :: 0 :: 0x1E :: false :: internals :: HNil =>
        var newHealth : Int = health
        var newInternals : Option[InternalSlot] = internals
        if(health == 0 || internals.isEmpty) {
          newHealth = 0
          newInternals = None
        }
        val newDeploy = CommonFieldData(deploy.pos, deploy.faction, deploy.unk, player)
        Attempt.successful(OneMannedFieldTurretData(newDeploy, newHealth, newInternals))

      case _ =>
       Attempt.failure(Err("invalid omft data format"))
    },
    {
      case OneMannedFieldTurretData(deploy, health, internals) =>
        var newHealth : Int = health
        var newInternals : Option[InternalSlot] = internals
        if(health == 0 || internals.isEmpty) {
          newHealth = 0
          newInternals = None
        }
        val newDeploy = CommonFieldData(deploy.pos, deploy.faction, deploy.unk)
        Attempt.successful(newDeploy :: false :: deploy.player_guid :: false :: newHealth :: 0 :: 0x1E :: false :: newInternals :: HNil)

      case _ =>
        Attempt.failure(Err("invalid omft data format"))
    }
  )
}
