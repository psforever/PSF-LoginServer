package net.psforever.zones

import java.io.FileNotFoundException

import net.psforever.objects.serverobject.terminals.{
  CaptureTerminal,
  CaptureTerminalDefinition,
  ProximityTerminal,
  ProximityTerminalDefinition,
  Terminal,
  TerminalDefinition
}
import net.psforever.objects.serverobject.mblocker.Locker
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorContext
import io.circe._
import io.circe.parser._
import net.psforever.objects.{GlobalDefinitions, LocalLockerItem, LocalProjectile}
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.definition.BasicDefinition
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.generator.Generator
import net.psforever.objects.serverobject.implantmech.ImplantTerminalMech
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.pad.{VehicleSpawnPad, VehicleSpawnPadDefinition}
import net.psforever.objects.serverobject.painbox.{Painbox, PainboxDefinition}
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.{
  Building,
  BuildingDefinition,
  FoundationBuilder,
  StructureType,
  WarpGate
}
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.serverobject.turret.{FacilityTurret, FacilityTurretDefinition}
import net.psforever.objects.zones.{MapInfo, Zone, ZoneInfo, ZoneMap}
import net.psforever.types.{PlanetSideEmpire, Vector3}
import net.psforever.util.DefinitionUtil

import scala.io.Source
import scala.collection.parallel.CollectionConverters._

object Zones {

  private case class ZoneMapEntity(
      id: Int,
      objectName: String,
      objectType: String,
      owner: Option[Int],
      absX: Float,
      absY: Float,
      absZ: Float,
      yaw: Float,
      guid: Int,
      mapId: Option[Int],
      isChildObject: Boolean
  ) {
    val position: Vector3 = Vector3(absX, absY, absZ)

    def objectDefinition: BasicDefinition = {
      DefinitionUtil.fromString(objectType)
    }
  }

  private implicit val decodeZoneMapEntity: Decoder[ZoneMapEntity] = Decoder.forProduct11(
    "Id",
    "ObjectName",
    "ObjectType",
    "Owner",
    "AbsX",
    "AbsY",
    "AbsZ",
    "Yaw",
    "GUID",
    "MapID",
    "IsChildObject"
  )(ZoneMapEntity.apply)

  // monolith, hst, warpgate are ignored for now as the scala code isn't ready to handle them.
  // BFR terminals/doors are ignored as top level elements as sanctuaries have them with no associated building. (repair_silo also has this problem, but currently is ignored in the AmenityExtrator project)
  // Force domes have GUIDs but are currently classed as separate entities. The dome is controlled by sending GOAM 44 / 48 / 52 to the building GUID
  private val ignoredEntities = Seq(
    "monolith",
    "bfr_door",
    "bfr_terminal",
    "force_dome_dsp_physics",
    "force_dome_comm_physics",
    "force_dome_cryo_physics",
    "force_dome_tech_physics",
    "force_dome_amp_physics"
  )

  private val towerTypes    = Seq("tower_a", "tower_b", "tower_c")
  private val facilityTypes = Seq("amp_station", "cryo_facility", "comm_station", "comm_station_dsp", "tech_plant")
  private val bunkerTypes   = Seq("bunker_gauntlet", "bunker_lg", "bunker_sm")
  private val warpGateTypes = Seq("hst", "warpgate", "warpgate_small", "warpgate_cavern")
  private val miscBuildingTypes = Seq(
    "orbital_building_vs",
    "orbital_building_tr",
    "orbital_building_nc",
    "VT_building_vs",
    "VT_building_tr",
    "VT_building_nc",
    "vt_dropship",
    "vt_spawn",
    "vt_vehicle"
  )
  private val cavernBuildingTypes = Seq(
    "ceiling_bldg_a",
    "ceiling_bldg_b",
    "ceiling_bldg_c",
    "ceiling_bldg_d",
    "ceiling_bldg_e",
    "ceiling_bldg_f",
    "ceiling_bldg_g",
    "ceiling_bldg_h",
    "ceiling_bldg_i",
    "ceiling_bldg_j",
    "ceiling_bldg_z",
    "ground_bldg_a",
    "ground_bldg_b",
    "ground_bldg_c",
    "ground_bldg_d",
    "ground_bldg_e",
    "ground_bldg_f",
    "ground_bldg_g",
    "ground_bldg_h",
    "ground_bldg_i",
    "ground_bldg_j",
    "ground_bldg_z",
    "redoubt",
    "vanu_control_point",
    "vanu_core",
    "vanu_vehicle_station"
  )
  private val basicTerminalTypes =
    Seq("order_terminal", "spawn_terminal", "cert_terminal", "order_terminal", "vanu_equipment_term")
  private val spawnPadTerminalTypes = Seq(
    "ground_vehicle_terminal",
    "air_vehicle_terminal",
    "vehicle_terminal",
    "vehicle_terminal_combined",
    "dropship_vehicle_terminal",
    "vanu_air_vehicle_term",
    "vanu_vehicle_term"
  )
  private val terminalTypes = basicTerminalTypes ++ spawnPadTerminalTypes

  private val spawnPadTypes = Seq(
    "mb_pad_creation",
    "dropship_pad_doors",
    "vanu_vehicle_creation_pad"
  )

  private val doorTypes = Seq(
    "gr_door_garage_int",
    "gr_door_int",
    "gr_door_med",
    "spawn_tube_door",
    "amp_cap_door",
    "door_dsp",
    "gr_door_ext",
    "gr_door_garage_ext",
    "gr_door_main",
    "gr_door_mb_ext",
    "gr_door_mb_int",
    "gr_door_mb_lrg",
    "gr_door_mb_obsd",
    "gr_door_mb_orb",
    "door_spawn_mb",
    "ancient_door",
    "ancient_garage_door"
  )

  lazy val zoneMaps: Seq[ZoneMap] = {
    val res  = Source.fromResource(s"zonemaps/lattice.json")
    val json = res.mkString
    res.close()
    val lattice = parse(json).toOption.get

    MapInfo.values.par
      .map { info =>
        val data =
          try {
            val res  = Source.fromResource(s"zonemaps/${info.value}.json")
            val json = res.mkString
            res.close()
            decode[Seq[ZoneMapEntity]](json).toOption.get.filter(e => !ignoredEntities.contains(e.objectType))
          } catch {
            case _: FileNotFoundException => Seq()
          }
        (info, data)
      }
      .map {
        case (info, data) =>
          val zoneMap = new ZoneMap(info.value)

          zoneMap.checksum = info.checksum
          zoneMap.scale = info.scale

          // This keeps track of the last used turret weapon guid, as they seem to be arbitrarily assigned at 5000+
          val turretWeaponGuid = new AtomicInteger(5000)

          val (structures, zoneObjects) = data
            .filter(!_.isChildObject)
            .partition(e =>
              facilityTypes.contains(e.objectType) ||
                towerTypes.contains(e.objectType) ||
                bunkerTypes.contains(e.objectType) ||
                warpGateTypes.contains(e.objectType) ||
                miscBuildingTypes.contains(e.objectType) ||
                cavernBuildingTypes.contains(e.objectType)
            )

          structures.foreach { structure =>
            // For some reason when spawning at a Redoubt building the client requests a spawn type of Tower
            // likely to allow the choice of spawning at both Redoubt and Module buildings
            val structureType =
              if (towerTypes.contains(structure.objectType) || structure.objectType == "redoubt")
                StructureType.Tower
              else if (facilityTypes.contains(structure.objectType))
                StructureType.Facility
              else if (bunkerTypes.contains(structure.objectType))
                StructureType.Bunker
              else
                StructureType.Building
            // todo: Platform types

            structure.objectType match {
              case objectType @ "hst" if warpGateTypes.contains(objectType) =>
                zoneMap.addLocalBuilding(
                  structure.objectName,
                  structure.guid,
                  structure.mapId.get,
                  FoundationBuilder(
                    WarpGate.Structure(Vector3(structure.absX, structure.absY, structure.absZ), GlobalDefinitions.hst)
                  )
                )
              case objectType if warpGateTypes.contains(objectType) =>
                zoneMap.addLocalBuilding(
                  structure.objectName,
                  structure.guid,
                  structure.mapId.get,
                  FoundationBuilder(WarpGate.Structure(Vector3(structure.absX, structure.absY, structure.absZ)))
                )
              case _ =>
                zoneMap.addLocalBuilding(
                  structure.objectName,
                  structure.guid,
                  structure.mapId.get,
                  FoundationBuilder(
                    Building.Structure(
                      structureType,
                      Vector3(structure.absX, structure.absY, structure.absZ),
                      Vector3(0f, 0f, structure.yaw),
                      structure.objectDefinition.asInstanceOf[BuildingDefinition]
                    )
                  )
                )

            }

            createObjects(
              zoneMap,
              data.filter(_.owner.contains(structure.id)),
              structure.guid,
              Some(structure),
              turretWeaponGuid
            )

          }

          createObjects(
            zoneMap,
            zoneObjects,
            0,
            None,
            turretWeaponGuid
          )

          (Projectile.baseUID until Projectile.rangeUID) foreach {
            zoneMap.addLocalObject(_, LocalProjectile.Constructor)
          }
          40150 until 40450 foreach {
            zoneMap.addLocalObject(_, LocalLockerItem.Constructor)
          }

          lattice.asObject.get(info.value).foreach { obj =>
            obj.asArray.get.foreach { entry =>
              val arr = entry.asArray.get
              zoneMap.addLatticeLink(arr(0).asString.get, arr(1).asString.get)
            }
          }

          zoneMap
      }
      .seq
  }

  private def createObjects(
      zoneMap: ZoneMap,
      objects: Seq[ZoneMapEntity],
      ownerGuid: Int,
      structure: Option[ZoneMapEntity],
      turretWeaponGuid: AtomicInteger
  ): Unit = {
    val spawnPads        = objects.filter(e => spawnPadTypes.contains(e.objectType))
    val doors            = objects.filter(e => doorTypes.contains(e.objectType))
    val implantTerminals = objects.filter(_.objectType == "implant_terminal")
    val genControls      = objects.filter(_.objectType == "gen_control")

    objects.foreach { obj =>
      if (ownerGuid == 0) assert(obj.owner.isEmpty)

      obj.objectType match {
        case "capture_terminal" | "secondary_capture" | "vanu_control_console" =>
          zoneMap.addLocalObject(
            obj.guid,
            CaptureTerminal.Constructor(
              obj.position,
              obj.objectDefinition.asInstanceOf[CaptureTerminalDefinition]
            ),
            owningBuildingGuid = ownerGuid
          )

        case objectType if doorTypes.contains(objectType) =>
          zoneMap
            .addLocalObject(obj.guid, Door.Constructor(obj.position), owningBuildingGuid = ownerGuid)

        case "locker_cryo" | "locker_med" | "mb_locker" =>
          zoneMap
            .addLocalObject(obj.guid, Locker.Constructor(obj.position), owningBuildingGuid = ownerGuid)

        case "lock_external" | "lock_garage" | "lock_small" =>
          val closestDoor = doors.minBy(d => Vector3.Distance(d.position, obj.position))

          // Since tech plant garage locks are the only type where the lock does not face the same direction as the door we need to apply an offset for those, otherwise the door won't operate properly when checking inside/outside angles.
          val yawOffset = if (obj.objectType == "lock_garage") 90 else 0

          zoneMap.addLocalObject(
            obj.guid,
            IFFLock.Constructor(obj.position, Vector3(0, 0, obj.yaw + yawOffset)),
            owningBuildingGuid = ownerGuid,
            doorGuid = closestDoor.guid
          )

        case objectType if structure.isDefined && terminalTypes.contains(objectType) =>
          // SoE in their infinite wisdom decided to remap vehicle_terminal to vehicle_terminal_combined in certain cases in the game_objects.adb file.
          // As such, we have to work around it.

          /*
            startup.pak-out/game_objects.adb.lst:1097:add_property amp_station child_remap vehicle_terminal vehicle_terminal_combined
            startup.pak-out/game_objects.adb.lst:7654:add_property comm_station child_remap vehicle_terminal vehicle_terminal_combined
            startup.pak-out/game_objects.adb.lst:7807:add_property cryo_facility child_remap vehicle_terminal vehicle_terminal_combined
           */
          val terminalType = (obj.objectType, structure.get.objectType) match {
            case ("vehicle_terminal", "amp_station" | "comm_station" | "cryo_facility") =>
              "vehicle_terminal_combined"
            // FIXME we're always using ground_vehicle_terminal in place of vehicle_terminal
            case ("vehicle_terminal", _) =>
              "ground_vehicle_terminal"
            case _ =>
              obj.objectType
          }

          zoneMap.addLocalObject(
            obj.guid,
            Terminal.Constructor(
              obj.position,
              DefinitionUtil.fromString(terminalType).asInstanceOf[TerminalDefinition]
            ),
            owningBuildingGuid = ownerGuid
          )

          if (spawnPadTerminalTypes.contains(obj.objectType)) {
            val closestSpawnPad =
              spawnPads.minBy(point => Vector3.DistanceSquared(point.position, obj.position))

            // It appears that spawn pads have a default rotation that it +90 degrees from where it should be
            // presumably the model is rotated differently to the expected orientation
            // On top of that, some spawn pads also have an additional rotation (vehiclecreationzorientoffset)
            // when spawning vehicles set in game_objects.adb.lst - this should be handled on the Scala side
            val adjustedYaw = closestSpawnPad.yaw - 90;

            zoneMap.addLocalObject(
              closestSpawnPad.guid,
              VehicleSpawnPad.Constructor(
                closestSpawnPad.position,
                closestSpawnPad.objectDefinition.asInstanceOf[VehicleSpawnPadDefinition],
                Vector3(0, 0, adjustedYaw)
              ),
              owningBuildingGuid = ownerGuid,
              terminalGuid = obj.guid
            )
          }

        case "resource_silo" =>
          zoneMap.addLocalObject(
            obj.guid,
            ResourceSilo.Constructor(obj.position),
            owningBuildingGuid = ownerGuid
          )

        case "respawn_tube" | "mb_respawn_tube" | "redoubt_floor" | "vanu_spawn_room_pad" if structure.isDefined =>
          zoneMap.addLocalObject(
            obj.guid,
            if (towerTypes.contains(structure.get.objectType))
              SpawnTube
                .Constructor(obj.position, GlobalDefinitions.respawn_tube_tower, Vector3(0, 0, obj.yaw))
            else if (structure.get.objectType.startsWith("VT_building_")) {
              SpawnTube
                .Constructor(obj.position, GlobalDefinitions.respawn_tube_sanctuary, Vector3(0, 0, obj.yaw))
            } else
              SpawnTube
                .Constructor(obj.position, Vector3(0, 0, obj.yaw)),
            owningBuildingGuid = ownerGuid
          )

        case "adv_med_terminal" | "repair_silo" | "pad_landing_frame" | "pad_landing_tower_frame" | "medical_terminal" |
            "crystals_health_a" | "crystals_health_b" =>
          zoneMap.addLocalObject(
            obj.guid,
            ProximityTerminal
              .Constructor(
                obj.position,
                obj.objectDefinition.asInstanceOf[ProximityTerminalDefinition]
              ),
            owningBuildingGuid = ownerGuid
          )

          // Some objects such as repair_silo and pad_landing_frame have special terminal objects
          // (e.g. bfr rearm, ground vehicle repair, ground vehicle rearm) that should follow immediately after,
          // with incrementing GUIDs. As such, these will be hardcoded for now.

          obj.objectType match {
            case "repair_silo" =>
              // startup.pak-out/game_objects.adb.lst:27235:add_property repair_silo has_aggregate_bfr_terminal true
              // startup.pak-out/game_objects.adb.lst:27236:add_property repair_silo has_aggregate_rearm_terminal true
              // startup.pak-out/game_objects.adb.lst:27237:add_property repair_silo has_aggregate_recharge_terminal true
              zoneMap.addLocalObject(
                obj.guid + 1,
                Terminal.Constructor(obj.position, GlobalDefinitions.ground_rearm_terminal),
                owningBuildingGuid = ownerGuid
              )
            case "pad_landing_frame" | "pad_landing_tower_frame" =>
              // startup.pak-out/game_objects.adb.lst:22518:add_property pad_landing_frame has_aggregate_rearm_terminal true
              // startup.pak-out/game_objects.adb.lst:22519:add_property pad_landing_frame has_aggregate_recharge_terminal true
              // startup.pak-out/game_objects.adb.lst:22534:add_property pad_landing_tower_frame has_aggregate_rearm_terminal true
              // startup.pak-out/game_objects.adb.lst:22535:add_property pad_landing_tower_frame has_aggregate_recharge_terminal true
              zoneMap.addLocalObject(
                obj.guid + 1,
                Terminal.Constructor(obj.position, GlobalDefinitions.air_rearm_terminal),
                owningBuildingGuid = ownerGuid
              )
            case _ => ;
          }

        case "manned_turret" | "vanu_sentry_turret" =>
          zoneMap.addLocalObject(
            obj.guid,
            FacilityTurret.Constructor(
              obj.position,
              obj.objectDefinition.asInstanceOf[FacilityTurretDefinition]
            ),
            owningBuildingGuid = ownerGuid
          )
          zoneMap.linkTurretToWeapon(obj.guid, turretWeaponGuid.getAndIncrement())

        case "implant_terminal_mech" =>
          zoneMap.addLocalObject(
            obj.guid,
            ImplantTerminalMech.Constructor(obj.position),
            owningBuildingGuid = ownerGuid
          )

          val closestTerminal = implantTerminals.minBy(e => Vector3.DistanceSquared(e.position, obj.position))

          zoneMap.addLocalObject(
            closestTerminal.guid,
            Terminal.Constructor(closestTerminal.position, GlobalDefinitions.implant_terminal_interface),
            owningBuildingGuid = ownerGuid
          )

          zoneMap.linkTerminalToInterface(obj.guid, closestTerminal.guid)

        case "painbox" | "painbox_continuous" | "painbox_door_radius" | "painbox_door_radius_continuous" |
            "painbox_radius" | "painbox_radius_continuous" =>
          zoneMap
            .addLocalObject(
              obj.guid,
              Painbox.Constructor(
                obj.position,
                obj.objectDefinition.asInstanceOf[PainboxDefinition]
              ),
              owningBuildingGuid = ownerGuid
            )

        case "generator" =>
          zoneMap
            .addLocalObject(
              obj.guid,
              Generator.Constructor(obj.position),
              owningBuildingGuid = ownerGuid
            )

          val genControl = genControls.minBy(e => Vector3.DistanceSquared(e.position, obj.position))

          zoneMap
            .addLocalObject(
              genControl.guid,
              Terminal.Constructor(genControl.position, GlobalDefinitions.gen_control),
              owningBuildingGuid = ownerGuid
            )

        case _ => ()
      }

    }
  }

  lazy val zones: Seq[Zone] = ZoneInfo.values.map { info =>
    new Zone(info.id, zoneMaps.find(_.name == info.map.value).get, info.value) {
      override def init(implicit context: ActorContext): Unit = {
        super.init(context)

        if (!info.id.startsWith("tz")) {
          this.HotSpotCoordinateFunction = Zones.HotSpots.standardRemapping(info.map.scale, 80, 80)
          this.HotSpotTimeFunction = Zones.HotSpots.standardTimeRules
          Zones.initZoneAmenities(this)
        }

        info.id match {
          case "home1" =>
            this.Buildings.values.foreach(_.Faction = PlanetSideEmpire.NC)
          case "home2" =>
            this.Buildings.values.foreach(_.Faction = PlanetSideEmpire.TR)
          case "home3" =>
            this.Buildings.values.foreach(_.Faction = PlanetSideEmpire.VS)
          case _ => ()
        }

        // Set up warp gate factions aka "sanctuary link". Those names make no sense anymore, don't even ask.
        this.Buildings.foreach {
          case (_, building) if building.Name.startsWith("WG") =>
            building.Name match {
              case "WG_Amerish_to_Solsar" | "WG_Esamir_to_VSSanc"    => building.Faction = PlanetSideEmpire.NC
              case "WG_Hossin_to_VSSanc" | "WG_Solsar_to_Amerish"    => building.Faction = PlanetSideEmpire.TR
              case "WG_Ceryshen_to_Hossin" | "WG_Forseral_to_Solsar" => building.Faction = PlanetSideEmpire.VS
              case _                                                 => ()
            }
          case _ => ()
        }
      }
    }
  }

  def initZoneAmenities(zone: Zone): Unit = {
    initResourceSilos(zone)
    initWarpGates(zone)

    def initWarpGates(zone: Zone): Unit = {
      // todo: work out which faction owns links to this warpgate and if they should be marked as broadcast or not
      // todo: enable geowarps to go to the correct cave
      zone.Buildings.values.collect {
        case wg: WarpGate
            if wg.Definition == GlobalDefinitions.warpgate || wg.Definition == GlobalDefinitions.warpgate_small =>
          wg.Active = true
          wg.Faction = PlanetSideEmpire.NEUTRAL
          wg.Broadcast = true
        case geowarp: WarpGate
            if geowarp.Definition == GlobalDefinitions.warpgate_cavern || geowarp.Definition == GlobalDefinitions.hst =>
          geowarp.Faction = PlanetSideEmpire.NEUTRAL
          geowarp.Active = false
      }
    }

    def initResourceSilos(zone: Zone): Unit = {
      // todo: load silo charge from database
      zone.Buildings.values.flatMap {
        _.Amenities.collect {
          case silo: ResourceSilo =>
            silo.Actor ! ResourceSilo.UpdateChargeLevel(silo.MaxNtuCapacitor)
        }
      }
    }
  }

  /**
    * Get the zone identifier name for the sanctuary continent of a given empire.
    *
    * @param faction the empire
    * @return the zone id
    */
  def sanctuaryZoneId(faction: PlanetSideEmpire.Value): String = {
    faction match {
      case PlanetSideEmpire.NC => "home1"
      case PlanetSideEmpire.TR => "home2"
      case PlanetSideEmpire.VS => "home3"
      case _                   => throw new UnsupportedOperationException()
    }
  }

  /**
    * Get the zone number for the sanctuary continent of a given empire.
    *
    * @param faction the empire
    * @return the zone number, within the sequence 1-32
    */
  def sanctuaryZoneNumber(faction: PlanetSideEmpire.Value): Int = {
    faction match {
      case PlanetSideEmpire.NC => 11
      case PlanetSideEmpire.TR => 12
      case PlanetSideEmpire.VS => 13
      case _                   => throw new UnsupportedOperationException()
    }
  }

  /**
    * Given a zone identification string, provide that zone's ordinal number.
    * As zone identification naming is extremely formulaic,
    * just being able to poll the zone's identifier by its first few letters will produce its ordinal position.
    *
    * @param id a zone id string
    * @return a zone number
    */
  def numberFromId(id: String): Int = {
    if (id.startsWith("z")) { //z2 -> 2
      id.substring(1).toInt
    } else if (id.startsWith("home")) { //home2 -> 2 + 10 = 12
      id.substring(4).toInt + 10
    } else if (id.startsWith("tz")) { //tzconc -> (14 + (3 * 1) + 2) -> 19
      (List("tr", "nc", "vs").indexOf(id.substring(4)) * 3) + List("sh", "dr", "co").indexOf(id.substring(2, 4)) + 14
    } else if (id.startsWith("c")) { //c2 -> 2 + 21 = 23
      id.substring(1).toInt + 21
    } else if (id.startsWith("i")) { //i2 -> 2 + 28 = 30
      id.substring(1).toInt + 28
    } else {
      0
    }
  }

  object HotSpots {
    import net.psforever.objects.ballistics.SourceEntry
    import net.psforever.objects.zones.MapScale
    import net.psforever.types.Vector3

    import scala.concurrent.duration._

    /**
      * Produce hotspot coordinates based on map coordinates.
      *
      * @see `FindClosestDivision`
      * @param scale      the map's scale (width and height)
      * @param longDivNum the number of division lines spanning the width of the `scale`
      * @param latDivNum  the number of division lines spanning the height of the `scale`
      * @param pos        the absolute position of the activity reported
      * @return the position for a hotspot
      */
    def standardRemapping(scale: MapScale, longDivNum: Int, latDivNum: Int)(pos: Vector3): Vector3 = {
      Vector3(
        //x
        findClosestDivision(pos.x, scale.width, longDivNum.toFloat),
        //y
        findClosestDivision(pos.y, scale.height, latDivNum.toFloat),
        //z is always zero - maps are flat 2D planes
        0
      )
    }

    /**
      * Produce hotspot coordinates based on map coordinates.<br>
      * <br>
      * Transform a reported number by mapping it
      * into a division from a regular pattern of divisions
      * defined by the scale divided evenly a certain number of times.
      * The depicted number of divisions is actually one less than the parameter number
      * as the first division is used to represent everything before that first division (there is no "zero").
      * Likewise, the last division occurs before the farther edge of the scale is counted
      * and is used to represent everything after that last division.
      * This is not unlike rounding.
      *
      * @param coordinate the point to scale
      * @param scale      the map's scale (width and height)
      * @param divisions  the number of division lines spanning across the `scale`
      * @return the closest regular division
      */
    private def findClosestDivision(coordinate: Float, scale: Float, divisions: Float): Float = {
      val divLength: Float = scale / divisions
      if (coordinate >= scale - divLength) {
        scale - divLength
      } else if (coordinate >= divLength) {
        val sector: Float     = (coordinate * divisions / scale).toInt * divLength
        val nextSector: Float = sector + divLength
        if (coordinate - sector < nextSector - coordinate) {
          sector
        } else {
          nextSector
        }
      } else {
        divLength
      }
    }

    /**
      * Determine a duration for which the hotspot will be displayed on the zone map.
      * Friendly fire is not recognized.
      *
      * @param defender the defending party
      * @param attacker the attacking party
      * @return the duration
      */
    def standardTimeRules(defender: SourceEntry, attacker: SourceEntry): FiniteDuration = {
      import net.psforever.objects.GlobalDefinitions
      import net.psforever.objects.ballistics._
      if (attacker.Faction == defender.Faction) {
        0 seconds
      } else {
        //TODO is target occupy-able and occupied, or jammer-able and jammered?
        defender match {
          case _: PlayerSource =>
            60 seconds
          case _: VehicleSource =>
            60 seconds
          case t: ObjectSource if t.Definition == GlobalDefinitions.manned_turret =>
            60 seconds
          case _: DeployableSource =>
            60 seconds
          case _: ComplexDeployableSource =>
            60 seconds
          case _ =>
            0 seconds
        }
      }
    }
  }
}
