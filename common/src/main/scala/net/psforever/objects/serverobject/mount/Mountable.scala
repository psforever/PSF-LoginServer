// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.mount

import akka.actor.ActorRef
import net.psforever.objects.Player
import net.psforever.objects.vehicles.Seat

/**
  * A `Trait` common to all game objects that permit players to
  * interact with established spatial locations external to the object ("mount points") and
  * attach to the object in internal indices ("seats") for an undefined length of time.
  * @see `Seat`
  */
trait Mountable {

  /**
    * Retrieve a mapping of each seat from its internal index.
    * @return the mapping of index to seat
    */
  def Seats: Map[Int, Seat]

  /**
    * Given a seat's index position, retrieve the internal `Seat` object.
    * @return the specific seat
    */
  def Seat(seatNum: Int): Option[Seat]

  /**
    * Retrieve a mapping of each seat from its mount point index.
    * @return the mapping of mount point to seat
    */
  def MountPoints: Map[Int, Int]

  /**
    * Given a mount point index, return the associated seat index.
    * @param mount the mount point
    * @return the seat index
    */
  def GetSeatFromMountPoint(mount: Int): Option[Int]

  /**
    * Given a player, determine if that player is seated.
    * @param user the player
    * @return the seat index
    */
  def PassengerInSeat(user: Player): Option[Int]

  /**
    * A reference to an `Actor` that governs the logic of the object to accept `Mountable` messages.
    * Specifically, the `Actor` should intercept the logic of `MountableControl.`
    * @see `MountableControl`
    * @see `PlanetSideServerObject.Actor`
    * @return the internal `ActorRef`
    */
  def Actor: ActorRef //TODO can we enforce this desired association to MountableControl?
}

object Mountable {

  /**
    * Message used by the player to indicate the desire to board a `Mountable` object.
    * @param player the player who sent this request message
    * @param seat_num the seat index
    */
  final case class TryMount(player: Player, seat_num: Int)

  final case class TryDismount(player: Player, seat_num: Int)

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
    * @param seat_num the seat index
    */
  final case class CanMount(obj: Mountable, seat_num: Int) extends Exchange

  /**
    * Message sent in response to the player failing to access a `Mountable` object.
    * The player would have been be seated at the given index.
    * @param obj the `Mountable` object
    * @param seat_num the seat index
    */
  final case class CanNotMount(obj: Mountable, seat_num: Int) extends Exchange

  /**
    * Message sent in response to the player succeeding to disembark a `Mountable` object.
    * The player was previously seated at the given index.
    * @param obj the `Mountable` object
    * @param seat_num the seat index
    */
  final case class CanDismount(obj: Mountable, seat_num: Int) extends Exchange

  /**
    * Message sent in response to the player failing to disembark a `Mountable` object.
    * The player is still seated at the given index.
    * @param obj the `Mountable` object
    * @param seat_num the seat index
    */
  final case class CanNotDismount(obj: Mountable, seat_num: Int) extends Exchange
}
