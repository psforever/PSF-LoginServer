//Copyright (c) 2020 PSForever
package net.psforever.objects.vital

/**
  * na<br>
  * <br>
  * The expected (but not enforced) relationship between values follows:
  * `0 <= DamageDestroysAt <= DamageDisablesAt < RepairRestoresAt <= MaxHealth`.
  */
trait VitalityDefinition {
  /** the maximum amount of health that any of the objects can be allocated;
    * corresponds to ADB property "maxhealth" */
  private var maxHealth : Int = 0
  /** the amount of health that all of the objects are spawned with;
    * defaults to `MaxHealth` if unset */
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
  /** whether the object type accepts damage;
    * corresponds to ABD property "damageable" */
  private var damageable : Boolean = false
  /** whether the object type accepts damage from allied sources;
    * corresponds to the opposite of ABD property "damage_immune_to_friendly_fire" */
  private var damageableByFriendlyFire : Boolean = true
  /** at what `Health` value the object type is considered "destroyed" */
  private var damageDestroysAt : Int = 0
  /** at what `Health` value the object type is considered "disabled";
    * some object types do not have anything to disable and just transition between "not destroyed" and "destroyed" */
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
  /** whether the object type accepts attempts to repair it with a nano dispenser tool;
    * corresponds to ABD property "canberepairedbynanodispenser" */
  private var repairable : Boolean = false
  /** how far away a target can get before repairing is no longer possible;
    * not exact in the least */
  private var repairDistance : Float = 5
  /** if the object type is damaged to the condition of "destroyed",
    * is it possible to repair it back to the condition of "not destroyed" */
  private var repairIfDestroyed : Boolean = false
  /** at what `Health` value the object type is considered "not destroyed";
    * this state is synonymous with "normal" or "functional";
    * as thus, it is opposite of both `damageDestroysAt` and `damageDisablesAt` */
  private var repairRestoresAt : Option[Int] = None
  /** object type specific modification value that chnages the base repair quality;
    * treat as additive */
  private var repairMod : Int = 0

  def Repairable : Boolean = repairable

  def Repairable_=(repair : Boolean) : Boolean = {
    repairable = repair
    Repairable
  }

  def RepairDistance : Float = repairDistance

  def RepairDistance_=(distance : Float) : Float = {
    repairDistance = math.max(0, distance)
    RepairDistance
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
