// Copyright (c) 2020 PSForever
package net.psforever.objects.vital

import net.psforever.objects.vital.damage.DamageProfile

class SpecificDamageProfile extends DamageProfile {
  private var damage0: Int = 0
  private var damage1: Int = 0
  private var damage2: Int = 0
  private var damage3: Int = 0
  private var damage4: Int = 0

  def Damage0: Int = damage0

  def Damage0_=(damage: Int): Int = {
    damage0 = damage
    Damage0
  }

  def Damage1: Int = damage1

  def Damage1_=(damage: Int): Int = {
    damage1 = damage
    Damage1
  }

  def Damage2: Int = damage2

  def Damage2_=(damage: Int): Int = {
    damage2 = damage
    Damage2
  }

  def Damage3: Int = damage3

  def Damage3_=(damage: Int): Int = {
    damage3 = damage
    Damage3
  }

  def Damage4: Int = damage4

  def Damage4_=(damage: Int): Int = {
    damage4 = damage
    Damage4
  }
}

object SpecificDamageProfile {
  def apply(
             damage0: Int = 0,
             damage1: Int = 0,
             damage2: Int = 0,
             damage3: Int = 0,
             damage4: Int = 0
           ): SpecificDamageProfile = {
    val obj = new SpecificDamageProfile
    obj.Damage0 = damage0
    obj.Damage1 = damage1
    obj.Damage2 = damage2
    obj.Damage3 = damage3
    obj.Damage4 = damage4
    obj
  }
}
