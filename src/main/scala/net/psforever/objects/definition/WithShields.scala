package net.psforever.objects.definition

import net.psforever.objects.vital.{DamagingActivity, InGameActivity, ShieldCharge}

trait WithShields {
  /** ... */
  var shieldUiAttribute: Int = 68
  /** how many points of shield the vehicle starts with (should default to 0 if unset through the accessor) */
  private var defaultShields : Option[Int] = None
  /** maximum vehicle shields (generally: 20% of health)
   * for normal vehicles, offered through amp station facility benefits
   * for omft, gained in friendly soi (in which the turret may not be constructed)
   * for BFR's, it charges naturally
   **/
  private var maxShields: Int = 0
  /** the minimum amount of time that must elapse in between damage and shield charge activities (ms) */
  private var shieldChargeDamageCooldown : Long = 5000L
  /** the minimum amount of time that must elapse in between distinct shield charge activities (ms) */
  private var shieldChargePeriodicCooldown : Long = 1000L
  /** if the shield recharges on its own, this value will be non-`None` and indicate by how much */
  private var autoShieldRecharge : Option[Int] = None
  private var autoShieldRechargeSpecial : Option[Int] = None
  /** shield drain is what happens to the shield under special conditions, e.g., bfr flight;
   * the drain interval is 250ms which is convenient for us
   * we can skip needing to define is explicitly */
  private var shieldDrain : Option[Int] = None

  def DefaultShields: Int = defaultShields.getOrElse(0)

  def DefaultShields_=(shield: Int): Int = DefaultShields_=(Some(shield))

  def DefaultShields_=(shield: Option[Int]): Int = {
    defaultShields = shield
    DefaultShields
  }

  def MaxShields: Int = maxShields

  def MaxShields_=(shields: Int): Int = {
    maxShields = shields
    MaxShields
  }

  def ShieldPeriodicDelay : Long = shieldChargePeriodicCooldown

  def ShieldPeriodicDelay_=(cooldown: Long): Long = {
    shieldChargePeriodicCooldown = cooldown
    ShieldPeriodicDelay
  }

  def ShieldDamageDelay: Long = shieldChargeDamageCooldown

  def ShieldDamageDelay_=(cooldown: Long): Long = {
    shieldChargeDamageCooldown = cooldown
    ShieldDamageDelay
  }

  def ShieldAutoRecharge: Option[Int] = autoShieldRecharge

  def ShieldAutoRecharge_=(charge: Int): Option[Int] = ShieldAutoRecharge_=(Some(charge))

  def ShieldAutoRecharge_=(charge: Option[Int]): Option[Int] = {
    autoShieldRecharge = charge
    ShieldAutoRecharge
  }

  def ShieldAutoRechargeSpecial: Option[Int] = autoShieldRechargeSpecial.orElse(ShieldAutoRecharge)

  def ShieldAutoRechargeSpecial_=(charge: Int): Option[Int] = ShieldAutoRechargeSpecial_=(Some(charge))

  def ShieldAutoRechargeSpecial_=(charge: Option[Int]): Option[Int] = {
    autoShieldRechargeSpecial = charge
    ShieldAutoRechargeSpecial
  }

  def ShieldDrain: Option[Int] = shieldDrain

  def ShieldDrain_=(drain: Int): Option[Int] = ShieldDrain_=(Some(drain))

  def ShieldDrain_=(drain: Option[Int]): Option[Int] = {
    shieldDrain = drain
    ShieldDrain
  }
}

object WithShields {
  /**
   * Determine if a given activity entry would invalidate the act of charging shields this tick.
   * @param now the current time (in milliseconds)
   * @param act a `VitalsActivity` entry to test
   * @return `true`, if the shield charge would be blocked;
   *        `false`, otherwise
   */
  def LastShieldChargeOrDamage(now: Long, vdef: WithShields)(act: InGameActivity): Boolean = {
    act match {
      case dact: DamagingActivity   => now - dact.time < vdef.ShieldDamageDelay //damage delays next charge
      case vsc: ShieldCharge        => now - vsc.time < vdef.ShieldPeriodicDelay //previous charge delays next
      case _                        => false
    }
  }
}
