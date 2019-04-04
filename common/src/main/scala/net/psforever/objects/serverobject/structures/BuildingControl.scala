// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.structures

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, Props}
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.terminals.CaptureTerminal
import net.psforever.packet.game.{BuildingInfoUpdateMessage, PlanetSideGeneratorState}
import net.psforever.types.PlanetSideEmpire
import services.ServiceManager
import services.ServiceManager.Lookup
import services.galaxy.{GalaxyAction, GalaxyResponse, GalaxyServiceMessage, GalaxyServiceResponse}
import services.local.support.HackCaptureActor

import scala.util.Success
import scala.concurrent.duration._
import akka.pattern.ask

import scala.concurrent.{Await, Future}

class BuildingControl(building : Building) extends Actor with FactionAffinityBehavior.Check {
  def FactionObject : FactionAffinity = building
  var galaxyService : ActorRef = Actor.noSender
  var localService : ActorRef = Actor.noSender
  private[this] val log = org.log4s.getLogger

  override def preStart = {
    log.info(s"Starting BuildingControl for ${building.GUID} / ${building.MapId}")
    ServiceManager.serviceManager ! Lookup("galaxy")
    ServiceManager.serviceManager ! Lookup("local")
  }

  def receive : Receive = checkBehavior.orElse {
    case ServiceManager.LookupResult("galaxy", endpoint) =>
      galaxyService = endpoint
      log.info("BuildingControl: Building " + building.GUID + " Got galaxy service " + endpoint)
    case ServiceManager.LookupResult("local", endpoint) =>
      localService = endpoint
      log.info("BuildingControl: Building " + building.GUID + " Got local service " + endpoint)
    case FactionAffinity.ConvertFactionAffinity(faction) =>
      val originalAffinity = building.Faction
      if(originalAffinity != (building.Faction = faction)) {
        building.Amenities.foreach(_.Actor forward FactionAffinity.ConfirmFactionAffinity())
      }
      sender ! FactionAffinity.AssertFactionAffinity(building, faction)
    case Building.SendMapUpdate(all_clients: Boolean) =>
      log.info(s"Sending BuildingInfoUpdateMessage update. Zone: ${building.Zone.Number} - Building: ${building.GUID} / MapId: ${building.MapId}")
      var ntuLevel = 0
      var is_hacked = false
      var hack_time_remaining_ms = 0L;
      var hacked_by_faction = PlanetSideEmpire.NEUTRAL

      // Get Ntu level from silo if it exists
      building.Amenities.filter(x => (x.Definition == GlobalDefinitions.resource_silo)).headOption.asInstanceOf[Option[ResourceSilo]] match {
        case Some(obj: ResourceSilo) =>
          ntuLevel = obj.CapacitorDisplay.toInt
        case _ => ;
      }

      // Get hack status & time from control console if it exists
      building.Amenities.filter(x => x.Definition == GlobalDefinitions.capture_terminal).headOption.asInstanceOf[Option[CaptureTerminal with Hackable]] match {
        case Some(obj: CaptureTerminal with Hackable) =>
          if(!obj.HackedBy.isEmpty) {
            is_hacked = true
            hacked_by_faction = obj.HackedBy.get._1.Faction
          }

          import scala.concurrent.ExecutionContext.Implicits.global
          val future = ask(localService, HackCaptureActor.GetHackTimeRemainingNanos(obj.GUID))(1 second)

          //todo: this is blocking. Not so bad when we're only retrieving one piece of data but as more functionality is added we'll need to change this to be async but wait for all replies before sending BIUM to clients
          val time = Await.result(future, 1 second).asInstanceOf[Long]
          hack_time_remaining_ms = TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS)
        case _ => ;
      }

      val msg = BuildingInfoUpdateMessage(
        continent_id = building.Zone.Number, //Zone
        building_map_id = building.MapId, //Facility
        ntu_level = ntuLevel,
        is_hacked,
        hacked_by_faction,
        hack_time_remaining_ms,
        empire_own = building.Faction,
        unk1 = 0, //!! Field != 0 will cause malformed packet. See class def.
        unk1x = None,
        generator_state = PlanetSideGeneratorState.Normal,
        spawn_tubes_normal = true,
        force_dome_active = false,
        lattice_benefit = 0,
        cavern_benefit = 0, //!! Field > 0 will cause malformed packet. See class def.
        unk4 = Nil,
        unk5 = 0,
        unk6 = false,
        unk7 = 8, //!! Field != 8 will cause malformed packet. See class def.
        unk7x = None,
        boost_spawn_pain = false,
        boost_generator_pain = false
      )

      if(all_clients) {
        galaxyService ! GalaxyServiceMessage(GalaxyAction.MapUpdate(msg))
      } else {
        // Fake a GalaxyServiceResponse response back to just the sender
        sender ! GalaxyServiceResponse("", GalaxyResponse.MapUpdate(msg))
      }

    case default =>
      log.warn(s"BuildingControl: Unknown message ${default} received from ${sender().path}")
  }
}
