// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.mount

import akka.actor.ActorRef
import net.psforever.objects.Player
import net.psforever.types.BailType

import scala.annotation.tailrec

/**
  * A `Trait` common to all game objects that permit players to
  * interact with established spatial locations external to the object ("mount points") and
  * attach to the object in internal indices ("seats") for an undefined length of time.
  * @see `Seat`
  */
trait Mountable {
  protected var seats: Map[Int, Seat] = Map.empty

  /**
    * Retrieve a mapping of each mount from its internal index.
    * @return the mapping of index to mount
    */
  def Seats: Map[Int, Seat] = seats

  /**
    * Given a mount's index position, retrieve the internal `Seat` object.
    * @return the specific mount
    */
  def Seat(seatNumber: Int): Option[Seat] = {
    if (seatNumber >= 0 && seatNumber < seats.size) {
      seats.get(seatNumber)
    } else {
      None
    }
  }

  /**
   * All the seats that have occupants by their seat number.
   * @return list of the numbers of all occupied seats
   */
  def OccupiedSeats(): List[Int] = {
    seats
      .collect { case (index, seat) if seat.isOccupied => index }
      .toList
      .sorted
  }

  /**
    * Retrieve a mapping of each mount from its mount point index.
    * @return the mapping of mount point to mount
    */
  def MountPoints: Map[Int, MountInfo] = Definition.MountPoints.toMap

  /**
    * Given a mount point index, return the associated mount index.
    * @param mountPoint the mount point
    * @return the mount index
    */
  def GetSeatFromMountPoint(mountPoint: Int): Option[Int] = {
    MountPoints.get(mountPoint) match {
      case Some(mp) => Some(mp.seatIndex)
      case _        => None
    }
  }

  /**
    * Given a player, determine if that player is seated.
    * @param user the player
    * @return the mount index
    */
  def PassengerInSeat(user: Player): Option[Int] = recursivePassengerInSeat(seats.iterator, user)

  @tailrec private def recursivePassengerInSeat(iter: Iterator[(Int, Seat)], player: Player): Option[Int] = {
    if (!iter.hasNext) {
      None
    } else {
      val (seatNumber, seat) = iter.next()
      if (seat.occupant.contains(player)) {
        Some(seatNumber)
      } else {
        recursivePassengerInSeat(iter, player)
      }
    }
  }

  /**
    * A reference to an `Actor` that governs the logic of the object to accept `Mountable` messages.
    * Specifically, the `Actor` should intercept the logic of `MountableControl.`
    * @see `MountableControl`
    * @see `PlanetSideServerObject.Actor`
    * @return the internal `ActorRef`
    */
  def Actor: ActorRef //TODO can we enforce this desired association to MountableControl?

  def Definition: MountableDefinition
}

object Mountable {

  /**
    * Message used by the player to indicate the desire to board a `Mountable` object.
    * @param player the player who sent this request message
    * @param mount_point the mount index
    */
  final case class TryMount(player: Player, mount_point: Int)

  /**
    * Message used by the player to indicate the desire to escape a `Mountable` object.
    * @param player the player who sent this request message
    * @param seat_num the seat index
    */
  final case class TryDismount(player: Player, seat_num: Int, bailType: BailType.Value)

  object TryDismount {
    def apply(player: Player, seatNum: Int): TryDismount = TryDismount(player, seatNum, BailType.Normal)
  }

  /**
    * A basic `Trait` connecting all of the actionable `Mountable` response messages.
    */
  sealed trait Exchange

  /**
    * Message that carries the result of the processed request message back to the original user (`player`).
    * @param player the player who sent this request message
    * @param response the result of the processed request
    */
  final case class MountMessages(player: Player, response: Exchange)

  /**
    * Message sent in response to the player succeeding to access a `Mountable` object.
    * The player should be seated at the given index.
    * @param obj the `Mountable` object
    * @param mount_point the mount index
    */
  final case class CanMount(obj: Mountable, seat_number: Int, mount_point: Int) extends Exchange

  /**
    * Message sent in response to the player failing to access a `Mountable` object.
    * The player would have been be seated at the given index.
    * @param obj the `Mountable` object
    * @param mount_point the mount index
    */
  final case class CanNotMount(obj: Mountable, mount_point: Int) extends Exchange

  /**
    * Message sent in response to the player succeeding to disembark a `Mountable` object.
    * The player was previously seated at the given index.
    * @param obj the `Mountable` object
    * @param seat_num the seat index
    */
  final case class CanDismount(obj: Mountable, seat_num: Int, mount_point: Int) extends Exchange

  /**
    * Message sent in response to the player failing to disembark a `Mountable` object.
    * The player is still seated at the given index.
    * @param obj the `Mountable` object
    * @param seat_num the seat index
    */
  final case class CanNotDismount(obj: Mountable, seat_num: Int, bailType: BailType.Value) extends Exchange
}
