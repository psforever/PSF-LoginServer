// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles

import akka.actor.ActorRef
import akka.actor.typed.scaladsl.adapter._
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.{NtuContainer, NtuContainerDefinition, _}
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.equipment.EquipmentSlot
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.WarpGate
import net.psforever.objects.serverobject.transfer.{TransferBehavior, TransferContainer}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait BfrTransferBehavior
  extends TransferBehavior
  with NtuStorageBehavior {
  var ntuProcessingRequest: Boolean = false
  var ntuProcessingTick             = Default.Cancellable

  findChargeTargetFunc              = Vehicles.FindBfrChargingSource
  findDischargeTargetFunc           = Vehicles.FindBfrDischargingTarget

  def TransferMaterial = Ntu.Nanites

  private var pairedSlotList: Option[List[(VehicleSubsystem, (Int, EquipmentSlot))]] = None
  /**
    * Return the paired arm weapon subsystems with arm weapon equipment mount and the slot number for that mount,
    * connecting "left" to "left" and "right" to "right".
    * Either return the existing connection or create that connection for the first time and retain it for future use.
    * Works regardless of the type of battleframe unit.
    * @return the arm weapon subsystems for each arm weapon mount and that mount's slot number
    */
  def pairedArmSlotSubsystems(): List[(VehicleSubsystem, (Int, EquipmentSlot))] = {
    pairedSlotList.getOrElse {
      val obj = ChargeTransferObject
      val pairs = obj.Subsystems()
        .filter { sub =>
          sub.sys.name.startsWith("BattleframeLeftArm") || sub.sys.name.startsWith("BattleframeRightArm")
        }
        .zip(
          obj.Weapons.filter { case (a, _) =>
            a == 1 || a == 2 || a == 3 //gunner -> 2,3; flight -> 1,2
          }
        )
      pairedSlotList = Some(pairs)
      pairs
    }
  }
  private var pairedList: Option[List[(VehicleSubsystem, EquipmentSlot)]] = None
  /**
    * Return the paired arm weapon subsystems with arm weapon mount,
    * connecting "left" to "left" and "right" to "right".
    * Either return the existing connection or create that connection for the first time and retain it for future use.
    * Works regardless of the type of battleframe unit.
    * @return the arm weapon subsystems for each arm weapon mount
    */
  def pairedArmSubsystems(): List[(VehicleSubsystem, EquipmentSlot)] = {
    pairedList.getOrElse {
      val pairs = pairedArmSlotSubsystems().map { case (a, (_, c)) => (a, c) }
      pairedList = Some(pairs)
      pairs
    }
  }

  def getNtuContainer(): Option[NtuContainer] = {
    pairedArmSubsystems()
      .find { case (sub, arm) =>
        //find an active ntu siphon
        arm.Equipment.nonEmpty &&
        GlobalDefinitions.isBattleFrameNTUSiphon(arm.Equipment.get.Definition) &&
        sub.Enabled
      }
      .map { d => d._2.Equipment.get } match {
      case Some(equipment: Tool) =>
        Some(new NtuSiphon(equipment, ChargeTransferObject.Definition))
      case _ =>
        None
    }
  }

  def ChargeTransferObject: Vehicle with NtuContainer

  def bfrBehavior: Receive = storageBehavior
    .orElse(transferBehavior)
    .orElse {
      case BfrTransferBehavior.NextProcessTick(event) =>
        transferTarget match {
          case Some(target)
            if event == transferEvent && ntuProcessingRequest && event == TransferBehavior.Event.Charging =>
            HandleChargingOps(target)
          case Some(target)
            if event == transferEvent && ntuProcessingRequest && event == TransferBehavior.Event.Discharging =>
            HandleDischargingEvent(target)
          case Some(target)
            if event == transferEvent && !ntuProcessingRequest =>
            TryStopChargingEvent(target)
          case _ => ;
            TryStopChargingEvent(ChargeTransferObject)
        }
    }

  def UpdateNtuUI(vehicle: Vehicle with NtuContainer): Unit = {
    getNtuContainer() match {
      case Some(siphon) =>
        UpdateNtuUI(vehicle, siphon)
      case None => ;
    }
  }

  def UpdateNtuUI(vehicle: Vehicle with NtuContainer, siphon: NtuContainer): Unit = {
    siphon match {
      case equip: NtuSiphon =>
        vehicle.Zone.VehicleEvents ! VehicleServiceMessage(
          vehicle.Actor.toString,
          VehicleAction.InventoryState2(PlanetSideGUID(0), equip.storageGUID, siphon.GUID, siphon.NtuCapacitor.toInt)
        )
      case _ => ;
    }
  }

  def HandleChargingEvent(target: TransferContainer): Boolean = {
    if (transferEvent == TransferBehavior.Event.None) {
      HandleChargingOps(target)
    } else {
      ntuProcessingRequest = true
      false
    }
  }

  def HandleChargingOps(target: TransferContainer): Boolean = {
    ntuProcessingRequest = false
    getNtuContainer() match {
      case Some(siphon: NtuSiphon)
        if siphon.NtuCapacitor < siphon.MaxNtuCapacitor =>
        //charging
        transferTarget = Some(target)
        transferEvent = TransferBehavior.Event.Charging
        val max = siphon.NtuCapacitor
        val fromMax = siphon.MaxNtuCapacitor - max
        target match {
          case _: WarpGate =>
            //siphon.drain -> math.min(math.min(siphon.MaxNtuCapacitor / 75f, fromMax)
            target.Actor ! BuildingActor.Ntu(NtuCommand.Request(math.min(siphon.drain.toFloat, fromMax), context.self))
          case _: ResourceSilo =>
            //siphon.drain -> scala.math.min(silo.MaxNtuCapacitor * 0.325f / max, fromMax)
            target.Actor ! NtuCommand.Request(scala.math.min(0.5f * siphon.drain, fromMax), context.self)
          case _ => ;
        }
        ntuProcessingTick.cancel()
        ntuProcessingTick = context.system.scheduler.scheduleOnce(
          delay = 1250 milliseconds,
          self,
          BfrTransferBehavior.NextProcessTick(transferEvent)
        )
        true
      case _ =>
        TryStopChargingEvent(ChargeTransferObject)
        false
    }
  }

  def ReceiveAndDepositUntilFull(vehicle: Vehicle, amount: Float): Boolean = {
    getNtuContainer() match {
      case Some(siphon) =>
        ReceiveAndDepositUntilFull(vehicle, siphon, amount)
      case None =>
        false
    }
  }

  def ReceiveAndDepositUntilFull(vehicle: Vehicle, obj: NtuContainer, amount: Float): Boolean = {
    val isNotFull = (obj.NtuCapacitor += amount) < obj.MaxNtuCapacitor
    UpdateNtuUI(vehicle, obj)
    isNotFull
  }

  /** Discharging */
  def HandleDischargingEvent(target: TransferContainer): Boolean = {
    if (transferEvent == TransferBehavior.Event.None) {
      HandleDischargingOps(target)
    } else {
      ntuProcessingRequest = true
      false
    }
  }

  def HandleDischargingOps(target: TransferContainer): Boolean = {
    ntuProcessingRequest = false
    val obj = ChargeTransferObject
    getNtuContainer() match {
      case Some(siphon)
        if siphon.NtuCapacitor > 0 =>
        transferTarget = Some(target)
        transferEvent = TransferBehavior.Event.Discharging
        target.Actor ! Ntu.Offer(obj)
        ntuProcessingTick.cancel()
        ntuProcessingTick = context.system.scheduler.scheduleOnce(
          delay = 1250 milliseconds,
          self,
          BfrTransferBehavior.NextProcessTick(transferEvent)
        )
        true
      case _ =>
        TryStopChargingEvent(obj)
        false
    }
  }

  def WithdrawAndTransmit(vehicle: Vehicle, maxRequested: Float): Any = {
    val chargeable      = ChargeTransferObject
    val chargeToDeposit = getNtuContainer() match {
      case Some(siphon) =>
        var chargeToDeposit = Math.min(Math.min(siphon.NtuCapacitor, 100), maxRequested)
        siphon.NtuCapacitor -= chargeToDeposit
        UpdateNtuUI(chargeable, siphon)
        chargeToDeposit
      case _ =>
        0
    }
    Ntu.Grant(chargeable, chargeToDeposit)
  }

  /** Stopping */
  override def TryStopChargingEvent(container: TransferContainer): Unit = {
    ntuProcessingTick.cancel()
    ntuProcessingRequest = false
    transferTarget match {
      case Some(target: WarpGate) =>
        target.Actor ! BuildingActor.Ntu(NtuCommand.Grant(null, 0))
      case Some(target) =>
        target.Actor ! NtuCommand.Grant(null, 0)
      case _ => ;
    }
    //cleanup
    val obj = ChargeTransferObject
    super.TryStopChargingEvent(obj)
  }

  def StopNtuBehavior(sender: ActorRef): Unit = TryStopChargingEvent(ChargeTransferObject)

  def HandleNtuOffer(sender: ActorRef, src: NtuContainer): Unit = {}

  def HandleNtuRequest(sender: ActorRef, min: Float, max: Float): Unit = {
    val chargeable = ChargeTransferObject
    getNtuContainer() match {
      case Some(siphon) =>
        if (transferEvent == TransferBehavior.Event.Discharging) {
          val capacitor = siphon.NtuCapacitor
          val bonus = System.currentTimeMillis()%2
          val (chargeBase, chargeToDeposit): (Float, Float) = if (min == 0) {
            transferTarget match {
              case Some(silo: ResourceSilo) =>
                // silos would charge from 0-30% in a full siphon's payload according to the wiki
                val calcChargeBase = scala.math.min(scala.math.min(silo.MaxNtuCapacitor * 0.325f / siphon.MaxNtuCapacitor, capacitor), max)
                (calcChargeBase, calcChargeBase + bonus)
              case _ =>
                (0f, 0)
            }
          } else {
            val charge = scala.math.min(min, capacitor)
            (charge, charge + bonus)
          }
          siphon.NtuCapacitor -= chargeBase
          UpdateNtuUI(chargeable, siphon)
          sender ! Ntu.Grant(chargeable, chargeToDeposit)
        } else {
          TryStopChargingEvent(chargeable)
          sender ! Ntu.Grant(chargeable, 0)
        }
      case None => ;
    }
  }

  def HandleNtuGrant(sender: ActorRef, src: NtuContainer, amount: Float): Unit = {
    val obj = ChargeTransferObject
    if (transferEvent != TransferBehavior.Event.Charging || !ReceiveAndDepositUntilFull(obj, amount)) {
      sender ! Ntu.Request(0, 0)
    }
  }
}

object BfrTransferBehavior {
  private case class NextProcessTick(eventType: TransferBehavior.Event.Value)
}

class NtuSiphon(
                 val equipment: Tool,
                 private val definition: ObjectDefinition with NtuContainerDefinition
               ) extends NtuContainer {
  def Faction: PlanetSideEmpire.Value = equipment.Faction

  def storageGUID: PlanetSideGUID = equipment.AmmoSlot.Box.GUID

  def drain: Int = equipment.FireMode.RoundsPerShot

  def NtuCapacitor: Float = equipment.Magazine.toFloat

  def NtuCapacitor_=(value: Float): Float = equipment.Magazine_=(value.toInt).toFloat

  def MaxNtuCapacitor: Float = equipment.MaxMagazine.toFloat

  override def Definition: ObjectDefinition with NtuContainerDefinition = definition

  def Actor: ActorRef = null

  override def GUID : PlanetSideGUID = equipment.GUID

  override def GUID_=(guid : PlanetSideGUID): PlanetSideGUID = equipment.GUID

  override def Position: Vector3 = Vector3.Zero

  override def Position_=(vec: Vector3): Vector3 = Vector3.Zero

  override def Orientation: Vector3 = Vector3.Zero

  override def Orientation_=(vec: Vector3): Vector3 = Vector3.Zero

  override def Velocity: Option[Vector3] = None

  override def Velocity_=(vec: Option[Vector3]): Option[Vector3] = None
}
