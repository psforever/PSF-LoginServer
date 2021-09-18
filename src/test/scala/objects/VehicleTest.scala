// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects._
import net.psforever.objects.definition.VehicleDefinition
import net.psforever.objects.serverobject.mount._
import net.psforever.objects.vehicles._
import net.psforever.types.{PlanetSideGUID, _}
import org.specs2.mutable._

class VehicleTest extends Specification {
  import VehicleTest._

  "SeatDefinition" should {
    val seat = new SeatDefinition
    seat.restriction = MaxOnly
    seat.bailable = true

    "define (default)" in {
      val t_seat = new SeatDefinition
      t_seat.restriction mustEqual NoMax
      t_seat.bailable mustEqual false
    }

    "define (custom)" in {
      seat.restriction mustEqual MaxOnly
      seat.bailable mustEqual true
    }
  }

  "VehicleDefinition" should {
    "define" in {
      val fury = GlobalDefinitions.fury
      fury.CanBeOwned.contains(true) mustEqual true
      fury.CanCloak mustEqual false
      fury.Seats.size mustEqual 1
      fury.Seats(0).bailable mustEqual true
      fury.MountPoints.size mustEqual 2
      fury.MountPoints.get(1).contains(MountInfo(0, Vector3(0,0,0))) mustEqual true
      fury.MountPoints.get(2).contains(MountInfo(0, Vector3(0,0,0))) mustEqual true
      fury.Weapons.size mustEqual 1
      fury.Weapons.get(0).isEmpty mustEqual true
      fury.Weapons.get(1).contains(GlobalDefinitions.fury_weapon_systema)
      fury.TrunkSize.Width mustEqual 11
      fury.TrunkSize.Height mustEqual 11
      fury.TrunkOffset mustEqual 30
    }
  }

  "Seat" should {
    val seat_def = new SeatDefinition
    seat_def.restriction = MaxOnly
    seat_def.bailable = true

    "construct" in {
      val seat = new Seat(seat_def)
      seat.definition.restriction mustEqual MaxOnly
      seat.bailable mustEqual true
      seat.isOccupied mustEqual false
      seat.occupant.isEmpty mustEqual true
    }

    "player can sit" in {
      val seat = new Seat(seat_def)
      seat.isOccupied mustEqual false

      val player1 = Player(avatar1)
      player1.ExoSuit = ExoSuitType.MAX
      seat.mount(player1)
      seat.isOccupied mustEqual true
      seat.occupant.contains(player1) mustEqual true
    }

    "one occupant at a time" in {
      val seat = new Seat(seat_def)
      seat.isOccupied mustEqual false

      val player1 = Player(avatar1)
      player1.ExoSuit = ExoSuitType.MAX
      seat.mount(player1)
      seat.isOccupied mustEqual true
      seat.occupant.contains(player1) mustEqual true

      val player2 = Player(avatar1)
      player2.ExoSuit = ExoSuitType.MAX
      seat.mount(player2)
      seat.isOccupied mustEqual true
      seat.occupant.contains(player1) mustEqual true
    }

    "one player must get out of mount before other can get in" in {
      val seat = new Seat(seat_def)
      seat.isOccupied mustEqual false

      val player1 = Player(avatar1)
      player1.ExoSuit = ExoSuitType.MAX
      seat.mount(player1)
      seat.isOccupied mustEqual true
      seat.occupant.contains(player1) mustEqual true

      val player2 = Player(avatar2)
      player2.ExoSuit = ExoSuitType.MAX
      seat.mount(player2)
      seat.isOccupied mustEqual true
      seat.occupant.contains(player2) mustEqual false
      seat.occupants.contains(player1) mustEqual true

      seat.unmount(player1)
      seat.isOccupied mustEqual false
      seat.mount(player2)
      seat.isOccupied mustEqual true
      seat.occupants.contains(player2) mustEqual true
    }
  }

  "Vehicle" should {
    "construct" in {
      Vehicle(GlobalDefinitions.fury)
      ok
    }

    "construct (detailed)" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.Owner.isEmpty mustEqual true
      fury_vehicle.Seats.size mustEqual 1
      fury_vehicle.Seats(0).definition.restriction mustEqual NoMax
      fury_vehicle.Seats(0).isOccupied mustEqual false
      fury_vehicle.Seats(0).occupants.isEmpty mustEqual true
      fury_vehicle.Seats(0).bailable mustEqual true
      fury_vehicle.PermissionGroup(0).contains(VehicleLockState.Locked) //driver
      fury_vehicle.PermissionGroup(1).contains(VehicleLockState.Empire) //gunner
      fury_vehicle.PermissionGroup(2).contains(VehicleLockState.Empire) //passenger
      fury_vehicle.PermissionGroup(3).contains(VehicleLockState.Locked) //trunk
      fury_vehicle.Weapons.size mustEqual 1
      fury_vehicle.Weapons.get(0).isEmpty mustEqual true
      fury_vehicle.Weapons.get(1).isDefined mustEqual true
      fury_vehicle.Weapons(1).Equipment.isDefined mustEqual true
      fury_vehicle.Weapons(1).Equipment.get.Definition mustEqual GlobalDefinitions.fury.Weapons(1)
      fury_vehicle.WeaponControlledFromSeat(0) contains fury_vehicle.Weapons(1).Equipment.get mustEqual true
      fury_vehicle.Trunk.Width mustEqual 11
      fury_vehicle.Trunk.Height mustEqual 11
      fury_vehicle.Trunk.Offset mustEqual 30
      fury_vehicle.GetSeatFromMountPoint(1).contains(0)
      fury_vehicle.GetSeatFromMountPoint(2).contains(0)
      fury_vehicle.Decal mustEqual 0
      fury_vehicle.Health mustEqual fury_vehicle.Definition.MaxHealth
    }

    "can be owned by a player" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.Owner.isDefined mustEqual false

      val player1 = Player(avatar1)
      player1.GUID = PlanetSideGUID(1)
      fury_vehicle.Owner = player1
      fury_vehicle.Owner.isDefined mustEqual true
      fury_vehicle.Owner.contains(PlanetSideGUID(1)) mustEqual true
    }

    "ownership depends on who last was granted it" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.Owner.isDefined mustEqual false

      val player1 = Player(avatar1)
      player1.GUID = PlanetSideGUID(1)
      fury_vehicle.Owner = player1
      fury_vehicle.Owner.isDefined mustEqual true
      fury_vehicle.Owner.contains(PlanetSideGUID(1)) mustEqual true

      val player2 = Player(avatar2)
      player2.GUID = PlanetSideGUID(2)
      fury_vehicle.Owner = player2
      fury_vehicle.Owner.isDefined mustEqual true
      fury_vehicle.Owner.contains(PlanetSideGUID(2)) mustEqual true
    }

    "can use mount point to get mount number" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.GetSeatFromMountPoint(0).isEmpty mustEqual true
      fury_vehicle.GetSeatFromMountPoint(1).contains(0)
      fury_vehicle.GetSeatFromMountPoint(2).contains(0)
      fury_vehicle.GetSeatFromMountPoint(3).isEmpty mustEqual true
    }

    "has four permission groups" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id).contains(VehicleLockState.Locked)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Gunner.id).contains(VehicleLockState.Empire)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Passenger.id).contains(VehicleLockState.Empire)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Trunk.id).contains(VehicleLockState.Locked)
    }

    "set new permission level" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id).contains(VehicleLockState.Locked)
      fury_vehicle
        .PermissionGroup(AccessPermissionGroup.Driver.id, VehicleLockState.Group.id)
        .contains(VehicleLockState.Group)
    }

    "set the same permission level" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id).contains(VehicleLockState.Locked)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id, VehicleLockState.Locked.id).isEmpty mustEqual true
    }

    "alternate permission level indices" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id) mustEqual fury_vehicle.PermissionGroup(10)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Gunner.id) mustEqual fury_vehicle.PermissionGroup(11)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Passenger.id) mustEqual fury_vehicle.PermissionGroup(12)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Trunk.id) mustEqual fury_vehicle.PermissionGroup(13)

      (AccessPermissionGroup.Driver.id + 10) mustEqual 10
      fury_vehicle
        .PermissionGroup(AccessPermissionGroup.Driver.id, VehicleLockState.Group.id)
        .contains(VehicleLockState.Group)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id) mustEqual fury_vehicle.PermissionGroup(10)
    }

    "can determine permission group from mount" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      harasser_vehicle.SeatPermissionGroup(0).contains(AccessPermissionGroup.Driver)
      harasser_vehicle.SeatPermissionGroup(1).contains(AccessPermissionGroup.Gunner)
      harasser_vehicle.SeatPermissionGroup(2).isEmpty mustEqual true
      //TODO test for AccessPermissionGroup.Passenger later
    }

    "can find a passenger in a mount" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      val player1          = Player(avatar1)
      player1.GUID = PlanetSideGUID(1)
      val player2 = Player(avatar2)
      player2.GUID = PlanetSideGUID(2)
      harasser_vehicle.Seat(0).get.mount(player1) //don't worry about ownership for now
      harasser_vehicle.Seat(1).get.mount(player2)

      harasser_vehicle.PassengerInSeat(player1).contains(0)
      harasser_vehicle.PassengerInSeat(player2).contains(1)
      harasser_vehicle.Seat(0).get.unmount(player1)
      harasser_vehicle.PassengerInSeat(player1).isEmpty mustEqual true
      harasser_vehicle.PassengerInSeat(player2).contains(1)
    }

    "can find a weapon controlled from mount" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      val chaingun_p       = harasser_vehicle.Weapons(2).Equipment
      chaingun_p.isDefined mustEqual true

      harasser_vehicle.WeaponControlledFromSeat(0).isEmpty mustEqual true
      harasser_vehicle.WeaponControlledFromSeat(1).contains(chaingun_p.get) mustEqual true
    }

    "can filter utilities with indices that are natural numbers" in {
      val objDef = VehicleDefinition(1)
      objDef.Utilities += -1 -> UtilityType.order_terminala
      objDef.Utilities += 0  -> UtilityType.order_terminalb
      objDef.Utilities += 2  -> UtilityType.order_terminalb
      val obj = Vehicle(objDef)

      obj.Utilities.size mustEqual 3
      obj.Utilities(-1).UtilType mustEqual UtilityType.order_terminala
      obj.Utilities(0).UtilType mustEqual UtilityType.order_terminalb
      obj.Utilities.get(1).isEmpty mustEqual true
      obj.Utilities(2).UtilType mustEqual UtilityType.order_terminalb

      val filteredMap = Vehicle.EquipmentUtilities(obj.Utilities)
      filteredMap.size mustEqual 2
      filteredMap.get(-1).isEmpty mustEqual true
      filteredMap(0).UtilType mustEqual UtilityType.order_terminalb
      filteredMap(2).UtilType mustEqual UtilityType.order_terminalb
    }

    "access its mounted weapons by Slot" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      harasser_vehicle.Weapons(2).Equipment.get.GUID = PlanetSideGUID(10)

      harasser_vehicle.Slot(2).Equipment.get.GUID mustEqual PlanetSideGUID(10)
    }

    "access its trunk by Slot" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      val ammobox          = AmmoBox(GlobalDefinitions.armor_canister)
      ammobox.GUID = PlanetSideGUID(10)
      harasser_vehicle.Inventory += 30 -> ammobox

      harasser_vehicle.Slot(30).Equipment.get.GUID mustEqual PlanetSideGUID(10)
    }

    "find its mounted weapons by GUID" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      harasser_vehicle.Weapons(2).Equipment.get.GUID = PlanetSideGUID(10)

      harasser_vehicle.Find(PlanetSideGUID(10)).contains(2)
    }

    "find items in its trunk by GUID" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      val ammobox          = AmmoBox(GlobalDefinitions.armor_canister)
      ammobox.GUID = PlanetSideGUID(10)
      harasser_vehicle.Inventory += 30 -> ammobox

      harasser_vehicle.Find(PlanetSideGUID(10)).contains(30)
    }
  }
}

object VehicleTest {
  import net.psforever.objects.avatar.Avatar
  import net.psforever.types.{CharacterSex, PlanetSideEmpire}

  val avatar1 = Avatar(0, "test1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute)
  val avatar2 = Avatar(1, "test2", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute)
}
