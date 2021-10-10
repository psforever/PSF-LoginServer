// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import akka.actor.Cancellable
import net.psforever.objects._
import net.psforever.objects.ballistics.VehicleSource
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.vital.VehicleShieldCharge
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.packet.game.GenericObjectActionMessage
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.PlanetSideGUID
//import net.psforever.objects.vehicles._
//import net.psforever.types.DriveState

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * A vehicle control agency exclusive to the battleframe robotics (BFR) combat vehicle system.
  * @param vehicle the battleframe robotics unit
  */
class BfrControl(vehicle: Vehicle)
  extends VehicleControl(vehicle) {
  /** shield-auto charge */
  var shieldCharge: Cancellable = Default.Cancellable
  if (vehicle.Shields < vehicle.MaxShields) {
    chargeShields(amount = 0) //start charging if starts as uncharged
  }

  override def postStop(): Unit = {
    super.postStop()
    shieldCharge.cancel()
  }

  override def DamageAwareness(target: Target, cause: DamageResult, amount: Any) : Unit = {
    super.DamageAwareness(target, cause, amount)
    //manage shield display and charge
    disableShield()
    if (shieldCharge != Default.Cancellable && vehicle.Shields < vehicle.MaxShields) {
      shieldCharge.cancel()
      shieldCharge = context.system.scheduler.scheduleOnce(
        delay = vehicle.Definition.ShieldDamageDelay milliseconds,
        self,
        Vehicle.ChargeShields(0)
      )
    }
  }

  override def DestructionAwareness(target: Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    shieldCharge.cancel()
    shieldCharge = Default.Cancellable
    disableShield()
  }

  def disableShield(): Unit = {
    if (vehicle.Shields == 0) {
      val zone = vehicle.Zone
      zone.VehicleEvents ! VehicleServiceMessage(
        s"${zone.id}",
        VehicleAction.SendResponse(PlanetSideGUID(0), GenericObjectActionMessage(vehicle.GUID, 180))
      )
    }
  }

  override def chargeShields(amount: Int): Unit = {
    if (canChargeShields()) {
      val guid0 = Service.defaultPlayerGUID
      val vguid = vehicle.GUID
      val zone = vehicle.Zone
      val events = zone.VehicleEvents
      val before = vehicle.Shields
      val chargeAmount = vehicle.Definition.ShieldAutoRecharge.getOrElse(amount)
      vehicle.History(VehicleShieldCharge(VehicleSource(vehicle), chargeAmount))
      vehicle.Shields = before + chargeAmount
      val after = vehicle.Shields
      if (before == 0 && before < after) {
        events ! VehicleServiceMessage(
          s"${zone.id}",
          VehicleAction.SendResponse(guid0, GenericObjectActionMessage(vguid, 176))
        )
      }
      events ! VehicleServiceMessage(
        s"${vehicle.Actor}",
        VehicleAction.PlanetsideAttribute(guid0, vguid, vehicle.Definition.shieldUiAttribute, after)
      )
      //continue charge?
      shieldCharge.cancel()
      if (after < vehicle.Definition.MaxShields) {
        shieldCharge = context.system.scheduler.scheduleOnce(
          delay = vehicle.Definition.ShieldPeriodicDelay milliseconds,
          self,
          Vehicle.ChargeShields(0)
        )
      } else {
        shieldCharge = Default.Cancellable
      }
    }
  }
}
