// Copyright (c) 2017 PSForever
package net.psforever.objects.equipment

/**
  * An `Enumeration` of the kit types in the game, paired with their object id as the `Value`.
  */
object Kits extends Enumeration {
  final val medkit           = Value(536)
  final val super_armorkit   = Value(842) //super repair kit
  final val super_medkit     = Value(843)
  final val super_staminakit = Value(844) //super stimpack
}
