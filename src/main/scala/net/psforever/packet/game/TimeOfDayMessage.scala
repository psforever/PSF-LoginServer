// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Sets Auraxis time for a continent (zone) on the client.
  * Use the slash-command `/time` to view the current time in the event window.
  * Auraxis time is represented as a standard military twenty-four hour clock, displayed in hours and minutes.<br>
  * <br>
  * Time is set per zone on map loading.
  * Time affects, primarily, ambient light on surfaces.
  * It goes from full daylight, to twilights, to slightly darker nights, though the actual intensity will differ by zone.<br>
  * <br>
  * Auraxis time is weird.
  * The data from the server is deconstructed into both a current time and a rate of progression.
  * The lower the value, the lower the rate; the greater the value, the greater the rate.
  * The rate is the product of the number of "cycles" between the current time and an origin time and a base interval.
  * The current time is constrained to a looping twenty-four hour interval.<br>
  * <br>
  * If no time is set, the client starts counting from 10:00 at an initial rate of about one Auraxis minute every four or five real seconds.
  * Setting the current time to 1107296256 sets the current time to 00:00 with an indeterminate, but slow, rate.
  * Time is normally initialized somewhere within an interval between 2:16 (0x460000, 1174405120) and 36:24 (0x47FFFF00, 1207959296).
  * Setting the current time extremely high (near the numerical maximum) can cause psychedelic rendering.
  * (Setting the time to 4294967040 exactly will reduce the rendering system to gibberish.)<br>
  * <br>

  * @param timeOfDay Auraxis time
  * @param timeSpeed consistently 10.0f
  */
final case class TimeOfDayMessage(timeOfDay: Float, timeSpeed: Float = 10.0f) extends PlanetSideGamePacket {
  type Packet = TimeOfDayMessage
  def opcode = GamePacketOpcode.TimeOfDayMessage
  def encode = TimeOfDayMessage.encode(this)
}

object TimeOfDayMessage extends Marshallable[TimeOfDayMessage] {
  implicit val codec: Codec[TimeOfDayMessage] = (
    ("timeOfDay" | floatL) ::
      ("timeSpeed" | floatL)
  ).as[TimeOfDayMessage]

  def GetTimeOfDayValue(hours: Int, minutes: Int): Float = {
    ((hours * 3600 + minutes * 60) % 86400).toFloat
  }

  def GetTimeOfDayValue(timeSpeed: Float = 10.0f): Float = {
    val now = System.currentTimeMillis()
    val scaled = now * timeSpeed
    val seconds = scaled / 1_000 // back to seconds
    val clamped = seconds % 86400 // clamp to 24h

    clamped
  }
}
