// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.PacketHelpers
import scodec.codecs._

/**
  * An `Enumeration` of the mobility states of vehicles.<br>
  * <br>
  * In general, two important mobility states exist - `Mobile` and "deployed."
  * There are three stages of a formal deployment.
  * For any deployment state other than the defined ones, the vehicle assumes it is in one of the transitional states.
  * If the target vehicle has no deployment behavior, a non-`Mobile` value will not affect it.
  */
object DriveState extends Enumeration {
  type Type = Value

  val Mobile = Value(0) //drivable
  val Undeployed = Value(1) //stationary
  val Unavailable = Value(2) //stationary, partial activation
  val Deployed = Value(3) //stationary, full activation

  val State7 = Value(7) //unknown; not encountered on a vehicle that can deploy; functions like Mobile

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint8L)
}