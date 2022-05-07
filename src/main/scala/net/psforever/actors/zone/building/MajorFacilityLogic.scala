// Copyright (c) 2022 PSForever
package net.psforever.actors.zone.building

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.{BuildingActor, ZoneActor}
import net.psforever.objects.serverobject.generator.{Generator, GeneratorControl}
import net.psforever.objects.serverobject.structures.{Amenity, Building}
import net.psforever.objects.serverobject.terminals.capture.{CaptureTerminal, CaptureTerminalAware, CaptureTerminalAwareBehavior}
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.galaxy.{GalaxyAction, GalaxyServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, PlanetSideGeneratorState}

case object MajorFacilityLogic
  extends BuildingLogic {
  import BuildingActor.Command

  def updateForceDome(details: BuildingControlDetails, stateOpt: Option[Boolean]): Behavior[Command] = {
    stateOpt match {
      case Some(updatedStatus) if details.building.IsCapitol && updatedStatus != details.building.ForceDomeActive =>
        updateForceDomeStatus(details, updatedStatus, mapUpdateOnChange = true)
      case _ =>
        alignForceDomeStatus(details)
    }
    Behaviors.same
  }

  /**
    * Evaluate the conditions of the building
    * and determine if its capitol force dome state should be updated
    * to reflect the actual conditions of the base or its surrounding bases.
    * If this building is considered a subcapitol facility to the zone's actual capitol facility,
    * and has the capitol force dome has a dependency upon it,
    * pass a message onto that facility that it should check its own state alignment.
    * @param mapUpdateOnChange if `true`, dispatch a `MapUpdate` message for this building
    */
  def alignForceDomeStatus(details: BuildingControlDetails, mapUpdateOnChange: Boolean = true): Behavior[Command] = {
    val building = details.building
    checkForceDomeStatus(building) match {
      case Some(updatedStatus) if updatedStatus != building.ForceDomeActive =>
        updateForceDomeStatus(details, updatedStatus, mapUpdateOnChange)
      case None if building.IsSubCapitol =>
        building.Neighbours match {
          case Some(buildings: Set[Building]) =>
            buildings
              .filter { _.IsCapitol }
              .foreach { _.Actor ! BuildingActor.UpdateForceDome() }
          case None => ;
        }
      case _ => ; //building is neither a capitol nor a subcapitol
    }
    Behaviors.same
  }

  /**
    * Dispatch a message to update the state of the clients with the server state of the capitol force dome.
    * @param updatedStatus the new capitol force dome status
    * @param mapUpdateOnChange if `true`, dispatch a `MapUpdate` message for this building
    */
  def updateForceDomeStatus(
                             details: BuildingControlDetails,
                             updatedStatus: Boolean,
                             mapUpdateOnChange: Boolean
                           ): Unit = {
    val building = details.building
    val zone = building.Zone
    building.ForceDomeActive = updatedStatus
    zone.LocalEvents ! LocalServiceMessage(
      zone.id,
      LocalAction.UpdateForceDomeStatus(Service.defaultPlayerGUID, building.GUID, updatedStatus)
    )
    if (mapUpdateOnChange) {
      details.context.self ! BuildingActor.MapUpdate()
    }
  }

  /**
    * The natural conditions of a facility that is not eligible for its capitol force dome to be expanded.
    * The only test not employed is whether or not the target building is a capitol.
    * Ommission of this condition makes this test capable of evaluating subcapitol eligibility
    * for capitol force dome expansion.
    * @param building the target building
    * @return `true`, if the conditions for capitol force dome are not met;
    *        `false`, otherwise
    */
  def invalidBuildingCapitolForceDomeConditions(building: Building): Boolean = {
    building.Faction == PlanetSideEmpire.NEUTRAL ||
    building.NtuLevel == 0 ||
    (building.Generator match {
      case Some(o) => o.Condition == PlanetSideGeneratorState.Destroyed
      case _ => false
    })
  }

  /**
    * If this building is a capitol major facility,
    * use the faction affinity, the generator status, and the resource silo's capacitance level
    * to determine if the capitol force dome should be active.
    * @param building the building being evaluated
    * @return the condition of the capitol force dome;
    *         `None`, if the facility is not a capitol building;
    *         `Some(true|false)` to indicate the state of the force dome
    */
  def checkForceDomeStatus(building: Building): Option[Boolean] = {
    if (building.IsCapitol) {
      val originalStatus = building.ForceDomeActive
      val faction = building.Faction
      val updatedStatus = if (invalidBuildingCapitolForceDomeConditions(building)) {
        false
      } else {
        val ownedSubCapitols = building.Neighbours(faction) match {
          case Some(buildings: Set[Building]) => buildings.count { b => !invalidBuildingCapitolForceDomeConditions(b) }
          case None                           => 0
        }
        if (originalStatus && ownedSubCapitols <= 1) {
          false
        } else if (!originalStatus && ownedSubCapitols > 1) {
          true
        } else {
          originalStatus
        }
      }
      Some(updatedStatus)
    } else {
      None
    }
  }

  def amenityStateChange(details: BuildingControlDetails, entity: Amenity, data: Option[Any]): Behavior[Command] = {
    entity match {
      case gen: Generator =>
        if (generatorStateChange(details, gen, data)) {
          // Request all buildings update their map data to refresh lattice linked benefits
          details.building.Zone.actor ! ZoneActor.ZoneMapUpdate()
        }
      case terminal: CaptureTerminal =>
        // Notify amenities that listen for CC hack state changes, e.g. wall turrets to dismount seated players
        details.building.Amenities.filter(x => x.isInstanceOf[CaptureTerminalAware]).foreach(amenity => {
          data match {
            case Some(isResecured: Boolean) => amenity.Actor ! CaptureTerminalAwareBehavior.TerminalStatusChanged(terminal, isResecured)
            case _ => log(details).warn("CaptureTerminal AmenityStateChange was received with no attached data.")
          }
        })
        // When a CC is hacked (or resecured) all currently hacked amenities for the base should return to their default unhacked state
        details.building.HackableAmenities.foreach(amenity => {
          if (amenity.HackedBy.isDefined) {
            details.building.Zone.LocalEvents ! LocalServiceMessage(amenity.Zone.id,LocalAction.ClearTemporaryHack(PlanetSideGUID(0), amenity))
          }
        })
      // No map update needed - will be sent by `HackCaptureActor` when required
      case _ =>
        details.galaxyService ! GalaxyServiceMessage(GalaxyAction.MapUpdate(details.building.infoUpdateMessage()))
    }
    Behaviors.same
  }

  def powerOff(details: BuildingControlDetails): Behavior[Command] = {
    details.building.Generator match {
      case Some(gen) => gen.Actor ! BuildingActor.NtuDepleted()
      case _ => powerLost(details)
    }
    Behaviors.same
  }

  def powerOn(details: BuildingControlDetails): Behavior[Command] = {
    details.building.Generator match {
      case Some(gen) if details.building.NtuLevel > 0 => gen.Actor ! BuildingActor.SuppliedWithNtu()
      case _ => powerRestored(details)
    }
    Behaviors.same
  }

  def ntuDepleted(details: BuildingControlDetails): Behavior[Command] = {
    // Someone let the base run out of nanites. No one gets anything.
    details.building.Amenities.foreach { amenity =>
      amenity.Actor ! BuildingActor.NtuDepleted()
    }
    setFactionTo(details, PlanetSideEmpire.NEUTRAL)
    details.hasNtuSupply = false
    Behaviors.same
  }

  def suppliedWithNtu(details: BuildingControlDetails): Behavior[Command] = {
    // Auto-repair restart, mainly.  If the Generator works, power should be restored too.
    details.hasNtuSupply = true
    details.building.Amenities.foreach { amenity =>
      amenity.Actor ! BuildingActor.SuppliedWithNtu()
    }
    Behaviors.same
  }

  def generatorStateChange(details: BuildingControlDetails, generator: Generator, event: Any): Boolean = {
    val building = details.building
    val zone = building.Zone
    event match {
      case Some(GeneratorControl.Event.UnderAttack) =>
        val events = zone.AvatarEvents
        val guid = building.GUID
        val msg = AvatarAction.GenericObjectAction(Service.defaultPlayerGUID, guid, 15)
        building.PlayersInSOI.foreach { player =>
          events ! AvatarServiceMessage(player.Name, msg)
        }
        false
      case Some(GeneratorControl.Event.Critical) =>
        val events = zone.AvatarEvents
        val guid = building.GUID
        val msg = AvatarAction.PlanetsideAttributeToAll(guid, 46, 1)
        building.PlayersInSOI.foreach { player =>
          events ! AvatarServiceMessage(player.Name, msg)
        }
        true
      case Some(GeneratorControl.Event.Destabilized) =>
        val events = zone.AvatarEvents
        val guid = building.GUID
        val msg = AvatarAction.GenericObjectAction(Service.defaultPlayerGUID, guid, 16)
        building.PlayersInSOI.foreach { player =>
          events ! AvatarServiceMessage(player.Name, msg)
        }
        false
      case Some(GeneratorControl.Event.Destroyed) =>
        true
      case Some(GeneratorControl.Event.Offline) =>
        powerLost(details)
        alignForceDomeStatus(details, mapUpdateOnChange = false)
        val zone = building.Zone
        val msg = AvatarAction.PlanetsideAttributeToAll(building.GUID, 46, 2)
        building.PlayersInSOI.foreach { player =>
          zone.AvatarEvents ! AvatarServiceMessage(player.Name, msg)
        } //???
        true
      case Some(GeneratorControl.Event.Normal) =>
        true
      case Some(GeneratorControl.Event.Online) =>
        // Power restored. Reactor Online. Sensors Online. Weapons Online. All systems nominal.
        powerRestored(details)
        alignForceDomeStatus(details, mapUpdateOnChange = false)
        val events = zone.AvatarEvents
        val guid = building.GUID
        val msg1 = AvatarAction.PlanetsideAttributeToAll(guid, 46, 0)
        val msg2 = AvatarAction.GenericObjectAction(Service.defaultPlayerGUID, guid, 17)
        building.PlayersInSOI.foreach { player =>
          val name = player.Name
          events ! AvatarServiceMessage(name, msg1) //reset ???; might be global?
          events ! AvatarServiceMessage(name, msg2) //This facility's generator is back on line
        }
        true
      case _ =>
        false
    }
  }

  def setFactionTo(
                    details: BuildingControlDetails,
                    faction: PlanetSideEmpire.Value
                  ): Behavior[Command] = {
    if (details.hasNtuSupply) {
      BuildingActor.setFactionTo(details, faction, log)
      alignForceDomeStatus(details, mapUpdateOnChange = false)
      val building = details.building
      building.Neighbours.getOrElse(Nil).foreach { _.Actor ! BuildingActor.AlertToFactionChange(building) }
    }
    Behaviors.same
  }

  def alertToFactionChange(details: BuildingControlDetails, building: Building): Behavior[Command] = {
    Behaviors.same
  }

  /**
    * Power has been severed.
    * All installed amenities are distributed a `PowerOff` message
    * and are instructed to display their "unpowered" model.
    * Additionally, the facility is now rendered unspawnable regardless of its player spawning amenities.
    */
  def powerLost(details: BuildingControlDetails): Behavior[Command] = {
    val building = details.building
    val zone = building.Zone
    val zoneId = zone.id
    val events = zone.AvatarEvents
    val guid = building.GUID
    val powerMsg = BuildingActor.PowerOff()
    building.Amenities.foreach { amenity =>
      amenity.Actor ! powerMsg
    }
    //amenities disabled; red warning lights
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(guid, 48, 1))
    //disable spawn target on deployment map
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(guid, 38, 0))
    Behaviors.same
  }

  /**
    * Power has been restored.
    * All installed amenities are distributed a `PowerOn` message
    * and are instructed to display their "powered" model.
    * Additionally, the facility is now rendered spawnable if its player spawning amenities are online.
    */
  def powerRestored(details: BuildingControlDetails): Behavior[Command] = {
    val building = details.building
    val zone = building.Zone
    val zoneId = zone.id
    val events = zone.AvatarEvents
    val guid = building.GUID
    val powerMsg = BuildingActor.PowerOn()
    building.Amenities.foreach { amenity =>
      amenity.Actor ! powerMsg
    }
    //amenities enabled; normal lights
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(guid, 48, 0))
    //enable spawn target on deployment map
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(guid, 38, 1))
    Behaviors.same
  }

  def ntu(details: BuildingControlDetails, msg: NtuCommand.Command): Behavior[Command] = {
    import NtuCommand._
    msg match {
      case Offer(_, _) =>
        Behaviors.same
      case Request(_, replyTo) =>
        //all other facilities require a storage silo for ntu
        details.building.NtuSource match {
          case Some(ntuContainer) =>
            ntuContainer.Actor ! msg //needs to redirect
            Behaviors.same
          case None =>
            replyTo ! NtuCommand.Grant(null, 0)
            Behaviors.unhandled
        }
      case _ =>
        Behaviors.same
    }
  }
}
