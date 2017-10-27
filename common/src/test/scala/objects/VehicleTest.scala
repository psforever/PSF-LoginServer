// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.{GlobalDefinitions, Vehicle}
import net.psforever.objects.definition.SeatDefinition
import net.psforever.objects.vehicles.{Seat, SeatArmorRestriction, VehicleLockState}
import org.specs2.mutable._

class VehicleTest extends Specification {

  "SeatDefinition" should {
    val seat = new SeatDefinition
    seat.ArmorRestriction = SeatArmorRestriction.MaxOnly
    seat.Bailable = true
    seat.ControlledWeapon = 5

    "define (default)" in {
      val t_seat = new SeatDefinition
      t_seat.ArmorRestriction mustEqual SeatArmorRestriction.NoMax
      t_seat.Bailable mustEqual false
      t_seat.ControlledWeapon mustEqual None
    }

    "define (custom)" in {
      seat.ArmorRestriction mustEqual SeatArmorRestriction.MaxOnly
      seat.Bailable mustEqual true
      seat.ControlledWeapon mustEqual Some(5)
    }
  }

  "VehicleDefinition" should {
    "define" in {
      val fury = GlobalDefinitions.fury
      fury.CanBeOwned mustEqual true
      fury.CanCloak mustEqual false
      fury.Seats.size mustEqual 1
      fury.Seats(0).Bailable mustEqual true
      fury.Seats(0).ControlledWeapon mustEqual Some(1)
      fury.MountPoints.size mustEqual 2
      fury.MountPoints.get(0) mustEqual Some(0)
      fury.MountPoints.get(1) mustEqual None
      fury.MountPoints.get(2) mustEqual Some(0)
      fury.Weapons.size mustEqual 1
      fury.Weapons.get(0) mustEqual None
      fury.Weapons.get(1) mustEqual Some(GlobalDefinitions.fury_weapon_systema)
      fury.TrunkSize.width mustEqual 11
      fury.TrunkSize.height mustEqual 11
      fury.TrunkOffset mustEqual 30
    }
  }

  "Seat" should {
    val seat_def = new SeatDefinition
    seat_def.ArmorRestriction = SeatArmorRestriction.MaxOnly
    seat_def.Bailable = true
    seat_def.ControlledWeapon = 5

    "construct" in {
      val seat = new Seat(seat_def)
      seat.ArmorRestriction mustEqual SeatArmorRestriction.MaxOnly
      seat.Bailable mustEqual true
      seat.ControlledWeapon mustEqual Some(5)
      seat.isOccupied mustEqual false
      seat.Occupant mustEqual None
    }
  }

  "Vehicle" should {
    "construct" in {
      Vehicle(GlobalDefinitions.fury)
      ok
    }

    "construct (detailed)" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.Owner mustEqual None
      fury_vehicle.Seats.size mustEqual 1
      fury_vehicle.Seats.head.ArmorRestriction mustEqual SeatArmorRestriction.NoMax
      fury_vehicle.Seats.head.isOccupied mustEqual false
      fury_vehicle.Seats.head.Occupant mustEqual None
      fury_vehicle.Seats.head.Bailable mustEqual true
      fury_vehicle.Seats.head.ControlledWeapon mustEqual Some(1)
      fury_vehicle.PermissionGroup(0) mustEqual Some(VehicleLockState.Locked) //driver
      fury_vehicle.PermissionGroup(1) mustEqual Some(VehicleLockState.Empire) //gunner
      fury_vehicle.PermissionGroup(2) mustEqual Some(VehicleLockState.Empire) //passenger
      fury_vehicle.PermissionGroup(3) mustEqual Some(VehicleLockState.Locked) //trunk
      fury_vehicle.Weapons.size mustEqual 1
      fury_vehicle.Weapons.get(0) mustEqual None
      fury_vehicle.Weapons.get(1).isDefined mustEqual true
      fury_vehicle.Weapons(1).Equipment.isDefined mustEqual true
      fury_vehicle.Weapons(1).Equipment.get.Definition mustEqual GlobalDefinitions.fury.Weapons(1)
      fury_vehicle.WeaponControlledFromSeat(0) mustEqual fury_vehicle.Weapons(1).Equipment
      fury_vehicle.Trunk.Width mustEqual 11
      fury_vehicle.Trunk.Height mustEqual 11
      fury_vehicle.Trunk.Offset mustEqual 30
      fury_vehicle.GetSeatFromMountPoint(0) mustEqual Some(0)
      fury_vehicle.GetSeatFromMountPoint(1) mustEqual None
      fury_vehicle.GetSeatFromMountPoint(2) mustEqual Some(0)
      fury_vehicle.Decal mustEqual 0
      fury_vehicle.Health mustEqual fury_vehicle.Definition.MaxHealth
    }
  }
}
