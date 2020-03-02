//Copyright (c) 2020 PSForever
package net.psforever.objects.vital

trait VitalityDefinition {
  private var maxHealth : Int = 0
  private var defaultHealth : Option[Int] = None

  def MaxHealth : Int = maxHealth

  def MaxHealth_=(max : Int) : Int = {
    maxHealth = math.min(math.max(0, max), 65535)
    MaxHealth
  }

  def DefaultHealth : Int = defaultHealth.getOrElse(MaxHealth)

  def DefaultHealth_=(default : Int) : Int = DefaultHealth_=(Some(default))

  def DefaultHealth_=(default : Option[Int]) : Int = {
    defaultHealth = default
    DefaultHealth
  }

  /* damageable */
  private var damageable : Boolean = false
  private var damageableByFriendlyFire : Boolean = true
  private var damageDestroysAt : Int = 0
  private var damageDisablesAt : Option[Int] = None

  def Damageable : Boolean = damageable

  def Damageable_=(state : Boolean) : Boolean = {
    damageable = state
    Damageable
  }

  def DamageableByFriendlyFire : Boolean = damageableByFriendlyFire

  def DamageableByFriendlyFire_=(state : Boolean) : Boolean = {
    damageableByFriendlyFire = state
    DamageableByFriendlyFire
  }

  def DamageDisablesAt : Int = damageDisablesAt.getOrElse(MaxHealth/2)

  def DamageDisablesAt_=(value : Int) : Int = DamageDisablesAt_=(Some(value))

  def DamageDisablesAt_=(value : Option[Int]) : Int = {
    damageDisablesAt = value
    DamageDisablesAt
  }

  def DamageDestroysAt : Int = damageDestroysAt

  def DamageDestroysAt_=(value : Int) : Int = {
    damageDestroysAt = value
    DamageDestroysAt
  }

  /* repairable */
  private var repairable : Boolean = false
  private var repairIfDestroyed : Boolean = false
  private var repairRestoresAt : Option[Int] = None
  private var repairMod : Int = 0

  def Repairable : Boolean = repairable

  def Repairable_=(repair : Boolean) : Boolean = {
    repairable = repair
    Repairable
  }

  def RepairIfDestroyed : Boolean = repairIfDestroyed

  def RepairIfDestroyed_=(repair : Boolean) : Boolean = {
    repairIfDestroyed = repair
    RepairIfDestroyed
  }

  def RepairRestoresAt : Int = repairRestoresAt.getOrElse(MaxHealth/2)

  def RepairRestoresAt_=(restore : Int) : Int = RepairRestoresAt_=(Some(restore))

  def RepairRestoresAt_=(restore : Option[Int]) : Int = {
    repairRestoresAt = restore
    RepairRestoresAt
  }

  def RepairMod : Int = repairMod

  def RepairMod_=(mod : Int) : Int = {
    repairMod = mod
    RepairMod
  }
}
