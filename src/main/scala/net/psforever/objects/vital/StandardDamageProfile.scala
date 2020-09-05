// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

import net.psforever.objects.vital.damage.DamageProfile

trait StandardDamageProfile extends DamageProfile {
  private var damage0: Int         = 0
  private var damage1: Option[Int] = None
  private var damage2: Option[Int] = None
  private var damage3: Option[Int] = None
  private var damage4: Option[Int] = None

  def Damage0: Int = damage0

  def Damage0_=(damage: Int): Int = {
    damage0 = damage
    damage0
  }

  def Damage0_=(damage: Option[Int]): Int = {
    damage0 = damage match {
      case Some(value) => value
      case None        => 0 //can not be set to None
    }
    Damage0
  }

  def Damage1: Int = damage1.getOrElse(Damage0)

  def Damage1_=(damage: Int): Int = Damage1_=(Some(damage))

  def Damage1_=(damage: Option[Int]): Int = {
    this.damage1 = damage
    Damage1
  }

  def Damage2: Int = damage2.getOrElse(Damage1)

  def Damage2_=(damage: Int): Int = Damage2_=(Some(damage))

  def Damage2_=(damage: Option[Int]): Int = {
    this.damage2 = damage
    Damage2
  }

  def Damage3: Int = damage3.getOrElse(Damage2)

  def Damage3_=(damage: Int): Int = Damage3_=(Some(damage))

  def Damage3_=(damage: Option[Int]): Int = {
    this.damage3 = damage
    Damage3
  }

  def Damage4: Int = damage4.getOrElse(Damage3)

  def Damage4_=(damage: Int): Int = Damage4_=(Some(damage))

  def Damage4_=(damage: Option[Int]): Int = {
    this.damage4 = damage
    Damage4
  }
}

object StandardDamageProfile {
  def apply(
             damage0: Option[Int] = None,
             damage1: Option[Int] = None,
             damage2: Option[Int] = None,
             damage3: Option[Int] = None,
             damage4: Option[Int] = None
           ): StandardDamageProfile = {
    val obj = new StandardDamageProfile { }
    obj.Damage0 = damage0
    obj.Damage1 = damage1
    obj.Damage2 = damage2
    obj.Damage3 = damage3
    obj.Damage4 = damage4
    obj
  }
}
