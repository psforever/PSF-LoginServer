// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.structures

import java.util.concurrent.TimeUnit

import akka.actor.ActorContext
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.{GlobalDefinitions, NtuContainer, Player}
import net.psforever.objects.serverobject.generator.Generator
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.painbox.Painbox
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.zones.Zone
import net.psforever.objects.zones.blockmap.BlockMapEntity
import net.psforever.packet.game.BuildingInfoUpdateMessage
import net.psforever.types._
import scalax.collection.{Graph, GraphEdge}
import akka.actor.typed.scaladsl.adapter._
import net.psforever.objects.serverobject.llu.{CaptureFlag, CaptureFlagSocket}
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminal

class Building(
    private val name: String,
    private val building_guid: Int,
    private val map_id: Int,
    private val zone: Zone,
    private val buildingType: StructureType,
    private val buildingDefinition: BuildingDefinition
) extends AmenityOwner
  with BlockMapEntity {

  private var faction: PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  private var playersInSOI: List[Player]      = List.empty
  private val capitols                        = List("Thoth", "Voltan", "Neit", "Anguta", "Eisa", "Verica")
  private var forceDomeActive: Boolean        = false
  super.Zone_=(zone)
  super.GUID_=(PlanetSideGUID(building_guid)) //set
  Invalidate()                                //unset; guid can be used during setup, but does not stop being registered properly later

  override def toString = name

  def Name: String = name

  /**
    * The map_id is the identifier number used in BuildingInfoUpdateMessage. This is the index that the building appears in the MPO file starting from index 1
    * The GUID is the identifier number used in SetEmpireMessage / Facility hacking / PlanetSideAttributeMessage.
    */
  def MapId: Int = map_id

  def IsCapitol: Boolean = capitols.contains(name)
  def IsSubCapitol: Boolean = {
    Neighbours match {
      case Some(buildings: Set[Building]) => buildings.exists(x => capitols.contains(x.name))
      case None                           => false
    }
  }
  def ForceDomeActive: Boolean = forceDomeActive
  def ForceDomeActive_=(activated: Boolean): Boolean = {
    forceDomeActive = activated
    forceDomeActive
  }

  def Faction: PlanetSideEmpire.Value = faction

  override def Faction_=(fac: PlanetSideEmpire.Value): PlanetSideEmpire.Value = {
    faction = fac
    Faction
  }

  def PlayersInSOI: List[Player] = playersInSOI

  def PlayersInSOI_=(list: List[Player]): List[Player] = {
    if (playersInSOI.isEmpty && list.nonEmpty) {
      Amenities.collect {
        case box: Painbox =>
          box.Actor ! Painbox.Start()
      }
    } else if (playersInSOI.nonEmpty && list.isEmpty) {
      Amenities.collect {
        case box: Painbox =>
          box.Actor ! Painbox.Stop()
      }
    }
    playersInSOI = list
    playersInSOI
  }

  // Get all lattice neighbours
  def AllNeighbours: Option[Set[Building]] = {
    zone.Lattice find this match {
      case Some(x) => Some(x.diSuccessors.map(x => x.toOuter))
      case None    => None
    }
  }

  // Get all lattice neighbours that are active
  // This is important because warp gates can be inactive
  def Neighbours: Option[Set[Building]] = {
    AllNeighbours collect {
      case wg: WarpGate if wg.Active => wg
      case b                         => b
    }
  }

  def NtuSource: Option[NtuContainer] = {
    Amenities.find(_.isInstanceOf[NtuContainer]) match {
      case Some(o: NtuContainer) => Some(o)
      case _                     => None
    }
  }

  def NtuLevel: Int = {
    //if we have a silo, get the NTU level
    Amenities.find(_.Definition == GlobalDefinitions.resource_silo) match {
      case Some(obj: ResourceSilo) =>
        obj.CapacitorDisplay.toInt
      case _ => //we have no silo; we have unlimited power
        10
    }
  }

  def Generator: Option[Generator] = {
    Amenities.find(_.isInstanceOf[Generator]) match {
      case Some(obj: Generator) => Some(obj)
      case _                    => None
    }
  }

  def CaptureTerminal: Option[CaptureTerminal] = {
    Amenities.find(_.isInstanceOf[CaptureTerminal]) match {
      case Some(term) => Some(term.asInstanceOf[CaptureTerminal])
      case _          => None
    }
  }

  def IsCtfBase: Boolean = GetFlagSocket match {
    case Some(_) => true
    case _ => false
  }

  def GetFlagSocket: Option[CaptureFlagSocket] = this.Amenities.find(_.Definition == GlobalDefinitions.llm_socket).asInstanceOf[Option[CaptureFlagSocket]]
  def GetFlag: Option[CaptureFlag] = {
    GetFlagSocket match {
      case Some(socket) => socket.captureFlag
      case None         => None
    }
  }

  def HackableAmenities: List[Amenity with Hackable] = {
    Amenities.filter(x => x.isInstanceOf[Hackable]).map(x => x.asInstanceOf[Amenity with Hackable])
  }

  def CaptureTerminalIsHacked: Boolean = {
    CaptureTerminal match {
      case Some(obj: CaptureTerminal) =>
        obj.HackedBy.isDefined
      case None => false
    }
  }

  // Get all lattice neighbours matching the specified faction
  def Neighbours(faction: PlanetSideEmpire.Value): Option[Set[Building]] = {
    this.Neighbours match {
      case Some(x: Set[Building]) =>
        val matching = x.filter(b => b.Faction == faction)
        if (matching.isEmpty) None else Some(matching)
      case None => None
    }
  }

  def infoUpdateMessage(): BuildingInfoUpdateMessage = {
    val ntuLevel: Int = NtuLevel
    //if we have a capture terminal, get the hack status & time (in milliseconds) from control console if it exists
    val (hacking, hackingFaction, hackTime): (Boolean, PlanetSideEmpire.Value, Long) = CaptureTerminal match {
      case Some(obj: CaptureTerminal with Hackable) =>
        obj.HackedBy match {
          case Some(Hackable.HackInfo(_, _, hfaction, _, start, length)) =>
            val hack_time_remaining_ms =
              TimeUnit.MILLISECONDS.convert(math.max(0, start + length - System.nanoTime), TimeUnit.NANOSECONDS)
            (true, hfaction, hack_time_remaining_ms)
          case _ =>
            (false, PlanetSideEmpire.NEUTRAL, 0L)
        }
      case _ =>
        (false, PlanetSideEmpire.NEUTRAL, 0L)
    }
    //if we have no generator, assume the state is "Normal"
    val (generatorState, boostGeneratorPain) = Generator match {
      case Some(obj) =>
        (obj.Condition, false) // todo: poll pain field strength
      case _ =>
        (PlanetSideGeneratorState.Normal, false)
    }
    //if we have spawn tubes, determine if any of them are active
    val (spawnTubesNormal, boostSpawnPain): (Boolean, Boolean) = {
      val o = Amenities.collect({ case tube: SpawnTube if !tube.Destroyed => tube })
      (o.nonEmpty, false) //TODO poll pain field strength
    }
    val cavernBenefit: Set[CavernBenefit] = if (
      generatorState != PlanetSideGeneratorState.Destroyed &&
      faction != PlanetSideEmpire.NEUTRAL &&
      connectedCavern().nonEmpty
    ) {
      Set(CavernBenefit.VehicleModule, CavernBenefit.EquipmentModule)
    } else {
      Set(CavernBenefit.None)
    }

    BuildingInfoUpdateMessage(
      Zone.Number,
      MapId,
      ntuLevel,
      hacking,
      hackingFaction,
      hackTime,
      if (ntuLevel > 0) Faction else PlanetSideEmpire.NEUTRAL,
      unk1 = 0, // unk1 != 0 will cause malformed packet
      unk1x = None,
      generatorState,
      spawnTubesNormal,
      forceDomeActive,
      latticeConnectedFacilityBenefits(),
      cavernBenefit,
      unk4 = Nil,
      unk5 = 0,
      unk6 = false,
      unk7 = 8,     // unk7 != 8 will cause malformed packet
      unk7x = None,
      boostSpawnPain,
      boostGeneratorPain
    )
  }

  def hasLatticeBenefit(wantedBenefit: LatticeBenefit): Boolean = {
    val genState = Generator match {
      case Some(obj) => obj.Condition != PlanetSideGeneratorState.Destroyed
      case _         => false
    }
    if (genState || Faction == PlanetSideEmpire.NEUTRAL) {
      false
    } else {
      // Check this Building is on the lattice first
      zone.Lattice find this match {
        case Some(_) =>
          val subGraph = Zone.Lattice filter (
            (b : Building) =>
              b.Faction == this.Faction &&
              !b.CaptureTerminalIsHacked &&
              b.NtuLevel > 0 &&
              (b.Generator.isEmpty || b.Generator.get.Condition != PlanetSideGeneratorState.Destroyed)
            )
          findLatticeBenefit(wantedBenefit, subGraph)
        case None =>
          false
      }
    }
  }

  private def findLatticeBenefit(
                                  wantedBenefit: LatticeBenefit,
                                  subGraph: Graph[Building, GraphEdge.UnDiEdge]
                                ): Boolean = {
    var found = false
    subGraph find this match {
      case Some(self) =>
        if (this.Definition.LatticeLinkBenefit == wantedBenefit) {
          found = true
        } else {
          self pathUntil (_.Definition.LatticeLinkBenefit == wantedBenefit) match {
            case Some(_) => found = true
            case None    => ;
          }
        }
      case None => ;
    }
    found
  }

  def latticeConnectedFacilityBenefits(): Set[LatticeBenefit] = {
    val genState = Generator match {
      case Some(obj) => obj.Condition
      case _         => PlanetSideGeneratorState.Normal
    }
    if (genState == PlanetSideGeneratorState.Destroyed || Faction == PlanetSideEmpire.NEUTRAL) {
      Set.empty[LatticeBenefit]
    } else {
      friendlyFunctionalNeighborhood().map { _.Definition.LatticeLinkBenefit }
    }
  }

  def friendlyFunctionalNeighborhood(): Set[Building] = {
    var (currBuilding, newNeighbors) = Neighbours(faction).getOrElse(Set.empty[Building]).toList.splitAt(1)
    var visitedNeighbors: Set[Int] = Set(MapId)
    var friendlyNeighborhood: List[Building] = List(this)
    while (currBuilding.nonEmpty) {
      val building = currBuilding.head
      val neighborsToAdd = if (!visitedNeighbors.contains(building.MapId)
                               && (building match { case _ : WarpGate => false;  case _ => true })
                               && !building.CaptureTerminalIsHacked
                               && building.NtuLevel > 0
                               && (building.Generator match {
        case Some(o) => o.Condition != PlanetSideGeneratorState.Destroyed
        case _ => true
      })
      ) {
        visitedNeighbors = visitedNeighbors ++ Set(building.MapId)
        friendlyNeighborhood = friendlyNeighborhood :+ building
        building.Neighbours(faction)
          .getOrElse(Set.empty[Building])
          .toList
          .filterNot { b => visitedNeighbors.contains(b.MapId) }
      } else {
        Nil
      }
      val allocatedNeighbors = newNeighbors ++ neighborsToAdd
      currBuilding = allocatedNeighbors.take(1)
      newNeighbors = allocatedNeighbors.drop(1)
    }
    friendlyNeighborhood.toSet
  }

  /**
    * Starting from an overworld zone facility,
    * find a lattice connected cavern facility that is the same faction as this starting building.
    * Except for the necessary examination of the major facility on the other side of a warp gate pair,
    * do not let the search escape the current zone into another.
    * If we start in a cavern zone, do not continue a fruitless search;
    * just fail.
    * @return the discovered faction-aligned cavern facility
    */
  def connectedCavern(): Option[Building] = net.psforever.objects.zones.Zone.findConnectedCavernFacility(building = this)

  def BuildingType: StructureType = buildingType

  override def Zone_=(zone: Zone): Zone = Zone //building never leaves zone after being set in constructor

  override def Continent: String = Zone.id

  override def Continent_=(zone: String): String = Continent //building never leaves zone after being set in constructor

  def Definition: BuildingDefinition = buildingDefinition
}

object Building {
  final val NoBuilding: Building =
    new Building(name = "", 0, map_id = 0, Zone.Nowhere, StructureType.Platform, GlobalDefinitions.building) {
      override def Faction_=(faction: PlanetSideEmpire.Value): PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
      override def Amenities_=(obj: Amenity): List[Amenity]                           = Nil
      GUID = net.psforever.types.PlanetSideGUID(0)
    }

  def apply(name: String, guid: Int, map_id: Int, zone: Zone, buildingType: StructureType): Building = {
    new Building(name, guid, map_id, zone, buildingType, GlobalDefinitions.building)
  }

  def Structure(
      buildingType: StructureType,
      location: Vector3,
      rotation: Vector3,
      definition: BuildingDefinition
  )(name: String, guid: Int, map_id: Int, zone: Zone, context: ActorContext): Building = {
    val obj = new Building(name, guid, map_id, zone, buildingType, definition)
    obj.Position = location
    obj.Orientation = rotation
    obj.Actor = context.spawn(BuildingActor(zone, obj), s"$map_id-$buildingType-building").toClassic
    obj
  }

  def Structure(
      buildingType: StructureType,
      location: Vector3
  )(name: String, guid: Int, map_id: Int, zone: Zone, context: ActorContext): Building = {
    val obj = new Building(name, guid, map_id, zone, buildingType, GlobalDefinitions.building)
    obj.Position = location
    obj.Actor = context.spawn(BuildingActor(zone, obj), s"$map_id-$buildingType-building").toClassic
    obj
  }

  def Structure(
      buildingType: StructureType
  )(name: String, guid: Int, map_id: Int, zone: Zone, context: ActorContext): Building = {
    val obj = new Building(name, guid, map_id, zone, buildingType, GlobalDefinitions.building)
    obj.Actor = context.spawn(BuildingActor(zone, obj), s"$map_id-$buildingType-building").toClassic
    obj
  }

  def Structure(
      buildingType: StructureType,
      buildingDefinition: BuildingDefinition,
      location: Vector3
  )(name: String, guid: Int, id: Int, zone: Zone, context: ActorContext): Building = {
    val obj = new Building(name, guid, id, zone, buildingType, buildingDefinition)
    obj.Position = location
    obj.Actor = context.spawn(BuildingActor(zone, obj), s"$id-$buildingType-building").toClassic
    obj
  }
}
