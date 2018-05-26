// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.structures

import akka.actor.{Actor, ActorRef}
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.packet.game.{BuildingInfoUpdateMessage, PlanetSideGeneratorState}
import net.psforever.types.PlanetSideEmpire
import services.ServiceManager
import services.ServiceManager.Lookup
import services.galaxy.{GalaxyAction, GalaxyServiceMessage}

class BuildingControl(building : Building) extends Actor with FactionAffinityBehavior.Check {
  def FactionObject : FactionAffinity = building
  var galaxyService : ActorRef = Actor.noSender
  private[this] val log = org.log4s.getLogger

  def receive : Receive = {
    case "startup" =>
      log.warn(s"Building ${building.GUID} / ${building.ModelId} / ${building.Id} in startup")
      ServiceManager.serviceManager ! Lookup("galaxy") //ask for a resolver to deal with the GUID system

    case ServiceManager.LookupResult("galaxy", endpoint) =>
      galaxyService = endpoint
      log.info("BuildingControl: Building " + building.ModelId + " Got galaxy service " + endpoint)

      // todo: This is just a temporary solution to drain NTU over time. When base object destruction is properly implemented NTU should be deducted when base objects repair themselves
      context.become(Processing)

    case _ => log.warn("Message received before startup called");
  }

  def Processing : Receive = checkBehavior.orElse {
    case FactionAffinity.ConvertFactionAffinity(faction) =>
      val originalAffinity = building.Faction
      if(originalAffinity != (building.Faction = faction)) {
        building.Amenities.foreach(_.Actor forward FactionAffinity.ConfirmFactionAffinity())
      }
      sender ! FactionAffinity.AssertFactionAffinity(building, faction)
    case Building.SendMapUpdateToAllClients() =>
      log.info(s"Sending facility map update to all clients. Zone: ${building.Zone.Number} - Building: ${building.ModelId}")
      var ntuLevel = 0
      building.Amenities.filter(x => (x.Definition == GlobalDefinitions.resource_silo)).headOption.asInstanceOf[Option[ResourceSilo]] match {
        case Some(obj: ResourceSilo) =>
          ntuLevel = obj.CapacitorDisplay.toInt
        case _ => ;
      }
      galaxyService ! GalaxyServiceMessage(GalaxyAction.MapUpdate(
      BuildingInfoUpdateMessage(
        continent_id = building.Zone.Number, //Zone
        building_id = building.Id, //Facility
        ntu_level = ntuLevel,
        is_hacked = false, //Hacked
        PlanetSideEmpire.NEUTRAL, //Base hacked by
        hack_time_remaining = 0, //Time remaining for hack (ms)
        empire_own = building.Faction, //Base owned by
        unk1 = 0, //!! Field != 0 will cause malformed packet. See class def.
        unk1x = None,
        generator_state = PlanetSideGeneratorState.Normal, //Generator state
        spawn_tubes_normal = true, //Respawn tubes operating state
        force_dome_active = false, //Force dome state
        lattice_benefit = 0, //Lattice benefits
        cavern_benefit = 0, //!! Field > 0 will cause malformed packet. See class def.
        unk4 = Nil,
        unk5 = 0,
        unk6 = false,
        unk7 = 8, //!! Field != 8 will cause malformed packet. See class def.
        unk7x = None,
        boost_spawn_pain = false, //Boosted spawn room pain field
        boost_generator_pain = false //Boosted generator room pain field
      )))
    case _ =>
      log.warn(s"BuildingControl: Unknown message received from ${sender().path}")
//    case default =>
//      log.warn(s"BuildingControl: Unknown message ${default} received from ${sender().path}")

  }
}
