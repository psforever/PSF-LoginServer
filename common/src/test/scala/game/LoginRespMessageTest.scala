// Copyright (c) 2017 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.packet.game.LoginRespMessage._
import scodec.bits._

class LoginRespMessageTest extends Specification {

  /// NOTE: the token is a C-string, meaning that the 18FABE0C junk seems like uninitialized memory due to memcpy, but
  /// not memset
  //val original = hex"02 4861484C64597A73305641536A6B73520000000018FABE0C0000000000000000" ++
  //  hex"00000000 01000000 02000000 6B7BD828 8C4169666671756F7469656E74 00000000 00"
  val string = hex"02 4861484C64597A73305641536A6B735200000000000000000000000000000000" ++
    hex"00000000 01000000 02000000 6B7BD828 8C4169666671756F7469656E74 00000000 00"

  val string_priv = hex"02 4861484C64597A73305641536A6B735200000000000000000000000000000000" ++
    hex"00000000 01000000 02000000 6B7BD828 8C4169666671756F7469656E74 11270000 80"

  "encode" in {
    val msg = LoginRespMessage("HaHLdYzs0VASjksR", LoginError.Success, StationError.AccountActive,
      StationSubscriptionStatus.Active, 685276011, "Aiffquotient", 0)

    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual string
  }

  "encode with privilege" in {
    val msg = LoginRespMessage("HaHLdYzs0VASjksR", LoginError.Success, StationError.AccountActive,
      StationSubscriptionStatus.Active, 685276011, "Aiffquotient", 10001)

    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual string_priv
  }

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case LoginRespMessage(token, error, stationError, subscription, unk, username, priv) =>
        token mustEqual "HaHLdYzs0VASjksR"
        error mustEqual LoginError.Success
        stationError mustEqual StationError.AccountActive
        subscription mustEqual StationSubscriptionStatus.Active
        unk mustEqual 685276011
        username mustEqual "Aiffquotient"
        priv mustEqual 0
      case _ =>
        ko
    }
  }

  "decode with privilege" in {
    PacketCoding.DecodePacket(string_priv).require match {
      case LoginRespMessage(token, error, stationError, subscription, unk, username, priv) =>
        token mustEqual "HaHLdYzs0VASjksR"
        error mustEqual LoginError.Success
        stationError mustEqual StationError.AccountActive
        subscription mustEqual StationSubscriptionStatus.Active
        unk mustEqual 685276011
        username mustEqual "Aiffquotient"
        priv mustEqual 10001
      case _ =>
        ko
    }
  }
}
