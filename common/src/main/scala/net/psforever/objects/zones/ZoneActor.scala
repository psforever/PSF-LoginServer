// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.Actor
import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject}
import net.psforever.objects.serverobject.structures.StructureType
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.vehicles.UtilityType
import net.psforever.types.{DriveState, Vector3}
import org.log4s.Logger

/**
  * na
  * @param zone the `Zone` governed by this `Actor`
  */
class ZoneActor(zone : Zone) extends Actor {
  private[this] val log = org.log4s.getLogger

  def receive : Receive = Init

  def Init : Receive = {
    case Zone.Init() =>
      zone.Init
      ZoneSetupCheck()
      context.become(Processing)

    case _ => ;
  }

  def Processing : Receive = {
    //frwd to Population Actor
    case msg @ Zone.Population.Join =>
      zone.Population forward msg

    case msg @ Zone.Population.Leave =>
      zone.Population forward msg

    case msg @ Zone.Population.Spawn =>
      zone.Population forward msg

    case msg @ Zone.Population.Release =>
      zone.Population forward msg

    case msg @ Zone.Corpse.Add =>
      zone.Population forward msg

    case msg @ Zone.Corpse.Remove =>
      zone.Population forward msg

    //frwd to Ground Actor
    case msg @ Zone.DropItemOnGround =>
      zone.Ground forward msg

    case msg @ Zone.GetItemOnGround =>
      zone.Ground forward msg

    //frwd to Vehicle Actor
    case msg @ Zone.Vehicle.Spawn =>
      zone.Transport forward msg

    case msg @ Zone.Vehicle.Despawn =>
      zone.Transport forward msg

    //own
    case Zone.Lattice.RequestSpawnPoint(zone_number, player, spawn_group) =>
      if(zone_number == zone.Number) {
        val playerPosition = player.Position.xy
        (
          if(spawn_group == 2) {
            //ams
            zone.Vehicles
              .filter(veh =>
                veh.DeploymentState == DriveState.Deployed &&
                  veh.Definition == GlobalDefinitions.ams &&
                  veh.Faction == player.Faction
              )
              .sortBy(veh => Vector3.DistanceSquared(playerPosition, veh.Position.xy))
              .flatMap(veh => veh.Utilities.values.filter(util => util.UtilType == UtilityType.ams_respawn_tube))
              .headOption match {
              case None =>
                None
              case Some(util) =>
                Some(List(util().asInstanceOf[SpawnTube]))
            }
          }
          else {
            //facilities, towers, and buildings
            val buildingTypeSet = if(spawn_group == 0) {
              Set(StructureType.Facility, StructureType.Tower, StructureType.Building)
            }
            else if(spawn_group == 6) {
              Set(StructureType.Tower)
            }
            else if(spawn_group == 7) {
              Set(StructureType.Facility, StructureType.Building)
            }
            else {
              Set.empty[StructureType.Value]
            }
            zone.SpawnGroups()
              .filter({ case ((building, _)) =>
                building.Faction == player.Faction &&
                  buildingTypeSet.contains(building.BuildingType)
              })
              .toSeq
              .sortBy({ case ((building, _)) =>
                Vector3.DistanceSquared(playerPosition, building.Position.xy)
              })
              .headOption match {
              case None | Some((_, Nil)) =>
                None
              case Some((_, tubes)) =>
                Some(tubes)
            }
          }
          ) match {
          case Some(List(tube)) =>
            sender ! Zone.Lattice.SpawnPoint(zone.Id, tube)

          case Some(tubes) =>
            sender ! Zone.Lattice.SpawnPoint(zone.Id, scala.util.Random.shuffle(tubes).head)

          case None =>
            sender ! Zone.Lattice.NoValidSpawnPoint(zone_number, Some(spawn_group))
        }
      }
      else { //wrong zone_number
        sender ! Zone.Lattice.NoValidSpawnPoint(zone_number, None)
      }

    case msg =>
      log.warn(s"Received unexpected message - $msg")
  }

  def ZoneSetupCheck() : Int = {
    import ZoneActor._
    val map = zone.Map
    def guid(id : Int) = zone.GUID(id)
    val slog = org.log4s.getLogger(s"zone/${zone.Id}/sanity")
    val errors = new AtomicInteger(0)
    val validateObject : (Int, (PlanetSideGameObject)=>Boolean, String) => Boolean = ValidateObject(guid, slog, errors)

    //check base to object associations
    map.ObjectToBuilding.foreach({ case((object_guid, building_id)) =>
      if(zone.Building(building_id).isEmpty) {
        slog.error(s"expected a building at id #$building_id")
        errors.incrementAndGet()
      }
      if(guid(object_guid).isEmpty) {
        slog.error(s"expected object id $object_guid to exist, but it did not")
        errors.incrementAndGet()
      }
    })

    //check door to lock association
    map.DoorToLock.foreach({ case((door_guid, lock_guid)) =>
      validateObject(door_guid, DoorCheck, "door")
      validateObject(lock_guid, LockCheck, "IFF lock")
    })

    //check vehicle terminal to spawn pad association
    map.TerminalToSpawnPad.foreach({ case ((term_guid, pad_guid)) =>
      validateObject(term_guid, TerminalCheck, "vehicle terminal")
      validateObject(pad_guid, VehicleSpawnPadCheck, "vehicle spawn pad")
    })

    //check implant terminal mech to implant terminal interface association
    map.TerminalToInterface.foreach({case ((mech_guid, interface_guid)) =>
      validateObject(mech_guid, ImplantMechCheck, "implant terminal mech")
      validateObject(interface_guid, TerminalCheck, "implant terminal interface")
    })
    errors.intValue()
  }
}

object ZoneActor {
//  import net.psforever.types.PlanetSideEmpire
//  import net.psforever.objects.Vehicle
//  import net.psforever.objects.serverobject.structures.Building
//  def AllSpawnGroup(zone : Zone, targetPosition : Vector3, targetFaction : PlanetSideEmpire.Value) : Option[List[SpawnTube]] = {
//    ClosestOwnedSpawnTube(AmsSpawnGroup(zone) ++ BuildingSpawnGroup(zone, 0), targetPosition, targetFaction)
//  }
//
//  def AmsSpawnGroup(vehicles : List[Vehicle]) : Iterable[(Vector3, PlanetSideEmpire.Value, Iterable[SpawnTube])] = {
//    vehicles
//      .filter(veh => veh.DeploymentState == DriveState.Deployed && veh.Definition == GlobalDefinitions.ams)
//      .map(veh =>
//        (veh.Position, veh.Faction,
//          veh.Utilities
//            .values
//            .filter(util => util.UtilType == UtilityType.ams_respawn_tube)
//            .map { _().asInstanceOf[SpawnTube] }
//        )
//      )
//  }
//
//  def AmsSpawnGroup(zone : Zone, spawn_group : Int = 2) : Iterable[(Vector3, PlanetSideEmpire.Value, Iterable[SpawnTube])] = {
//    if(spawn_group == 2) {
//      AmsSpawnGroup(zone.Vehicles)
//    }
//    else {
//      Nil
//    }
//  }
//
//  def BuildingSpawnGroup(spawnGroups : Map[Building, List[SpawnTube]]) : Iterable[(Vector3, PlanetSideEmpire.Value, Iterable[SpawnTube])] = {
//    spawnGroups
//      .map({ case ((building, tubes)) => (building.Position.xy, building.Faction, tubes) })
//  }
//
//  def BuildingSpawnGroup(zone : Zone, spawn_group : Int) : Iterable[(Vector3, PlanetSideEmpire.Value, Iterable[SpawnTube])] = {
//    val buildingTypeSet = if(spawn_group == 0) {
//      Set(StructureType.Facility, StructureType.Tower, StructureType.Building)
//    }
//    else if(spawn_group == 6) {
//      Set(StructureType.Tower)
//    }
//    else if(spawn_group == 7) {
//      Set(StructureType.Facility, StructureType.Building)
//    }
//    else {
//      Set.empty[StructureType.Value]
//    }
//    BuildingSpawnGroup(
//      zone.SpawnGroups().filter({ case((building, _)) => buildingTypeSet.contains(building.BuildingType) })
//    )
//  }
//
//  def ClosestOwnedSpawnTube(tubes : Iterable[(Vector3, PlanetSideEmpire.Value, Iterable[SpawnTube])], targetPosition : Vector3, targetFaction : PlanetSideEmpire.Value) : Option[List[SpawnTube]] = {
//    tubes
//      .toSeq
//      .filter({ case (_, faction, _) => faction == targetFaction })
//      .sortBy({ case (pos, _, _) => Vector3.DistanceSquared(pos, targetPosition) })
//      .take(1)
//      .map({ case (_, _, tubes : List[SpawnTube]) => tubes })
//      .headOption
//  }

  /**
    * Recover an object from a collection and perform any number of validating tests upon it.
    * If the object fails any tests, log an error.
    * @param guid access to an association between unique numbers and objects using some of those unique numbers
    * @param elog a contraction of "error log;"
    *             accepts `String` data
    * @param object_guid the unique indentifier being checked against the `guid` access point
    * @param test a test for the discovered object;
    *             expects at least `Type` checking
    * @param description an explanation of how the object, if not discovered, should be identified
    * @return `true` if the object was discovered and validates correctly;
    *        `false` if the object failed any tests
    */
  def ValidateObject(guid : (Int)=>Option[PlanetSideGameObject], elog : Logger, errorCounter : AtomicInteger)
                    (object_guid : Int, test : (PlanetSideGameObject)=>Boolean, description : String) : Boolean = {
    try {
      if(!test(guid(object_guid).get)) {
        elog.error(s"expected id $object_guid to be a $description, but it was not")
        errorCounter.incrementAndGet()
        false
      }
      else {
        true
      }
    }
    catch {
      case e : Exception =>
        elog.error(s"expected a $description at id $object_guid but no object is initialized - $e")
        errorCounter.incrementAndGet()
        false
    }
  }

  def LockCheck(obj : PlanetSideGameObject) : Boolean = {
    import net.psforever.objects.serverobject.locks.IFFLock
    obj.isInstanceOf[IFFLock]
  }

  def DoorCheck(obj : PlanetSideGameObject) : Boolean = {
    import net.psforever.objects.serverobject.doors.Door
    obj.isInstanceOf[Door]
  }

  def TerminalCheck(obj : PlanetSideGameObject) : Boolean = {
    import net.psforever.objects.serverobject.terminals.Terminal
    obj.isInstanceOf[Terminal]
  }

  def ImplantMechCheck(obj : PlanetSideGameObject) : Boolean = {
    import net.psforever.objects.serverobject.implantmech.ImplantTerminalMech
    obj.isInstanceOf[ImplantTerminalMech]
  }

  def VehicleSpawnPadCheck(obj : PlanetSideGameObject) : Boolean = {
    import net.psforever.objects.serverobject.pad.VehicleSpawnPad
    obj.isInstanceOf[VehicleSpawnPad]
  }
}
