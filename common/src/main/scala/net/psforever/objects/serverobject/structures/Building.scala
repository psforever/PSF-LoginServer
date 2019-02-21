// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.structures

import akka.actor.ActorContext
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{PlanetSideEmpire, Vector3}

class Building(private val building_guid : Int, private val map_id : Int, private val zone : Zone, private val buildingType : StructureType.Value) extends PlanetSideServerObject {
  /**
    * The map_id is the identifier number used in BuildingInfoUpdateMessage. This is the index that the building appears in the MPO file starting from index 1
    * The GUID is the identifier number used in SetEmpireMessage / Facility hacking / PlanetSideAttributeMessage.
  */
  private var faction : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  private var amenities : List[Amenity] = List.empty
  GUID = PlanetSideGUID(building_guid)

  def MapId : Int = map_id

  def Faction : PlanetSideEmpire.Value = faction

  override def Faction_=(fac : PlanetSideEmpire.Value) : PlanetSideEmpire.Value = {
    faction = fac
    Faction
  }

  def Amenities : List[Amenity] = amenities

  def Amenities_=(obj : Amenity) : List[Amenity] = {
    amenities = amenities :+ obj
    obj.Owner = this
    amenities
  }

  def Zone : Zone = zone

  def BuildingType : StructureType.Value = buildingType

  override def Continent : String = zone.Id

  override def Continent_=(zone : String) : String = Continent

  def Definition: ObjectDefinition = Building.BuildingDefinition
}

object Building {
  final val NoBuilding : Building = new Building(building_guid = 0, map_id = 0, Zone.Nowhere, StructureType.Platform) {
    override def Faction_=(faction : PlanetSideEmpire.Value) : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
    override def Amenities_=(obj : Amenity) : List[Amenity] = Nil
  }

  final val BuildingDefinition : ObjectDefinition = new ObjectDefinition(0) { Name = "building" }

  def apply(guid : Int, map_id : Int, zone : Zone, buildingType : StructureType.Value) : Building = {
    new Building(guid, map_id, zone, buildingType)
  }

  def Structure(buildingType : StructureType.Value, location : Vector3)(guid : Int, map_id : Int, zone : Zone, context : ActorContext) : Building = {
    import akka.actor.Props
    val obj = new Building(guid, map_id, zone, buildingType)
    obj.Position = location
    obj.Actor = context.actorOf(Props(classOf[BuildingControl], obj), s"$map_id-$buildingType-building")
    obj
  }

  def Structure(buildingType : StructureType.Value)(guid: Int, map_id : Int, zone : Zone, context : ActorContext) : Building = {
    import akka.actor.Props
    val obj = new Building(guid, map_id, zone, buildingType)
    obj.Actor = context.actorOf(Props(classOf[BuildingControl], obj), s"$map_id-$buildingType-building")
    obj
  }

  final case class SendMapUpdate(all_clients: Boolean)
}
