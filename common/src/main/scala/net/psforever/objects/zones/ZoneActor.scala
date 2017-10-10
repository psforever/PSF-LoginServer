// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.Actor
import net.psforever.objects.serverobject.locks.IFFLock

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

    //check door to locks association
    import net.psforever.objects.serverobject.doors.Door
    map.DoorToLock.foreach({ case((door_guid, lock_guid)) =>
      try {
        if(!guid(door_guid).get.isInstanceOf[Door]) {
          slog.error(s"expected id $door_guid to be a door, but it was not")
        }
      }
      catch {
        case _ : Exception =>
          slog.error(s"expected a door, but looking for uninitialized object $door_guid")
      }
      try {
        if(!guid(lock_guid).get.isInstanceOf[IFFLock]) {
          slog.error(s"expected id $lock_guid to be an IFF locks, but it was not")
        }
      }
      catch {
        case _ : Exception =>
          slog.error(s"expected an IFF locks, but looking for uninitialized object $lock_guid")
      }
    })
  }
}
