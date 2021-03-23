// Copyright (c) 2021 PSForever
package objects

import net.psforever.objects.{GlobalDefinitions, Player, Vehicle}
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.serverobject.shuttle.OrbitalShuttle
import net.psforever.objects.vehicles.AccessPermissionGroup
import net.psforever.types.{CharacterGender, CharacterVoice, PlanetSideEmpire}
import org.specs2.mutable.Specification

class OrbitalShuttleTest extends Specification {
  val testAvatar1 = Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)
  val testAvatar2 = Avatar(1, "TestCharacter2", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)
  val testAvatar3 = Avatar(2, "TestCharacter3", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)

  "OrbitalShuttle" should {
    "construct (proper definition)" in {
      new OrbitalShuttle(GlobalDefinitions.orbital_shuttle)
      ok
    }

    "construct (any type of vehicle)" in {
      new OrbitalShuttle(GlobalDefinitions.fury)
      ok
    }

    "only use known mount points" in {
      val fury = new OrbitalShuttle(GlobalDefinitions.fury)
      fury.MountPoints.get(0).isEmpty mustEqual true
      fury.MountPoints.get(1).nonEmpty mustEqual true
      fury.MountPoints.get(2).nonEmpty mustEqual true
      fury.MountPoints.get(3).isEmpty mustEqual true

      val shuttle = new OrbitalShuttle(GlobalDefinitions.orbital_shuttle)
      shuttle.MountPoints.get(0).isEmpty mustEqual true
      shuttle.MountPoints.get(1).nonEmpty mustEqual true
      shuttle.MountPoints.get(2).nonEmpty mustEqual true
      shuttle.MountPoints.get(3).nonEmpty mustEqual true
      shuttle.MountPoints.get(4).nonEmpty mustEqual true
      shuttle.MountPoints.get(5).nonEmpty mustEqual true
      shuttle.MountPoints.get(6).nonEmpty mustEqual true
      shuttle.MountPoints.get(7).nonEmpty mustEqual true
      shuttle.MountPoints.get(8).nonEmpty mustEqual true
      shuttle.MountPoints.get(9).isEmpty mustEqual true
    }

    "will only discover unoccupied seats" in {
      val fury1 = new OrbitalShuttle(GlobalDefinitions.fury)
      val player1 = Player(testAvatar1)
      fury1.GetSeatFromMountPoint(mountPoint = 1) match {
        case Some(seatNumber) => fury1.Seat(seatNumber) match {
          case Some(seat)     => seat.mount(player1).contains(player1) mustEqual true
          case _              => ko
        }
        case _                => ko
      }
      fury1.GetSeatFromMountPoint(mountPoint = 1).isEmpty mustEqual true //seat is occupied

      //comparison with normal Vehicle
      val fury2 = new Vehicle(GlobalDefinitions.fury)
      val player2 = Player(testAvatar2)
      fury2.GetSeatFromMountPoint(mountPoint = 1) match {
        case Some(seatNumber) => fury2.Seat(seatNumber) match {
          case Some(seat)     => seat.mount(player2).contains(player2) mustEqual true
          case _              => ko
        }
        case _                => ko
      }
      fury2.GetSeatFromMountPoint(mountPoint = 1).contains(0) mustEqual true //even though seat is occupied
    }

    "have a fixed number of normal seats (using normal definition)" in {
      val fury1 = new OrbitalShuttle(GlobalDefinitions.fury)
      fury1.Seats.size mustEqual 1
      fury1.MountPoints.size mustEqual 2
      val player1 = Player(testAvatar1)
      fury1.GetSeatFromMountPoint(mountPoint = 1) match {
        case Some(seatNumber) => fury1.Seat(seatNumber) match {
          case Some(seat)     => seat.mount(player1).contains(player1) mustEqual true
          case _              => ko
        }
        case _                => ko
      }
      fury1.Seats.size mustEqual 1
      fury1.MountPoints.size mustEqual 2
      val player2 = Player(testAvatar2)
      fury1.GetSeatFromMountPoint(mountPoint = 1).isEmpty mustEqual true
      fury1.Seats.size mustEqual 1
      fury1.MountPoints.size mustEqual 2

      //congruent with normal Vehicle
      val fury2 = new Vehicle(GlobalDefinitions.fury)
      fury2.Seats.size mustEqual 1
      fury2.MountPoints.size mustEqual 2
      val player3 = Player(testAvatar3)
      fury2.GetSeatFromMountPoint(mountPoint = 1) match {
        case Some(seatNumber) => fury2.Seat(seatNumber) match {
          case Some(seat)     => seat.mount(player3).contains(player3) mustEqual true
          case _              => ko
        }
        case _                => ko
      }
      fury2.Seats.size mustEqual 1
      fury2.MountPoints.size mustEqual 2
      fury2.GetSeatFromMountPoint(mountPoint = 1) match {
        case Some(seatNumber) => fury2.Seat(seatNumber) match {
          case Some(seat)     => seat.mount(player2).contains(player2) mustEqual false
          case _              => ko
        }
        case _                => ko
      }
      fury2.Seats.size mustEqual 1
      fury2.MountPoints.size mustEqual 2
    }

    "create seats as needed (with appropriate definition)" in {
      GlobalDefinitions.fury
        .Seats(0).occupancy == 1 mustEqual true
      GlobalDefinitions.orbital_shuttle
        .Seats(0).occupancy > 1 mustEqual true

      val shuttle1 = new OrbitalShuttle(GlobalDefinitions.orbital_shuttle)
      shuttle1.Seats.size mustEqual 1
      shuttle1.MountPoints.size mustEqual 8
      val player1 = Player(testAvatar1)
      shuttle1.GetSeatFromMountPoint(mountPoint = 1) match {
        case Some(seatNumber) => shuttle1.Seat(seatNumber) match {
          case Some(seat)     => seat.mount(player1).contains(player1) mustEqual true
          case _              => ko
        }
        case _                => ko
      }
      shuttle1.Seats.size mustEqual 1
      shuttle1.MountPoints.size mustEqual 8
      val player2 = Player(testAvatar2)
      shuttle1.GetSeatFromMountPoint(mountPoint = 1) match {
        case Some(seatNumber) => shuttle1.Seat(seatNumber) match {
          case Some(seat)     => seat.mount(player2).contains(player2) mustEqual true
          case _              => ko
        }
        case _                => ko
      }
      shuttle1.Seats.size mustEqual 2
      shuttle1.MountPoints.size mustEqual 8

      //comparison with normal Vehicle
      val shuttle2 = new Vehicle(GlobalDefinitions.orbital_shuttle)
      shuttle2.Seats.size mustEqual 1
      shuttle2.MountPoints.size mustEqual 8
      val player3 = Player(testAvatar3)
      shuttle2.GetSeatFromMountPoint(mountPoint = 1) match {
        case Some(seatNumber) => shuttle2.Seat(seatNumber) match {
          case Some(seat)     => seat.mount(player3).contains(player3) mustEqual true
          case _              => ko
        }
        case _                => ko
      }
      shuttle2.Seats.size mustEqual 1
      shuttle2.MountPoints.size mustEqual 8
      shuttle2.GetSeatFromMountPoint(mountPoint = 1) match {
        case Some(seatNumber) => shuttle2.Seat(seatNumber) match {
          case Some(seat)     => seat.mount(player2).contains(player2) mustEqual false
          case _              => ko
        }
        case _                => ko
      }
      shuttle2.Seats.size mustEqual 1
      shuttle2.MountPoints.size mustEqual 8
    }

    "not create new seats out of order" in {
      val shuttle = new OrbitalShuttle(GlobalDefinitions.orbital_shuttle)
      val player1 = Player(testAvatar1)
      shuttle.Seat(seatNumber = 0) match {
        case Some(seat) => seat.mount(player1).contains(player1) mustEqual true
        case _          => ko
      }
      val player2 = Player(testAvatar2)
      shuttle.Seat(seatNumber = 2).isEmpty mustEqual true
    }

    "recognize proper seating arrangements" in {
      val shuttle = new OrbitalShuttle(GlobalDefinitions.orbital_shuttle)
      val player1 = Player(testAvatar1)
      shuttle.Seat(seatNumber = 0) match {
        case Some(seat) => seat.mount(player1).contains(player1) mustEqual true
        case _          => ko
      }
      val player2 = Player(testAvatar2)
      shuttle.Seat(seatNumber = 1) match {
        case Some(seat) => seat.mount(player2).contains(player2) mustEqual true
        case _          => ko
      }
      val player3 = Player(testAvatar3)
      shuttle.Seat(seatNumber = 2) match {
        case Some(seat) => seat.mount(player3).contains(player3) mustEqual true
        case _          => ko
      }
      shuttle.PassengerInSeat(player1).contains(0) mustEqual true
      shuttle.PassengerInSeat(player2).contains(1) mustEqual true
      shuttle.PassengerInSeat(player3).contains(2) mustEqual true
    }

    "retain created seats after dismount" in {
      val shuttle = new OrbitalShuttle(GlobalDefinitions.orbital_shuttle)
      val player1 = Player(testAvatar1)
      shuttle.Seat(seatNumber = 0) match {
        case Some(seat) => seat.mount(player1).contains(player1) mustEqual true
        case _          => ko
      }
      val player2 = Player(testAvatar2)
      shuttle.Seat(seatNumber = 1) match {
        case Some(seat) => seat.mount(player2).contains(player2) mustEqual true
        case _          => ko
      }
      val player3 = Player(testAvatar3)
      shuttle.Seat(seatNumber = 2) match {
        case Some(seat) => seat.mount(player3).contains(player3) mustEqual true
        case _          => ko
      }

      shuttle.Seats(0).isOccupied mustEqual true
      shuttle.Seats(1).isOccupied mustEqual true
      shuttle.Seats(2).isOccupied mustEqual true
      shuttle.Seats.size mustEqual 3
      //IMPORTANT TO NOTE
      shuttle.GetSeatFromMountPoint(mountPoint = 1).contains(3) mustEqual true //new seat

      shuttle.Seat(seatNumber = 1) match {
        case Some(seat) => seat.unmount(player2).isEmpty mustEqual true
        case _          => ko
      }
      shuttle.Seats(0).isOccupied mustEqual true
      shuttle.Seats(1).isOccupied mustEqual false
      shuttle.Seats(2).isOccupied mustEqual true
      shuttle.Seats.size mustEqual 3
      //IMPORTANT TO NOTE
      shuttle.GetSeatFromMountPoint(mountPoint = 1).contains(1) mustEqual true //reuse newly unoccupied seat
    }

    "consider all seats as passenger seats" in {
      val fury1 = Vehicle(GlobalDefinitions.fury)
      fury1.SeatPermissionGroup(seatNumber = 0).contains(AccessPermissionGroup.Driver)
      fury1.SeatPermissionGroup(seatNumber = 1).isEmpty mustEqual true

      val fury2 = Vehicle(GlobalDefinitions.orbital_shuttle)
      fury2.SeatPermissionGroup(seatNumber = 0).contains(AccessPermissionGroup.Driver)
      fury2.SeatPermissionGroup(seatNumber = 1).isEmpty mustEqual true

      val shuttle1 = new OrbitalShuttle(GlobalDefinitions.fury)
      shuttle1.SeatPermissionGroup(seatNumber = 0).contains(AccessPermissionGroup.Passenger)
      shuttle1.SeatPermissionGroup(seatNumber = 1).isEmpty mustEqual true

      val shuttle2 = new OrbitalShuttle(GlobalDefinitions.orbital_shuttle)
      shuttle2.SeatPermissionGroup(seatNumber = 0).contains(AccessPermissionGroup.Passenger)
      shuttle2.SeatPermissionGroup(seatNumber = 1).contains(AccessPermissionGroup.Passenger) //seat does not exist yet
      shuttle2.SeatPermissionGroup(seatNumber = 2).isEmpty mustEqual true
      val player1 = Player(testAvatar1)
      shuttle2.Seat(seatNumber = 0) match {
        case Some(seat) => seat.mount(player1).contains(player1) mustEqual true
        case _          => ko
      }
      val player2 = Player(testAvatar2)
      shuttle2.Seat(seatNumber = 1) match {
        case Some(seat) => seat.mount(player2).contains(player2) mustEqual true
        case _          => ko
      }
      shuttle2.SeatPermissionGroup(seatNumber = 0).contains(AccessPermissionGroup.Passenger)
      shuttle2.SeatPermissionGroup(seatNumber = 1).contains(AccessPermissionGroup.Passenger)
      shuttle2.SeatPermissionGroup(seatNumber = 2).contains(AccessPermissionGroup.Passenger) //seat does not exist yet
      shuttle2.SeatPermissionGroup(seatNumber = 3).isEmpty mustEqual true
    }
  }
}
