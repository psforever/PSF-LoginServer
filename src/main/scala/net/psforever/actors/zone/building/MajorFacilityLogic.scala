// Copyright (c) 2022 PSForever
package net.psforever.actors.zone.building

import akka.{actor => classic}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.{BuildingActor, BuildingControlDetails, ZoneActor}
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.serverobject.dome.ForceDomePhysics
import net.psforever.objects.serverobject.generator.{Generator, GeneratorControl}
import net.psforever.objects.serverobject.resourcesilo.ResourceSiloControl
import net.psforever.objects.serverobject.structures.{Amenity, Building}
import net.psforever.objects.serverobject.terminals.capture.{CaptureTerminal, CaptureTerminalAware, CaptureTerminalAwareBehavior}
import net.psforever.objects.sourcing.PlayerSource
import net.psforever.packet.game.PlanetsideAttributeMessage
import net.psforever.services.{InterstellarClusterService, Service}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.galaxy.{GalaxyAction, GalaxyServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

/**
  * A package class that conveys the important information for handling facility updates.
  * Major facilities have power systems and structural components that manage this flow of power.
  * The primary concern is a quick means of detecting whether or not the system is operating
  * due to a provision of nanites (synchronization on it).
  * @see `FacilityLogic`
  * @see `Generator`
  * @see `ResourceSilo`
  * @param building building entity
  * @param context message-passing reference
  * @param galaxyService event system for state updates to the whole server
  * @param interstellarCluster event system for behavior updates from the whole server
  */
final case class MajorFacilityWrapper(
                                       building: Building,
                                       context: ActorContext[BuildingActor.Command],
                                       galaxyService: classic.ActorRef,
                                       interstellarCluster: ActorRef[InterstellarClusterService.Command]
                                     )
  extends BuildingWrapper {
  var hasNtuSupply: Boolean = true
}

/**
  * The logic that governs "major facilities" in the overworld -
  * those bases that have lattice connectivity and individual nanite resource stockpiles.
  */
case object MajorFacilityLogic
  extends BuildingLogic {
  import BuildingActor.Command

  override def wrapper(
                        building: Building,
                        context: ActorContext[BuildingActor.Command],
                        details: BuildingControlDetails
                      ): BuildingWrapper = {
    MajorFacilityWrapper(building, context, details.galaxyService, details.interstellarCluster)
  }

  /**
    * The power structure of major facilities has to be statused on the continental map
    * via the state of its nanite-to-energy generator, and
    * those facilities can be captured and controlled by a particular empire.
    * @param details package class that conveys the important information
    * @param entity the installed `Amenity` entity
    * @param data optional information
    * @return the next behavior for this control agency messaging system
    */
  def amenityStateChange(details: BuildingWrapper, entity: Amenity, data: Option[Any]): Behavior[Command] = {
    import net.psforever.objects.GlobalDefinitions
    entity match {
      case gen: Generator =>
        if (generatorStateChange(details, gen, data)) {
          // Request all buildings update their map data to refresh lattice linked benefits
          details.building.Zone.actor ! ZoneActor.ZoneMapUpdate()
        }
      case terminal: CaptureTerminal =>
        val building = details.building
        // Notify amenities that listen for CC hack state changes, e.g. wall turrets to dismount seated players
        data match {
          case Some(isResecured: Boolean) =>
            //pass hack information to amenities
            building.Amenities.filter(x => x.isInstanceOf[CaptureTerminalAware]).foreach(amenity => {
              amenity.Actor ! CaptureTerminalAwareBehavior.TerminalStatusChanged(terminal, isResecured)
            })
          case _ =>
            log(details).warn("CaptureTerminal AmenityStateChange was received with no attached data.")
        }
        // When a CC is hacked (or resecured) clear hacks on amenities based on currently installed virus
        val hackedAmenities = building.HackableAmenities.filter(_.HackedBy.isDefined)
        val amenitiesToClear = building.virusId match {
          case 0 =>
            hackedAmenities.filterNot(a => a.Definition == GlobalDefinitions.lock_external || a.Definition == GlobalDefinitions.main_terminal)
          case 4 =>
            hackedAmenities.filterNot(a => a.Definition == GlobalDefinitions.order_terminal || a.Definition == GlobalDefinitions.main_terminal)
          case 8 =>
            hackedAmenities
          case _ =>
            hackedAmenities
        }
        amenitiesToClear.foreach { amenity =>
          building.Zone.LocalEvents ! LocalServiceMessage(
            amenity.Zone.id,
            LocalAction.ClearTemporaryHack(PlanetSideGUID(0), amenity)
          )
        }
      // No map update needed - will be sent by `HackCaptureActor` when required
      case dome: ForceDomePhysics =>
        val building = details.building
        // The protection of the force dome modifies the NTU drain rate
        val multiplier: Float = calculateNtuDrainMultiplierFrom(details.building, domeOpt = Some(dome))
        building.NtuSource.foreach(_.Actor ! ResourceSiloControl.DrainMultiplier(multiplier))
        // The protection of the force dome marks the generator (and some other amenities) as being invulnerable
        val msg = Damageable.Vulnerability(dome.Perimeter.nonEmpty)
        val applicable = dome.Definition.ApplyProtectionTo
        building
          .Amenities
          .filter(amenity => applicable.contains(amenity.Definition))
          .foreach { _.Actor ! msg }
      case _ =>
        details.galaxyService ! GalaxyServiceMessage(GalaxyAction.MapUpdate(details.building.infoUpdateMessage()))
    }
    Behaviors.same
  }

  /**
    * Power has been severed.
    * All installed amenities are distributed a `PowerOff` message
    * and are instructed to display their "unpowered" model.
    * Additionally, the facility is now rendered unspawnable regardless of its player spawning amenities.
    */
  def powerOff(details: BuildingWrapper): Behavior[Command] = {
    details.building.Generator match {
      case Some(gen) => gen.Actor ! BuildingActor.NtuDepleted()
      case _ => powerLost(details)
    }
    Behaviors.same
  }

  /**
    * Power has been restored.
    * All installed amenities are distributed a `PowerOn` message
    * and are instructed to display their "powered" model.
    * Additionally, the facility is now rendered spawnable if its player spawning amenities are online.
    */
  def powerOn(details: BuildingWrapper): Behavior[Command] = {
    details.building.Generator match {
      case Some(gen) if details.building.NtuLevel > 0 => gen.Actor ! BuildingActor.SuppliedWithNtu()
      case _ => powerRestored(details)
    }
    Behaviors.same
  }

  /**
    * Running out of nanites is a huge deal.
    * Without a supply of nanites, not only does the power go out but
    * the faction affiliation of the facility is wiped away and it is rendered neutral.
    * @param details package class that conveys the important information
    * @return the next behavior for this control agency messaging system
    */
  def ntuDepleted(details: BuildingWrapper): Behavior[Command] = {
    // Someone let the base run out of nanites. No one gets anything.
    details.building.Amenities.foreach { amenity =>
      amenity.Actor ! BuildingActor.NtuDepleted()
    }
    setFactionTo(details, PlanetSideEmpire.NEUTRAL)
    details.asInstanceOf[MajorFacilityWrapper].hasNtuSupply = false
    details.building.Zone.lockedBy = PlanetSideEmpire.NEUTRAL
    details.building.Zone.NotifyContinentalLockBenefits(details.building.Zone, details.building)
    Behaviors.same
  }

  /**
    * Running out of nanites is a huge deal.
    * Once a supply of nanites has been provided, however,
    * the power may be restored if the facility generator is operational.
    * @param details package class that conveys the important information
    * @return the next behavior for this control agency messaging system
    */
  def suppliedWithNtu(details: BuildingWrapper): Behavior[Command] = {
    // Auto-repair restart, mainly.  If the Generator works, power should be restored too.
    details.asInstanceOf[MajorFacilityWrapper].hasNtuSupply = true
    details.building.Amenities.foreach { amenity =>
      amenity.Actor ! BuildingActor.SuppliedWithNtu()
    }
    Behaviors.same
  }

  /**
    * The generator is an extremely important amenity of a major facility
    * that is given its own status indicators that are apparent from the continental map
    * and warning messages that are displayed to everyone who might have an interest in the that particular generator.
    * @param details package class that conveys the important information
    * @param generator the facility generator
    * @param event how the generator changed
    * @return `true`, to update the continental map;
    *        `false`, otherwise
    */
  private def generatorStateChange(details: BuildingWrapper, generator: Generator, event: Any): Boolean = {
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
        if (building.hasCavernLockBenefit) {
          zone.LocalEvents ! LocalServiceMessage(
            zone.id,
            LocalAction.SendResponse(PlanetsideAttributeMessage(building.GUID, 67, 0))
          )
        }
        false
      case Some(GeneratorControl.Event.Destroyed) =>
        true
      case Some(GeneratorControl.Event.Offline) =>
        powerLost(details)
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
                    details: BuildingWrapper,
                    faction: PlanetSideEmpire.Value
                  ): Behavior[Command] = {
    if (details.asInstanceOf[MajorFacilityWrapper].hasNtuSupply) {
      BuildingActor.setFactionTo(details, faction, log)
      val building = details.building
      val alertMsg = BuildingActor.AlertToFactionChange(building)
      building.Neighbours.getOrElse(Nil).foreach { _.Actor ! alertMsg }
      building.Amenities.foreach { _.Actor ! alertMsg }
    }
    Behaviors.same
  }

  def alertToFactionChange(details: BuildingWrapper, building: Building): Behavior[Command] = {
    val bldg = details.building
    bldg.Amenities.foreach { _.Actor ! BuildingActor.AlertToFactionChange(building) } //todo map update?
    //the presence of the flag means that we are involved in an ongoing llu hack
    (bldg.GetFlag, bldg.CaptureTerminal) match {
      case (Some(flag), Some(terminal)) if (flag.Target eq building) && flag.Faction != building.Faction =>
        //our hack destination may have been compromised and the hack needs to be cancelled
        bldg.Zone.LocalEvents ! LocalServiceMessage("", LocalAction.ResecureCaptureTerminal(terminal, PlayerSource.Nobody))
      case _ => ()
    }
    Behaviors.same
  }

  /**
    * Power has been severed.
    * All installed amenities are distributed a `PowerOff` message
    * and are instructed to display their "unpowered" model.
    * Additionally, the facility is now rendered unspawnable regardless of its player spawning amenities.
    */
  private def powerLost(details: BuildingWrapper): Behavior[Command] = {
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
  private def powerRestored(details: BuildingWrapper): Behavior[Command] = {
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

  /**
    * Major facilities have individual nanite reservoirs that are depleted
    * as other installed components require.
    * The main internal use of nanite resources is for auto-repair
    * but various nefarious implements can be used to drain nanite resources from the facility directly.
    * @param details package class that conveys the important information
    * @param msg the original message that instigated this upoate
    * @return the next behavior for this control agency messaging system
    */
  def ntu(details: BuildingWrapper, msg: NtuCommand.Command): Behavior[Command] = {
    import NtuCommand._
    msg match {
      case Request(_, replyTo) =>
        details.building.NtuSource match {
          case Some(ntuContainer) =>
            ntuContainer.Actor ! msg //redirect
            Behaviors.same
          case None =>
            replyTo ! NtuCommand.Grant(null, 0) //hm ...
        }

      case _ =>
    }
    Behaviors.same
  }

  private def calculateNtuDrainMultiplierFrom(
                                               building: Building,
                                               domeOpt: Option[ForceDomePhysics] = None,
                                               mainTerminalOpt: Option[Any] = None
                                             ): Float = {
    val domeParam = domeOpt.orElse {
      building.Amenities.find(_.isInstanceOf[ForceDomePhysics]) match {
        case Some(d: ForceDomePhysics) => Some(d)
        case _ => None
      }
    }
    val mainTerminalParam = mainTerminalOpt.orElse(None) //todo main terminal and viruses
    getNtuDrainMultiplierFromAmenities(domeParam, mainTerminalParam)
  }

  private def getNtuDrainMultiplierFromAmenities(
                                                  dome: Option[ForceDomePhysics],
                                                  mainTerminal: Option[Any]
                                                ): Float = {
    // The force dome being expanded means all repairs are essentially for free
    dome
      .flatMap {
        case d if d.Energized => Some(0f)
        case _ => None
      }
      .orElse {
        mainTerminal.flatMap { _ => Some(2f) } //todo main terminal and viruses
      }
      .getOrElse(1f)
  }
}
