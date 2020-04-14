// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.structures

import akka.actor.{Actor, ActorRef}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.generator.Generator
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.zones.InterstellarCluster
import net.psforever.packet.game.BuildingInfoUpdateMessage
import services.ServiceManager
import services.ServiceManager.Lookup
import services.galaxy.{GalaxyAction, GalaxyResponse, GalaxyServiceMessage, GalaxyServiceResponse}

class BuildingControl(building : Building) extends Actor with FactionAffinityBehavior.Check {
  def FactionObject : FactionAffinity = building
  var galaxyService : ActorRef = Actor.noSender
  var interstellarCluster : ActorRef = Actor.noSender
  private[this] val log = org.log4s.getLogger

  override def preStart = {
    log.trace(s"Starting BuildingControl for ${building.GUID} / ${building.MapId}")
    ServiceManager.serviceManager ! Lookup("galaxy")
    ServiceManager.serviceManager ! Lookup("cluster")
  }

  def receive : Receive = checkBehavior.orElse {
    case ServiceManager.LookupResult("galaxy", endpoint) =>
      galaxyService = endpoint
      log.trace("BuildingControl: Building " + building.GUID + " Got galaxy service " + endpoint)
    case ServiceManager.LookupResult("cluster", endpoint) =>
      interstellarCluster = endpoint
      log.trace("BuildingControl: Building " + building.GUID + " Got interstellar cluster service " + endpoint)

    case FactionAffinity.ConvertFactionAffinity(faction) =>
      val originalAffinity = building.Faction
      if(originalAffinity != (building.Faction = faction)) {
        building.Amenities.foreach(_.Actor forward FactionAffinity.ConfirmFactionAffinity())
      }
      sender ! FactionAffinity.AssertFactionAffinity(building, faction)

    case Building.AmenityStateChange(obj : SpawnTube) =>
      if(building.Amenities.contains(obj)) {
        SendMapUpdate(allClients = true)
      }

    case Building.AmenityStateChange(obj : Generator) =>
      if(building.Amenities.contains(obj)) {
        SendMapUpdate(allClients = true)
      }

    case Building.TriggerZoneMapUpdate(zone_num: Int) =>
      if(interstellarCluster != ActorRef.noSender) interstellarCluster ! InterstellarCluster.ZoneMapUpdate(zone_num)

    case Building.SendMapUpdate(all_clients: Boolean) =>
      SendMapUpdate(all_clients)

    case _ =>
  }

  /**
    * na
    * @param allClients na
    */
  def SendMapUpdate(allClients : Boolean) : Unit = {
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

    if(allClients) {
      if(galaxyService != ActorRef.noSender) galaxyService ! GalaxyServiceMessage(GalaxyAction.MapUpdate(msg))
    } else {
      // Fake a GalaxyServiceResponse response back to just the sender
      sender ! GalaxyServiceResponse("", GalaxyResponse.MapUpdate(msg))
    }
  }
}
