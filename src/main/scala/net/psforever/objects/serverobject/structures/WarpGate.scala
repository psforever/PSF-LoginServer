// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.structures

import akka.actor.ActorContext
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.{GlobalDefinitions, NtuContainer, SpawnPoint}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.BuildingInfoUpdateMessage
import net.psforever.types._
import akka.actor.typed.scaladsl.adapter._
import net.psforever.actors.zone.BuildingActor

class WarpGate(name: String, building_guid: Int, map_id: Int, zone: Zone, buildingDefinition: WarpGateDefinition)
    extends Building(name, building_guid, map_id, zone, StructureType.WarpGate, buildingDefinition)
    with NtuContainer
    with SpawnPoint {
  /** can this building be used as an active warp gate */
  private var active: Boolean = true

  /** what faction views this warp gate as a broadcast gate */
  private var passageFor: Set[PlanetSideEmpire.Value] = Set(PlanetSideEmpire.NEUTRAL)

  override def infoUpdateMessage(): BuildingInfoUpdateMessage = {
    BuildingInfoUpdateMessage(
      Zone.Number,
      MapId,
      0,
      is_hacked = false,
      PlanetSideEmpire.NEUTRAL,
      0L,
      Faction, //should be neutral in most cases
      0, //Field != 0 will cause malformed packet. See class def.
      None,
      PlanetSideGeneratorState.Normal,
      spawn_tubes_normal = true,
      force_dome_active = false,
      Set(LatticeBenefit.None),
      Set(CavernBenefit.None), //Field > 0 will cause malformed packet. See class def.
      Nil,
      0,
      unk6 = false,
      8, //!! Field != 8 will cause malformed packet. See class def.
      None,
      boost_spawn_pain = false,
      boost_generator_pain = false
    )
  }

  /**
    * If a warp gate is active, it can be used to transport faction-affiliated forces between other gates.
    * For transportation of faction-opposed forces, use broadcast logic for that faction.
    * @return `true`, if the warp gate can be used for transport;
    *        `false`, otherwise
    */
  def Active: Boolean = active

  /**
    * Control whether a warp gate is usable for transporting faction-affiliated forces between other gates.
    * @param state `true`, to activate the gate;
    *             `false`, otherwise
    * @return `true`, if the gate is active;
    *        `false`, otherwise
    */
  def Active_=(state: Boolean): Boolean = {
    active = state
    Active
  }

  /**
    * Determine whether any faction interacts with this warp gate as "broadcast."
    * The gate must be active first.
    * A broadcast gate allows specific factions only.
    * @return `true`, if some faction sees this warp gate as a "broadcast gate";
    *        `false`, otherwise
    */
  def Broadcast: Boolean = Active && !passageFor.contains(PlanetSideEmpire.NEUTRAL)

  /**
    * Determine whether a specific faction interacts with this warp gate as "broadcast."
    * @return `true`, if the given faction interacts with this warp gate as a "broadcast gate";
    *        `false`, otherwise
    */
  def Broadcast(faction: PlanetSideEmpire.Value): Boolean = {
    Broadcast && passageFor.contains(faction)
  }

  /**
    * Which factions interact with this warp gate as "broadcast"?
    * @return the set of all factions who interact with this warp gate as "broadcast"
    */
  def AllowBroadcastFor: Set[PlanetSideEmpire.Value] = passageFor

  /**
    * Allow a faction to interact with a given warp gate as "broadcast" if it is active.
    * @param bcast the faction
    * @return the set of all factions who interact with this warp gate as "broadcast"
    */
  def AllowBroadcastFor_=(bcast: PlanetSideEmpire.Value): Set[PlanetSideEmpire.Value] = {
    AllowBroadcastFor_=(Set(bcast))
  }

  /**
    * Allow some factions to interact with a given warp gate as "broadcast" if it is active.
    * @param bcast the factions
    * @return the set of all factions who interact with this warp gate as "broadcast"
    */
  def AllowBroadcastFor_=(bcast: Set[PlanetSideEmpire.Value]): Set[PlanetSideEmpire.Value] = {
    val validFactions = bcast.filterNot(_ == PlanetSideEmpire.NEUTRAL)
    passageFor = if (bcast.isEmpty || validFactions.isEmpty) {
      Set(PlanetSideEmpire.NEUTRAL)
    } else {
      validFactions
    }
    AllowBroadcastFor
  }

  def Owner: PlanetSideServerObject = this

  def NtuCapacitor: Float = Definition.MaxNtuCapacitor

  def NtuCapacitor_=(value: Float): Float = NtuCapacitor

  def MaxNtuCapacitor : Float = Int.MaxValue

  override def isOffline: Boolean = !Active

  override def NtuSource: Option[NtuContainer] = Some(this)

  override def hasLatticeBenefit(wantedBenefit: LatticeBenefit): Boolean = false

  override def latticeConnectedFacilityBenefits(): Set[LatticeBenefit] = Set.empty[LatticeBenefit]

  override def Definition: WarpGateDefinition = buildingDefinition
}

object WarpGate {
  def apply(name: String, guid: Int, map_id: Int, zone: Zone, buildingDefinition: WarpGateDefinition): WarpGate = {
    new WarpGate(name, guid, map_id, zone, buildingDefinition)
  }

  def Structure(name: String, guid: Int, map_id: Int, zone: Zone, context: ActorContext): WarpGate = {
    val obj = new WarpGate(name, guid, map_id, zone, GlobalDefinitions.warpgate)
    obj.Actor = context.spawn(BuildingActor(zone, obj), name = s"$map_id-$guid-gate").toClassic
    obj
  }

  def Structure(
      location: Vector3
  )(name: String, guid: Int, map_id: Int, zone: Zone, context: ActorContext): WarpGate = {
    val obj = new WarpGate(name, guid, map_id, zone, GlobalDefinitions.warpgate)
    obj.Position = location
    obj.Actor = context.spawn(BuildingActor(zone, obj), name = s"$map_id-$guid-gate").toClassic
    obj
  }

  def Structure(
      location: Vector3,
      buildingDefinition: WarpGateDefinition
  )(name: String, guid: Int, map_id: Int, zone: Zone, context: ActorContext): WarpGate = {
    val obj = new WarpGate(name, guid, map_id, zone, buildingDefinition)
    obj.Position = location
    obj.Actor = context.spawn(BuildingActor(zone, obj), name = s"$map_id-$guid-gate").toClassic
    obj
  }
}
