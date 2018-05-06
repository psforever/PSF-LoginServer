// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.Actor
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.serverobject.structures.StructureType
import net.psforever.types.Vector3
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
        val buildingTypeSet = if(spawn_group == 6) {
          Set(StructureType.Tower)
        }
        else if(spawn_group == 7) {
          Set(StructureType.Facility, StructureType.Building)
        }
        else {
          Set.empty[StructureType.Value]
        }
        val playerPosition = player.Position.xy
        zone.SpawnGroups()
          .filter({ case((building, _)) =>
            building.Faction == player.Faction && buildingTypeSet.contains(building.BuildingType)
          })
          .toSeq
          .sortBy({ case ((building, _)) =>
            Vector3.DistanceSquared(playerPosition, building.Position.xy)
          })
          .headOption match {
          case Some((building, List(tube))) =>
            sender ! Zone.Lattice.SpawnPoint(zone.Id, building, tube)

          case Some((building, tubes)) =>
            sender ! Zone.Lattice.SpawnPoint(zone.Id, building, scala.util.Random.shuffle(tubes).head)

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
