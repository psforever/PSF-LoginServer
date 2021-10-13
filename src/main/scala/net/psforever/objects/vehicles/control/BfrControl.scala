// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import akka.actor.Cancellable
import net.psforever.objects._
import net.psforever.objects.ballistics.VehicleSource
import net.psforever.objects.definition.ToolDefinition
import net.psforever.objects.equipment.{Equipment, EquipmentHandiness, Handiness}
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.vital.VehicleShieldCharge
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.packet.game.GenericObjectActionMessage
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.{DriveState, PlanetSideGUID}
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

  override def RemoveItemFromSlotCallback(item: Equipment, slot: Int): Unit = {
    BfrControl.dimorphics.find { _.contains(item.Definition) } match {
      case Some(dimorph) if vehicle.VisibleSlots.contains(slot) => //revert to a generic variant
        Tool.LoadDefinition(
          item.asInstanceOf[Tool],
          dimorph.transform(Handiness.Generic).asInstanceOf[ToolDefinition]
        )
      case None => ; //no dimorphic entry; place as-is
    }
    super.RemoveItemFromSlotCallback(item, slot)
  }

  override def PutItemInSlotCallback(item: Equipment, slot: Int): Unit = {
    BfrControl.dimorphics.find { _.contains(item.Definition) } match {
      case Some(dimorph) if vehicle.VisibleSlots.contains(slot) => //left-handed or right-handed variant
        Tool.LoadDefinition(
          item.asInstanceOf[Tool],
          dimorph.transform(
            if (slot == 2) Handiness.Left else Handiness.Right
          ).asInstanceOf[ToolDefinition]
        )
      case Some(dimorph) => //revert to a generic variant
        Tool.LoadDefinition(
          item.asInstanceOf[Tool],
          dimorph.transform(Handiness.Generic).asInstanceOf[ToolDefinition]
        )
      case None => ; //no dimorphic entry; place as-is
    }
    super.PutItemInSlotCallback(item, slot)
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
      val definition = vehicle.Definition
      val events = zone.VehicleEvents
      val before = vehicle.Shields
      val chargeAmount = (if (vehicle.DeploymentState == DriveState.Kneeling || vehicle.Seats(0).occupant.isEmpty) {
        definition.ShieldAutoRechargeSpecial
      } else {
        definition.ShieldAutoRecharge
      }).getOrElse(amount)
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
        VehicleAction.PlanetsideAttribute(guid0, vguid, definition.shieldUiAttribute, after)
      )
      //continue charge?
      shieldCharge.cancel()
      if (after < definition.MaxShields) {
        shieldCharge = context.system.scheduler.scheduleOnce(
          delay = definition.ShieldPeriodicDelay milliseconds,
          self,
          Vehicle.ChargeShields(0)
        )
      } else {
        shieldCharge = Default.Cancellable
      }
    }
  }
}

object BfrControl {
  val dimorphics: List[EquipmentHandiness] = {
    import GlobalDefinitions._
    List(
      EquipmentHandiness(aphelion_armor_siphon, aphelion_armor_siphon_left, aphelion_armor_siphon_right),
      EquipmentHandiness(aphelion_laser, aphelion_laser_left, aphelion_laser_right),
      EquipmentHandiness(aphelion_ntu_siphon, aphelion_ntu_siphon_left, aphelion_ntu_siphon_right),
      EquipmentHandiness(aphelion_ppa, aphelion_ppa_left, aphelion_ppa_right),
      EquipmentHandiness(aphelion_starfire, aphelion_starfire_left, aphelion_starfire_right),
      EquipmentHandiness(colossus_armor_siphon, colossus_armor_siphon_left, colossus_armor_siphon_right),
      EquipmentHandiness(colossus_burster, colossus_burster_left, colossus_burster_right),
      EquipmentHandiness(colossus_chaingun, colossus_chaingun_left, colossus_chaingun_right),
      EquipmentHandiness(colossus_ntu_siphon, colossus_ntu_siphon_left, colossus_ntu_siphon_right),
      EquipmentHandiness(colossus_tank_cannon, colossus_tank_cannon_left, colossus_tank_cannon_right),
      EquipmentHandiness(peregrine_armor_siphon, peregrine_armor_siphon_left, peregrine_armor_siphon_right),
      EquipmentHandiness(peregrine_dual_machine_gun, peregrine_dual_machine_gun_left, peregrine_dual_machine_gun_right),
      EquipmentHandiness(peregrine_mechhammer, peregrine_mechhammer_left, peregrine_mechhammer_right),
      EquipmentHandiness(peregrine_ntu_siphon, peregrine_ntu_siphon_left, peregrine_ntu_siphon_right),
      EquipmentHandiness(peregrine_sparrow, peregrine_sparrow_left, peregrine_sparrow_right)
    )
  }
}
