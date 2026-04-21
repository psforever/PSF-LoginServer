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
      case _ => ()
    }
    isMounting = None
    zone.GUID(isDismounting) match {
      case Some(v: Vehicle) => v.Actor ! CargoBehavior.EndCargoDismounting(obj.GUID)
      case _ => ()
    }
    isDismounting = None
    startCargoDismountingNoCleanup(bailed = false)
  }

  val cargoBehavior: Receive = {
    case CargoBehavior.StartCargoMounting(carrier_guid, mountPoint) =>
      startCargoMounting(carrier_guid, mountPoint)

    case CargoBehavior.StartCargoDismounting(bailed) =>
      startCargoDismounting(bailed)

    case CargoBehavior.EndCargoMounting(carrier_guid) =>
      endCargoMounting(carrier_guid)

    case CargoBehavior.EndCargoDismounting(carrier_guid) =>
      endCargoDismounting(carrier_guid)
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
    if (!startCargoDismountingNoCleanup(bailed)) {
      isDismounting = None
      CargoObject.MountedIn = None
    }
  }

  def startCargoDismountingNoCleanup(bailed: Boolean): Boolean = {
    val obj = CargoObject
    obj.Zone.GUID(obj.MountedIn)
      .collect { case carrier: Vehicle =>
        (carrier, carrier.CargoHolds.find { case (_, hold) => hold.occupant.contains(obj) })
      }
      .collect { case (carrier, Some((mountPoint, _)))
        if isDismounting.isEmpty && isMounting.isEmpty =>
        isDismounting = obj.MountedIn
        carrier.Actor ! CarrierBehavior.CheckCargoDismount(obj.GUID, mountPoint, 0, bailed)
        true
      }
      .nonEmpty
  }

  def endCargoMounting(carrierGuid: PlanetSideGUID): Unit = {
    if (isMounting.contains(carrierGuid)) {
      isMounting = None
    }
  }

  def endCargoDismounting(carrierGuid: PlanetSideGUID): Unit = {
    if (isDismounting.contains(carrierGuid)) {
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
