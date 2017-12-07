// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.Actor

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
    def guid(id : Int) = zone.GUID(id)
    val map = zone.Map
    val slog = org.log4s.getLogger(s"zone/${zone.Id}/sanity")

    //check base to object associations
    map.ObjectToBase.foreach({ case((object_guid, base_id)) =>
      if(zone.Base(base_id).isEmpty) {
        slog.error(s"expected a base #$base_id")
      }
      if(guid(object_guid).isEmpty) {
        slog.error(s"expected object id $object_guid to exist, but it did not")
      }
    })

    //check door to lock association
    import net.psforever.objects.serverobject.doors.Door
    import net.psforever.objects.serverobject.locks.IFFLock
    map.DoorToLock.foreach({ case((door_guid, lock_guid)) =>
      try {
        if(!guid(door_guid).get.isInstanceOf[Door]) {
          slog.error(s"expected id $door_guid to be a door, but it was not")
        }
      }
      catch {
        case _ : Exception =>
          slog.error(s"expected a door at id $door_guid but no object is initialized")
      }
      try {
        if(!guid(lock_guid).get.isInstanceOf[IFFLock]) {
          slog.error(s"expected id $lock_guid to be an IFF locks but it was not")
        }
      }
      catch {
        case _ : Exception =>
          slog.error(s"expected an IFF locks at id $lock_guid but no object is initialized")
      }
    })

    //check vehicle terminal to spawn pad association
    import net.psforever.objects.serverobject.pad.VehicleSpawnPad
    import net.psforever.objects.serverobject.terminals.Terminal
    map.TerminalToSpawnPad.foreach({ case ((term_guid, pad_guid)) =>
      try {
        if(!guid(term_guid).get.isInstanceOf[Terminal]) { //TODO check is vehicle terminal
          slog.error(s"expected id $term_guid to be a terminal, but it was not")
        }
      }
      catch {
        case _ : Exception =>
          slog.error(s"expected a terminal at id $term_guid but no object is initialized")
      }
      try {
        if(!guid(pad_guid).get.isInstanceOf[VehicleSpawnPad]) {
          slog.error(s"expected id $pad_guid to be a spawn pad, but it was not")
        }
      }
      catch {
        case _ : Exception =>
          slog.error(s"expected a spawn pad at id $pad_guid but no object is initialized")
      }
    })

    //check implant terminal mech to implant terminal interface association
    import net.psforever.objects.serverobject.implantmech.ImplantTerminalMech
    map.TerminalToInterface.foreach({case ((mech_guid, interface_guid)) =>
      try {
        if(!guid(mech_guid).get.isInstanceOf[ImplantTerminalMech]) {
          slog.error(s"expected id $mech_guid to be an implant terminal mech, but it was not")
        }
      }
      catch {
        case _ : Exception =>
          slog.error(s"expected a implant terminal mech at id $mech_guid but no object is initialized")
      }
      try {
        if(!guid(interface_guid).get.isInstanceOf[Terminal]) { //TODO check is implant terminal
          slog.error(s"expected id $interface_guid to be an implant terminal interface, but it was not")
        }
      }
      catch {
        case _ : Exception =>
          slog.error(s"expected a implant terminal interface at id $interface_guid but no object is initialized")
      }
    })
  }
}
