// Copyright (c) 2019 PSForever
package net.psforever.objects.zones

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import net.psforever.objects.DefaultCancellable
import net.psforever.types.{PlanetSideEmpire, Vector3}
import services.ServiceManager

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

/**
  * Manage hotspot information for a given zone,
  * keeping track of aggressive faction interactions,
  * and maintaining the visibility state of the hotspots that alert of the location of that activity.<br>
  * <br>
  * Initializes two internal devices to manage the hotspot activity reported by the zone.
  * The first device - "projector" - keeps track of any hotspots that are currently being displayed on the zone map.
  * The second device - "backup" - is designed to maintain a much longer record of the same hostpot activity
  * that was displayed by the projector.
  * Messages sent to this device are sent automatically to each internal device.
  * The internal devices do not have to be messaged separately.
  * @see `ZoneHotSpotProjector`
  * @see `ZoneHotSpotHistory`
  * @param zone the zone whose map serves as the "screen" for the hotspot data
  * @param outputList an external list used for storing displayed activity hotspots
  * @param outputBlanking the period of decay time before hotspot information is forgotten
  * @param dataList an external list used for storing activity for prolonged periods of time
  * @param dataBlanking the period of decay time before prolonged activity information is forgotten
  */
class ZoneHotSpotDisplay(zone : Zone,
                         outputList : ListBuffer[HotSpotInfo],
                         outputBlanking : FiniteDuration,
                         dataList : ListBuffer[HotSpotInfo],
                         dataBlanking : FiniteDuration) extends Actor {
  val projector = context.actorOf(Props(classOf[ZoneHotSpotProjector], zone, outputList, outputBlanking), s"${zone.Id}-hotspot-projector")
  val backup = context.actorOf(Props(classOf[ZoneHotSpotHistory], zone, dataList, dataBlanking), s"${zone.Id}-hotspot-backup")

  def receive : Receive = {
    case _ if sender == projector || sender == backup => ; //catch and disrupt cyclic messaging paths
    case msg =>
      projector ! msg
      backup ! msg
  }
}

/**
  * Manage hotspot information for a given zone,
  * keeping track of aggressive faction interactions,
  * and maintaining the visibility state of the hotspots that alert of the location of that activity.
  * One of the internal devices controlled by the `ZoneHotSpotDisplay`,
  * this is the "projector" component that actually displays hotspots onto the zone's map.
  * @see `ZoneHotSpotDisplay`
  * @param zone the zone
  * @param hotspots the data structure of hot spot information that this projector will be leveraging
  * @param blankingTime how long to wait in between blanking periods
  */
class ZoneHotSpotProjector(zone : Zone, hotspots : ListBuffer[HotSpotInfo], blankingTime : FiniteDuration) extends Actor {
  /** a hook for the `GalaxyService` used to broadcast messages */
  var galaxy : ActorRef = ActorRef.noSender
  /** the timer for the blanking process */
  var blanking : Cancellable = DefaultCancellable.obj
  /** how long to wait in between blanking periods while hotspots decay */
  var blankingDelay : FiniteDuration = blankingTime

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
      UpdateHotSpots(PlanetSideEmpire.values, hotspots)
      import scala.concurrent.ExecutionContext.Implicits.global
      blanking = context.system.scheduler.scheduleOnce(blankingDelay, self, ZoneHotSpotProjector.BlankingPhase())

    case ZoneHotSpotProjector.UpdateMappingFunction() =>
      //remapped hotspots are produced from their `DisplayLocation` determined by the previous function
      //this is different from the many individual activity locations that contributed to that `DisplayLocation`
      blanking.cancel
      UpdateMappingFunction()
      UpdateHotSpots(PlanetSideEmpire.values, hotspots)
      import scala.concurrent.ExecutionContext.Implicits.global
      blanking = context.system.scheduler.scheduleOnce(blankingDelay, self, ZoneHotSpotProjector.BlankingPhase())

    case Zone.HotSpot.Activity(defender, attacker, location) =>
      log.trace(s"received information about activity in ${zone.Id}@$location")
      val defenderFaction = defender.Faction
      val attackerFaction = attacker.Faction
      val noPriorHotSpots = hotspots.isEmpty
      val duration = zone.HotSpotTimeFunction(defender, attacker)
      if(duration.toNanos > 0) {
        val hotspot = TryHotSpot( zone.HotSpotCoordinateFunction(location) )
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
          UpdateHotSpots(affectedFactions, hotspots)
          if(noPriorHotSpots) {
            import scala.concurrent.ExecutionContext.Implicits.global
            blanking.cancel
            blanking = context.system.scheduler.scheduleOnce(blankingDelay, self, ZoneHotSpotProjector.BlankingPhase())
          }
        }
      }

    case Zone.HotSpot.UpdateNow =>
      log.trace(s"asked to update for zone ${zone.Id} without a blanking period or new activity")
      UpdateHotSpots(PlanetSideEmpire.values, hotspots)

    case ZoneHotSpotProjector.BlankingPhase() | Zone.HotSpot.Cleanup() =>
      blanking.cancel
      val curr : Long = System.nanoTime
      //blanking dated activity reports
      val changed = hotspots.flatMap(spot => {
        spot.Activity.collect {
          case (b, a) if a.LastReport + a.Duration.toNanos <= curr =>
            a.Clear() //this faction has no more activity in this sector
            (b, spot)
        }
      })
      //collect and re-assign still-relevant hotspots
      val spots = hotspots.filter(spot => {
        spot.Activity
          .values
          .collect {
            case a if a.Heat > 0 =>
              true
          }
          .foldLeft(false)(_ || _)
      })
      val newSize = spots.size
      val changesOnMap = hotspots.size - newSize
      log.trace(s"blanking out $changesOnMap hotspots from zone ${zone.Id}; $newSize remain active")
      hotspots.clear
      hotspots.insertAll(0, spots)
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
      hotspots.clear()
      UpdateHotSpots(PlanetSideEmpire.values, Nil)

    case _ => ;
  }

  /**
    * Match a hotspot location with a data structure for keeping track of activity information,
    * either an existing structure or one that was created in the list of activity data for this location.
    * @see `HotSpotInfo`
    * @param displayLoc the location for the hotpot that was normalized by the coordinate mapping function
    * @return the hotspot data that corresponds to this location
    */
  def TryHotSpot(displayLoc : Vector3) : HotSpotInfo = {
    hotspots.find(spot => spot.DisplayLocation == displayLoc) match {
      case Some(spot) =>
        //hotspot already exists
        spot
      case None =>
        //insert new hotspot
        val spot = new HotSpotInfo(displayLoc)
        hotspots += spot
        spot
    }
  }

  /**
    * Assign a new functionality for determining how long hotspots remain active.
    * Recalculate all current hotspot information.
    */
  def UpdateDurationFunction(): Unit = {
    hotspots.foreach { spot =>
      spot.Activity.values.foreach { report =>
        val heat = report.Heat
        report.Clear()
        report.Report(heat)
        report.Duration = 0L
      }
    }
    log.trace(s"new duration remapping function provided; reloading ${hotspots.size} hotspots for one blanking phase")
  }

  /**
    * Assign new functionality for determining where to depict howspots on a given zone map.
    * Recalculate all current hotspot information.
    */
  def UpdateMappingFunction() : Unit = {
    val redoneSpots = hotspots.map { spot =>
      val newSpot = new HotSpotInfo( zone.HotSpotCoordinateFunction(spot.DisplayLocation) )
      PlanetSideEmpire.values.foreach { faction =>
        if(spot.ActivityBy(faction)) {
          newSpot.Activity(faction).Report( spot.Activity(faction).Heat )
          newSpot.Activity(faction).Duration = spot.Activity(faction).Duration
        }
      }
      newSpot
    }
    log.trace(s"new coordinate remapping function provided; updating $redoneSpots.size hotspots")
    hotspots.clear()
    hotspots.insertAll(0, redoneSpots)
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
  def UpdateHotSpots(affectedFactions : Iterable[PlanetSideEmpire.Value], hotSpotInfos : Iterable[HotSpotInfo]) : Unit = {
    val zoneNumber = zone.Number
    val hotSpotInfoList = hotSpotInfos.toList
    affectedFactions.foreach(faction =>
      galaxy ! Zone.HotSpot.Update(
        faction,
        zoneNumber,
        1,
        ZoneHotSpotProjector.SpecificHotSpotInfo(faction, hotSpotInfoList)
      )
    )
  }
}

/**
  * Manage hotspot information for a given zone,
  * keeping track of aggressive faction interactions,
  * and maintaining the visibility state of the hotspots that alert of the location of that activity.
  * One of the internal devices controlled by the `ZoneHotSpotDisplay`,
  * this is the "backup" component that is intended to retain reported activity for a longer period of time.
  * @see `ZoneHotSpotDisplay`
  * @see `ZoneHotSpotProjector`
  * @param zone the zone
  * @param hotspots the data structure of hot spot information that this projector will be leveraging
  * @param blankingTime how long to wait in between blanking periods
  */
class ZoneHotSpotHistory(zone : Zone, hotspots : ListBuffer[HotSpotInfo], blankingTime : FiniteDuration) extends ZoneHotSpotProjector(zone, hotspots, blankingTime) {
  /* the galaxy service is unnecessary */
  override def preStart() : Unit = { context.become(Established) }
  /* this component does not actually the visible hotspots
   * a duplicate of the projector device otherwise */
  override def UpdateHotSpots(affectedFactions : Iterable[PlanetSideEmpire.Value], hotSpotInfos : Iterable[HotSpotInfo]) : Unit = { }
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
