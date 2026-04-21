// Copyright (c) 2026 PSForever
package net.psforever.objects.zones.interaction

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.Default

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait IndependentZoneInteraction {
  _: Actor =>
  /** ... */
  private var zoneInteractionIntervalDefault: FiniteDuration = 250.milliseconds
  /** ... */
  private var zoneInteractionTimer: Cancellable = Default.Cancellable

  def ZoneInteractionObject: InteractsWithZone

  val zoneInteractionBehavior: Receive = {
    case IndependentZoneInteraction.InteractionTick =>
      PerformZoneInteractionSelfReporting()

    case IndependentZoneInteraction.SelfReportRunCheck =>
      PerformSelfReportRunCheck()
  }

  def ZoneInteractionInterval: FiniteDuration = zoneInteractionIntervalDefault

  def ZoneInteractionInterval_=(interval: FiniteDuration): FiniteDuration = {
    zoneInteractionIntervalDefault = interval
    ZoneInteractionInterval
  }

  def TestToStartSelfReporting(): Boolean

  def PerformZoneInteractionSelfReporting(): Unit = {
    if (!zoneInteractionTimer.isCancelled) {
      ZoneInteractionObject.zoneInteractions()
    }
  }

  def PerformSelfReportRunCheck(): Unit = {
    if (TestToStartSelfReporting()) {
      StartInteractionSelfReporting()
    } else {
      StopInteractionSelfReporting()
    }
  }

  final def StartInteractionSelfReporting(): Unit = {
    zoneInteractionTimer.cancel()
    zoneInteractionTimer = context.system.scheduler.scheduleWithFixedDelay(
      0.seconds,
      zoneInteractionIntervalDefault,
      self,
      IndependentZoneInteraction.InteractionTick
    )
  }

  final def StartInteractionSelfReporting(initialDelay: FiniteDuration): Unit = {
    zoneInteractionTimer.cancel()
    zoneInteractionTimer = context.system.scheduler.scheduleWithFixedDelay(
      initialDelay,
      zoneInteractionIntervalDefault,
      self,
      IndependentZoneInteraction.InteractionTick
    )
  }

  final def TryStopInteractionSelfReporting(): Boolean = {
    if (!zoneInteractionTimer.isCancelled) {
      ZoneInteractionObject.resetInteractions()
      zoneInteractionTimer.cancel()
    } else {
      false
    }
  }

  final def StopInteractionSelfReporting(): Boolean = {
    ZoneInteractionObject.resetInteractions()
    zoneInteractionTimer.cancel()
  }

  final def StopInteractionSelfReportingNoReset(): Boolean = {
    zoneInteractionTimer.cancel()
  }

  final def ZoneInteractionSelfReportingIsRunning: Boolean = !zoneInteractionTimer.isCancelled
}

object IndependentZoneInteraction {
  private case object InteractionTick

  final case object SelfReportRunCheck
}
