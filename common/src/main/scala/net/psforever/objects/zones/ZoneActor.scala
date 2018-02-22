// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.Actor
import net.psforever.objects.PlanetSideGameObject
import org.log4s.Logger

/**
  * na
  * @param zone the `Zone` governed by this `Actor`
  */
class ZoneActor(zone : Zone) extends Actor {
  private[this] val log = org.log4s.getLogger

  def receive : Receive = {
    case Zone.Init() =>
      zone.Init
      ZoneSetupCheck()

    case msg =>
      log.warn(s"Received unexpected message - $msg")
  }

  def ZoneSetupCheck(): Unit = {
    import ZoneActor._
    def guid(id : Int) = zone.GUID(id)
    val map = zone.Map
    val slog = org.log4s.getLogger(s"zone/${zone.Id}/sanity")
    val validateObject : (Int, (PlanetSideGameObject)=>Boolean, String) => Boolean = ValidateObject(guid, slog)

    //check base to object associations
    map.ObjectToBuilding.foreach({ case((object_guid, base_id)) =>
      if(zone.Building(base_id).isEmpty) {
        slog.error(s"expected a building at id #$base_id")
      }
      if(guid(object_guid).isEmpty) {
        slog.error(s"expected object id $object_guid to exist, but it did not")
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
  def ValidateObject(guid : (Int)=>Option[PlanetSideGameObject], elog : Logger)
                    (object_guid : Int, test : (PlanetSideGameObject)=>Boolean, description : String) : Boolean = {
    try {
      if(!test(guid(object_guid).get)) {
        elog.error(s"expected id $object_guid to be a $description, but it was not")
        false
      }
      else {
        true
      }
    }
    catch {
      case _ : Exception =>
        elog.error(s"expected a $description at id $object_guid but no object is initialized")
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
