// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

/**
  * Values for the equipment holster slot whose contained ("held") equipment can be drawn.
  * The values for these Enums match the slot number by index for Infantry weapons.<br>
  * <br>
  * `None` is not a kludge.
  * While any "not a holster" number can be used to indicate "no weapon drawn," seven is the value PlanetSide is looking for.
  * Using five or six delays the first weapon draw while the client corrects its internal state.
  */
object DrawnSlot extends Enumeration {
  type Type = Value

  val Pistol1 = Value(0)
  val Pistol2 = Value(1)
  val Rifle1 = Value(2)
  val Rifle2 = Value(3)
  val Melee = Value(4)
  val None = Value(7)

  import net.psforever.packet.PacketHelpers
  import scodec.codecs._
  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint(3))
}
