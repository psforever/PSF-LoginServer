// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles

import akka.actor.{Actor, Cancellable}
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.zones.Zone
import net.psforever.objects._
import net.psforever.packet.game.{
  CargoMountPointStatusMessage,
  ObjectAttachMessage,
  ObjectDetachMessage,
  PlanetsideAttributeMessage
}
import net.psforever.types.{BailType, CargoStatus, PlanetSideGUID, Vector3}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.duration._

trait CarrierBehavior {
  _: Actor =>
  private var cargoMountTimer: Cancellable          = Default.Cancellable
  private var cargoDismountTimer: Cancellable       = Default.Cancellable

  /* gate-keep mounting behavior so that another vehicle does not attempt to mount, or dismount in the middle */
  private var isMounting: Option[PlanetSideGUID]    = None
  /* gate-keep dismounting behavior so that another vehicle does not attempt to dismount, or dismount in the middle */
  private var isDismounting: Option[PlanetSideGUID] = None

  def CarrierObject: Vehicle

  def endAllCarrierOperations(): Unit = {
    cargoMountTimer.cancel()
    cargoDismountTimer.cancel()
    val obj = CarrierObject
    val zone = obj.Zone
    zone.GUID(isMounting) match {
      case Some(v : Vehicle) => v.Actor ! CargoBehavior.EndCargoMounting(obj.GUID)
      case _ => ;
    }
    isMounting = None
    zone.GUID(isDismounting) match {
      case Some(v : Vehicle) => v.Actor ! CargoBehavior.EndCargoDismounting(obj.GUID)
      case _ => ;
    }
    isDismounting = None
  }

  val carrierBehavior: Receive = {
    case CarrierBehavior.CheckCargoMounting(cargo_guid, mountPoint, iteration) =>
      checkCargoMounting(cargo_guid, mountPoint, iteration)

    case CarrierBehavior.CheckCargoDismount(cargo_guid, mountPoint, iteration, bailed) =>
      checkCargoDismount(cargo_guid, mountPoint, iteration, bailed)
  }

  def checkCargoMounting(cargo_guid: PlanetSideGUID, mountPoint: Int, iteration: Int): Unit = {
    val obj = CarrierObject
    if (
      (isMounting.isEmpty || isMounting.contains(cargo_guid)) && isDismounting.isEmpty &&
      CarrierBehavior.HandleCheckCargoMounting(obj.Zone, obj.GUID, cargo_guid, obj, mountPoint, iteration)
    ) {
      if (iteration == 0) {
        //open the cargo bay door
        obj.Zone.AvatarEvents ! AvatarServiceMessage(
          obj.Zone.id,
          AvatarAction.SendResponse(
            Service.defaultPlayerGUID,
            CargoMountPointStatusMessage(
              obj.GUID,
              PlanetSideGUID(0),
              cargo_guid,
              PlanetSideGUID(0),
              mountPoint,
              CargoStatus.InProgress,
              0
            )
          )
        )
      }
      isMounting = Some(cargo_guid)
      import scala.concurrent.ExecutionContext.Implicits.global
      cargoMountTimer.cancel()
      cargoMountTimer = context.system.scheduler.scheduleOnce(
        250 milliseconds,
        self,
        CarrierBehavior.CheckCargoMounting(cargo_guid, mountPoint, iteration + 1)
      )
    }
    else {
      obj.Zone.GUID(isMounting) match {
        case Some(v: Vehicle) => v.Actor ! CargoBehavior.EndCargoMounting(obj.GUID)
        case _ => ;
      }
      isMounting = None
    }
  }

  def checkCargoDismount(cargo_guid: PlanetSideGUID, mountPoint: Int, iteration: Int, bailed: Boolean): Unit = {
    val obj = CarrierObject
    val zone = obj.Zone
    val guid = obj.GUID
    if ((isDismounting.isEmpty || isDismounting.contains(cargo_guid)) && isMounting.isEmpty) {
      val prolongedDismount = if (iteration == 0) {
        zone.GUID(cargo_guid) match {
          case Some(cargo : Vehicle) =>
            CarrierBehavior.HandleVehicleCargoDismount(
              cargo_guid,
              cargo,
              guid,
              obj,
              bailed,
              requestedByPassenger = false,
              kicked = false
            )
          case _ =>
            obj.CargoHold(mountPoint) match {
              case Some(hold) if hold.isOccupied && hold.occupant.get.GUID == cargo_guid =>
                hold.unmount(hold.occupant.get)
              case _ => ;
            }
            false
        }
      } else {
        CarrierBehavior.HandleCheckCargoDismounting(zone, guid, cargo_guid, obj, mountPoint, iteration, bailed)
      }
      if (prolongedDismount) {
        isDismounting = Some(cargo_guid)
        import scala.concurrent.ExecutionContext.Implicits.global
        cargoDismountTimer.cancel()
        cargoDismountTimer = context.system.scheduler.scheduleOnce(
          250 milliseconds,
          self,
          CarrierBehavior.CheckCargoDismount(cargo_guid, mountPoint, iteration + 1, bailed)
        )
      } else {
        zone.GUID(isDismounting.getOrElse(cargo_guid)) match {
          case Some(cargo: Vehicle) =>
            cargo.Actor ! CargoBehavior.EndCargoDismounting(guid)
          case _ => ;
        }
        isDismounting = None
      }
    } else {
      zone.GUID(isDismounting.getOrElse(cargo_guid)) match {
        case Some(cargo: Vehicle) => cargo.Actor ! CargoBehavior.EndCargoDismounting(guid)
        case _ => ;
      }
      isDismounting = None
    }
  }
}

object CarrierBehavior {
  private val log = org.log4s.getLogger(name = "CarrierBehavior")

  final case class CheckCargoMounting(cargo_guid: PlanetSideGUID, cargo_mountpoint: Int, iteration: Int)
  final case class CheckCargoDismount(cargo_guid: PlanetSideGUID, cargo_mountpoint: Int, iteration: Int, bailed: Boolean)

  /**
    * na
    * @param carrierGUID the ferrying carrier vehicle
    * @param cargoGUID the vehicle being ferried as cargo
    * @param carrier the ferrying carrier vehicle
    * @param mountPoint the cargo hold to which the cargo vehicle is stowed
    * @param iteration number of times a proper mounting for this combination has been queried
    */
  def HandleCheckCargoMounting(
                                zone: Zone,
                                carrierGUID: PlanetSideGUID,
                                cargoGUID: PlanetSideGUID,
                                carrier: Vehicle,
                                mountPoint: Int,
                                iteration: Int
                              ): Boolean = {
    zone.GUID(cargoGUID) match {
      case Some(cargo: Vehicle) =>
        HandleCheckCargoMounting(cargoGUID, cargo, carrierGUID, carrier, mountPoint, iteration)
      case cargo if iteration > 0 =>
        log.warn(s"HandleCheckCargoMounting: participant vehicles changed in the middle of a mounting event")
        LogCargoEventMissingVehicleError("HandleCheckCargoMounting: cargo", cargo, cargoGUID)
        false
      case _ =>
        false
    }
  }

  /**
    * na
    * @param cargoGUID the vehicle being ferried as cargo
    * @param cargo the vehicle being ferried as cargo
    * @param carrierGUID the ferrying carrier vehicle
    * @param carrier the ferrying carrier vehicle
    * @param mountPoint the cargo hold to which the cargo vehicle is stowed
    * @param iteration number of times a proper mounting for this combination has been queried
    */
  private def HandleCheckCargoMounting(
                                        cargoGUID: PlanetSideGUID,
                                        cargo: Vehicle,
                                        carrierGUID: PlanetSideGUID,
                                        carrier: Vehicle,
                                        mountPoint: Int,
                                        iteration: Int
                                      ): Boolean = {
    val zone     = carrier.Zone
    val distance = Vector3.DistanceSquared(cargo.Position, carrier.Position)
    carrier.CargoHold(mountPoint) match {
      case Some(hold) if !hold.isOccupied && hold.canBeOccupiedBy(cargo) =>
        log.debug(
          s"HandleCheckCargoMounting: mount distance between $cargoGUID and $carrierGUID - actual=$distance, target=64"
        )
        if (distance <= 64) {
          //cargo vehicle is close enough to assume to be physically within the carrier's hold; mount it
          log.debug(s"HandleCheckCargoMounting: mounting cargo vehicle in carrier at distance of $distance")
          hold.mount(cargo)
          cargo.MountedIn = carrierGUID
          cargo.Velocity = None
          cargo.Actor ! CargoBehavior.EndCargoMounting(carrierGUID)
          zone.VehicleEvents ! VehicleServiceMessage(
            s"${cargo.Actor}",
            VehicleAction.SendResponse(PlanetSideGUID(0), PlanetsideAttributeMessage(cargoGUID, 0, cargo.Health))
          )
          zone.VehicleEvents ! VehicleServiceMessage(
            s"${cargo.Actor}",
            VehicleAction.SendResponse(PlanetSideGUID(0), PlanetsideAttributeMessage(cargoGUID, cargo.Definition.shieldUiAttribute, cargo.Shields))
          )
          CargoMountBehaviorForAll(carrier, cargo, mountPoint)
          zone.actor ! ZoneActor.RemoveFromBlockMap(cargo)
          false
        } else if (distance > 625 || iteration >= 40) {
          //vehicles moved too far away or took too long to get into proper position; abort mounting
          log.debug(
            "HandleCheckCargoMounting: cargo vehicle is too far away or didn't mount within allocated time - aborting"
          )
          cargo.Actor ! CargoBehavior.EndCargoMounting(carrierGUID)
          val cargoDriverGUID = cargo.Seats(0).occupant.get.GUID
          zone.VehicleEvents ! VehicleServiceMessage(
            zone.id,
            VehicleAction.SendResponse(
              cargoDriverGUID,
              CargoMountPointStatusMessage(
                carrierGUID,
                PlanetSideGUID(0),
                PlanetSideGUID(0),
                cargoGUID,
                mountPoint,
                CargoStatus.Empty,
                0
              )
            )
          )
          false
          //sending packet to the cargo vehicle's client results in player being lock in own vehicle
          //player gets stuck as "always trying to remount the cargo hold"
          //obviously, don't do this
        } else {
          //cargo vehicle still not in position but there is more time to wait; reschedule check
          true
        }
      case None =>
        log.warn(s"HandleCheckCargoMounting: carrier vehicle $carrier does not have a cargo hold #$mountPoint")
        cargo.Actor ! CargoBehavior.EndCargoMounting(carrierGUID)
        false
      case _ =>
        if (iteration == 0) {
          log.warn(
            s"HandleCheckCargoMounting: carrier vehicle $carrier already possesses cargo in hold #$mountPoint; this operation was initiated incorrectly"
          )
        } else {
          log.error(
            s"HandleCheckCargoMounting: something has attached to the carrier vehicle $carrier cargo of hold #$mountPoint while a cargo dismount event was ongoing; stopped at iteration $iteration / 40"
          )
        }
        cargo.Actor ! CargoBehavior.EndCargoMounting(carrierGUID)
        false
    }
  }

  /**
    * na
    * @param cargoGUID  na
    * @param carrierGUID na
    * @param mountPoint na
    * @param iteration na
    */
  def HandleCheckCargoDismounting(
                                   zone: Zone,
                                   carrierGUID: PlanetSideGUID,
                                   cargoGUID: PlanetSideGUID,
                                   carrier: Vehicle,
                                   mountPoint: Int,
                                   iteration: Int,
                                   bailed: Boolean
                                 ): Boolean = {
    zone.GUID(cargoGUID) match {
      case Some(cargo: Vehicle) =>
        HandleCheckCargoDismounting(cargoGUID, cargo, carrierGUID, carrier, mountPoint, iteration, bailed)
      case cargo if iteration > 0 =>
        log.error(s"HandleCheckCargoDismounting: participant vehicles changed in the middle of a mounting event")
        LogCargoEventMissingVehicleError("HandleCheckCargoDismounting: carrier", cargo, cargoGUID)
        false
      case _ =>
        false
    }
  }

  /**
    * na
    * @param cargoGUID na
    * @param cargo na
    * @param carrierGUID na
    * @param carrier na
    * @param mountPoint na
    * @param iteration na
    */
  private def HandleCheckCargoDismounting(
                                           cargoGUID: PlanetSideGUID,
                                           cargo: Vehicle,
                                           carrierGUID: PlanetSideGUID,
                                           carrier: Vehicle,
                                           mountPoint: Int,
                                           iteration: Int,
                                           bailed: Boolean
                                         ): Boolean = {
    val zone = carrier.Zone
    carrier.CargoHold(mountPoint) match {
      case Some(hold) =>
        val distance = Vector3.DistanceSquared(cargo.Position, carrier.Position)
        log.debug(
          s"HandleCheckCargoDismounting: mount distance between $cargoGUID and $carrierGUID - actual=$distance, target=225"
        )
        if ((bailed && iteration > 0) || distance > 225) {
          //cargo vehicle has moved far enough away; close the carrier's hold door
          log.debug(
            s"HandleCheckCargoDismounting: dismount of cargo vehicle from carrier complete at distance of $distance"
          )
          cargo.Actor ! CargoBehavior.EndCargoDismounting(carrierGUID)
          val cargoDriverGUID = cargo.Seats(0).occupant.get.GUID
          zone.VehicleEvents ! VehicleServiceMessage(
            zone.id,
            VehicleAction.SendResponse(
              cargoDriverGUID,
              CargoMountPointStatusMessage(
                carrierGUID,
                PlanetSideGUID(0),
                PlanetSideGUID(0),
                cargoGUID,
                mountPoint,
                CargoStatus.Empty,
                0
              )
            )
          )
          false
          //sending packet to the cargo vehicle's client results in player being lock in own vehicle
          //player gets stuck as "always trying to remount the cargo hold"
          //obviously, don't do this
        } else if (iteration > 40) {
          //cargo vehicle has spent too long not getting far enough away; restore the cargo's mount in the carrier hold
          hold.mount(cargo)
          cargo.MountedIn = carrierGUID
          cargo.Actor ! CargoBehavior.EndCargoMounting(carrierGUID)
          CargoMountBehaviorForAll(carrier, cargo, mountPoint)
          zone.actor ! ZoneActor.RemoveFromBlockMap(cargo)
          false
        } else {
          //cargo vehicle did not move far away enough yet and there is more time to wait; reschedule check
          true
        }
      case None =>
        log.warn(s"HandleCheckCargoDismounting: carrier vehicle $carrier does not have a cargo hold #$mountPoint")
        cargo.Actor ! CargoBehavior.EndCargoDismounting(carrierGUID)
        false
      case _ =>
        if (iteration == 0) {
          log.warn(
            s"HandleCheckCargoDismounting: carrier vehicle $carrier will not discharge the cargo of hold #$mountPoint; this operation was initiated incorrectly"
          )
        } else {
          log.error(
            s"HandleCheckCargoDismounting: something has attached to the carrier vehicle $carrier cargo of hold #$mountPoint while a cargo dismount event was ongoing; stopped at iteration $iteration / 40"
          )
        }
        cargo.Actor ! CargoBehavior.EndCargoDismounting(carrierGUID)
        false
    }
  }

  /**
    * na
    * @param zone na
    * @param cargo_guid na
    * @param bailed na
    * @param requestedByPassenger na
    * @param kicked na
    */
  def HandleVehicleCargoDismount(
                                  zone: Zone,
                                  cargo_guid: PlanetSideGUID,
                                  bailed: Boolean,
                                  requestedByPassenger: Boolean,
                                  kicked: Boolean
                                ): Boolean = {
    zone.GUID(cargo_guid) match {
      case Some(cargo: Vehicle) =>
        zone.GUID(cargo.MountedIn) match {
          case Some(ferry: Vehicle) =>
            HandleVehicleCargoDismount(cargo_guid, cargo, ferry.GUID, ferry, bailed, requestedByPassenger, kicked)
          case _ =>
            log.warn(
              s"DismountVehicleCargo: target ${cargo.Definition.Name}@$cargo_guid does not know what treats it as cargo"
            )
            false
        }
      case _ =>
        log.warn(s"DismountVehicleCargo: target $cargo_guid either is not a vehicle in ${zone.id} or does not exist")
        false
    }
  }

  /**
    * na
    * @param cargoGUID the globally unique number for the vehicle being ferried
    * @param cargo the vehicle being ferried
    * @param carrierGUID the globally unique number for the vehicle doing the ferrying
    * @param carrier the vehicle doing the ferrying
    * @param bailed the ferried vehicle is bailing from the cargo hold
    * @param requestedByPassenger the ferried vehicle is being politely disembarked from the cargo hold
    * @param kicked the ferried vehicle is being kicked out of the cargo hold
    */
  def HandleVehicleCargoDismount(
                                  cargoGUID: PlanetSideGUID,
                                  cargo: Vehicle,
                                  carrierGUID: PlanetSideGUID,
                                  carrier: Vehicle,
                                  bailed: Boolean,
                                  requestedByPassenger: Boolean,
                                  kicked: Boolean
                                ): Boolean = {
    val zone = carrier.Zone
    carrier.CargoHolds.find({ case (_, hold) => hold.occupant.contains(cargo) }) match {
      case Some((mountPoint, hold)) =>
        cargo.MountedIn = None
        hold.unmount(
          cargo,
          if (bailed) BailType.Bailed else if (kicked) BailType.Kicked else BailType.Normal
        )
        val driverOpt = cargo.Seats(0).occupant
        val rotation: Vector3 = if (Vehicles.CargoOrientation(cargo) == 1) { //TODO: BFRs will likely also need this set
          //dismount router "sideways" from the lodestar
          carrier.Orientation.xy + Vector3.z((carrier.Orientation.z - 90) % 360)
        } else {
          carrier.Orientation
        }
        val cargoHoldPosition: Vector3 = if (carrier.Definition == GlobalDefinitions.dropship) {
          //the galaxy cargo bay is offset backwards from the center of the vehicle
          carrier.Position + Vector3.Rz(Vector3(0, -7, 0), math.toRadians(carrier.Orientation.z))
        } else {
          //the lodestar's cargo hold is almost the center of the vehicle
          carrier.Position
        }
        val GUID0      = Service.defaultPlayerGUID
        val zoneId     = zone.id
        val events     = zone.VehicleEvents
        val cargoActor = cargo.Actor
        events ! VehicleServiceMessage(
          s"$cargoActor",
          VehicleAction.SendResponse(GUID0, PlanetsideAttributeMessage(cargoGUID, 0, cargo.Health))
        )
        events ! VehicleServiceMessage(
          s"$cargoActor",
          VehicleAction.SendResponse(GUID0, PlanetsideAttributeMessage(cargoGUID, cargo.Definition.shieldUiAttribute, cargo.Shields))
        )
        zone.actor ! ZoneActor.AddToBlockMap(cargo, carrier.Position)
        if (carrier.isFlying) {
          //the carrier vehicle is flying; eject the cargo vehicle
          val ejectCargoMsg =
            CargoMountPointStatusMessage(carrierGUID, GUID0, GUID0, cargoGUID, mountPoint, CargoStatus.InProgress, 0)
          val detachCargoMsg = ObjectDetachMessage(carrierGUID, cargoGUID, cargoHoldPosition - Vector3.z(1), rotation)
          val resetCargoMsg =
            CargoMountPointStatusMessage(carrierGUID, GUID0, GUID0, cargoGUID, mountPoint, CargoStatus.Empty, 0)
          events ! VehicleServiceMessage(zoneId, VehicleAction.SendResponse(GUID0, ejectCargoMsg))
          events ! VehicleServiceMessage(zoneId, VehicleAction.SendResponse(GUID0, detachCargoMsg))
          events ! VehicleServiceMessage(zoneId, VehicleAction.SendResponse(GUID0, resetCargoMsg))
          log.debug(s"HandleVehicleCargoDismount: eject - $ejectCargoMsg, detach - $detachCargoMsg")
          if (driverOpt.isEmpty) {
            //TODO cargo should drop like a rock like normal; until then, deconstruct it
            cargoActor ! Vehicle.Deconstruct()
          }
          false
        } else {
          //the carrier vehicle is not flying; just open the door and let the cargo vehicle back out; force it out if necessary
          val cargoStatusMessage =
            CargoMountPointStatusMessage(carrierGUID, GUID0, cargoGUID, GUID0, mountPoint, CargoStatus.InProgress, 0)
          val cargoDetachMessage =
            ObjectDetachMessage(carrierGUID, cargoGUID, cargoHoldPosition + Vector3.z(1f), rotation)
          events ! VehicleServiceMessage(zoneId, VehicleAction.SendResponse(GUID0, cargoStatusMessage))
          events ! VehicleServiceMessage(zoneId, VehicleAction.SendResponse(GUID0, cargoDetachMessage))
          driverOpt match {
            case Some(driver) =>
              events ! VehicleServiceMessage(
                s"${driver.Name}",
                VehicleAction.KickCargo(GUID0, cargo, cargo.Definition.AutoPilotSpeed2, 2500)
              )
            case None =>
              val resetCargoMsg =
                CargoMountPointStatusMessage(carrierGUID, GUID0, GUID0, cargoGUID, mountPoint, CargoStatus.Empty, 0)
              events ! VehicleServiceMessage(zoneId, VehicleAction.SendResponse(GUID0, resetCargoMsg)) //lazy
              //TODO cargo should back out like normal; until then, deconstruct it
              cargoActor ! Vehicle.Deconstruct()
          }
          true
        }

      case None =>
        log.warn(s"HandleDismountVehicleCargo: can not locate cargo $cargo in any hold of the carrier vehicle $carrier")
        false
    }
  }

  //logging and messaging support functions
  /**
    * na
    * @param decorator custom text for these messages in the log
    * @param target an optional the target object
    * @param targetGUID the expected globally unique identifier of the target object
    */
  def LogCargoEventMissingVehicleError(
                                        decorator: String,
                                        target: Option[PlanetSideGameObject],
                                        targetGUID: PlanetSideGUID
                                      ): Unit = {
    target match {
      case Some(_: Vehicle) => ;
      case Some(_)          => log.error(s"$decorator target $targetGUID no longer identifies as a vehicle")
      case None             => log.error(s"$decorator target $targetGUID has gone missing")
    }
  }

  /**
    * Produce an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet
    * that will set up a realized parent-child association between a ferrying vehicle and a ferried vehicle.
    * @see `CargoMountPointStatusMessage`
    * @see `ObjectAttachMessage`
    * @see `Vehicles.CargoOrientation`
    * @param carrier the ferrying vehicle
    * @param cargo the ferried vehicle
    * @param mountPoint the point on the ferryoing vehicle where the ferried vehicle is attached;
    *                   also known as a "cargo hold"
    * @return a tuple composed of an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet
    */
  def CargoMountMessages(
                          carrier: Vehicle,
                          cargo: Vehicle,
                          mountPoint: Int
                        ): (ObjectAttachMessage, CargoMountPointStatusMessage) = {
    CargoMountMessages(carrier.GUID, cargo.GUID, mountPoint, Vehicles.CargoOrientation(cargo))
  }

  /**
    * Produce an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet
    * that will set up a realized parent-child association between a ferrying vehicle and a ferried vehicle.
    * @see `CargoMountPointStatusMessage`
    * @see `ObjectAttachMessage`
    * @param carrierGUID the ferrying vehicle
    * @param cargoGUID the ferried vehicle
    * @param mountPoint the point on the ferryoing vehicle where the ferried vehicle is attached
    * @param orientation the positioning of the cargo vehicle in the carrier cargo bay
    * @return a tuple composed of an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet
    */
  def CargoMountMessages(
                          carrierGUID: PlanetSideGUID,
                          cargoGUID: PlanetSideGUID,
                          mountPoint: Int,
                          orientation: Int
                        ): (ObjectAttachMessage, CargoMountPointStatusMessage) = {
    (
      ObjectAttachMessage(carrierGUID, cargoGUID, mountPoint),
      CargoMountPointStatusMessage(
        carrierGUID,
        cargoGUID,
        cargoGUID,
        PlanetSideGUID(0),
        mountPoint,
        CargoStatus.Occupied,
        orientation
      )
    )
  }

  /**
    * Dispatch an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet to all other clients, not this one.
    * @see `CargoMountPointStatusMessage`
    * @see `ObjectAttachMessage`
    * @param carrier the ferrying vehicle
    * @param cargo the ferried vehicle
    * @param mountPoint the point on the ferryoing vehicle where the ferried vehicle is attached
    * @return a tuple composed of an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet
    */
  def CargoMountBehaviorForOthers(
                                   carrier: Vehicle,
                                   cargo: Vehicle,
                                   mountPoint: Int,
                                   exclude: PlanetSideGUID
                                 ): (ObjectAttachMessage, CargoMountPointStatusMessage) = {
    val msgs @ (attachMessage, mountPointStatusMessage) = CargoMountMessages(carrier, cargo, mountPoint)
    CargoMountMessagesForOthers(carrier.Zone, exclude, attachMessage, mountPointStatusMessage)
    msgs
  }

  /**
    * Dispatch an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet to all other clients, not this one.
    * @see `CargoMountPointStatusMessage`
    * @see `ObjectAttachMessage`
    * @param attachMessage an `ObjectAttachMessage` packet suitable for initializing cargo operations
    * @param mountPointStatusMessage a `CargoMountPointStatusMessage` packet suitable for initializing cargo operations
    */
  def CargoMountMessagesForOthers(
                                   zone: Zone,
                                   exclude: PlanetSideGUID,
                                   attachMessage: ObjectAttachMessage,
                                   mountPointStatusMessage: CargoMountPointStatusMessage
                                 ): Unit = {
    zone.VehicleEvents ! VehicleServiceMessage(zone.id, VehicleAction.SendResponse(exclude, attachMessage))
    zone.VehicleEvents ! VehicleServiceMessage(zone.id, VehicleAction.SendResponse(exclude, mountPointStatusMessage))
  }

  /**
    * Dispatch an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet to everyone.
    * @see `CargoMountPointStatusMessage`
    * @see `ObjectAttachMessage`
    * @param carrier the ferrying vehicle
    * @param cargo the ferried vehicle
    * @param mountPoint the point on the ferryoing vehicle where the ferried vehicle is attached
    * @return a tuple composed of an `ObjectAttachMessage` packet and a `CargoMountPointStatusMessage` packet
    */
  def CargoMountBehaviorForAll(
                                carrier: Vehicle,
                                cargo: Vehicle,
                                mountPoint: Int
                              ): (ObjectAttachMessage, CargoMountPointStatusMessage) = {
    val zone                                            = carrier.Zone
    val zoneId                                          = zone.id
    val msgs @ (attachMessage, mountPointStatusMessage) = CargoMountMessages(carrier, cargo, mountPoint)
    zone.VehicleEvents ! VehicleServiceMessage(
      zoneId,
      VehicleAction.SendResponse(Service.defaultPlayerGUID, attachMessage)
    )
    zone.VehicleEvents ! VehicleServiceMessage(
      zoneId,
      VehicleAction.SendResponse(Service.defaultPlayerGUID, mountPointStatusMessage)
    )
    msgs
  }
}

