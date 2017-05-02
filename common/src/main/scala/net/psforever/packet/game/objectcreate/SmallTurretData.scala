// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * A representation of the Spitfire-based small turrets deployed using an adaptive construction engine.<br>
  * <br>
  * The turret may contain substructure defining a weapon is a turret weapon contained within the turret itself.
  * Furthermore, that turret-like weapon is loaded with turret-like ammunition.
  * In other words, this outer turret can be considered a weapons platform for the inner turret weapon.<br>
  * <br>
  * If the turret has no `health`, it is rendered as destroyed.
  * If the turret has no internal weapon, it is safest rendered as destroyed.
  * @param deploy data common to objects spawned by the (advanced) adaptive construction engine
  * @param health the amount of health the object has, as a percentage of a filled bar
  * @param internals data regarding the mounted weapon
  */
final case class SmallTurretData(deploy : ACEDeployableData,
                                 health : Int,
                                 internals : Option[InternalSlot] = None
                                ) extends ConstructorData {
  override def bitsize : Long = {
    val deploySize = deploy.bitsize
    val internalSize = if(internals.isDefined) { ACEDeployableData.internalWeapon_bitsize + internals.get.bitsize } else { 0L }
    23L + deploySize + internalSize //1u + 8u + 7u + 4u + 2u + 1u
  }
}

object SmallTurretData extends Marshallable[SmallTurretData] {
  /**
    * Overloaded constructor that mandates information about the internal weapon of the small turret.
    * @param deploy data common to objects spawned by the (advanced) adaptive construction engine
    * @param health the amount of health the object has, as a percentage of a filled bar
    * @param internals data regarding the mounted weapon
    * @return a `SmallTurretData` object
    */
  def apply(deploy : ACEDeployableData,  health : Int,  internals : InternalSlot) : SmallTurretData =
    new SmallTurretData(deploy, health, Some(internals))

  /**
    * Prefabricated weapon data for both Spitfires (`spitfire_turret`) and Shadow Turrets (`spitfire_cloaked`).
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
  def spitfire(wep_guid : PlanetSideGUID, wep_unk1 : Int, wep_unk2 : Int, ammo_guid : PlanetSideGUID, ammo_unk : Int) : InternalSlot =
    InternalSlot(ObjectClass.spitfire_weapon, wep_guid, 0,
      WeaponData(wep_unk1, wep_unk2, ObjectClass.spitfire_ammo, ammo_guid, 0,
        AmmoBoxData(ammo_unk)
      )
    )

  /**
    * Prefabricated weapon data for Cerebus turrets (`spitfire_aa`).
    * @param wep_guid the uid to assign to the weapon
    * @param wep_unk1 na;
    *                used by `WeaponData`
    * @param ammo_guid the uid to assign to the ammo
    * @param wep_unk2 na;
    *                used by `WeaponData`
    * @param ammo_unk na;
    *                 used by `AmmoBoxData`
    * @return an `InternalSlot` object
    */
  def cerebus(wep_guid : PlanetSideGUID, wep_unk1 : Int, wep_unk2 : Int, ammo_guid : PlanetSideGUID, ammo_unk : Int) : InternalSlot =
    InternalSlot(ObjectClass.spitfire_aa_weapon, wep_guid, 0,
      WeaponData(wep_unk1, wep_unk2, ObjectClass.spitfire_aa_ammo, ammo_guid, 0,
        AmmoBoxData(ammo_unk)
      )
    )

  implicit val codec : Codec[SmallTurretData] = (
    ("deploy" | ACEDeployableData.codec) ::
      bool ::
      ("health" | uint8L) ::
      uintL(7) ::
      uint4L ::
      uint2L ::
      optional(bool, "internals" | ACEDeployableData.internalWeaponCodec)
  ).exmap[SmallTurretData] (
    {
      case deploy :: false :: health :: 0 :: 0xF :: 0 :: internals :: HNil =>
        var newHealth : Int = health
        var newInternals : Option[InternalSlot] = internals
        if(health == 0 || internals.isEmpty) {
          newHealth = 0
          newInternals = None
        }
        Attempt.successful(SmallTurretData(deploy, newHealth, newInternals))

      case _ =>
        Attempt.failure(Err("invalid small turret data format"))
    },
    {
      case SmallTurretData(deploy, health, internals) =>
        var newHealth : Int = health
        var newInternals : Option[InternalSlot] = internals
        if(health == 0 || internals.isEmpty) {
          newHealth = 0
          newInternals = None
        }
        Attempt.successful(deploy :: false :: newHealth :: 0 :: 0xF :: 0 :: newInternals :: HNil)
    }
  )
}
