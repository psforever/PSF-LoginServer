// Copyright (c) 2022 PSForever
package net.psforever.actors.zone.building

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.serverobject.structures.{Amenity, Building, WarpGate}
import net.psforever.services.galaxy.{GalaxyAction, GalaxyServiceMessage}
import net.psforever.types.PlanetSideEmpire

case object WarpGateLogic
  extends BuildingLogic {
  import BuildingActor.Command

  def updateForceDome(details: BuildingControlDetails, stateOpt: Option[Boolean]): Behavior[Command] = {
    Behaviors.same
  }

  def alignForceDomeStatus(details: BuildingControlDetails, mapUpdateOnChange: Boolean): Behavior[Command] = {
    Behaviors.same
  }

  def amenityStateChange(details: BuildingControlDetails, entity: Amenity, data: Option[Any]): Behavior[Command] = {
    Behaviors.same
  }

  def powerOff(details: BuildingControlDetails): Behavior[Command] = {
    Behaviors.same
  }

  def powerOn(details: BuildingControlDetails): Behavior[Command] = {
    Behaviors.same
  }

  def ntuDepleted(details: BuildingControlDetails): Behavior[Command] = {
    Behaviors.same
  }

  def suppliedWithNtu(details: BuildingControlDetails): Behavior[Command] = {
    Behaviors.same
  }

  def setFactionTo(details: BuildingControlDetails, faction: PlanetSideEmpire.Value): Behavior[Command] = {
    //don't set the faction manually, but determine what it should be by checking neighbor facilities
    // rule: we have no neighbors, use parameter faction
    // rule: we have a neighboring warp gate of the prospective faction, use parameter faction
    // rule: all of our neighbors are warp gates, use parameter faction
    // rule: all of our neighbors are same faction, use neighbor's faction
    val warpgate = details.building
    if (warpgate.Faction != faction) {
      val local = warpgate.Neighbours.getOrElse(Nil)
      BuildingActor.setFactionTo(details, faction, log)
      if (local.isEmpty) {
        log(details).error(s"warp gate ${warpgate.Name} isolated from neighborhood; check intercontinental linkage")
      } else {
        local.foreach { _.Actor ! BuildingActor.AlertToFactionChange(warpgate) }
      }
    }
    Behaviors.same
  }

  def alertToFactionChange(details: BuildingControlDetails, building: Building): Behavior[Command] = {
    val warpgate = details.building
    val local = warpgate.Neighbours.getOrElse(Nil)
    if (local.exists { _ eq building }) {
      /*
      output: Building, WarpGate:Us, WarpGate, Building
      where ":Us" means `details.building`, and ":Msg" means the caller `building`
      it could be "Building:Msg, WarpGate:Us, x, y" or "x, Warpgate:Us, Warpgate:Msg, y"
      */
      val (thisBuilding, thisWarpGate, otherWarpGate, otherBuilding) = if (local.exists { _ eq building }) {
        building match {
          case _: WarpGate =>
            (
              findNeighborhoodNormalBuilding(local), Some(warpgate),
              Some(building), findNeighborhoodNormalBuilding(building.Neighbours.getOrElse(Nil))
            )
          case _ =>
            findNeighborhoodWarpGate(local) match {
              case out @ Some(gate) =>
                (
                  Some(building), Some(warpgate),
                  out, findNeighborhoodNormalBuilding(gate.Neighbours.getOrElse(Nil))
                )
              case None =>
                (Some(building), Some(warpgate), None, None)
            }
        }
      } else {
        (None, None, None, None)
      }
      (thisBuilding, thisWarpGate, otherWarpGate, otherBuilding) match {
        case (Some(bldg), Some(wg: WarpGate), Some(otherWg: WarpGate), Some(otherBldg)) =>
          //standard case where a building connected to a warp gate pair changes faction
          val bldgFaction = bldg.Faction
          val otherBldgFaction = otherBldg.Faction
          val setBroadcastTo = Set(bldgFaction, otherBldgFaction)
          updateBroadcastCapabilitiesOfWarpGate(details, wg, setBroadcastTo)
          updateBroadcastCapabilitiesOfWarpGate(details, otherWg, setBroadcastTo)

        case (Some(_), Some(wg: WarpGate), Some(otherWg: WarpGate), None) =>
          handleWarpGateDeadendPair(details, wg, otherWg)

        case (None, Some(wg: WarpGate), Some(otherWg: WarpGate), Some(_)) =>
          handleWarpGateDeadendPair(details, otherWg, wg)

        case (a, b, c, d) =>
          //everything else is a degenerate pattern that should be reported
          log(details).warn(
            s"WarpGateLogic found degenerate intercontinental lattice link - $a <-> $b <=> $c <-> $d"
          )
      }
    }
    Behaviors.same
  }

  private def findNeighborhoodWarpGate(neighborhood: Iterable[Building]): Option[Building] = {
    neighborhood.find { _ match { case _: WarpGate => true; case _ => false } }
  }

  private def findNeighborhoodNormalBuilding(neighborhood: Iterable[Building]): Option[Building] = {
    neighborhood.find { _ match { case _: WarpGate => false; case _ => true } }
  }

  private def handleWarpGateDeadendPair(
                                         details: BuildingControlDetails,
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

  private def updateBroadcastCapabilitiesOfWarpGate(
                                                     details: BuildingControlDetails,
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

  def powerLost(details: BuildingControlDetails): Behavior[Command] = {
    Behaviors.same
  }

  def powerRestored(details: BuildingControlDetails): Behavior[Command] = {
    Behaviors.same
  }

  def ntu(details: BuildingControlDetails, msg: NtuCommand.Command): Behavior[Command] = {
    import NtuCommand._
    msg match {
      case Offer(_, _) =>
        Behaviors.same
      case Request(amount, replyTo) =>
        //warp gates are an infinite source of nanites
        val gate = details.building.asInstanceOf[WarpGate]
        replyTo ! Grant(gate, if (gate.Active) amount else 0)
        Behaviors.same
      case _ =>
        Behaviors.same
    }
  }
}
