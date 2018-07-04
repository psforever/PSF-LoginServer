// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

/**
  * The different values for five common types of damage that can be dealt, based on target and application.
  * In the same way, the five damage modifiers that are applied to the same kind of damage.
  */
trait DamageProfile {
  def Damage0 : Int

  def Damage0_=(damage : Int) : Int

  def Damage1 : Int

  def Damage1_=(damage : Int) : Int

  def Damage2 : Int

  def Damage2_=(damage : Int) : Int

  def Damage3 : Int

  def Damage3_=(damage : Int) : Int

  def Damage4 : Int

  def Damage4_=(damage : Int) : Int
}
