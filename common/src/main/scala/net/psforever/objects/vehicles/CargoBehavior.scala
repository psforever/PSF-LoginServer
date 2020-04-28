// Copyright (c) 2020 PSForever
package net.psforever.objects.vehicles

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.zones.Zone
import net.psforever.objects._
import net.psforever.objects.vehicles.CargoBehavior.{CheckCargoDismount, CheckCargoMounting}
import net.psforever.packet.game.{CargoMountPointStatusMessage, ObjectAttachMessage, ObjectDetachMessage, PlanetsideAttributeMessage}
import net.psforever.types.{CargoStatus, PlanetSideGUID, Vector3}
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.Service
import services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.duration._

trait CargoBehavior {
  _ : Actor =>
  private var cargoMountTimer : Cancellable = DefaultCancellable.obj
  private var cargoDismountTimer : Cancellable = DefaultCancellable.obj

  /* gate-keep mounting behavior so that unit does not try to dismount as cargo, or mount different vehicle */
  private var isMounting : Option[PlanetSideGUID] = None
  /* gate-keep dismounting behavior so that unit does not try to mount as cargo, or dismount from different vehicle */
  private var isDismounting : Option[PlanetSideGUID] = None

  def CargoObject : Vehicle

  val cargoBehavior : Receive = {
    case CheckCargoMounting(carrier_guid, mountPoint, iteration) =>
      val obj = CargoObject
      if((isMounting.isEmpty || isMounting.contains(carrier_guid)) && isDismounting.isEmpty &&
        CargoBehavior.HandleCheckCargoMounting(obj.Zone, carrier_guid, obj.GUID, obj, mountPoint, iteration)) {
        if(iteration == 0) {
          //open the cargo bay door
          obj.Zone.AvatarEvents ! AvatarServiceMessage(
            obj.Zone.Id,
            AvatarAction.SendResponse(
              Service.defaultPlayerGUID,
              CargoMountPointStatusMessage(carrier_guid, PlanetSideGUID(0), obj.GUID, PlanetSideGUID(0), mountPoint, CargoStatus.InProgress, 0)
            )
          )
        }
        isMounting = Some(carrier_guid)
        import scala.concurrent.ExecutionContext.Implicits.global
        cargoMountTimer.cancel
        cargoMountTimer = context.system.scheduler.scheduleOnce(250 milliseconds, self, CheckCargoMounting(carrier_guid, mountPoint, iteration + 1))
      }
      else {
        isMounting = None
      }

    case CheckCargoDismount(carrier_guid, mountPoint, iteration) =>
      val obj = CargoObject
      if((isDismounting.isEmpty || isDismounting.contains(carrier_guid)) && isMounting.isEmpty &&
        CargoBehavior.HandleCheckCargoDismounting(obj.Zone, carrier_guid, obj.GUID, obj, mountPoint, iteration)) {
        isDismounting = Some(carrier_guid)
        import scala.concurrent.ExecutionContext.Implicits.global
        cargoDismountTimer.cancel
        cargoDismountTimer = context.system.scheduler.scheduleOnce(250 milliseconds, self, CheckCargoDismount(carrier_guid, mountPoint, iteration + 1))
      }
      else {
        isDismounting = None
      }
  }
}

object CargoBehavior {
  private val log = org.log4s.getLogger("CargoBehavior")

  final case class CheckCargoMounting(carrier_guid: PlanetSideGUID, cargo_mountpoint: Int, iteration: Int)
  final case class CheckCargoDismount(carrier_guid: PlanetSideGUID, cargo_mountpoint: Int, iteration: Int)

  /**
    * na
    * @param carrierGUID the ferrying carrier vehicle
    * @param cargoGUID the vehicle being ferried as cargo
    * @param cargo the vehicle being ferried as cargo
    * @param mountPoint the cargo hold to which the cargo vehicle is stowed
    * @param iteration number of times a proper mounting for this combination has been queried
    */
  def HandleCheckCargoMounting(zone : Zone, carrierGUID : PlanetSideGUID, cargoGUID : PlanetSideGUID, cargo : Vehicle, mountPoint : Int, iteration : Int) : Boolean = {
    zone.GUID(carrierGUID) match {
      case Some(carrier : Vehicle) =>
        HandleCheckCargoMounting(cargoGUID, cargo, carrierGUID, carrier, mountPoint, iteration)
      case carrier if iteration > 0 =>
        log.error(s"HandleCheckCargoMounting: participant vehicles changed in the middle of a mounting event")
        LogCargoEventMissingVehicleError("HandleCheckCargoMounting: carrier", carrier, carrierGUID)
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
  private def HandleCheckCargoMounting(cargoGUID : PlanetSideGUID, cargo : Vehicle, carrierGUID : PlanetSideGUID, carrier : Vehicle, mountPoint : Int, iteration : Int) : Boolean = {
    val zone = carrier.Zone
    val distance = Vector3.DistanceSquared(cargo.Position, carrier.Position)
    carrier.CargoHold(mountPoint) match {
      case Some(hold) if !hold.isOccupied =>
        log.debug(s"HandleCheckCargoMounting: mount distance between $cargoGUID and $carrierGUID - actual=$distance, target=64")
        if(distance <= 64) {
          //cargo vehicle is close enough to assume to be physically within the carrier's hold; mount it
          log.info(s"HandleCheckCargoMounting: mounting cargo vehicle in carrier at distance of $distance")
          cargo.MountedIn = carrierGUID
          hold.Occupant = cargo
          cargo.Velocity = None
          zone.VehicleEvents ! VehicleServiceMessage(s"${cargo.Actor}", VehicleAction.SendResponse(PlanetSideGUID(0), PlanetsideAttributeMessage(cargoGUID, 0, cargo.Health)))
          zone.VehicleEvents ! VehicleServiceMessage(s"${cargo.Actor}", VehicleAction.SendResponse(PlanetSideGUID(0), PlanetsideAttributeMessage(cargoGUID, 68, cargo.Shields)))
          val (attachMsg, mountPointMsg) = CargoMountBehaviorForAll(carrier, cargo, mountPoint)
          log.info(s"HandleCheckCargoMounting: $attachMsg")
          log.info(s"HandleCheckCargoMounting: $mountPointMsg")
          false
        }
        else if(distance > 625 || iteration >= 40) {
          //vehicles moved too far away or took too long to get into proper position; abort mounting
          log.info("HandleCheckCargoMounting: cargo vehicle is too far away or didn't mount within allocated time - aborting")
          val cargoDriverGUID = cargo.Seats(0).Occupant.get.GUID
          zone.VehicleEvents ! VehicleServiceMessage(
            zone.Id,
            VehicleAction.SendResponse(
              cargoDriverGUID,
              CargoMountPointStatusMessage(carrierGUID, PlanetSideGUID(0), PlanetSideGUID(0), cargoGUID, mountPoint, CargoStatus.Empty, 0)
            )
          )
          false
          //sending packet to the cargo vehicle's client results in player locking himself in his vehicle
          //player gets stuck as "always trying to remount the cargo hold"
          //obviously, don't do this
        }
        else {
          //cargo vehicle still not in position but there is more time to wait; reschedule check
          true
        }
      case None => ;
        log.warn(s"HandleCheckCargoMounting: carrier vehicle $carrier does not have a cargo hold #$mountPoint")
        false
      case _ =>
        if(iteration == 0) {
          log.warn(s"HandleCheckCargoMounting: carrier vehicle $carrier already possesses cargo in hold #$mountPoint; this operation was initiated incorrectly")
        }
        else {
          log.error(s"HandleCheckCargoMounting: something has attached to the carrier vehicle $carrier cargo of hold #$mountPoint while a cargo dismount event was ongoing; stopped at iteration $iteration / 40")
        }
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
  def HandleCheckCargoDismounting(zone : Zone, carrierGUID : PlanetSideGUID, cargoGUID : PlanetSideGUID, cargo : Vehicle, mountPoint : Int, iteration : Int) : Boolean = {
    zone.GUID(carrierGUID) match {
      case Some(carrier : Vehicle) =>
        HandleCheckCargoDismounting(cargoGUID, cargo, carrierGUID, carrier, mountPoint, iteration)
      case carrier if iteration > 0 =>
        log.error(s"HandleCheckCargoDismounting: participant vehicles changed in the middle of a mounting event")
        LogCargoEventMissingVehicleError("HandleCheckCargoDismounting: carrier", carrier, carrierGUID)
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
  private def HandleCheckCargoDismounting(cargoGUID : PlanetSideGUID, cargo : Vehicle, carrierGUID : PlanetSideGUID, carrier : Vehicle, mountPoint : Int, iteration : Int) : Boolean = {
    val zone = carrier.Zone
    carrier.CargoHold(mountPoint) match {
      case Some(hold) if !hold.isOccupied =>
        val distance = Vector3.DistanceSquared(cargo.Position, carrier.Position)
        log.debug(s"HandleCheckCargoDismounting: mount distance between $cargoGUID and $carrierGUID - actual=$distance, target=225")
        if(distance > 225) {
          //cargo vehicle has moved far enough away; close the carrier's hold door
          log.info(s"HandleCheckCargoDismounting: dismount of cargo vehicle from carrier complete at distance of $distance")
          val cargoDriverGUID = cargo.Seats(0).Occupant.get.GUID
          zone.VehicleEvents ! VehicleServiceMessage(
            zone.Id,
            VehicleAction.SendResponse(
              cargoDriverGUID,
              CargoMountPointStatusMessage(carrierGUID, PlanetSideGUID(0), PlanetSideGUID(0), cargoGUID, mountPoint, CargoStatus.Empty, 0)
            )
          )
          false
          //sending packet to the cargo vehicle's client results in player locking himself in his vehicle
          //player gets stuck as "always trying to remount the cargo hold"
          //obviously, don't do this
        }
        else if(iteration > 40) {
          //cargo vehicle has spent too long not getting far enough away; restore the cargo's mount in the carrier hold
          cargo.MountedIn = carrierGUID
          hold.Occupant = cargo
          CargoMountBehaviorForAll(carrier, cargo, mountPoint)
          false
        }
        else {
          //cargo vehicle did not move far away enough yet and there is more time to wait; reschedule check
          true
        }
      case None =>
        log.warn(s"HandleCheckCargoDismounting: carrier vehicle $carrier does not have a cargo hold #$mountPoint")
        false
      case _ =>
        if(iteration == 0) {
          log.warn(s"HandleCheckCargoDismounting: carrier vehicle $carrier will not discharge the cargo of hold #$mountPoint; this operation was initiated incorrectly")
        }
        else {
          log.error(s"HandleCheckCargoDismounting: something has attached to the carrier vehicle $carrier cargo of hold #$mountPoint while a cargo dismount event was ongoing; stopped at iteration $iteration / 40")
        }
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
  def HandleVehicleCargoDismount(zone : Zone, cargo_guid : PlanetSideGUID, bailed : Boolean, requestedByPassenger : Boolean, kicked : Boolean) : Unit = {
    zone.GUID(cargo_guid) match {
      case Some(cargo : Vehicle) =>
        zone.GUID(cargo.MountedIn) match {
          case Some(ferry : Vehicle) =>
            HandleVehicleCargoDismount(cargo_guid, cargo, ferry.GUID, ferry, bailed, requestedByPassenger, kicked)
          case _ =>
            log.warn(s"DismountVehicleCargo: target ${cargo.Definition.Name}@$cargo_guid does not know what treats it as cargo")
        }
      case _ =>
        log.warn(s"DismountVehicleCargo: target $cargo_guid either is not a vehicle in ${zone.Id} or does not exist")
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
  def HandleVehicleCargoDismount(cargoGUID : PlanetSideGUID, cargo : Vehicle, carrierGUID : PlanetSideGUID, carrier : Vehicle, bailed : Boolean, requestedByPassenger : Boolean, kicked : Boolean) : Unit = {
    val zone = carrier.Zone
    carrier.CargoHolds.find({case(_, hold) => hold.Occupant.contains(cargo)}) match {
      case Some((mountPoint, hold)) =>
        cargo.MountedIn = None
        hold.Occupant = None
        val driverOpt = cargo.Seats(0).Occupant
        val rotation : Vector3 = if(Vehicles.CargoOrientation(cargo) == 1) { //TODO: BFRs will likely also need this set
          //dismount router "sideways" in a lodestar
          carrier.Orientation.xy + Vector3.z((carrier.Orientation.z - 90) % 360)
        }
        else {
          carrier.Orientation
        }
        val cargoHoldPosition : Vector3 = if(carrier.Definition == GlobalDefinitions.dropship) {
          //the galaxy cargo bay is offset backwards from the center of the vehicle
          carrier.Position + Vector3.Rz(Vector3(0, 7, 0), math.toRadians(carrier.Orientation.z))
        }
        else {
          //the lodestar's cargo hold is almost the center of the vehicle
          carrier.Position
        }
        val GUID0 = Service.defaultPlayerGUID
        val zoneId = zone.Id
        val events = zone.VehicleEvents
        val cargoActor = cargo.Actor
        events ! VehicleServiceMessage(s"$cargoActor", VehicleAction.SendResponse(GUID0, PlanetsideAttributeMessage(cargoGUID, 0, cargo.Health)))
        events ! VehicleServiceMessage(s"$cargoActor", VehicleAction.SendResponse(GUID0, PlanetsideAttributeMessage(cargoGUID, 68, cargo.Shields)))
        if(carrier.Flying) {
          //the carrier vehicle is flying; eject the cargo vehicle
          val ejectCargoMsg = CargoMountPointStatusMessage(carrierGUID, GUID0, GUID0, cargoGUID, mountPoint, CargoStatus.InProgress, 0)
          val detachCargoMsg = ObjectDetachMessage(carrierGUID, cargoGUID, cargoHoldPosition - Vector3.z(1), rotation)
          val resetCargoMsg = CargoMountPointStatusMessage(carrierGUID, GUID0, GUID0, cargoGUID, mountPoint, CargoStatus.Empty, 0)
          events ! VehicleServiceMessage(zoneId, VehicleAction.SendResponse(GUID0, ejectCargoMsg))
          events ! VehicleServiceMessage(zoneId, VehicleAction.SendResponse(GUID0, detachCargoMsg))
          events ! VehicleServiceMessage(zoneId, VehicleAction.SendResponse(GUID0, resetCargoMsg))
          log.debug(ejectCargoMsg.toString)
          log.debug(detachCargoMsg.toString)
          if(driverOpt.isEmpty) {
            //TODO cargo should drop like a rock like normal; until then, deconstruct it
            cargo.Actor ! Vehicle.Deconstruct()
          }
        }
        else {
          //the carrier vehicle is not flying; just open the door and let the cargo vehicle back out; force it out if necessary
          val cargoStatusMessage = CargoMountPointStatusMessage(carrierGUID, GUID0, cargoGUID, GUID0, mountPoint, CargoStatus.InProgress, 0)
          val cargoDetachMessage = ObjectDetachMessage(carrierGUID, cargoGUID, cargoHoldPosition + Vector3.z(1f), rotation)
          events ! VehicleServiceMessage(zoneId, VehicleAction.SendResponse(GUID0, cargoStatusMessage))
          events ! VehicleServiceMessage(zoneId, VehicleAction.SendResponse(GUID0, cargoDetachMessage))
          driverOpt match {
            case Some(driver) =>
              events ! VehicleServiceMessage(s"${driver.Name}", VehicleAction.KickCargo(GUID0, cargo, cargo.Definition.AutoPilotSpeed2, 2500))
              //check every quarter second if the vehicle has moved far enough away to be considered dismounted
              cargoActor ! CheckCargoDismount(carrierGUID, mountPoint, 0)
            case None =>
              val resetCargoMsg = CargoMountPointStatusMessage(carrierGUID, GUID0, GUID0, cargoGUID, mountPoint, CargoStatus.Empty, 0)
              events ! VehicleServiceMessage(zoneId, VehicleAction.SendResponse(GUID0, resetCargoMsg)) //lazy
              //TODO cargo should back out like normal; until then, deconstruct it
              cargoActor ! Vehicle.Deconstruct()
          }
        }

      case None =>
        log.warn(s"HandleDismountVehicleCargo: can not locate cargo $cargo in any hold of the carrier vehicle $carrier")
    }
  }

  //logging and messaging support functions
  /**
    * na
    * @param decorator custom text for these messages in the log
    * @param target an optional the target object
    * @param targetGUID the expected globally unique identifier of the target object
    */
  def LogCargoEventMissingVehicleError(decorator : String, target : Option[PlanetSideGameObject], targetGUID : PlanetSideGUID) : Unit = {
    target match {
      case Some(_ : Vehicle) => ;
      case Some(_) => log.error(s"$decorator target $targetGUID no longer identifies as a vehicle")
      case None => log.error(s"$decorator target $targetGUID has gone missing")
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
  def CargoMountMessages(carrier : Vehicle, cargo : Vehicle, mountPoint : Int) : (ObjectAttachMessage, CargoMountPointStatusMessage) = {
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
  def CargoMountMessages(carrierGUID : PlanetSideGUID, cargoGUID : PlanetSideGUID, mountPoint : Int, orientation : Int) : (ObjectAttachMessage, CargoMountPointStatusMessage) = {
    (
      ObjectAttachMessage(carrierGUID, cargoGUID, mountPoint),
      CargoMountPointStatusMessage(carrierGUID, cargoGUID, cargoGUID, PlanetSideGUID(0), mountPoint, CargoStatus.Occupied, orientation)
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
  def CargoMountBehaviorForOthers(carrier : Vehicle, cargo : Vehicle, mountPoint : Int, exclude : PlanetSideGUID) : (ObjectAttachMessage, CargoMountPointStatusMessage) = {
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
  def CargoMountMessagesForOthers(zone : Zone, exclude : PlanetSideGUID, attachMessage : ObjectAttachMessage, mountPointStatusMessage : CargoMountPointStatusMessage) : Unit = {
    zone.VehicleEvents ! VehicleServiceMessage(zone.Id, VehicleAction.SendResponse(exclude, attachMessage))
    zone.VehicleEvents ! VehicleServiceMessage(zone.Id, VehicleAction.SendResponse(exclude, mountPointStatusMessage))
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
  def CargoMountBehaviorForAll(carrier : Vehicle, cargo : Vehicle, mountPoint : Int) : (ObjectAttachMessage, CargoMountPointStatusMessage) = {
    val zone = carrier.Zone
    val zoneId = zone.Id
    val msgs @ (attachMessage, mountPointStatusMessage) = CargoMountMessages(carrier, cargo, mountPoint)
    zone.VehicleEvents ! VehicleServiceMessage(zoneId, VehicleAction.SendResponse(Service.defaultPlayerGUID, attachMessage))
    zone.VehicleEvents ! VehicleServiceMessage(zoneId, VehicleAction.SendResponse(Service.defaultPlayerGUID, mountPointStatusMessage))
    msgs
  }
}
