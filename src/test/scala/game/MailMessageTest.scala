// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class MailMessageTest extends Specification {
  //we've never received this packet before so this whole test is faked
  val string = hex"F1 86466174654A489250726 96F72697479204D61696C2054657374 8E48656C6C6F204175726178697321"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case MailMessage(sender, subject, msg) =>
        sender mustEqual "FateJH"
        subject mustEqual "Priority Mail Test"
        msg mustEqual "Hello Auraxis!"
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = MailMessage("FateJH", "Priority Mail Test", "Hello Auraxis!")
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
