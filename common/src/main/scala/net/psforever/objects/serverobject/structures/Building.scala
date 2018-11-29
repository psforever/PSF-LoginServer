// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.structures

import akka.actor.ActorContext
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{PlanetSideEmpire, Vector3}

class Building(private val mapId : Int, private val zone : Zone, private val buildingType : StructureType.Value) extends PlanetSideServerObject {
  /**
    * The mapId is the identifier number used in BuildingInfoUpdateMessage.
    * The modelId is the identifier number used in SetEmpireMessage / Facility hacking / PlanetSideAttributeMessage.
  */
  private var modelId : Option[Int] = None
  private var faction : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  private var amenities : List[Amenity] = List.empty
  GUID = PlanetSideGUID(0)

  def Id : Int = mapId

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

  def ModelId : Int = modelId.getOrElse(Id)

  def ModelId_=(id : Int) : Int = ModelId_=(Some(id))

  def ModelId_=(id : Option[Int]) : Int = {
    modelId = id
    ModelId
  }

  def BuildingType : StructureType.Value = buildingType

  override def Continent : String = zone.Id

  override def Continent_=(zone : String) : String = Continent

  def Definition: ObjectDefinition = Building.BuildingDefinition
}

object Building {
  final val NoBuilding : Building = new Building(0, Zone.Nowhere, StructureType.Platform) {
    override def Faction_=(faction : PlanetSideEmpire.Value) : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
    override def Amenities_=(obj : Amenity) : List[Amenity] = Nil
  }

  final val BuildingDefinition : ObjectDefinition = new ObjectDefinition(0) { Name = "building" }

  def apply(id : Int, zone : Zone, buildingType : StructureType.Value) : Building = {
    new Building(id, zone, buildingType)
  }

  def Structure(buildingType : StructureType.Value, location : Vector3)(id : Int, zone : Zone, context : ActorContext) : Building = {
    import akka.actor.Props
    val obj = new Building(id, zone, buildingType)
    obj.Position = location
    obj.Actor = context.actorOf(Props(classOf[BuildingControl], obj), s"$id-$buildingType-building")
    obj
  }

  def Structure(buildingType : StructureType.Value)(id : Int, zone : Zone, context : ActorContext) : Building = {
    import akka.actor.Props
    val obj = new Building(id, zone, buildingType)
    obj.Actor = context.actorOf(Props(classOf[BuildingControl], obj), s"$id-$buildingType-building")
    obj
  }

  final case class SendMapUpdate(all_clients: Boolean)
}
