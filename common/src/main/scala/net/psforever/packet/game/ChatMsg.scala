// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

// ChatMessageTypes were reversed by:
  // Checking the type in the packet upon receipt by server while using slash commands
  // Replaying the ChatMsg packet back to the sender after modifying the messagetype value

// Some other unchecked commands, if they actually exist as message types:
// Reply (/r), BattleGroup (/bg), ReplyCSR (/gmreply)

object ChatMessageType extends Enumeration {
  type Type = Value
  val Unk0,                   // ??? Appears in top chat pane
      Unk1,                   // ??? Appears in top chat pane
      Unk2,                   // ??? Appears in top chat pane
      Broadcast,              // /b
      Command,                // /c
      Global,                 // /comall
      SituationReport,        // /sitrep
      SanctuaryAll,           // /comsan
      OrbitalStationAll,      // No slash command???
      SupaiAll,               // /comsu
      HunhauAll,              // /comhu
      AdlivunAll,             // /comad
      ByblosAll,              // /comby
      AnnwnAll,               // /coman
      DrugaskanAll,           // /comdr
      SolsarAll,              // /comso
      HossinAll,              // /comho
      CyssorAll,              // /comcy
      IshundarAll,            // /comis
      ForseralAll,            // /comfo
      CeryshenAll,            // /comce
      EsamirAll,              // /comes
      OshurAll,               // /comos
      OshurPrimeAll,          // /compr
      SearhusAll,             // /comse
      AmerishAll,             // /comam
      Local,                  // /l
      Outfit,                 // /o
      Platoon,                // /p
      SquadLeaders,           // /pl
      Squad,                  // /s
      SquadCommand,           // /sl
      Tell,                   // /t
      BlackOps,               // No slash command???
      CSRBroadcast,           // /gmbroadcast
      CSRBroadcastNC,         // /ncbroadcast
      CSRBroadcastTR,         // /trbroadcast
      CSRBroadcastVS,         // /vsbroadcast
      CSRWorldBroadcast,      // /worldbroadcast || /wb
      CSR,                    // /gmlocal
      CSRTell,                // /gmtell (actually causes normal /tell 0x20 when not a gm???)
      Note,                   // /note
      CSRWorldBroadcastPopup, // /gmpopup
      CSRTellTo,              // Recipient of /gmtell
      TellTo,                 // Recipient of /t
      Unk45,                  // ??? Looks like local?
      Unk46,                  // ??? This actually causes the client to ping back to the server with some stringified numbers "80 120" (with the same 46 chatmsg causing infinite loop?) - may be incorrect decoding
      PopupInstantAction,     // Sent when Instant Action invoked
      PopupRecallSanctuary,   // Sent when Recall To Sanctuary invoked
      Unk49,                  // ???
      Unk50,                  // ???
      Unk51,                  // ???
      Unk52,                  // ???
      Unk53,                  // ???
      Unk54,                  // ???
      Unk55,                  // ???
      Unk56,                  // ???
      Unk57,                  // ???
      Unk58,                  // ???
      Unk59,                  // ???
      Unk60,                  // ???
      PopupQuit               // Sent when Quit invoked
      // Could be more types
      = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint8L)
}

final case class ChatMsg(channel : ChatMessageType.Value,
                         unk1 : Boolean,
                         recipient : String,
                         contents : String)
  extends PlanetSideGamePacket {
  type Packet = ChatMsg
  def opcode = GamePacketOpcode.ChatMsg
  def encode = ChatMsg.encode(this)
}

object ChatMsg extends Marshallable[ChatMsg] {
  implicit val codec : Codec[ChatMsg] = (
    ("messagetype" | ChatMessageType.codec) ::
      ("unk1" | bool) ::
      ("recipient" | PacketHelpers.encodedWideStringAligned(7)) ::
      ("contents" | PacketHelpers.encodedWideString)
    ).as[ChatMsg]
}
