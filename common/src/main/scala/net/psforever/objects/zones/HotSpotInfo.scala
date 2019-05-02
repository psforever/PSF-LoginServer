// Copyright (c) 2019 PSForever
package net.psforever.objects.zones

import net.psforever.types.{PlanetSideEmpire, Vector3}

import scala.concurrent.duration._

class HotSpotInfo(val DisplayLocation : Vector3) {
  private val activity : Map[PlanetSideEmpire.Value, ActivityReport] = Map(
    PlanetSideEmpire.TR -> new ActivityReport(),
    PlanetSideEmpire.NC -> new ActivityReport(),
    PlanetSideEmpire.VS -> new ActivityReport()
  )

  def Activity : Map[PlanetSideEmpire.Value, ActivityReport] = activity

  def ActivityFor(faction : PlanetSideEmpire.Value) : Option[ActivityReport] = {
    activity.get(faction)
  }

  def ActivityBy() : Set[PlanetSideEmpire.Value] = {
    for {
      faction <- PlanetSideEmpire.values
      if ActivityBy(faction)
    } yield faction
  }

  def ActivityBy(faction : PlanetSideEmpire.Value) : Boolean = {
    activity.get(faction) match {
      case Some(report) =>
        report.Heat > 0
      case None =>
        false
    }
  }
}

class ActivityReport {
  private var heat : Int = 0
  private var lastReport : Option[Long] = None
  private var duration : FiniteDuration = 0 seconds

  def Heat : Int = heat

  def LastReport : Long = lastReport match { case Some(t) => t; case _ => 0L }

  def Duration : FiniteDuration = duration

  def Duration_=(time : FiniteDuration) : FiniteDuration = {
    Duration_=(time.toNanos)
  }

  def Duration_=(time : Long) : FiniteDuration = {
    if(time > duration.toNanos) {
      duration = FiniteDuration(time, "nanoseconds")
      Renew
    }
    Duration
  }

  def Report() : ActivityReport = {
    heat += 1
    Renew
    this
  }

  def Renew : Long = {
    val t = System.nanoTime
    lastReport = Some(t)
    t
  }

  def Clear() : Unit = {
    heat = 0
    lastReport = None
    duration = FiniteDuration(0, "nanoseconds")
  }
}
