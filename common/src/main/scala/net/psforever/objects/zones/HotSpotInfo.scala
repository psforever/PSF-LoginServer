// Copyright (c) 2019 PSForever
package net.psforever.objects.zones

import net.psforever.types.{PlanetSideEmpire, Vector3}

import scala.concurrent.duration._

/**
  * Information necessary to determine if a hotspot should be displayed.
  * Hotspots are used on the zone maps to indicate activity.
  * Each of the factions will view different hotspot configurations
  * but one faction may encounter hotspots in the same places as another faction
  * with information referring to the same encounter.
  * @param DisplayLocation the coordinates where the hotspot will be depicted on some zone's map
  */
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

  /**
    * Which factions claim a current level of activity that they might see this hotspot?
    * @return the active factions
    */
  def ActivityBy() : Set[PlanetSideEmpire.Value] = {
    for {
      faction <- PlanetSideEmpire.values
      if ActivityBy(faction)
    } yield faction
  }

  /**
    * Does a specific faction claim a current level of activity that they might see this hotspot?
    * @param faction the faction
    * @return `true`, if the heat level is non-zero;
    *        `false`, otherwise
    */
  def ActivityBy(faction : PlanetSideEmpire.Value) : Boolean = {
    activity.get(faction) match {
      case Some(report) =>
        report.Heat > 0
      case None =>
        false
    }
  }
}

/**
  * Information about interactions in respect to a given denomination in the game world.
  * In terms of hotspots, the "denomination" are the factions.
  * While a given report of activity will only be valid from the time or arrival for a given amount of time,
  * subsequent activity reporting before this duration concludes will cause the lifespan to artificially increase.
  */
class ActivityReport {
  /** heat increases each time the hotspot is considered active and receives more activity */
  private var heat : Int = 0
  /** the time of the last activity report */
  private var lastReport : Option[Long] = None
  /** the length of time from the last reporting that this (ongoing) activity will stay relevant */
  private var duration : FiniteDuration = 0 seconds

  /**
    * The increasing heat does nothing, presently, but acts as a flag for activity.
    * @return the heat
    */
  def Heat : Int = heat

  /**
    * As a `Long` value, if there was no previous report, the value will be considered `0L`.
    * @return the time of the last activity report
    */
  def LastReport : Long = lastReport.getOrElse(0L)

  def SetLastReport(time : Long) : Long = {
    lastReport = Some(time)
    LastReport
  }

  /**
    * The length of time that this (ongoing) activity is relevant.
    * @return the time
    */
  def Duration : FiniteDuration = duration

  /**
    * Set the length of time that this (ongoing) activity is relevant.
    * @param time the time, as a `Duration`
    * @return the time
    */
  def Duration_=(time : FiniteDuration) : FiniteDuration = {
    Duration_=(time.toNanos)
  }

  /**
    * Set the length of time that this (ongoing) activity is relevant.
    * The duration length can only increase.
    * @param time the time, as a `Long` value
    * @return the time
    */
  def Duration_=(time : Long) : FiniteDuration = {
    if(time > duration.toNanos) {
      duration = FiniteDuration(time, "nanoseconds")
      Renew
    }
    Duration
  }

  /**
    * Submit new activity, increasing the lifespan of the current report's existence.
    * @see `Renew`
    * @return the current report
    */
  def Report() : ActivityReport = {
    RaiseHeat(1)
    Renew
    this
  }

  /**
    * Submit new activity, increasing the lifespan of the current report's existence.
    * @see `Renew`
    * @return the current report
    */
  def Report(pow : Int) : ActivityReport = {
    RaiseHeat(pow)
    Renew
    this
  }

  /**
    * Submit new activity.
    * Do not increase the lifespan of the current report's existence.
    * @return the current report
    */
  def ReportOld(pow : Int) : ActivityReport = {
    RaiseHeat(pow)
    this
  }

  private def RaiseHeat(addHeat : Int) : Int = {
    if(addHeat < (Integer.MAX_VALUE - heat)) {
      heat += addHeat
    }
    else {
      heat = Integer.MAX_VALUE
    }
    heat
  }

  /**
    * Reset the time of the last report to the present.
    * @return the current time
    */
  def Renew : Long = {
    val t = System.nanoTime
    lastReport = Some(t)
    t
  }

  /**
    * Act as if no activity was ever valid for this report.
    * Set heat to zero to flag no activity and set duration to "0 seconds" to eliminate its lifespan.
    */
  def Clear() : Unit = {
    heat = 0
    lastReport = None
    duration = FiniteDuration(0, "nanoseconds")
  }
}
