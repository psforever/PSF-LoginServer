// Copyright (c) 2019 PSForever
package net.psforever.objects.zones

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects.DefaultCancellable
import net.psforever.types.PlanetSideEmpire
import services.ServiceManager

import scala.concurrent.duration._

/**
  * Manage hotspot information for a given zone,
  * keeping track of aggressive faction interactions,
  * and maintaining the visibility state of the hotspots that alert of the location of that activity.
  * @param zone the zone
  */
class ZoneHotSpotProjector(zone : Zone) extends Actor {
  /** a hook for the `GalaxyService` used to broadcast messages */
  private var galaxy : ActorRef = ActorRef.noSender
  /** the timer for the blanking process */
  private var blanking : Cancellable = DefaultCancellable.obj
  /** how long to wait in between blanking periods while hotspots decay */
  private val blankingDelay : FiniteDuration = 15 seconds

  private[this] val log = org.log4s.getLogger(s"${zone.Id.capitalize}HotSpotProjector")

  /**
    * Actions that occur before this `Actor` is formally started.
    * Request a hook for the `GalaxyService`.
    * @see `ServiceManager`
    * @see `ServiceManager.Lookup`
    */
  override def preStart() : Unit = {
    super.preStart()
    ServiceManager.serviceManager ! ServiceManager.Lookup("galaxy")
  }

  /**
    * Actions that occur after this `Actor` is formally stopped.
    * Cancel all future blanking actions and release the `GalaxyService` hook.
    */
  override def postStop() : Unit = {
    blanking.cancel
    galaxy = ActorRef.noSender
    super.postStop()
  }

  def receive : Receive = Initializing

  /**
    * Accept the `GalaxyService` hook and switch to active message processing when it arrives.
    * @see `ActorContext.become`
    * @see `ServiceManager`
    * @see `ServiceManager.LookupResult`
    * @see `ZoneHotSpotProjector.UpdateDurationFunction`
    * @see `ZoneHotSpotProjector.UpdateMappingFunction`
    * @return a partial function
    */
  def Initializing : Receive = {
    case ServiceManager.LookupResult("galaxy", galaxyRef) =>
      galaxy = galaxyRef
      context.become(Established)

    case ZoneHotSpotProjector.UpdateDurationFunction() =>
      UpdateDurationFunction()

    case ZoneHotSpotProjector.UpdateMappingFunction() =>
      UpdateMappingFunction()

    case _ =>
      log.warn("not ready - still waiting on event system hook")
  }

  /**
    * The active message processing message handler.
    * @see `Zone.HotSpot.Activity`
    * @see `Zone.HotSpot.ClearAll`
    * @see `Zone.HotSpot.UpdateNow`
    * @see `Zone.ActivityBy`
    * @see `Zone.ActivityFor`
    * @see `Zone.TryHotSpot`
    * @see `ZoneHotSpotProjector.BlankingPhase`
    * @see `ZoneHotSpotProjector.UpdateDurationFunction`
    * @see `ZoneHotSpotProjector.UpdateMappingFunction`
    * @return a partial function
    */
  def Established : Receive = {
    case ZoneHotSpotProjector.UpdateDurationFunction() =>
      blanking.cancel
      UpdateDurationFunction()
      UpdateHotSpots(PlanetSideEmpire.values, zone.HotSpots)
      import scala.concurrent.ExecutionContext.Implicits.global
      blanking = context.system.scheduler.scheduleOnce(blankingDelay, self, ZoneHotSpotProjector.BlankingPhase())

    case ZoneHotSpotProjector.UpdateMappingFunction() =>
      //remapped hotspots are produced from their `DisplayLocation` determined by the previous function
      //this is different from the many individual activity locations that contributed to that `DisplayLocation`
      blanking.cancel
      UpdateMappingFunction()
      UpdateHotSpots(PlanetSideEmpire.values, zone.HotSpots)
      import scala.concurrent.ExecutionContext.Implicits.global
      blanking = context.system.scheduler.scheduleOnce(blankingDelay, self, ZoneHotSpotProjector.BlankingPhase())

    case Zone.HotSpot.Activity(defender, attacker, location) =>
      log.trace(s"received information about activity in ${zone.Id}@$location")
      val defenderFaction = defender.Faction
      val attackerFaction = attacker.Faction
      val noPriorHotSpots = zone.HotSpots.isEmpty
      val duration = zone.HotSpotTimeFunction(defender, attacker)
      if(duration.toNanos > 0) {
        val hotspot = zone.TryHotSpot( zone.HotSpotCoordinateFunction(location) )
        log.trace(s"updating activity status for ${zone.Id} hotspot x=${hotspot.DisplayLocation.x} y=${hotspot.DisplayLocation.y}")
        val noPriorActivity = !(hotspot.ActivityBy(defenderFaction) && hotspot.ActivityBy(attackerFaction))
        //update the activity report for these factions
        val affectedFactions = Seq(attackerFaction, defenderFaction)
        affectedFactions.foreach { f =>
          hotspot.ActivityFor(f) match {
            case Some(events) =>
              events.Duration = duration
              events.Report()
            case None => ;
          }
        }
        //if the level of activity changed for one of the participants or the number of hotspots was zero
        if(noPriorActivity || noPriorHotSpots) {
          UpdateHotSpots(affectedFactions, zone.HotSpots)
          if(noPriorHotSpots) {
            import scala.concurrent.ExecutionContext.Implicits.global
            blanking.cancel
            blanking = context.system.scheduler.scheduleOnce(blankingDelay, self, ZoneHotSpotProjector.BlankingPhase())
          }
        }
      }

    case Zone.HotSpot.UpdateNow =>
      log.trace(s"asked to update for zone ${zone.Id} without a blanking period or new activity")
      UpdateHotSpots(PlanetSideEmpire.values, zone.HotSpots)

    case ZoneHotSpotProjector.BlankingPhase() | Zone.HotSpot.Cleanup() =>
      blanking.cancel
      val curr : Long = System.nanoTime
      //blanking dated activity reports
      val changed = zone.HotSpots.flatMap(spot => {
        spot.Activity.collect {
          case (b, a) if a.LastReport + a.Duration.toNanos <= curr =>
            a.Clear() //this faction has no more activity in this sector
            (b, spot)
        }
      })
      //collect and re-assign still-relevant hotspots
      val spots = zone.HotSpots.filter(spot => {
        spot.Activity
          .values
          .collect {
            case a if a.Heat > 0 =>
              true
          }
          .foldLeft(false)(_ || _)
      })
      val changesOnMap = zone.HotSpots.size - spots.size
      log.trace(s"blanking out $changesOnMap hotspots from zone ${zone.Id}; ${spots.size} remain active")
      zone.HotSpots = spots
      //other hotspots still need to be blanked later
      if(spots.nonEmpty) {
        import scala.concurrent.ExecutionContext.Implicits.global
        blanking.cancel
        blanking = context.system.scheduler.scheduleOnce(blankingDelay, self, ZoneHotSpotProjector.BlankingPhase())
      }
      //if hotspots changed, redraw the remaining ones for the groups that changed
      if(changed.nonEmpty && changesOnMap > 0) {
        UpdateHotSpots(changed.map( { case (a : PlanetSideEmpire.Value, _) => a } ).toSet, spots)
      }

    case Zone.HotSpot.ClearAll() =>
      log.trace(s"blanking out all hotspots from zone ${zone.Id} immediately")
      blanking.cancel
      zone.HotSpots = Nil
      UpdateHotSpots(PlanetSideEmpire.values, Nil)

    case _ => ;
  }

  /**
    * Assign a new functionality for determining how long hotspots remain active.
    * Recalculate all current hotspot information.
    */
  def UpdateDurationFunction(): Unit = {
    zone.HotSpots.foreach { spot =>
      spot.Activity.values.foreach { report =>
        val heat = report.Heat
        report.Clear()
        report.Report(heat)
        report.Duration = 0L
      }
    }
    log.trace(s"new duration remapping function provided; reloading ${zone.HotSpots.size} hotspots for one blanking phase")
  }

  /**
    * Assign new functionality for determining where to depict howspots on a given zone map.
    * Recalculate all current hotspot information.
    */
  def UpdateMappingFunction() : Unit = {
    val redoneSpots = zone.HotSpots.map { spot =>
      val newSpot = new HotSpotInfo( zone.HotSpotCoordinateFunction(spot.DisplayLocation) )
      PlanetSideEmpire.values.foreach { faction =>
        if(spot.ActivityBy(faction)) {
          newSpot.Activity(faction).Report( spot.Activity(faction).Heat )
          newSpot.Activity(faction).Duration = spot.Activity(faction).Duration
        }
      }
      newSpot
    }
    log.trace(s"new coordinate remapping function provided; updating ${redoneSpots.size} hotspots")
    zone.HotSpots = redoneSpots
  }

  /**
    * Submit new updates regarding the hotspots for a given group (faction) in this zone.
    * As per how the client operates, all previous hotspots not represented in this list will be erased.
    * @param affectedFactions the factions whose hotspots for this zone need to be redrawn;
    *                         if empty, no update/redraw calls are generated
    * @param hotSpotInfos the information for the current hotspots in this zone;
    *                     if empty or contains no information for a selected group,
    *                     that group's hotspots will be eliminated (blanked) as a result
    */
  def UpdateHotSpots(affectedFactions : Iterable[PlanetSideEmpire.Value], hotSpotInfos : List[HotSpotInfo]) : Unit = {
    val zoneNumber = zone.Number
    affectedFactions.foreach(faction =>
      galaxy ! Zone.HotSpot.Update(
        faction,
        zoneNumber,
        1,
        ZoneHotSpotProjector.SpecificHotSpotInfo(faction, hotSpotInfos)
      )
    )
  }

  def CreateHotSpotUpdate(faction : PlanetSideEmpire.Value, hotSpotInfos : List[HotSpotInfo]) : List[HotSpotInfo] = {
    Nil
  }
}

object ZoneHotSpotProjector {
  /**
    * Reload the current hotspots for one more blanking phase.
    */
  final case class UpdateDurationFunction()
  /**
    * Reload the current hotspots by directly mapping the current ones to new positions.
    */
  final case class UpdateMappingFunction()
  /**
    * The internal message for eliminating hotspot data whose lifespan has exceeded its set duration.
    */
  private case class BlankingPhase()

  /**
    * Extract related hotspot activity information based on association with a faction.
    * @param faction the faction
    * @param hotSpotInfos the total activity information
    * @return the discovered activity information that aligns with `faction`
    */
  def SpecificHotSpotInfo(faction : PlanetSideEmpire.Value, hotSpotInfos : List[HotSpotInfo]) : List[HotSpotInfo] = {
    hotSpotInfos.filter { spot => spot.ActivityBy(faction) }
  }
}
