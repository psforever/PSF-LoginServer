// Copyright (c) 2022 PSForever
package net.psforever.actors.zone.building

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.{BuildingActor, ZoneActor}
import net.psforever.objects.serverobject.structures.{Amenity, Building, WarpGate}
import net.psforever.services.galaxy.{GalaxyAction, GalaxyServiceMessage}
import net.psforever.types.PlanetSideEmpire
import net.psforever.util.Config

/**
  * The logic that governs warp gates.
  */
case object WarpGateLogic
  extends BuildingLogic {
  import BuildingActor.Command

  def amenityStateChange(details: BuildingWrapper, entity: Amenity, data: Option[Any]): Behavior[Command] = {
    Behaviors.same
  }

  def powerOff(details: BuildingWrapper): Behavior[Command] = {
    Behaviors.same
  }

  def powerOn(details: BuildingWrapper): Behavior[Command] = {
    Behaviors.same
  }

  def ntuDepleted(details: BuildingWrapper): Behavior[Command] = {
    Behaviors.same
  }

  def suppliedWithNtu(details: BuildingWrapper): Behavior[Command] = {
    Behaviors.same
  }

  /**
    * Setting the faction on a warp gate is dicey at best
    * since the formal logic that controls warp gate faction affiliation is entirely dependent on connectivity.
    * The majority of warp gates are connected in pairs and both gates must possess the same faction affinity,
    * @param details package class that conveys the important information
    * @param faction the faction affiliation to which the facility will update
    * @return the next behavior for this control agency messaging system
    */
  def setFactionTo(details: BuildingWrapper, faction: PlanetSideEmpire.Value): Behavior[Command] = {
    /*
    in reality, the faction of most gates is neutral;
    the ability to move through the gates is determined by empire-related broadcast settings;
    the broadcast settings are dependent by the combined faction affiliations
    of the normal facilities connected to either side of the gate pair;
    if a faction is assigned to a gate, however, both gates in a pair must possess the same faction affinity
     */
    val warpgate = details.building.asInstanceOf[WarpGate]
    if (warpgate.Active && warpgate.Faction != faction) {
      val local = warpgate.Neighbours.getOrElse(Nil)
      BuildingActor.setFactionOnEntity(details, faction, log)
      if (local.isEmpty) {
        log(details).error(s"warp gate ${warpgate.Name} isolated from neighborhood; check intercontinental linkage")
      } else {
        local.foreach { _.Actor ! BuildingActor.AlertToFactionChange(warpgate) }
      }
    }
    Behaviors.same
  }

  /**
    * When a building adjacent to this gate changes its faction affiliation,
    * the empire-related broadcast settings of this warp gate also update.
    * @param details package class that conveys the important information
    * @param building the neighbor facility that has had its faction changed
    * @return the next behavior for this control agency messaging system
    */
  def alertToFactionChange(details: BuildingWrapper, building: Building): Behavior[Command] = {
    val warpgate = details.building.asInstanceOf[WarpGate]
    if (warpgate.Active) {
      val local = warpgate.Neighbours.getOrElse(Nil)
      /*
      output: Building, WarpGate:Us, WarpGate, Building
      where ":Us" means `details.building`, and ":Msg" means the caller `building`
      it could be "Building:Msg, WarpGate:Us, x, y" or "x, Warpgate:Us, Warpgate:Msg, y"
      */
      val (thisBuilding, thisWarpGate, otherWarpGate, otherBuilding) = if (local.exists {
        _ eq building
      }) {
        building match {
          case _ : WarpGate =>
            (
              findNeighborhoodNormalBuilding(local), Some(warpgate),
              Some(building), findNeighborhoodNormalBuilding(building.Neighbours.getOrElse(Nil))
            )
          case _ =>
            findNeighborhoodWarpGate(local) match {
              case out@Some(gate) =>
                (
                  Some(building), Some(warpgate),
                  out, findNeighborhoodNormalBuilding(gate.Neighbours.getOrElse(Nil))
                )
              case None =>
                (Some(building), Some(warpgate), None, None)
            }
        }
      }
      else {
        (None, None, None, None)
      }
      (thisBuilding, thisWarpGate, otherWarpGate, otherBuilding) match {
        case (Some(bldg), Some(wg : WarpGate), Some(otherWg : WarpGate), Some(otherBldg)) =>
          //standard case where a building connected to a warp gate pair changes faction
          val bldgFaction = bldg.Faction
          val otherBldgFaction = otherBldg.Faction
          val setBroadcastTo = if (Config.app.game.warpGates.broadcastBetweenConflictedFactions) {
            Set(bldgFaction, otherBldgFaction)
          }
          else if (bldgFaction == otherBldgFaction) {
            Set(bldgFaction)
          }
          else {
            Set(PlanetSideEmpire.NEUTRAL)
          }
          updateBroadcastCapabilitiesOfWarpGate(details, wg, setBroadcastTo)
          updateBroadcastCapabilitiesOfWarpGate(details, otherWg, setBroadcastTo)
          if (wg.Zone.map.cavern && !otherWg.Zone.map.cavern) {
            otherWg.Zone.actor ! ZoneActor.ZoneMapUpdate()
          }

        case (Some(_), Some(wg : WarpGate), Some(otherWg : WarpGate), None) =>
          handleWarpGateDeadendPair(details, wg, otherWg)

        case (None, Some(wg : WarpGate), Some(otherWg : WarpGate), Some(_)) =>
          handleWarpGateDeadendPair(details, otherWg, wg)

        case (_, Some(wg: WarpGate), None, None) if !wg.Active =>
          updateBroadcastCapabilitiesOfWarpGate(details, wg, Set(PlanetSideEmpire.NEUTRAL))

        case (None, None, Some(wg: WarpGate), _) if !wg.Active =>
          updateBroadcastCapabilitiesOfWarpGate(details, wg, Set(PlanetSideEmpire.NEUTRAL))

        case _ => ;
          //everything else is a degenerate pattern that should have been reported at an earlier point
      }
    }
    Behaviors.same
  }

  /**
    * Do these buildings include a warp gate?
    * @param neighborhood a series of buildings of various types
    * @return the discovered warp gate
    */
  def findNeighborhoodWarpGate(neighborhood: Iterable[Building]): Option[Building] = {
    neighborhood.find { _ match { case _: WarpGate => true; case _ => false } }
  }

  /**
    * Do these buildings include any facility that is not a warp gate?
    * @param neighborhood a series of buildings of various types
    * @return the discovered warp gate
    */
  def findNeighborhoodNormalBuilding(neighborhood: Iterable[Building]): Option[Building] = {
    neighborhood.find { _ match { case _: WarpGate => false; case _ => true } }
  }

  /**
    * Normally, warp gates are connected to each other in a transcontinental pair.
    * Onto either gate is a non-gate facility of some sort.
    * The facilities on either side normally influence the gate pair; but,
    * in this case, only one side of the pair has a facility connected to it.
    * Some warp gates are directed to point to different destination warp gates based on the server's policies.
    * Another exception to the gate pair rule is a pure broadcast warp gate.
    * @param details package class that conveys the important information
    * @param warpgate one side of the warp gate pair (usually "our" side)
    * @param otherWarpgate the other side of the warp gate pair
    */
  private def handleWarpGateDeadendPair(
                                         details: BuildingWrapper,
                                         warpgate: WarpGate,
                                         otherWarpgate: WarpGate
                                       ): Unit = {
    //either the terminal warp gate messaged its connected gate, or the connected gate messaged the terminal gate
    //make certain the connected gate matches the terminal gate's faction
    val otherWarpgateFaction = otherWarpgate.Faction
    if (warpgate.Faction != otherWarpgateFaction) {
      warpgate.Faction = otherWarpgateFaction
      details.galaxyService ! GalaxyServiceMessage(GalaxyAction.MapUpdate(warpgate.infoUpdateMessage()))
    }
    //can not be considered broadcast for other factions
    val wgBroadcastAllowances = warpgate.AllowBroadcastFor
    if (!wgBroadcastAllowances.contains(PlanetSideEmpire.NEUTRAL) || !wgBroadcastAllowances.contains(otherWarpgateFaction)) {
      updateBroadcastCapabilitiesOfWarpGate(details, warpgate, Set(PlanetSideEmpire.NEUTRAL))
    }
  }

  /**
    * The broadcast settings of a warp gate are changing
    * and updates must be provided to the affected factions.
    * @param details package class that conveys the important information
    * @param warpgate the warp gate entity
    * @param setBroadcastTo factions(s) to which the warp gate is to broadcast going forward
    */
  private def updateBroadcastCapabilitiesOfWarpGate(
                                                     details: BuildingWrapper,
                                                     warpgate: WarpGate,
                                                     setBroadcastTo: Set[PlanetSideEmpire.Value]
                                                   ) : Unit = {
    val previousAllowances = warpgate.AllowBroadcastFor
    val events = details.galaxyService
    val msg = GalaxyAction.UpdateBroadcastPrivileges(
      warpgate.Zone.Number, warpgate.MapId, previousAllowances, setBroadcastTo
    )
    warpgate.AllowBroadcastFor = setBroadcastTo
    (setBroadcastTo ++ previousAllowances).foreach { faction =>
      events ! GalaxyServiceMessage(faction.toString, msg)
    }
  }

  /**
    * Warp gates are limitless sources of nanite transfer units when they are active.
    * They will always provide the amount specified.
    * @param details package class that conveys the important information
    * @param msg the original message that instigated this upoate
    * @return the next behavior for this control agency messaging system
    */
  def ntu(details: BuildingWrapper, msg: NtuCommand.Command): Behavior[Command] = {
    import NtuCommand._
    msg match {
      case Request(amount, replyTo) =>
        //warp gates are an infinite source of nanites
        val gate = details.building.asInstanceOf[WarpGate]
        replyTo ! Grant(gate, if (gate.Active) amount else 0)

      case _ => ;
    }
    Behaviors.same
  }
}
