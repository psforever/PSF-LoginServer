// Copyright (c) 2020 PSForever
package net.psforever.objects.vehicles

import akka.actor.Actor
import net.psforever.objects._
import net.psforever.types.PlanetSideGUID

trait CargoBehavior {
  _: Actor =>
  /* gate-keep mounting behavior so that unit does not try to dismount as cargo, or mount different vehicle */
  private var isMounting: Option[PlanetSideGUID] = None
  /* gate-keep dismounting behavior so that unit does not try to mount as cargo, or dismount from different vehicle */
  private var isDismounting: Option[PlanetSideGUID] = None

  def CargoObject: Vehicle

  def endAllCargoOperations(): Unit = {
    val obj = CargoObject
    val zone = obj.Zone
    zone.GUID(isMounting) match {
      case Some(v : Vehicle) => v.Actor ! CargoBehavior.EndCargoMounting(obj.GUID)
      case _ => ;
    }
    isMounting = None
    zone.GUID(isDismounting) match {
      case Some(v: Vehicle) => v.Actor ! CargoBehavior.EndCargoDismounting(obj.GUID)
      case _ => ;
    }
    isDismounting = None
    startCargoDismounting(bailed = false)
  }

  val cargoBehavior: Receive = {
    case CargoBehavior.StartCargoMounting(carrier_guid, mountPoint) =>
      startCargoMounting(carrier_guid, mountPoint)

    case CargoBehavior.StartCargoDismounting(bailed) =>
      startCargoDismounting(bailed)

    case CargoBehavior.EndCargoMounting(carrier_guid) =>
      if (isMounting.contains(carrier_guid)) {
        isMounting = None
      }

    case CargoBehavior.EndCargoDismounting(carrier_guid) =>
      if (isDismounting.contains(carrier_guid)) {
        isDismounting = None
      }
  }

  def startCargoMounting(carrier_guid: PlanetSideGUID, mountPoint: Int): Unit = {
    val obj = CargoObject
    obj.Zone.GUID(carrier_guid) match {
      case Some(carrier: Vehicle)
        if isMounting.isEmpty && isDismounting.isEmpty && (carrier.CargoHolds.get(mountPoint) match {
          case Some(hold) => !hold.isOccupied
          case _ => false
        }) =>
        isMounting = Some(carrier_guid)
        carrier.Actor ! CarrierBehavior.CheckCargoMounting(obj.GUID, mountPoint, 0)
      case _ => ;
        isMounting = None
    }
  }

  def startCargoDismounting(bailed: Boolean): Unit = {
    val obj = CargoObject
    obj.Zone.GUID(obj.MountedIn) match {
      case Some(carrier: Vehicle) =>
        carrier.CargoHolds.find { case (_, hold) => hold.occupant.contains(obj) } match {
          case Some((mountPoint, _))
            if isDismounting.isEmpty && isMounting.isEmpty =>
            isDismounting = obj.MountedIn
            carrier.Actor ! CarrierBehavior.CheckCargoDismount(obj.GUID, mountPoint, 0, bailed)

          case _ =>
            obj.MountedIn = None
            isDismounting = None
        }
      case _ =>
        obj.MountedIn = None
        isDismounting = None
    }
  }
}

object CargoBehavior {
  final case class StartCargoMounting(cargo_guid: PlanetSideGUID, cargo_mountpoint: Int)
  final case class StartCargoDismounting(bailed: Boolean)
  final case class EndCargoMounting(carrier_guid: PlanetSideGUID)
  final case class EndCargoDismounting(carrier_guid: PlanetSideGUID)
}
