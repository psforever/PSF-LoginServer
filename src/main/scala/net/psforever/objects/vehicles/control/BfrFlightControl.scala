// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import net.psforever.objects.equipment.Handiness
import net.psforever.objects.{Vehicle, equipment}
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.vehicles.{VehicleSubsystem, VehicleSubsystemEntry}
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.types.Vector3

/**
  * ...
  */
class BfrFlightControl(vehicle: Vehicle)
  extends BfrControl(vehicle)
  with VehicleCapacitance {
  def CapacitanceObject: Vehicle = vehicle

  var flying: Option[Boolean] = None

  override def postStop() : Unit = {
    super.postStop()
    capacitancePostStop()
  }

  override def commonEnabledBehavior: Receive = super.commonEnabledBehavior
    .orElse(capacitorBehavior)
    .orElse {
      case BfrFlight.Soaring(flightValue) =>
        val localFlyingValue = flying
        vehicle.Flying = Some(flightValue)
        //capacitor drain
        if (vehicle.Capacitor > 0) {
          val definition = vehicle.Definition
          val (_, cdrain) = if (flightValue == 0 || flightValue == -0) {
            (0, vehicle.Capacitor)
          } else {
            val vdrain = if (flightValue > 0) definition.CapacitorDrain else 0
            val hdrain = if ({
              val vec = vehicle.Velocity.getOrElse(Vector3.Zero).xy
              vec.x > 0.5f || vec.y > 0.5f
            }) definition.CapacitorDrainSpecial else 0
            (vdrain, vdrain + hdrain)
          }
          flying = Some(if (cdrain > 0) {
            val modDrain = math.max(1, (cdrain * vehicle.SubsystemStatusMultiplier(sys = "BattleframeFlightPod.UseRate")).toInt)
            if (super.capacitorOnlyCharge(-modDrain) || vehicle.Capacitor < vehicle.Definition.MaxCapacitor) {
              startCapacitorTimer()
            }
            true
          } else {
            false
          })
        }
        //shield drain
        if (vehicle.Shields > 0) {
          vehicle.Definition.ShieldDrain match {
            case Some(drain) if localFlyingValue.isEmpty =>
              //shields off
              disableShield()
              vehicle.Subsystems(VehicleSubsystemEntry.BattleframeShieldGenerator).get.Enabled = false
              vehicle.Shields -= drain
              showShieldCharge()
            case None if localFlyingValue.isEmpty =>
              //shields off
              disableShield()
              vehicle.Subsystems(VehicleSubsystemEntry.BattleframeShieldGenerator).get.Enabled = false
            case Some(drain) =>
              vehicle.Shields -= drain
              showShieldCharge()
            case _ => ;
          }
        }
        if (vehicle.Subsystems(VehicleSubsystemEntry.BattleframeFlightPod).get.Jammed) {

        }

      case BfrFlight.Landed =>
        if (flying.nonEmpty) {
          flying = None
          vehicle.Flying = None
          vehicle.Subsystems(VehicleSubsystemEntry.BattleframeShieldGenerator).get.Enabled = true
          if (vehicle.Shields > 0) {
            enableShield()
          }
          shieldCharge(delay = 2000)
        }

      case _ => ;
    }

  override def PrepareForDisabled(kickPassengers: Boolean) : Unit = {
    super.PrepareForDisabled(kickPassengers)
    capacitanceStop()
  }

  override def destructionDelayed(delay: Long, cause: DamageResult): Unit = {
    super.destructionDelayed(delay, cause)
    capacitanceStop()
  }

  override def DestructionAwareness(target: Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    capacitancePostStop()
  }

  override def chargeShieldsOnly(amount: Int): Unit = {
    if (flying != null && (flying.isEmpty || flying.contains(false))) {
      super.chargeShieldsOnly(amount)
    }
  }

  override protected def capacitorOnlyCharge(amount: Int): Boolean = {
    if (flying.isEmpty || flying.contains(false)) {
      val mod = math.max(1, amount * vehicle.SubsystemStatusMultiplier(sys = "BattleframeFlightPod.RechargeRate").toInt)
      super.capacitorOnlyCharge(mod)
    } else {
      false
    }
  }

  override def bfrHandiness(side: equipment.Hand): Int = {
    if (side == Handiness.Left) 1
    else if (side == Handiness.Right) 2
    else throw new Exception("no hand associated with this slot; caller screwed up")
  }

  override def bfrHandiness(slot: Int): equipment.Hand = {
    //for the benefit of BFR equipment slots interacting with MoveItemMessage
    if (slot == 1) Handiness.Left
    else if (slot == 2) Handiness.Right
    else Handiness.Generic
  }

  override def bfrHandSubsystem(side: equipment.Hand): Option[VehicleSubsystem] = {
    //for the benefit of BFR equipment slots interacting with MoveItemMessage
    side match {
      case Handiness.Left  => vehicle.Subsystems(VehicleSubsystemEntry.BattleframeFlightLeftArm)
      case Handiness.Right => vehicle.Subsystems(VehicleSubsystemEntry.BattleframeFlightRightArm)
      case _               => None
    }
  }
}

object BfrFlight {
  final case class Soaring(flyingValue: Int)
  case object Landed
}
