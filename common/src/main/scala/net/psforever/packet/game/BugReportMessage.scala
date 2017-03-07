// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

/**
  * An `Enumeration` of the kinds of bugs applicable to the reporting system.
  */
object BugType extends Enumeration {
  type Type = Value
  val CRASH,
      GAMEPLAY,
      ART,
      SOUND,
      HARDWARE,
      OTHER
      = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint4L)
}

/**
  * Allow the user to report a bug they have found in the game.<br>
  * <br>
  * Bug reports are prepended by the version of the client on which the player is encountering the issue.
  * The last delivered client by Sony Online Entertainment was `3.15.84` with date `Dec&nbsp;&nbsp;2 2009`.<br>
  * <br>
  * The path of bug reports submitted to the game's official server is not known.
  * @param version_major the client's major version number
  * @param version_minor the client's minor version number
  * @param version_date the date the client was compiled
  * @param bug_type the kind of bug that took place
  * @param repeatable whether the bug is repeatable
  * @param location 0 when "other location", 2 when "current location"
  * @param zone which zone the bug took place
  * @param pos the x y z location where the bug took place
  * @param summary a short explanation of the bug
  * @param desc a detailed explanation of the bug
  */
final case class BugReportMessage(version_major : Long,
                                  version_minor : Long,
                                  version_date : String,
                                  bug_type : BugType.Value,
                                  repeatable : Boolean,
                                  location : Int,
                                  zone : Int,
                                  pos : Vector3,
                                  summary : String,
                                  desc : String)
  extends PlanetSideGamePacket {
  type Packet = BugReportMessage
  def opcode = GamePacketOpcode.BugReportMessage
  def encode = BugReportMessage.encode(this)
}

object BugReportMessage extends Marshallable[BugReportMessage] {
  implicit val codec : Codec[BugReportMessage] = (
    ("versionMajor" | uint32L) ::
      ("versionMinor" | uint32L) ::
      ("versionDate" | PacketHelpers.encodedString) ::
      ("bug_type" | BugType.codec) ::
      ignore(3) ::
      ("repeatable" | bool) ::
      ("location" | uint4L) ::
      ("zone" | uint8L) ::
      ("pos" | Vector3.codec_pos) ::
      ("summary" | PacketHelpers.encodedWideStringAligned(4)) ::
      ("desc" | PacketHelpers.encodedWideString)
    ).as[BugReportMessage]
}
