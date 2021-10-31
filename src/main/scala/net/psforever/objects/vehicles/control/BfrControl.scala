// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import akka.actor.Cancellable
import net.psforever.objects._
import net.psforever.objects.ballistics.VehicleSource
import net.psforever.objects.definition.{ToolDefinition, VehicleDefinition}
import net.psforever.objects.equipment.{Equipment, EquipmentHandiness, Handiness}
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.vehicles.{VehicleSubsystem, VehicleSubsystemEntry}
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
    disableShieldIfDrained()
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
    //if the weapon arm is disabled, enable it before removing (makes life easy)
    parseObjectAction(item.GUID, action = 38, None)
    super.RemoveItemFromSlotCallback(item, slot)
  }

  override def PutItemInSlotCallback(item: Equipment, slot: Int): Unit = {
    val definition = item.Definition
    val handiness = BfrControl.dimorphics.find { _.contains(definition) } match {
      case Some(dimorph) if vehicle.VisibleSlots.contains(slot) => //left-handed or right-handed variant
        val handiness = bfrHandiness(slot)
        Tool.LoadDefinition(
          item.asInstanceOf[Tool],
          dimorph.transform(handiness).asInstanceOf[ToolDefinition]
        )
        handiness
      case Some(dimorph) => //revert to a generic variant
        Tool.LoadDefinition(
          item.asInstanceOf[Tool],
          dimorph.transform(Handiness.Generic).asInstanceOf[ToolDefinition]
        )
        Handiness.Generic
      case None => //no dimorphic entry; place as-is
        Handiness.Generic
    }
    super.PutItemInSlotCallback(item, slot)
    specialArmWeaponEquipManagement(Some(item, slot, handiness))
  }

  def disableShieldIfDrained(): Unit = {
    if (vehicle.Shields == 0) {
      disableShield()
    }
  }

  def disableShield(): Unit = {
    val zone = vehicle.Zone
    zone.VehicleEvents ! VehicleServiceMessage(
      s"${zone.id}",
      VehicleAction.SendResponse(PlanetSideGUID(0), GenericObjectActionMessage(vehicle.GUID, 45))
    )
  }

  def enableShield(): Unit = {
    val zone = vehicle.Zone
    zone.VehicleEvents ! VehicleServiceMessage(
      s"${zone.id}",
      VehicleAction.SendResponse(PlanetSideGUID(0), GenericObjectActionMessage(vehicle.GUID, 44))
    )
  }

  override def chargeShields(amount: Int): Unit = {
    chargeShieldsOnly(amount)
    shieldCharge(vehicle.Shields, vehicle.Definition, delay = 0) //continue charge?
  }

  def chargeShieldsOnly(amount: Int): Unit = {
    val definition = vehicle.Definition
    val before = vehicle.Shields
    if (canChargeShields()) {
      val chargeAmount = (if (vehicle.DeploymentState == DriveState.Kneeling || vehicle.Seats(0).occupant.nonEmpty) {
        definition.ShieldAutoRechargeSpecial
      } else {
        definition.ShieldAutoRecharge
      }).getOrElse(amount)
      vehicle.History(VehicleShieldCharge(VehicleSource(vehicle), chargeAmount))
      vehicle.Shields = before + chargeAmount
      val after = vehicle.Shields
      showShieldCharge()
      if (before == 0 && after > 0) {
        enableShield()
      }
    }
  }

  def shieldCharge(delay: Long): Unit = {
    shieldCharge(vehicle.Shields, vehicle.Definition, delay)
  }

  def shieldCharge(after:Int, definition: VehicleDefinition, delay: Long): Unit = {
    shieldCharge.cancel()
    if (after < definition.MaxShields) {
      shieldCharge = context.system.scheduler.scheduleOnce(
        delay = definition.ShieldPeriodicDelay + delay milliseconds,
        self,
        Vehicle.ChargeShields(0)
      )
    } else {
      shieldCharge = Default.Cancellable
    }
  }

  def showShieldCharge(): Unit = {
    val vguid = vehicle.GUID
    val zone = vehicle.Zone
    val shields = vehicle.Shields
    zone.VehicleEvents ! VehicleServiceMessage(
      s"${vehicle.Actor}",
      VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vguid, vehicle.Definition.shieldUiAttribute, shields)
    )
  }

  override def parseObjectAction(guid: PlanetSideGUID, action: Int, other: Option[Any]): Unit = {
    super.parseObjectAction(guid, action, other)
    if (action == 38 || action == 39) {
      //disable or enable fire control for the left arm weapon or for the right arm weapon
      ((vehicle.Weapons.find { case (_, slot) => slot.Equipment.nonEmpty && slot.Equipment.get.GUID == guid } match {
        case Some((slot, _)) =>
          bfrHandSubsystem(bfrHandiness(slot))
        case _ =>
          None
      }) match {
        case subsys @ Some(subsystem) =>
          if (action == 38 && !subsystem.enabled) {
            subsystem.enabled = true
            subsys
          } else if (action == 39 && subsystem.enabled) {
            subsystem.enabled = false
            subsys
          } else {
            None
          }
        case None =>
          None
      }) match {
        case Some(_) =>
          val doNotSendTo = other match {
            case Some(pguid: PlanetSideGUID) => pguid
            case _                           => Service.defaultPlayerGUID
          }
          val zone = vehicle.Zone
          zone.VehicleEvents ! VehicleServiceMessage(
            zone.id,
            VehicleAction.GenericObjectAction(doNotSendTo, guid, action)
          )
        case None => ;
      }
    }
  }

  def bfrHandiness(slot: Int): equipment.Hand = {
    //for the benefit of BFR equipment slots interacting with MoveItemMessage
    if (slot == 2) Handiness.Left else if (slot == 3) Handiness.Right else Handiness.Generic
  }

  def bfrHandSubsystem(side: equipment.Hand): Option[VehicleSubsystem] = {
    //for the benefit of BFR equipment slots interacting with MoveItemMessage
    side match {
      case Handiness.Left => vehicle.Subsystems(VehicleSubsystemEntry.BattleframeLeftArm)
      case Handiness.Right => vehicle.Subsystems(VehicleSubsystemEntry.BattleframeRightArm)
      case _              => None
    }
  }

  def specialArmWeaponEquipManagement(params: Option[(Equipment, Int, equipment.Hand)]): Unit = {
    val weapons = vehicle.Weapons
    val firstArmSlot = vehicle.Weapons.keys.min
    val leftArm = bfrHandSubsystem(Handiness.Left).get
    val rightArm = bfrHandSubsystem(Handiness.Right).get
    params match {
      case Some((item, slot, handiness)) =>
        //budget logic: the arm weapons are "next to each other" index-wise
        val otherArmEquipment = (if (firstArmSlot == slot) weapons(slot + 1) else weapons(slot - 1)).Equipment
        val definition = item.Definition
        if (
          (GlobalDefinitions.isBattleFrameArmorSiphon(definition) ||
           GlobalDefinitions.isBattleFrameNTUSiphon(definition)) &&
          otherArmEquipment.nonEmpty
        ) {
          if ((handiness == Handiness.Left && rightArm.enabled) ||
             (handiness == Handiness.Right && leftArm.enabled)
          ) {
            //there is active equipment attached to the other arm; this arm can safely be disabled
            parseObjectAction(item.GUID, action = 39, None)
          } else if (
            ((handiness == Handiness.Left && !rightArm.enabled) ||
             (handiness == Handiness.Right && !leftArm.enabled)) &&
            {
              val edef = otherArmEquipment.get.Definition
              !(GlobalDefinitions.isBattleFrameArmorSiphon(edef) || GlobalDefinitions.isBattleFrameNTUSiphon(edef))
            }
          ) {
            //there is non-siphon equipment attached to the other arm, but it needs to be re-enabled
            parseObjectAction(otherArmEquipment.get.GUID, action = 38, None)
            parseObjectAction(item.GUID, action = 39, None)
          }
        }
      case None =>
        //if one arm is a siphon, only one arm on the BFR should be enabled at a time
        (
          weapons.get(firstArmSlot  ).map { _.Equipment }.head,
          weapons.get(firstArmSlot+1).map { _.Equipment }.head
        ) match {
          case (Some(lequipment), Some(requipment)) =>
            if (leftArm.enabled && rightArm.enabled) {
              if ( {
                val lEquipDef = lequipment.Definition
                GlobalDefinitions.isBattleFrameArmorSiphon(lEquipDef) ||
                GlobalDefinitions.isBattleFrameNTUSiphon(lEquipDef)
              }) {
                //disable left arm
                parseObjectAction(lequipment.GUID, action = 39, None)
              }
              else if ( {
                val rEquipDef = requipment.Definition
                GlobalDefinitions.isBattleFrameArmorSiphon(rEquipDef) ||
                GlobalDefinitions.isBattleFrameNTUSiphon(rEquipDef)
              }) {
                //disable right arm
                parseObjectAction(requipment.GUID, action = 39, None)
              }
            }
          case (Some(lequipment), None) =>
            //make certain left arm is enabled
            parseObjectAction(lequipment.GUID, action = 38, None)
          case (None, Some(requipment)) =>
            //make certain right arm is enabled
            parseObjectAction(requipment.GUID, action = 38, None)
          case _ => ;
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
