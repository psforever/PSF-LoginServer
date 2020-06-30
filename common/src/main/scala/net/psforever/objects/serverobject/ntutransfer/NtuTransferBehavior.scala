// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.ntutransfer

import net.psforever.objects.{Ntu, NtuContainer, NtuStorageBehavior}

trait NtuTransferBehavior extends NtuStorageBehavior {
  var ntuChargingEvent : Ntu.ChargeEvent.Value = Ntu.ChargeEvent.None
  var ntuChargingTarget : Option[NtuContainer] = None
  var findChargeTargetFunc : (NtuContainer, Option[NtuContainer])=> Option[NtuContainer] = NtuTransferBehavior.FindNoTargets
  var findDischargeTargetFunc : (NtuContainer, Option[NtuContainer])=> Option[NtuContainer] = NtuTransferBehavior.FindNoTargets

  def NtuChargeableObject : NtuContainer

  val ntuBehavior : Receive = storageBehavior.orElse {
    case NtuTransferBehavior.Charging()
      if ntuChargingEvent == Ntu.ChargeEvent.None || ntuChargingEvent == Ntu.ChargeEvent.Charging =>
      TryChargingActivity()

    case NtuTransferBehavior.Discharging()
      if ntuChargingEvent == Ntu.ChargeEvent.None || ntuChargingEvent == Ntu.ChargeEvent.Discharging =>
      TryDischargingActivity()

    case NtuTransferBehavior.Stopping() =>
      TryStopChargingEvent(NtuChargeableObject)

    case NtuTransferBehavior.Charging() | NtuTransferBehavior.Discharging() => ; //message while in wrong state
  }

  /** Charging */
  def TryChargingActivity() : Unit = {
    if(ntuChargingEvent != Ntu.ChargeEvent.Discharging) {
      val chargeable = NtuChargeableObject
      findChargeTargetFunc(chargeable, ntuChargingTarget) match {
        case Some(obj) if ntuChargingEvent == Ntu.ChargeEvent.None =>
          if(HandleNtuCharging(chargeable, obj)) {
            InitialCharge(chargeable)
          }
        case Some(obj) =>
          if(HandleNtuCharging(chargeable, obj)) {
            IncrementalCharge(chargeable)
          }
        case None if ntuChargingEvent == Ntu.ChargeEvent.Charging =>
          FinalCharge(chargeable)
          TryStopChargingEvent(chargeable)
        case _ => ;
      }
    }
  }

  def HandleNtuCharging(container : NtuContainer, target : NtuContainer) : Boolean

  def InitialCharge(container : NtuContainer) : Unit

  def IncrementalCharge(container : NtuContainer) : Unit

  def FinalCharge(container : NtuContainer) : Unit

  /** Discharging */
  def TryDischargingActivity() : Unit = {
    if(ntuChargingEvent != Ntu.ChargeEvent.Charging) {
      val chargeable = NtuChargeableObject
      //determine how close we are to something that we can discharge into
      findDischargeTargetFunc(chargeable, ntuChargingTarget) match {
        case Some(obj) if ntuChargingEvent == Ntu.ChargeEvent.None =>
          if(HandleNtuDischarging(NtuChargeableObject, obj)) {
            InitialDischarge(chargeable)
          }
        case Some(obj) =>
          if(HandleNtuDischarging(NtuChargeableObject, obj)) {
            IncrementalDischarge(chargeable)
          }
        case None if ntuChargingEvent == Ntu.ChargeEvent.Discharging =>
          FinalDischarge(chargeable)
          TryStopChargingEvent(chargeable)
        case _ => ;
      }
    }
  }

  def HandleNtuDischarging(container : NtuContainer, target : NtuContainer) : Boolean

  def InitialDischarge(container : NtuContainer) : Unit

  def IncrementalDischarge(container : NtuContainer) : Unit

  def FinalDischarge(container : NtuContainer) : Unit

  /** Stopping */
  def TryStopChargingEvent(container : NtuContainer) : Unit = {
    ntuChargingEvent = Ntu.ChargeEvent.None
    ntuChargingTarget = None
  }
}

object NtuTransferBehavior {
  final case class Charging()

  final case class Discharging()

  final case class Stopping()

  def FindNoTargets(obj : NtuContainer, ntuChargingTarget : Option[NtuContainer]) : Option[NtuContainer] = None
}
