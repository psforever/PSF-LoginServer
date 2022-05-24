// Copyright (c) 2022 PSForever
package net.psforever.actors.zone.building

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.serverobject.structures.{Amenity, Building, WarpGate}
import net.psforever.services.galaxy.{GalaxyAction, GalaxyServiceMessage}
import net.psforever.types.PlanetSideEmpire
import net.psforever.util.Config

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

  def setFactionTo(details: BuildingWrapper, faction: PlanetSideEmpire.Value): Behavior[Command] = {
    //in general, the faction set not be set manually, but instead determined by checking neighbor facilities
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

  def alertToFactionChange(details: BuildingWrapper, building: Building): Behavior[Command] = {
    val warpgate = details.building.asInstanceOf[WarpGate]
    if (warpgate.Active) {
      val local = warpgate.Neighbours.getOrElse(Nil)
      if (local.exists {
        _ eq building
      }) {
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

          case (Some(_), Some(wg : WarpGate), Some(otherWg : WarpGate), None) =>
            handleWarpGateDeadendPair(details, wg, otherWg)

          case (None, Some(wg : WarpGate), Some(otherWg : WarpGate), Some(_)) =>
            handleWarpGateDeadendPair(details, otherWg, wg)

          case (_, Some(wg: WarpGate), None, None) if !wg.Active =>
            updateBroadcastCapabilitiesOfWarpGate(details, wg, Set(PlanetSideEmpire.NEUTRAL))

          case (None, None, Some(wg: WarpGate), _) if !wg.Active =>
            updateBroadcastCapabilitiesOfWarpGate(details, wg, Set(PlanetSideEmpire.NEUTRAL))

          case _ => ;
            //everything else is a degenerate pattern that should be reported at an earlier point
        }
      }
    }
    Behaviors.same
  }

  def findNeighborhoodWarpGate(neighborhood: Iterable[Building]): Option[Building] = {
    neighborhood.find { _ match { case _: WarpGate => true; case _ => false } }
  }

  def findNeighborhoodNormalBuilding(neighborhood: Iterable[Building]): Option[Building] = {
    neighborhood.find { _ match { case _: WarpGate => false; case _ => true } }
  }

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

  def powerLost(details: BuildingWrapper): Behavior[Command] = {
    Behaviors.same
  }

  def powerRestored(details: BuildingWrapper): Behavior[Command] = {
    Behaviors.same
  }

  def ntu(details: BuildingWrapper, msg: NtuCommand.Command): Behavior[Command] = {
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
