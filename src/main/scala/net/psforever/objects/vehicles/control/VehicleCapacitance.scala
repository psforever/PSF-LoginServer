// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.control

import akka.actor.Actor
import net.psforever.objects._
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * ...
  */
trait VehicleCapacitance {
  _: Actor =>
  def CapacitanceObject: Vehicle

  protected var capacitor = Default.Cancellable

  startCapacitorTimer()

  def capacitanceStop(): Unit = {
    capacitor.cancel()
  }

  def capacitanceStopAndBlank(): Unit = {
    capacitor.cancel()
    capacitor = Default.Cancellable
  }

  def capacitancePostStop(): Unit = {
    capacitanceStopAndBlank()
    CapacitanceObject.Capacitor = 0
  }

  def capacitorBehavior: Receive = {
    case VehicleCapacitance.CapacitorCharge(amount) =>
      capacitorCharge(amount)
  }

  protected def capacitorCharge(amount: Int): Boolean = {
    capacitorOnlyCharge(amount)
    startCapacitorTimer()
    true
  }

  protected def capacitorOnlyCharge(amount: Int): Boolean = {
    val obj = CapacitanceObject
    val capacitorBefore = obj.Capacitor
    val capacitorAfter = obj.Capacitor += amount
    if (capacitorBefore != capacitorAfter) {
      showCapacitorCharge()
      true
    } else {
      false
    }
  }

  protected def showCapacitorCharge(): Unit = {
    val obj = CapacitanceObject
    obj.Zone.VehicleEvents ! VehicleServiceMessage(
      self.toString(),
      VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 113, obj.Capacitor)
    )
  }

  protected def startCapacitorTimer(): Unit = {
    val obj = CapacitanceObject
    if (obj.Capacitor < obj.Definition.MaxCapacitor) {
      capacitor.cancel()
      capacitor = context.system.scheduler.scheduleOnce(
        delay = 1000 millisecond,
        self,
        VehicleCapacitance.CapacitorCharge(obj.Definition.CapacitorRecharge)
      )
    }
  }
}

object VehicleCapacitance {
  /**
    * Charge the vehicle's internal capacitor by the given amount during the scheduled charge event.
    * @param amount how much energy in the charge
    */
  private case class CapacitorCharge(amount: Int)
}
