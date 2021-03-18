// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.shuttle

import net.psforever.objects.Vehicle
import net.psforever.objects.definition.VehicleDefinition
import net.psforever.objects.serverobject.mount.Seat
import net.psforever.objects.vehicles.AccessPermissionGroup

/**
  * The high altitude rapid transport (HART) orbital shuttle is a special vehicle
  * that is paired with a formal building `Amenity` called the orbital shuttle pad (`obbasemesh`)
  * and is only found in the HART buildings (`orbital_building_`{faction}) of a given faction's sanctuary zone.<br>
  * <br>
  * It has no pilot and can not be piloted.
  * Unlike other vehicles, it has the potential for a very sizeable passenger capacity.
  * Despite this, it is intended to start with a single mount.
  * That one mount should contain the information needed to create a given number of spontaneous passenger mount points.
  * Whenever a valid user would try to find a mount, and there are no mounts available,
  * and the total number of created mounts has not yet exceeded the limits set by the original mount's designation,
  * then a completely new mount can be created and the user attached.
  * All spontaneous mounts have the same properties as the original mount.
  * @param sdef the vehicle's definition entry
  */
class OrbitalShuttle(sdef: VehicleDefinition) extends Vehicle(sdef) {
  /**
    * Either locate a place for a passenger to mount,
    * or designate a spontaneous mount point to handle a new passenger.
    * The only time there is no more space is when the no new spontaneous seats can be counted.
    * @param mountPoint the mount point
    * @return the mount index
    */
  override def GetSeatFromMountPoint(mountPoint: Int): Option[Int] = {
    super.GetSeatFromMountPoint(mountPoint) match {
      case Some(0) =>
        seats.find { case (_, seat) => !seat.isOccupied } match {
          case Some((seatNumber, _))                              => Some(seatNumber)
          case None if seats.size < seats(0).definition.occupancy => Some(seats.size)
          case _                                                  => None
        }
      case _ =>
        None
    }
  }

  /**
    * Either locate a place for a passenger to mount,
    * or create a spontaneous mount point to handle the new passenger.
    * The only time there is no more space is when the no new spontaneous seats can be created.
    * This new seat becomes "real" and will continue to exist after being dismounted.
    * @param seatNumber the index of a mount point
    * @return the specific mount
    */
  override def Seat(seatNumber: Int): Option[Seat] = {
    val sdef = seats(0).definition
    super.Seat(seatNumber) match {
      case out @ Some(_) =>
        out
      case None if seatNumber == seats.size && seatNumber < sdef.occupancy =>
        val newSeat = new Seat(sdef)
        seats = seats ++ Map(seatNumber -> newSeat)
        Some(newSeat)
      case _ =>
        None
    }
  }

  /**
    * All players mounted in the shuttle are passengers only.  No driver.  No gunners.
    * Even if it does not exist yet, as long as it has the potential to be created,
    * discuss the next seat that would be created as if it already exists.
    * @param seatNumber the index of a mount point
    * @return `Passenger` permissions
    */
  override def SeatPermissionGroup(seatNumber : Int) : Option[AccessPermissionGroup.Value] = {
    Seats.get(seatNumber) match {
      case Some(_) =>
        Some(AccessPermissionGroup.Passenger)
      case None
        if seats.size == seatNumber && Seats.values.exists { _.definition.occupancy > seats.size } =>
        Some(AccessPermissionGroup.Passenger)
      case _ =>
        None
    }
  }
}
