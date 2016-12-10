// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import scodec.{Attempt, Codec, Err}
import scodec.codecs._

import scala.annotation.switch

/**
  * A reference between all object class codes and the name of the object they represent.<br>
  * <br>
  * Object classes compose a number between 0 and (probably) 2047, always translating into an 11-bit value.
  * They are recorded as little-endian hexadecimal values here.
  * In `scodec` terms, that's a `uintL(11)` or `uintL(0xB)`.
  */
object ObjectClass {
  //character
  final val AVATAR = 0x79
  //ammunition
  final val BULLETS_9MM = 0x1C
  final val BULLETS_9MM_AP = 0x1D
  final val ENERGY_CELL = 0x110
  final val JAMMER_GRENADE_AMMO = 0x1A1
  final val FORCE_BLADE_AMMO = 0x21C
  final val PLASMA_GRENADE_AMMO = 0x2A9
  final val BUCKSHOT = 0x2F3 //TODO apply internal name, eventually
  //weapons
  final val SUPPRESSOR = 0x34D
  final val BEAMER = 0x8C
  final val SWEEPER = 0x130
  final val FORCE_BLADE = 0x144
  final val GAUSS = 0x159
  final val JAMMER_GRENADE = 0x1A0
  final val PLASMA_GRENADE = 0x2A8
  //tools
  final val MEDKIT = 0x218
  final val REK = 0x2D8
  //unknown
  final val SLOT_BLOCKER = 0x1C8 //strange item found in inventory slot #5, between holsters and grid

  //TODO refactor this function into another object later
  /**
    * Given an object class, retrieve the `Codec` used to parse and translate the constructor data for that type.<br>
    * <br>
    * This function serves as a giant `switch` statement that loosely connects object data to object class.
    * All entries, save the default, merely point to the `Codec` of pattern `ConstructorData.genericPattern`.
    * This pattern connects all `Codec`s back to the superclass `ConstructorData`.
    * The default case is a failure case for trying to either decode or encode an unknown class of object.
    * @param objClass the code for the type of object being constructed
    * @return the `Codec` that handles the format of data for that particular item class, or a failing `Codec`
    */
  def selectDataCodec(objClass : Int) : Codec[ConstructorData.genericPattern] = {
    (objClass : @switch) match {
      case ObjectClass.AVATAR => CharacterData.genericCodec
      case ObjectClass.BEAMER => WeaponData.genericCodec
      case ObjectClass.BUCKSHOT => AmmoBoxData.genericCodec
      case ObjectClass.BULLETS_9MM => AmmoBoxData.genericCodec
      case ObjectClass.BULLETS_9MM_AP => AmmoBoxData.genericCodec
      case ObjectClass.ENERGY_CELL => AmmoBoxData.genericCodec
      case ObjectClass.FORCE_BLADE_AMMO => AmmoBoxData.genericCodec
      case ObjectClass.FORCE_BLADE => WeaponData.genericCodec
      case ObjectClass.GAUSS => WeaponData.genericCodec
      case ObjectClass.JAMMER_GRENADE => WeaponData.genericCodec
      case ObjectClass.JAMMER_GRENADE_AMMO => AmmoBoxData.genericCodec
      case ObjectClass.MEDKIT => AmmoBoxData.genericCodec
      case ObjectClass.PLASMA_GRENADE => WeaponData.genericCodec
      case ObjectClass.PLASMA_GRENADE_AMMO => AmmoBoxData.genericCodec
      case ObjectClass.REK => REKData.genericCodec
      case ObjectClass.SLOT_BLOCKER => AmmoBoxData.genericCodec
      case ObjectClass.SUPPRESSOR => WeaponData.genericCodec
      case ObjectClass.SWEEPER => WeaponData.genericCodec
      //failure case
      case _ => conditional(false, bool).exmap[ConstructorData.genericPattern] (
        {
          case None | _ =>
            Attempt.failure(Err("decoding unknown object class"))
        },
        {
          case None | _ =>
            Attempt.failure(Err("encoding unknown object class"))
        }
      )
    }
  }
}
