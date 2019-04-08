// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.structures

import akka.actor.ActorContext
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.{GlobalDefinitions, SpawnPoint, SpawnPointDefinition}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{Additional1, Additional2, Additional3, PlanetSideGeneratorState}
import net.psforever.types.{PlanetSideEmpire, Vector3}

class WarpGate(building_guid : Int, map_id : Int, zone : Zone, buildingDefinition : ObjectDefinition with SpawnPointDefinition)
  extends Building(building_guid, map_id, zone, StructureType.WarpGate, buildingDefinition)
    with SpawnPoint {
  private var active : Boolean = true
  private var broadcast : Boolean = false

  override def Info : (
    Int,
      Boolean, PlanetSideEmpire.Value, Long, PlanetSideEmpire.Value,
      Long, Option[Additional1],
      PlanetSideGeneratorState.Value, Boolean, Boolean,
      Int, Int,
      List[Additional2], Long, Boolean,
      Int, Option[Additional3],
      Boolean, Boolean
    ) = {
    (
      0,
      false,
      PlanetSideEmpire.NEUTRAL,
      0L,
      Faction,
      0, //!! Field != 0 will cause malformed packet. See class def.
      None,
      PlanetSideGeneratorState.Normal,
      true, //TODO?
      false, //force_dome_active
      0, //lattice_benefit
      0, //cavern_benefit; !! Field > 0 will cause malformed packet. See class def.
      Nil,
      0,
      false,
      8, //!! Field != 8 will cause malformed packet. See class def.
      None,
      false, //boost_spawn_pain
      false //boost_generator_pain
    )
  }

  def Active : Boolean = active

  def Active_=(state : Boolean) : Boolean = {
    active = state
    Active
  }

  def Broadcast : Boolean = Active && broadcast

  def Broadcast_=(cast : Boolean) : Boolean = {
    broadcast = cast
    Broadcast
  }

  def Owner : PlanetSideServerObject = this

  override def Definition : ObjectDefinition with SpawnPointDefinition = buildingDefinition
  //TODO stuff later
}

object WarpGate {
  def apply(guid : Int, map_id : Int, zone : Zone, buildingDefinition : ObjectDefinition with SpawnPointDefinition) : WarpGate = {
    new WarpGate(guid, map_id, zone, buildingDefinition)
  }

  def Structure(guid : Int, map_id : Int, zone : Zone, context : ActorContext) : WarpGate = {
    import akka.actor.Props
    val obj = new WarpGate(guid, map_id, zone, GlobalDefinitions.warpgate)
    obj.Actor = context.actorOf(Props(classOf[BuildingControl], obj), s"$map_id-gate")
    obj
  }

  def Structure(location : Vector3)(guid : Int, map_id : Int, zone : Zone, context : ActorContext) : WarpGate = {
    import akka.actor.Props
    val obj = new WarpGate(guid, map_id, zone, GlobalDefinitions.warpgate)
    obj.Position = location
    obj.Actor = context.actorOf(Props(classOf[BuildingControl], obj), s"$map_id-gate")
    obj
  }

  def Structure(location : Vector3, buildingDefinition : ObjectDefinition with SpawnPointDefinition)(guid : Int, map_id : Int, zone : Zone, context : ActorContext) : WarpGate = {
    import akka.actor.Props
    val obj = new WarpGate(guid, map_id, zone, buildingDefinition)
    obj.Position = location
    obj.Actor = context.actorOf(Props(classOf[BuildingControl], obj), s"$map_id-gate")
    obj
  }
}
