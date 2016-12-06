// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import scodec.Codec

import scala.annotation.switch

object ObjectClass {
  //character
  final val PLAYER = 0x79
  //ammunition
  final val BULLETS_9MM = 0x1C
  final val BULLETS_9MM_AP = 0x1D
  final val ENERGY_CELL = 0x110
  final val FORCE_BLADE_AMMO = 0x21C
  //weapons
  final val BEAMER = 0x8C
  final val FORCE_BLADE = 0x144
  final val GAUSS = 0x159
  final val SUPPRESSOR = 0x34D
  //tools
  final val REK = 0x2D8
  //unknown
  final val SLOT_BLOCKER = 0x1C8

  def selectDataCodec(objClass : Int) : Codec[ConstructorData.genericPattern] = {
    (objClass : @switch) match {
      case ObjectClass.PLAYER => CharacterData.genericCodec
      case ObjectClass.BULLETS_9MM => AmmoBoxData.genericCodec
      case ObjectClass.BULLETS_9MM_AP => AmmoBoxData.genericCodec
      case ObjectClass.ENERGY_CELL => AmmoBoxData.genericCodec
      case ObjectClass.FORCE_BLADE_AMMO => AmmoBoxData.genericCodec
      case ObjectClass.BEAMER => WeaponData.genericCodec
      case ObjectClass.FORCE_BLADE => WeaponData.genericCodec
      case ObjectClass.GAUSS => WeaponData.genericCodec
      case ObjectClass.SUPPRESSOR => WeaponData.genericCodec
      case ObjectClass.REK => REKData.genericCodec
      case ObjectClass.SLOT_BLOCKER => AmmoBoxData.genericCodec
      case _ => RecoveredData.genericCodec
    }
  }

//  val failureCodec : Codec[ConstructorData.genericPattern] = conditional(false, bool).exmap[ConstructorData.genericPattern] (
//    {
//      case None | _ =>
//        Attempt.failure(Err("object class unrecognized during decoding"))
//    },
//    {
//      case None | _ =>
//        Attempt.failure(Err("object class unrecognized during encoding"))
//    }
//  )
}