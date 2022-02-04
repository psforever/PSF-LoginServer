// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import akka.actor.Cancellable
import net.psforever.objects._
import net.psforever.objects.ballistics.VehicleSource
import net.psforever.objects.definition.{ToolDefinition, VehicleDefinition}
import net.psforever.objects.equipment._
import net.psforever.objects.inventory.{GridInventory, InventoryItem}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.containable.ContainableBehavior
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.transfer.TransferBehavior
import net.psforever.objects.vehicles._
import net.psforever.objects.vital.VehicleShieldCharge
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.zones.Zone
import net.psforever.packet.game._
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * A vehicle control agency exclusive to the battleframe robotics (BFR) combat vehicle system.
  * @param vehicle the battleframe robotics unit
  */
class BfrControl(vehicle: Vehicle)
  extends VehicleControl(vehicle)
  with BfrTransferBehavior
  with ArmorSiphonBehavior.SiphonOwner {
  /** shield-auto charge;
    * active timer indicates a charging shield;
    * `Default.Cancellable` indicates a technical pause in charging;
    * `Cancellable.alreadyCancelled` indicates a permanant cessation of charging activity (vehicle destruction) */
  var shieldCharge: Cancellable = Default.Cancellable

  def SiphoningObject = vehicle

  def ChargeTransferObject = vehicle

  if (vehicle.Shields < vehicle.MaxShields) {
    chargeShields(amount = 0) //start charging if starts as uncharged
  }

  override def postStop(): Unit = {
    super.postStop()
    shieldCharge.cancel()
    repairPostStop()
  }

  def explosionBehavior: Receive = {
    case BfrControl.VehicleExplosion =>
      val guid = vehicle.GUID
      val guid0 = Service.defaultPlayerGUID
      val zone = vehicle.Zone
      val zoneid = zone.id
      val events = zone.VehicleEvents
      events ! VehicleServiceMessage(
        zoneid,
        VehicleAction.GenericObjectAction(guid0, guid, 46)
      )
      context.system.scheduler.scheduleOnce(delay = 500 milliseconds, self, BfrControl.VehicleExplosion)
  }

  override def commonEnabledBehavior: Receive = super.commonEnabledBehavior
    .orElse(siphonRepairBehavior)
    .orElse(bfrBehavior)
    .orElse(explosionBehavior)
    .orElse {
      case CommonMessages.Use(_, Some(item: Tool)) =>
        if (GlobalDefinitions.isBattleFrameNTUSiphon(item.Definition)) {
          context.system.scheduler.scheduleOnce(
            delay = 1000 milliseconds,
            self,
            TransferBehavior.Charging(Ntu.Nanites)
          )
        }

      case SpecialEmp.Burst() =>
        performEmpBurst()
    }

  override def commonDisabledBehavior: Receive = super.commonDisabledBehavior.orElse(explosionBehavior)

  override def PrepareForDisabled(kickPassengers: Boolean) : Unit = {
    super.PrepareForDisabled(kickPassengers)
    if (vehicle.Health == 0) {
      //shield off
      disableShield()
    }
  }

  override def damageChannels(obj: Vehicle): (String, String) = {
    val channel = obj.Zone.id
    (channel, channel)
  }

  override def DamageAwareness(target: Target, cause: DamageResult, amount: Any) : Unit = {
    super.DamageAwareness(target, cause, amount)
    //manage shield display and charge
    disableShieldIfDrained()
    if (shieldCharge != Cancellable.alreadyCancelled && vehicle.Shields < vehicle.MaxShields) {
      shieldCharge.cancel()
      shieldCharge = context.system.scheduler.scheduleOnce(
        delay = vehicle.Definition.ShieldDamageDelay milliseconds,
        self,
        Vehicle.ChargeShields(0)
      )
    }
  }

  override def destructionDelayed(delay: Long, cause: DamageResult): Unit = {
    super.destructionDelayed(delay, cause)
    shieldCharge.cancel()
    shieldCharge = Cancellable.alreadyCancelled
    //harmless boom boom's
    context.system.scheduler.scheduleOnce(delay = 0 milliseconds, self, BfrControl.VehicleExplosion)
  }

  override def DestructionAwareness(target: Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    shieldCharge.cancel()
    shieldCharge = Cancellable.alreadyCancelled
    disableShield()
  }

  override def RemoveItemFromSlotCallback(item: Equipment, slot: Int): Unit = {
    BfrControl.dimorphics.find { _.contains(item.Definition) } match {
      case Some(dimorph) if vehicle.VisibleSlots.contains(slot) => //revert to a generic variant
        Tool.LoadDefinition(
          item.asInstanceOf[Tool],
          dimorph.transform(Handiness.Generic).asInstanceOf[ToolDefinition]
        )
      case _ => ; //no dimorphic entry; place as-is
    }
    val guid0 = PlanetSideGUID(0)
    //if the weapon arm is disabled, enable it for later (makes life easy)
    parseObjectAction(guid0, BfrControl.ArmState.Enabled, Some(slot))
    //enable the other arm weapon regardless
    parseObjectAction(guid0, BfrControl.ArmState.Enabled, Some(
      //budget logic: the arm weapons are "next to each other" index-wise
      if (vehicle.Weapons.keys.min == slot) { slot + 1 } else { slot - 1 }
    ))
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
    specialArmWeaponEquipManagement(item, slot, handiness)
  }

  override def dismountCleanup(seatBeingDismounted: Int): Unit = {
    super.dismountCleanup(seatBeingDismounted)
    if (!vehicle.Seats.values.exists(_.isOccupied)) {
      vehicle.Subsystems(VehicleSubsystemEntry.BattleframeShieldGenerator) match {
        case Some(subsys) =>
            if (vehicle.Shields > 0) {
              vehicleSubsystemMessages(
                if (subsys.Enabled && !subsys.Enabled_=(state = false)) {
                  //turn off shield visually
                  subsys.changedMessages(vehicle)
                } else if (subsys.Jammed || subsys.stateOfStatus(statusName = "Damaged").contains(false)) {
                  //hard coded: shield is "off" functionally, turn off static effect and turn off standard shield swirl
                  ComponentDamageMessage(vehicle.GUID, SubsystemComponent.ShieldGeneratorOffline, None) +:
                  BattleframeShieldGeneratorOffline.getMessage(SubsystemComponent.ShieldGeneratorOffline, vehicle, vehicle.GUID)
                } else {
                  //shield is already off visually
                  Nil
                }
              )
            }
        case _ => ;
      }
    }
  }

  override def mountCleanup(mount_point: Int, user: Player): Unit = {
    super.mountCleanup(mount_point, user)
    if (vehicle.Seats.values.exists(_.isOccupied)) {
      vehicle.Subsystems(VehicleSubsystemEntry.BattleframeShieldGenerator) match {
        case Some(subsys)
          if !subsys.Enabled && vehicle.Shields > 0 && subsys.Enabled_=(state = true) =>
          //if the shield is damaged, it does not turn on until the damaged is cleared
          vehicleSubsystemMessages(subsys.changedMessages(vehicle))
        case _ => ;
      }
    }
  }

  override def permitTerminalMessage(player: Player, msg: ItemTransactionMessage): Boolean = {
    if (msg.transaction_type == TransactionType.Loadout) {
      !vehicle.Jammed
    } else {
      true
    }
  }

  override def handleTerminalMessageVehicleLoadout(
                                                    player: Player,
                                                    definition: VehicleDefinition,
                                                    weapons: List[InventoryItem],
                                                    inventory: List[InventoryItem]
                                                  ): (
      List[(Equipment, PlanetSideGUID)],
      List[InventoryItem],
      List[(Equipment, PlanetSideGUID)],
      List[InventoryItem]
    ) = {
    val vFaction = vehicle.Faction
    val vWeapons = vehicle.Weapons
    //remove old inventory
    val oldInventory = vehicle.Inventory.Clear().map { case InventoryItem(obj, _) => (obj, obj.GUID) }
    //"dropped" items are lost; if it doesn't go in the trunk, it vanishes into the nanite cloud
    val (_, afterInventory) = inventory.partition(ContainableBehavior.DropPredicate(player))
    val pairedArmSubsys = pairedArmSubsystems()
    val (oldWeapons, newWeapons, finalInventory) = if (GlobalDefinitions.isBattleFrameVehicle(definition)) {
      //vehicles are both battleframes; weapons must be swapped properly
      if(vWeapons.size == 3 && GlobalDefinitions.isBattleFrameFlightVehicle(definition)) {
        //battleframe is a gunner variant but loadout spec is for flight variant
        // remap the hands, ignore the gunner weapon mount, and refit the trunk
        val (stow, _) = GridInventory.recoverInventory(afterInventory, vehicle.Inventory)
        val afterWeapons = weapons
          .map { item => item.start += 1; item }
        (culledWeaponMounts(pairedArmSubsys.unzip._2), afterWeapons, stow)
      } else if(vWeapons.size == 2 && GlobalDefinitions.isBattleFrameGunnerVehicle(definition)) {
        //battleframe is a flight variant but loadout spec is for gunner variant
        // remap the hands, shave the gunner mount from the spec, and refit the trunk
        val (stow, _) = GridInventory.recoverInventory(afterInventory, vehicle.Inventory)
        val afterWeapons = weapons
          .filterNot { _.obj.Size == EquipmentSize.BFRGunnerWeapon }
          .map { item => item.start -= 1; item }
        (culledWeaponMounts(vWeapons.values), afterWeapons, stow)
      } else {
        //same variant type of battleframe
        // place as-is
        (culledWeaponMounts(vWeapons.values), weapons, afterInventory)
      }
    }
    else {
      //vehicle loadout is not for this vehicle; do not transfer over weapon ammo
      if (
        vehicle.Definition.TrunkSize == definition.TrunkSize && vehicle.Definition.TrunkOffset == definition.TrunkOffset
      ) {
        (Nil, Nil, afterInventory) //trunk is the same dimensions, however
      }
      else {
        //accommodate as much of inventory as possible
        val (stow, _) = GridInventory.recoverInventory(afterInventory, vehicle.Inventory)
        (Nil, Nil, stow)
      }
    }
    finalInventory.foreach {
      _.obj.Faction = vFaction
    }
    (oldWeapons, newWeapons, oldInventory, finalInventory)
  }

  def culledWeaponMounts(values: Iterable[EquipmentSlot]): List[(Equipment, PlanetSideGUID)] = {
    values.collect { case slot if slot.Equipment.nonEmpty =>
      val obj = slot.Equipment.get
      slot.Equipment = None
      (obj, obj.GUID)
    }.toList
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

  def enableShieldIfNotDrained(): Unit = {
    if (vehicle.Shields > 0) {
      enableShield()
    }
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
      val chargeAmount = math.max(1, ((if (vehicle.DeploymentState == DriveState.Kneeling && vehicle.Seats(0).occupant.nonEmpty) {
        definition.ShieldAutoRechargeSpecial
      } else {
        definition.ShieldAutoRecharge
      }).getOrElse(amount) * vehicle.SubsystemStatusMultiplier(sys = "BattleframeShieldGenerator.RechargeRate")).toInt)
      vehicle.Shields = before + chargeAmount
      val after = vehicle.Shields
      vehicle.History(VehicleShieldCharge(VehicleSource(vehicle), after - before))
      showShieldCharge()
      if (before == 0 && after > 0) {
        enableShield()
      }
    }
  }

  def shieldCharge(delay: Long): Unit = {
    shieldCharge(vehicle.Shields, vehicle.Definition, delay)
  }

  def shieldCharge(after: Int, definition: VehicleDefinition, delay: Long): Unit = {
    shieldCharge.cancel()
    if (after < definition.MaxShields && !vehicle.Jammed) {
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
      zone.id,
      VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, vguid, vehicle.Definition.shieldUiAttribute, shields)
    )
  }

  override def StartJammeredStatus(target: Any, dur: Int): Unit = {
    super.StartJammeredStatus(target, dur)
    //cancels shield charge timer
    shieldCharge(after = 0, vehicle.Definition, delay = 0)
  }

  override def CancelJammeredStatus(target: Any): Unit = {
    super.CancelJammeredStatus(target)
    //restarts shield charge timer
    shieldCharge(vehicle.Shields, vehicle.Definition, delay = 100)
  }

  override def JammableMountedWeaponsJammeredStatus(target: PlanetSideServerObject with MountedWeapons, statusCode: Int): Unit = {
    /** bfr weapons do not jam the same way normal vehicle weapons do */
  }

  override def parseObjectAction(guid: PlanetSideGUID, action: Int, other: Option[Any]): Unit = {
    super.parseObjectAction(guid, action, other)
    if (action == BfrControl.ArmState.Enabled || action == BfrControl.ArmState.Disabled) {
      //disable or enable fire control for the left arm weapon or for the right arm weapon
      ((other match {
        case Some(slot: Int)     => (slot, bfrHandSubsystem(bfrHandiness(slot)))
        case _ =>
          vehicle.Weapons.find { case (_, slot) => slot.Equipment.nonEmpty && slot.Equipment.get.GUID == guid } match {
            case Some((slot, _)) => (slot, bfrHandSubsystem(bfrHandiness(slot)))
            case _               => (0, None)
          }
      }) match {
        case out @ (_, Some(subsystem)) =>
          if (action == BfrControl.ArmState.Enabled && !subsystem.Enabled) {
            subsystem.Enabled = true
            out
          } else if (action == BfrControl.ArmState.Disabled && subsystem.Enabled) {
            subsystem.Enabled = false
            out
          } else {
            (0, None)
          }
        case _ =>
          (0, None)
      }) match {
        case (slot, Some(_)) =>
          specialArmWeaponActiveManagement(slot)
          val guid0 = Service.defaultPlayerGUID
          val doNotSendTo = other match {
            case Some(pguid: PlanetSideGUID) => pguid
            case _                           => guid0
          }
          (if (guid == guid0) {
            vehicle.Weapons(slot).Equipment match {
              case Some(equip) => Some(equip.GUID)
              case None        => None
            }
          } else {
            Some(guid)
          }) match {
            case Some(useThisGuid) =>
              val zone = vehicle.Zone
              zone.VehicleEvents ! VehicleServiceMessage(
                zone.id,
                VehicleAction.GenericObjectAction(doNotSendTo, useThisGuid, action)
              )
            case _ => ;
          }
        case _ => ;
      }
    }
  }

  def bfrHandiness(side: equipment.Hand): Int = {
    if (side == Handiness.Left) 2
    else if (side == Handiness.Right) 3
    else throw new Exception("no hand associated with this slot")
  }

  def bfrHandiness(slot: Int): equipment.Hand = {
    //for the benefit of BFR equipment slots interacting with MoveItemMessage
    if (slot == 2) Handiness.Left
    else if (slot == 3) Handiness.Right
    else Handiness.Generic
  }

  def bfrHandSubsystem(side: equipment.Hand): Option[VehicleSubsystem] = {
    //for the benefit of BFR equipment slots interacting with MoveItemMessage
    side match {
      case Handiness.Left  => vehicle.Subsystems(VehicleSubsystemEntry.BattleframeLeftArm)
      case Handiness.Right => vehicle.Subsystems(VehicleSubsystemEntry.BattleframeRightArm)
      case _               => None
    }
  }

  def specialArmWeaponEquipManagement(item: Equipment, slot: Int, handiness: equipment.Hand): Unit = {
    if (item.Size == EquipmentSize.BFRArmWeapon && vehicle.VisibleSlots.contains(slot)) {
      val weapons = vehicle.Weapons
      //budget logic: the arm weapons are "next to each other" index-wise
      val firstArmSlot = vehicle.Weapons.keys.min
      val otherArmSlot = if (firstArmSlot == slot) {
        slot + 1
      }
      else {
        slot - 1
      }
      val otherArmEquipment = weapons(otherArmSlot).Equipment
      if ( {
             val itemDef = item.Definition
             GlobalDefinitions.isBattleFrameArmorSiphon(itemDef) || GlobalDefinitions.isBattleFrameNTUSiphon(itemDef)
           } ||
           (otherArmEquipment match {
             case Some(thing) =>
               //some equipment is attached to the other arm weapon mount
               val otherDef = thing.Definition
               GlobalDefinitions.isBattleFrameArmorSiphon(otherDef) || GlobalDefinitions.isBattleFrameNTUSiphon(otherDef)
             case None =>
               false
           })
      ) {
        //installing a siphon; this siphon can safely be disabled
        //alternately, installing normal equipment, but the other arm weapon is a siphon
        parseObjectAction(PlanetSideGUID(0), BfrControl.ArmState.Enabled, Some(otherArmSlot)) //ensure enabled
        parseObjectAction(item.GUID, BfrControl.ArmState.Disabled, Some(slot))
      }
    }
  }

  /** since `specialArmWeaponActiveManagement` is called from `parseObjectAction`,
    * and `parseObjectAction` gets called in `specialArmWeaponActiveManagement`,
    * kill endless logic loops before they can happen */
  var notSpecialManagingArmWeapon: Boolean = true
  def specialArmWeaponActiveManagement(slotChanged: Int): Unit = {
    if (notSpecialManagingArmWeapon) {
      notSpecialManagingArmWeapon = false
      val (thisArm, otherArm) = {
        val pairedSystemsToSlots = pairedArmSlotSubsystems()
        if (pairedSystemsToSlots.head._2._1 == slotChanged) {
          (pairedSystemsToSlots.head, pairedSystemsToSlots(1))
        }
        else {
          (pairedSystemsToSlots(1), pairedSystemsToSlots.head)
        }
      }
      if (thisArm._1.Enabled) {
        //this arm weapon slot was enabled
        if ({
          val (thisArmExists, thisArmIsSiphon) = thisArm._2._2.Equipment match {
            case Some(thing) =>
              //some equipment is attached to the other arm weapon mount
              val definition = thing.Definition
              (
                true,
                GlobalDefinitions.isBattleFrameArmorSiphon(definition) || GlobalDefinitions.isBattleFrameNTUSiphon(definition)
              )
            case None =>
              (false, false)
          }
          val (otherArmExists, otherArmIsSiphon) = otherArm._2._2.Equipment match {
            case Some(thing) =>
              //some equipment is attached to the other arm weapon mount
              val definition = thing.Definition
              (
                true,
                GlobalDefinitions.isBattleFrameArmorSiphon(definition) || GlobalDefinitions.isBattleFrameNTUSiphon(definition)
              )
            case None =>
              (false, false)
          }
          thisArmExists && otherArmExists && (thisArmIsSiphon || otherArmIsSiphon)
        }) {
          //both arms weapons are installed and at least one of them is a siphon
          parseObjectAction(PlanetSideGUID(0), BfrControl.ArmState.Disabled, Some(otherArm._2._1))
        }
      }
      else {
        //this arm weapon slot was disabled
        thisArm._2._2.Equipment match {
          case Some(item) =>
            parseObjectAction(item.GUID, BfrControl.ArmState.Enabled, Some(otherArm._2._1)) //other arm must be enabled
          case None =>
            parseObjectAction(PlanetSideGUID(0), BfrControl.ArmState.Enabled, Some(thisArm._2._1)) //must stay enabled
        }
      }
      notSpecialManagingArmWeapon = true
    }
  }

  def performEmpBurst(): Unit = {
    val now = System.currentTimeMillis()
    val obj = ChargeTransferObject
    val zone = obj.Zone
    val events = zone.VehicleEvents
    val GUID0 = Service.defaultPlayerGUID
    getNtuContainer() match {
      case Some(siphon : NtuSiphon)
        if GlobalDefinitions.isBattleFrameNTUSiphon(siphon.equipment.Definition) &&
           siphon.equipment.FireModeIndex == 1 &&
           siphon.NtuCapacitor > 29 =>
        val elapsedWait = now - siphon.equipment.lastDischarge
        if (elapsedWait >= 30000) {
          val pos = obj.Position
          val emp = siphon.equipment.Projectile
          val faction = obj.Faction
          //need at least 30 ntu, so consume the charge
          siphon.NtuCapacitor -= 30
          UpdateNtuUI(obj, siphon)
          //cause the emp
          siphon.equipment.lastDischarge = now
          //TODO this is the apc emp effect; is there an ntu siphon emp effect?
          events ! VehicleServiceMessage(
            zone.id,
            VehicleAction.SendResponse(
              GUID0,
              TriggerEffectMessage(
                GUID0,
                s"apc_explosion_emp_${faction.toString.toLowerCase}",
                None,
                Some(TriggeredEffectLocation(pos, obj.Orientation))
              )
            )
          )
          //resolve what targets are affected by the emp
          Zone.serverSideDamage(
            zone,
            obj,
            emp,
            SpecialEmp.createEmpInteraction(emp, pos),
            ExplosiveDeployableControl.detectionForExplosiveSource(obj),
            Zone.findAllTargets
          )
        } else {
          //the siphon is not ready to dispatch another emp; chat message borrowed from kit use logic
          //the client actually enforces a hard limit of 30s before it will react to use of the siphon emp mode
          //it does not even dispatch the packet before that, making it rare if this precautionary message is seen
          events ! VehicleServiceMessage(
            obj.Seats(0).occupant.get.Name,
            VehicleAction.SendResponse(
              GUID0,
              ChatMsg(ChatMessageType.UNK_225, wideContents = false, "", s"@TimeUntilNextUse^${30000 - elapsedWait}", None)
            )
          )
        }
      case _ => ;
    }
  }
}

object BfrControl {
  /** arm state values related to the `GenericObjectActionMessage` action codes */
  object ArmState extends Enumeration {
    final val Enabled  = 38
    final val Disabled = 39
  }
  
  private case object VehicleExplosion

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
