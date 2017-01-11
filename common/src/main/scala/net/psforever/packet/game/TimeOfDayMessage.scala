// Copyright (c) 2016 PSForever.net to present
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
  * Time is normally initialized somewhere within an interval between 1174405120 and 1207959296.
  * Setting the current time extremely high (near the numerical maximum) can cause psychedelic rendering.
  * (Setting the time to 4294967040 exactly will reduce the rendering system to gibberish.)<br>
  * <br>
  * The interval from 1178164736 (~03:18) to 1203765248 (03:18) is about a full twenty-four hours.
  * That is a count of 25600512.
  * @param time Auraxis time
  * @param unk consistently 1092616192; does nothing?
  */
final case class TimeOfDayMessage(time : Long,
                                  unk : Long = 1092616192L)
  extends PlanetSideGamePacket {
  type Packet = TimeOfDayMessage
  def opcode = GamePacketOpcode.TimeOfDayMessage
  def encode = TimeOfDayMessage.encode(this)
}

object TimeOfDayMessage extends Marshallable[TimeOfDayMessage] {
  implicit val codec : Codec[TimeOfDayMessage] = (
      ("time" | uint32L) ::
      ("unk" | uint32L)
    ).as[TimeOfDayMessage]
}

/*
Time Testing Conducted in VS Sanctuary
48 00 __ __ __ 00 00 20 41
--------------------------
     +01 00 00
--------------------------
48 00 1B 00 47 00 00 20 41 //09:06
48 00 1C 00 47 00 00 20 41 //09:07
...
48 00 59 00 47 00 00 20 41 //09:08 (+3D 00 00)
--------------------------
     +10 00 00
--------------------------
48 00 00 00 47 00 00 20 41 //09:06 <--
48 00 10 00 47 00 00 20 41 //09:06
48 00 20 00 47 00 00 20 41 //09:07
48 00 30 00 47 00 00 20 41 //09:07
48 00 40 00 47 00 00 20 41 //09:07
48 00 50 00 47 00 00 20 41 //09:07
48 00 60 00 47 00 00 20 41 //09:08
48 00 70 00 47 00 00 20 41 //09:08
48 00 80 00 47 00 00 20 41 //09:08
48 00 90 00 47 00 00 20 41 //09:08
48 00 A0 00 47 00 00 20 41 //09:09
48 00 B0 00 47 00 00 20 41 //09:09
48 00 C0 00 47 00 00 20 41 //09:09
48 00 D0 00 47 00 00 20 41 //09:09
48 00 E0 00 47 00 00 20 41 //09:10
48 00 F0 00 47 00 00 20 41 //09:10
48 00 00 01 47 00 00 20 41 //09:10
--------------------------
     +00 01 00
--------------------------
48 00 00 00 47 00 00 20 41 //09:06 <--
48 00 00 01 47 00 00 20 41 //09:10
48 00 00 02 47 00 00 20 41 //09:15
48 00 00 03 47 00 00 20 41 //09:19
48 00 00 04 47 00 00 20 41 //09:23
48 00 00 05 47 00 00 20 41 //09:27
48 00 00 06 47 00 00 20 41 //09:32
48 00 00 07 47 00 00 20 41 //09:36
48 00 00 08 47 00 00 20 41 //09:40
48 00 00 09 47 00 00 20 41 //09:44
48 00 00 0A 47 00 00 20 41 //09:49
48 00 00 0B 47 00 00 20 41 //09:53
48 00 00 0C 47 00 00 20 41 //09:57
48 00 00 0D 47 00 00 20 41 //09:01
48 00 00 0E 47 00 00 20 41 //10:06
48 00 00 0F 47 00 00 20 41 //10:10
48 00 00 10 47 00 00 20 41 //10:14
--------------------------
     +00 10 00
--------------------------
48 00 00 00 46 00 00 20 41 //02:17 (-00:17)
48 00 00 10 46 00 00 20 41 //02:34 (-00:17)
48 00 00 20 46 00 00 20 41 //02:51 (-00:17)
48 00 00 30 46 00 00 20 41 //03:08 (-00:17)
48 00 00 40 46 00 00 20 41 //03:25 (-00:17)
48 00 00 50 46 00 00 20 41 //03:42 (-00:17)
48 00 00 60 46 00 00 20 41 //03:59 (-00:17)
48 00 00 70 46 00 00 20 41 //04:16 (-00:17)
48 00 00 80 46 00 00 20 41 //04:33 (-00:34)
48 00 00 90 46 00 00 20 41 //05:07 (-00:34)
48 00 00 A0 46 00 00 20 41 //05:41 (-00:35)
48 00 00 B0 46 00 00 20 41 //06:16 (-00:34)
48 00 00 C0 46 00 00 20 41 //06:50 (-00:34)
48 00 00 D0 46 00 00 20 41 //07:24 (-00:34)
48 00 00 E0 46 00 00 20 41 //07:58 (-00:34)
48 00 00 F0 46 00 00 20 41 //08:32 (-00:34)
48 00 00 00 47 00 00 20 41 //09:06 <--
48 00 00 10 47 00 00 20 41 //10:14 (+01:08)
48 00 00 20 47 00 00 20 41 //11:23 (+01:09)
48 00 00 30 47 00 00 20 41 //12:31 (+01:08)
48 00 00 40 47 00 00 20 41 //13:39 (+01:08)
48 00 00 50 47 00 00 20 41 //14:47 (+01:08)
48 00 00 60 47 00 00 20 41 //15:56 (+01:08)
48 00 00 70 47 00 00 20 41 //17:04 (+01:08)
48 00 00 80 47 00 00 20 41 //18:12 (+01:08)
48 00 00 90 47 00 00 20 41 //20:29 (+02:16)
48 00 00 A0 47 00 00 20 41 //22:45 (+02:16)
48 00 00 B0 47 00 00 20 41 //01:02 (+02:17)
48 00 00 C0 47 00 00 20 41 //03:18 (+02:16)
48 00 00 D0 47 00 00 20 41 //05:35 (+02:17)
48 00 00 E0 47 00 00 20 41 //07:51 (+02:16)
48 00 00 F0 47 00 00 20 41 //10:08 (+02:17)
48 00 00 00 48 00 00 20 41 //12:24 (+02:16)
*/
