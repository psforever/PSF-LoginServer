// Copyright (c) 2017 PSForever
package net.psforever.objects.equipment

import scala.collection.mutable

class FireModeDefinition {
  private val ammoTypeIndices : mutable.ListBuffer[Int] = mutable.ListBuffer[Int]() //indices pointing to all ammo types used
  private var ammoSlotIndex : Int = 0 //ammunition slot number this fire mode utilizes
  private var chamber : Int = 1 //how many rounds are queued to be fired at once, e.g., 3 for the Jackhammer's triple burst
  private var magazine : Int = 1 //how many rounds are queued for each reload cycle
//  private var target : Any = _ //target designation (self? other?)
  private var resetAmmoIndexOnSwap : Boolean = false //when changing fire modes, do not attempt to match previous mode's ammo type

  //damage modifiers will follow here ...

  def AmmoSlotIndex : Int = ammoSlotIndex

  def AmmoSlotIndex_=(index : Int) : Int = {
    ammoSlotIndex = index
    AmmoSlotIndex
  }

  def AmmoTypeIndices : mutable.ListBuffer[Int] = ammoTypeIndices

  def AmmoTypeIndices_=(index : Int) : mutable.ListBuffer[Int] = {
    ammoTypeIndices += index
  }

  def Chamber : Int = chamber

  def Chamber_=(inChamber : Int) : Int = {
    chamber = inChamber
    Chamber
  }

  def Magazine : Int = magazine

  def Magazine_=(inMagazine : Int) : Int = {
    magazine = inMagazine
    Magazine
  }

//  def Target : Any = target
//
//  def Target_+(setAsTarget : Any) : Any = {
//    target = setAsTarget
//    Target
//  }

  def ResetAmmoIndexOnSwap : Boolean = resetAmmoIndexOnSwap

  def ResetAmmoIndexOnSwap_=(reset : Boolean) : Boolean = {
    resetAmmoIndexOnSwap = reset
    ResetAmmoIndexOnSwap
  }
}

object FireModeDefinition {
  def apply() : FireModeDefinition = {
    new FireModeDefinition()
  }
}
