// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.structures

import akka.actor.{Actor, ActorRef}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.packet.game.BuildingInfoUpdateMessage
import services.ServiceManager
import services.ServiceManager.Lookup
import services.galaxy.{GalaxyAction, GalaxyResponse, GalaxyServiceMessage, GalaxyServiceResponse}

class BuildingControl(building : Building) extends Actor with FactionAffinityBehavior.Check {
  def FactionObject : FactionAffinity = building
  var galaxyService : ActorRef = Actor.noSender
  var localService : ActorRef = Actor.noSender
  private[this] val log = org.log4s.getLogger

  override def preStart = {
    log.trace(s"Starting BuildingControl for ${building.GUID} / ${building.MapId}")
    ServiceManager.serviceManager ! Lookup("galaxy")
    ServiceManager.serviceManager ! Lookup("local")
  }

  def receive : Receive = checkBehavior.orElse {
    case ServiceManager.LookupResult("galaxy", endpoint) =>
      galaxyService = endpoint
      log.trace("BuildingControl: Building " + building.GUID + " Got galaxy service " + endpoint)
    case ServiceManager.LookupResult("local", endpoint) =>
      localService = endpoint
      log.trace("BuildingControl: Building " + building.GUID + " Got local service " + endpoint)
    case FactionAffinity.ConvertFactionAffinity(faction) =>
      val originalAffinity = building.Faction
      if(originalAffinity != (building.Faction = faction)) {
        building.Amenities.foreach(_.Actor forward FactionAffinity.ConfirmFactionAffinity())
      }
      sender ! FactionAffinity.AssertFactionAffinity(building, faction)

    case Building.SendMapUpdate(all_clients: Boolean) =>
      val zoneNumber = building.Zone.Number
      val buildingNumber = building.MapId
      log.trace(s"sending BuildingInfoUpdateMessage update - zone=$zoneNumber, building=$buildingNumber")
      val (
        ntuLevel,
        isHacked, empireHack, hackTimeRemaining, controllingEmpire,
        unk1, unk1x,
        generatorState, spawnTubesNormal, forceDomeActive,
        latticeBenefit, cavernBenefit,
        unk4, unk5, unk6,
        unk7, unk7x,
        boostSpawnPain, boostGeneratorPain
        ) = building.Info
      val msg = BuildingInfoUpdateMessage(
        zoneNumber,
        buildingNumber,
        ntuLevel,
        isHacked, empireHack, hackTimeRemaining, controllingEmpire,
        unk1, unk1x,
        generatorState, spawnTubesNormal, forceDomeActive,
        latticeBenefit, cavernBenefit,
        unk4, unk5, unk6,
        unk7, unk7x,
        boostSpawnPain, boostGeneratorPain
      )

      if(all_clients) {
        galaxyService ! GalaxyServiceMessage(GalaxyAction.MapUpdate(msg))
      } else {
        // Fake a GalaxyServiceResponse response back to just the sender
        sender ! GalaxyServiceResponse("", GalaxyResponse.MapUpdate(msg))
      }

    case default =>
      log.warn(s"BuildingControl: Unknown message $default received from ${sender().path}")
  }
}
