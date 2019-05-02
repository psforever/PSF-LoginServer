// Copyright (c) 2019 PSForever
package net.psforever.objects.zones

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects.DefaultCancellable
import net.psforever.objects.ballistics.SourceEntry
import net.psforever.objects.zones.{HotSpotInfo => ZoneHotSpotInfo}
import net.psforever.types.Vector3
import services.ServiceManager

import scala.concurrent.duration._

class ZoneHotSpotProjector(zone : Zone) extends Actor {
  private var galaxy : ActorRef = ActorRef.noSender
  private val sectorDivs : Int = 64
  private val sectorFunc : (MapScale, Vector3, Int, Int)=>Vector3 = ZoneHotSpotProjector.Sector
  private val timeFunc : (SourceEntry, SourceEntry)=>FiniteDuration = ZoneHotSpotProjector.TimeRules
  private var blanking : Cancellable = DefaultCancellable.obj
  private val blankingDelay : FiniteDuration = 15 seconds

  private[this] val log = org.log4s.getLogger

  override def preStart() : Unit = {
    super.preStart()
    ServiceManager.serviceManager ! ServiceManager.Lookup("galaxy")
  }

  override def postStop() : Unit = {
    galaxy = ActorRef.noSender
    blanking.cancel
    super.postStop()
  }

  def receive : Receive = Initializing

  def Initializing : Receive = {
    case ServiceManager.LookupResult("galaxy", galaxyRef) =>
      galaxy = galaxyRef
      context.become(Established)
    case _ => ;
  }

  def Established : Receive = {
    case Zone.HotSpot.Activity(defender, attacker, location) =>
      log.trace(s"received information about activity in ${zone.Id}@$location")
      val defenderFaction = defender.Faction
      val attackerFaction = attacker.Faction
      val noPriorHotSpots = zone.HotSpots.isEmpty
      //a new hotspot with no activity will still be generated if the duration is 0 - this is okay
      val hotspot = zone.TryHotSpot( sectorFunc(zone.Map.Scale, location, sectorDivs, sectorDivs) )
      val noPriorActivity = !(hotspot.ActivityBy(defenderFaction) && hotspot.ActivityBy(attackerFaction))
      val duration = timeFunc(defender, attacker)
      if(duration.toNanos > 0) {
        log.trace(s"updating activity status for ${zone.Id} hotspot x=${hotspot.DisplayLocation.x} y=${hotspot.DisplayLocation.y}")
        //update the activity report for these factions
        Seq(attackerFaction, defenderFaction).foreach { f =>
          hotspot.ActivityFor(f) match {
            case Some(events) =>
              events.Duration = duration
              events.Report()
            case None => ;
          }
        }
        //if the level of activity changed for one of the participants or the number of hotspots was zero
        if(noPriorActivity || noPriorHotSpots) {
          UpdateHotSpots(zone, zone.HotSpots)
          if(noPriorHotSpots) {
            import scala.concurrent.ExecutionContext.Implicits.global
            blanking.cancel
            blanking = context.system.scheduler.scheduleOnce(blankingDelay, self, ZoneHotSpotProjector.BlankingPhase())
          }
        }
      }

    case ZoneHotSpotProjector.BlankingPhase() | Zone.HotSpot.UpdateNow() =>
      blanking.cancel
      val curr : Long = System.nanoTime
      //blanking dated activity reports
      val changed = zone.HotSpots.map(spot => {
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
      //hotspots still need to be cleared
      if(spots.nonEmpty) {
        import scala.concurrent.ExecutionContext.Implicits.global
        blanking.cancel
        blanking = context.system.scheduler.scheduleOnce(blankingDelay, self, ZoneHotSpotProjector.BlankingPhase())
      }
      //if hotspots changed, redraw the remaining ones
      if(changed.nonEmpty && changesOnMap > 0) {
        UpdateHotSpots(zone, spots)
      }

    case Zone.HotSpot.ClearAll() =>
      log.trace(s"blanking out all hotspots from zone ${zone.Id} immediately")
      blanking.cancel
      zone.HotSpots = Nil
      UpdateHotSpots(zone, Nil)

    case _ => ;
  }

  def UpdateHotSpots(zone : Zone, hotSpotInfos : List[ZoneHotSpotInfo]) : Unit = {
    //TODO combined hotspot map; separate by and distribute to empires involved
    galaxy ! Zone.HotSpot.Update(zone.Number, 0, hotSpotInfos)
  }
}

object ZoneHotSpotProjector {
  private case class BlankingPhase()

  def Sector(scale : MapScale, pos : Vector3, longDivNum : Int, latDivNum : Int) : Vector3 = {
    val (posx, posy) = (pos.x, pos.y)
    val width = scale.width
    val height = scale.height
    val divWidth : Float = width / longDivNum
    val divHeight : Float = height / latDivNum
    Vector3(
      //x
      if(posx >= width - divWidth) {
        width - divWidth
      }
      else if(posx >= divWidth) {
        val sector : Float = (posx * longDivNum / width).toInt * divWidth
        val nextSector : Float = sector + divWidth
        if(posx - sector < nextSector - posx) {
          sector
        }
        else {
          nextSector
        }
      }
      else {
        divWidth
      },
      //y
      if(posy >= height - divHeight) {
        height - divHeight
      }
      else if(posy >= divHeight) {
        val sector : Float = (posy * latDivNum / height).toInt * divHeight
        val nextSector : Float = sector + divHeight
        if(posy - sector < nextSector - posy) {
          sector
        }
        else {
          nextSector
        }
      }
      else {
        divHeight
      },
      //z
      0
    )
  }

  def TimeRules(defender : SourceEntry, attacker : SourceEntry) : FiniteDuration = {
    import net.psforever.objects.ballistics._
    import net.psforever.objects.GlobalDefinitions
    if(attacker.Faction == defender.Faction) {
      0 seconds
    }
    else {
      //TODO is target occupy-able and occupied, or jammed?
      defender match {
        case _ : PlayerSource =>
          60 seconds
        case _ : VehicleSource =>
          60 seconds
        case t : ObjectSource if t.Definition == GlobalDefinitions.manned_turret =>
          60 seconds
        case _ : DeployableSource =>
          30 seconds
        case _ : ComplexDeployableSource =>
          30 seconds
        case _ =>
          0 seconds
      }
    }
  }
}
