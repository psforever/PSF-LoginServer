// Copyright (c) 2016 PSForever.net to present
import java.net.{InetAddress, InetSocketAddress}

import akka.actor.{Actor, ActorRef, Cancellable, MDCContextAware}
import net.psforever.packet.{PlanetSideGamePacket, _}
import net.psforever.packet.control._
import net.psforever.packet.game._
import scodec.Attempt.{Failure, Successful}
import scodec.bits._
import org.log4s.MDC
import MDCContextAware.Implicits._
import net.psforever.types.ChatMessageType

class old_WorldSessionActor extends Actor with MDCContextAware {
  private[this] val log = org.log4s.getLogger

  private case class PokeClient()

  var sessionId : Long = 0
  var leftRef : ActorRef = ActorRef.noSender
  var rightRef : ActorRef = ActorRef.noSender

  var clientKeepAlive : Cancellable = null

  override def postStop() = {
    if(clientKeepAlive != null)
      clientKeepAlive.cancel()
  }

  def receive = Initializing

  def Initializing : Receive = {
    case HelloFriend(sessionId, right) =>
      this.sessionId = sessionId
      leftRef = sender()
      rightRef = right.asInstanceOf[ActorRef]

      context.become(Started)
    case _ =>
      log.error("Unknown message")
      context.stop(self)
  }

  def Started : Receive = {
    case ctrl @ ControlPacket(_, _) =>
      handlePktContainer(ctrl)
    case game @ GamePacket(_, _, _) =>
      handlePktContainer(game)
      // temporary hack to keep the client from disconnecting
    case PokeClient() =>
      sendResponse(PacketCoding.CreateGamePacket(0, KeepAliveMessage(0)))
    case default => failWithError(s"Invalid packet class received: $default")
  }

  def handlePkt(pkt : PlanetSidePacket) : Unit = pkt match {
    case ctrl : PlanetSideControlPacket =>
      handleControlPkt(ctrl)
    case game : PlanetSideGamePacket =>
      handleGamePkt(game)
    case default => failWithError(s"Invalid packet class received: $default")
  }

  def handlePktContainer(pkt : PlanetSidePacketContainer) : Unit = pkt match {
    case ctrl @ ControlPacket(opcode, ctrlPkt) =>
      handleControlPkt(ctrlPkt)
    case game @ GamePacket(opcode, seq, gamePkt) =>
      handleGamePkt(gamePkt)
    case default => failWithError(s"Invalid packet container class received: $default")
  }

  def handleControlPkt(pkt : PlanetSideControlPacket) = {
    pkt match {
      case SlottedMetaPacket(slot, subslot, innerPacket) =>
        sendResponse(PacketCoding.CreateControlPacket(SlottedMetaAck(slot, subslot)))

        PacketCoding.DecodePacket(innerPacket) match {
          case Failure(e) =>
            log.error(s"Failed to decode inner packet of SlottedMetaPacket: $e")
          case Successful(v) =>
            handlePkt(v)
        }
      case sync @ ControlSync(diff, unk, f1, f2, f3, f4, fa, fb) =>
        log.debug(s"SYNC: ${sync}")
        val serverTick = Math.abs(System.nanoTime().toInt) // limit the size to prevent encoding error
        sendResponse(PacketCoding.CreateControlPacket(ControlSyncResp(diff, serverTick,
          fa, fb, fb, fa)))
      case MultiPacket(packets) =>
        packets.foreach { pkt =>
          PacketCoding.DecodePacket(pkt) match {
            case Failure(e) =>
              log.error(s"Failed to decode inner packet of MultiPacket: $e")
            case Successful(v) =>
              handlePkt(v)
          }
        }
      case MultiPacketEx(packets) =>
        packets.foreach { pkt =>
          PacketCoding.DecodePacket(pkt) match {
            case Failure(e) =>
              log.error(s"Failed to decode inner packet of MultiPacketEx: $e")
            case Successful(v) =>
              handlePkt(v)
          }
        }
      case default =>
        log.debug(s"Unhandled ControlPacket $default")
    }
  }

  // XXX: hard coded ObjectCreateMessage
  val objectHex = hex"18 57 0C 00 00 BC 84 B0  06 C2 D7 65 53 5C A1 60 00 01 34 40 00 09 70 49  00 6C 00 6C 00 6C 00 49 00 49 00 49 00 6C 00 6C  00 6C 00 49 00 6C 00 49 00 6C 00 6C 00 49 00 6C  00 6C 00 6C 00 49 00 6C 00 6C 00 49 00 84 52 70  76 1E 80 80 00 00 00 00 00 3F FF C0 00 00 00 20  00 00 0F F6 A7 03 FF FF FF FF FF FF FF FF FF FF  FF FF FF FF FF FD 90 00 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00 00 01 90 01 90 00 64 00  00 01 00 7E C8 00 C8 00 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00 00 00 01 C0 00 42 C5 46  86 C7 00 00 00 80 00 00 12 40 78 70 65 5F 73 61  6E 63 74 75 61 72 79 5F 68 65 6C 70 90 78 70 65  5F 74 68 5F 66 69 72 65 6D 6F 64 65 73 8B 75 73  65 64 5F 62 65 61 6D 65 72 85 6D 61 70 31 33 00  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00 00 00 00 00 01 0A 23 02  60 04 04 40 00 00 10 00 06 02 08 14 D0 08 0C 80  00 02 00 02 6B 4E 00 82 88 00 00 02 00 00 C0 41  C0 9E 01 01 90 00 00 64 00 44 2A 00 10 91 00 00  00 40 00 18 08 38 94 40 20 32 00 00 00 80 19 05  48 02 17 20 00 00 08 00 70 29 80 43 64 00 00 32  00 0E 05 40 08 9C 80 00 06 40 01 C0 AA 01 19 90  00 00 C8 00 3A 15 80 28 72 00 00 19 00 04 0A B8  05 26 40 00 03 20 06 C2 58 00 A7 88 00 00 02 00  00 80 00 00 "

  def handleGamePkt(pkt : PlanetSideGamePacket) = pkt match {
    case ConnectToWorldRequestMessage(server, token, majorVersion, minorVersion, revision, buildDate, unk) =>

      val clientVersion = s"Client Version: ${majorVersion}.${minorVersion}.${revision}, ${buildDate}"

      log.info(s"New world login to ${server} with Token:${token}. ${clientVersion}")

      // ObjectCreateMessage
      sendRawResponse(objectHex)
      // XXX: hard coded message
      sendRawResponse(hex"14 0F 00 00 00 10 27 00  00 C1 D8 7A 02 4B 00 26 5C B0 80 00 ")

      // NOTE: PlanetSideZoneID just chooses the background
      sendResponse(PacketCoding.CreateGamePacket(0,
        CharacterInfoMessage(PlanetSideZoneID(1), 0, PlanetSideGUID(0), true, 0)))
    case msg @ CharacterRequestMessage(charId, action) =>
      log.info("Handling " + msg)

      action match {
        case CharacterRequestAction.Delete =>
          sendResponse(PacketCoding.CreateGamePacket(0, ActionResultMessage(false, Some(1))))
        case CharacterRequestAction.Select =>
          PacketCoding.DecodeGamePacket(objectHex).require match {
            case obj @ ObjectCreateMessage(len, cls, guid, _, _) =>
              log.debug("Object: " + obj)
              // LoadMapMessage 13714 in mossy .gcap
              // XXX: hardcoded shit
              sendRawResponse(hex"31 85 6D 61 70 31 33 85  68 6F 6D 65 33 A4 9C 19 00 00 00 AE 30 5E 70 00  ")
              sendRawResponse(objectHex)

              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(13), PlanetSideEmpire.VS))) // "The VS have captured the VS Sanctuary."
              sendResponse(PacketCoding.CreateGamePacket(0, BroadcastWarpgateUpdateMessage(PlanetSideGUID(13), PlanetSideGUID(1), 32))) // VS Sanctuary: Inactive Warpgate -> Broadcast Warpgate

              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(1), PlanetSideEmpire.NEUTRAL))) // "Solsar"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(2), PlanetSideEmpire.NEUTRAL))) // "Hossin"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(3), PlanetSideEmpire.NEUTRAL))) // "Cyssor"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(4), PlanetSideEmpire.NEUTRAL))) // "Ishundar"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(5), PlanetSideEmpire.NEUTRAL))) // "Forseral"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(6), PlanetSideEmpire.NEUTRAL))) // "Ceryshen"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(7), PlanetSideEmpire.NEUTRAL))) // "Esamir"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(8), PlanetSideEmpire.NEUTRAL))) // "Oshur"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(9), PlanetSideEmpire.NEUTRAL))) // "Searhus"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(10), PlanetSideEmpire.NEUTRAL))) // "Amerish"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(11), PlanetSideEmpire.NC))) // "NC sanctuary"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(12), PlanetSideEmpire.TR))) // "TR sanctuary"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(13), PlanetSideEmpire.VS))) // "VS sanctuary"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(14), PlanetSideEmpire.NC))) // "VR Shooting range"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(15), PlanetSideEmpire.TR))) // "VR Shooting range"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(16), PlanetSideEmpire.VS))) // "VR Shooting range"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(20), PlanetSideEmpire.NC))) // "VR Vehicule Training Area"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(21), PlanetSideEmpire.TR))) // "VR Vehicule Training Area"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(22), PlanetSideEmpire.VS))) // "VR Vehicule Training Area"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(23), PlanetSideEmpire.NEUTRAL))) // "Supai"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(24), PlanetSideEmpire.NEUTRAL))) // "Hunhau"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(25), PlanetSideEmpire.NEUTRAL))) // "Adlivun"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(26), PlanetSideEmpire.NEUTRAL))) // "Byblos"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(27), PlanetSideEmpire.NEUTRAL))) // "Annwn"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(28), PlanetSideEmpire.NEUTRAL))) // "Drugaskan"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(29), PlanetSideEmpire.NEUTRAL))) // "Extinction"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(30), PlanetSideEmpire.NEUTRAL))) // "Ascension"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(31), PlanetSideEmpire.NEUTRAL))) // "Desolation"
              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(32), PlanetSideEmpire.NEUTRAL))) // "Nexus"

              println(sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(1), PlanetSideEmpire.NEUTRAL))))


              // BuildingInfoUpdateMessage Decryptage
              //sendRawResponse(hex"A0 02 00 34 00 06 00 00  00 00 80 00 00 00 10 00  00 00 00 00 00 40 ")
              //sendRawResponse(hex"a0 17 00 bf 27 06 00 00  00 01 00 00 00 00 10 00  02 54 e0 70 10 67 4e 07 01 06 00 00 00 00 40 ") // Supai Module Building South _ VS
              //sendRawResponse(hex"a0 18 00 24 27 06 00 00  00 00 00 00 00 00 10 00  02 54 e0 70 10 67 4e 07 01 06 00 00 00 00 40 ") // Hunhau Module Building North _ TR
              //sendRawResponse(hex"a0 1b 00 13 27 06 00 00  00 00 80 00 000010000254e07010674e0701060000000040 ") // Annwn Module Building South East _ NC

              //sendRawResponse(hex"A0 02 00 34 00 06 00 00  00 00 00 00 00 00 10 00  00 00 00 00 00 40 ")
              //sendRawResponse(hex"a01700102706000000010000000010000254e07010674e0701060000000040 ") // Supai Module Building North _ VS
              //sendRawResponse(hex"a01700bf2706000000010000000010000254e07010674e0701060000000040 ") // Supai Module Building South _ VS
              //sendRawResponse(hex"a01800242706000000000000000010000254e07010674e0701060000000040 ") // Hunhau Module Building North _ TR
              //sendRawResponse(hex"a01800242706000000000000000080000254e07010674e0701060000000040 ") // Hunhau Module Building North _ TR _ Spawn down (modified)
              //sendRawResponse(hex"a01800722806000000000000000010000254e07010674e0701060000000040 ") // Hunhau Module Building South _ TR
              //sendRawResponse(hex"a01900152706000000000000000010000254e07010674e0701060000000040 ") // Adlivun Module Building South West _ TR
              //sendRawResponse(hex"a01900bd2706000000000000000010000254e07010674e0701060000000040 ") // Adlivun Module Building North East _ TR
              //sendRawResponse(hex"a01a00e72706000000010000000010000254e07010674e0701060000000040 ") // Byblos Module Building North _ VS
              //sendRawResponse(hex"a01a002d2806000000010000000010000254e07010674e0701060000000040 ") // Byblos Module Building South _ VS
              //sendRawResponse(hex"a01b00132706000000008000000010000254e07010674e0701060000000040 ") // Annwn Module Building South East _ NC
              //sendRawResponse(hex"a01b001c2706000000008000000010000254e07010674e0701060000000040 ") // Annwn Module Building North West _ NC
              //sendRawResponse(hex"a01c00112706000000000000000010000254e07010674e0701060000000040 ") // Drugaskan Module Building South _ TR
              //sendRawResponse(hex"a01c00122706000000000000000010000254e07010674e0701060000000040 ") // Drugaskan Module Building North _ TR
              //sendRawResponse(hex"a0170010270600000001000000001000025de0702067de0702060000000040 ") // Supai Module Building North _ VS (???)
              //sendRawResponse(hex"a01700bf270600000001000000001000025de0702067de0702060000000040 ") //
              //sendRawResponse(hex"a0180024270600000000000000001000025de0702067de0702060000000040 ") //
              //sendRawResponse(hex"a0180072280600000000000000001000025de0702067de0702060000000040 ") //
              //sendRawResponse(hex"a0190015270600000000000000001000025de0702067de0702060000000040 ") //
              //sendRawResponse(hex"a01900bd270600000000000000001000025de0702067de0702060000000040 ") //
              //sendRawResponse(hex"a01a00e7270600000001000000001000025de0702067de0702060000000040 ") //
              //sendRawResponse(hex"a01a002d280600000001000000001000025de0702067de0702060000000040 ") //
              //sendRawResponse(hex"a01b0013270600000000800000001000025de0702067de0702060000000040 ") //
              //sendRawResponse(hex"a01b001c270600000000800000001000025de0702067de0702060000000040 ") //
              //sendRawResponse(hex"a01c0011270600000000000000001000025de0702067de0702060000000040 ") //
              //sendRawResponse(hex"a01c0012270600000000000000001000025de0702067de0702060000000040 ") //
              //sendRawResponse(hex"a0 04 00 09 00 66 00 00 00 00 80 00 00 00 14 00 00 00 00 00 00 46 ") // Ishundar _ Girru _ NC _ 60%

              sendRawResponse(hex"a0040001000600000001800000001000000000000040")
              sendRawResponse(hex"a0040002000600000001800000001000000000000040")
              sendRawResponse(hex"a0040003000600000001800000001000000000000040")
              sendRawResponse(hex"a0040004000600000001800000001000000000000040")
              //sendRawResponse(hex"a004000500a6000000000000000017c0000000000040") // Akkan - TR - 100%
              //sendRawResponse(hex"a00400060086000000000000000017c0000000000040") // Baal - TR - 80%
              //sendRawResponse(hex"a00400070096000000000000000017c0000000000040") // Dagon - TR - 90%
              //sendRawResponse(hex"a00400080096000000000000000017c0000000000040") // Enkidu - TR - 90%
              sendRawResponse(hex"a0040009001600000000800000001700000000000040") // Girru - NC - 10%
              //sendRawResponse(hex"a004000a009600000000800000001700000000000040") // Hannish - NC - 90%
              //sendRawResponse(hex"a004000b005600000000800000001700000000000040") // Irkalla - NC - 50%
              //sendRawResponse(hex"a004000c009cf47c0a00000000001000000000000040") // Kusag - TR - 90% - Hack by VS (around 5min40 left)
              //sendRawResponse(hex"a004000d004c215a0c01800000002000000000000040") // Lahar - Neutral - 40% - Hack by VS (around 7min20 left) - Gen crit - Spawn down
              //sendRawResponse(hex"a004000e005600000000800000001400000000000040") // Marduk - NC - 50%
              //sendRawResponse(hex"a004000f0096000000000000000017c0000000000040") // Neti - TR - 90%
              //sendRawResponse(hex"a004001000a6000000000000000017c0000000000040") // Zaqar - TR - 100%
              //sendRawResponse(hex"a0040011000600000000800000001000000000000040") // S Marduk gun tower - NC
              //sendRawResponse(hex"a0040012000600000000000000001000000000000040") // NE Neti gun tower - TR
              //sendRawResponse(hex"a0040013000600000001000000001000000000000040") // SW Zaqar Gun tower - VS
              //sendRawResponse(hex"a0040014000600000000000000001000000000000040") // E Zaqar Gun tower - TR
//              sendRawResponse(hex"a0040015000600000000000000001000000000000040") // N Neti Watch tower - TR
//              sendRawResponse(hex"a0040016000600000000000000001000000000000040") // Gate outpost watch tower (S of Forseral WG) - TR
//              sendRawResponse(hex"a0040017000600000001000000001000000000000040") // S kusag gun tower - VS
//              sendRawResponse(hex"a0040018000600000000000000001000000000000040") // NW kusag watch tower - TR
//              sendRawResponse(hex"a0040019000600000000000000001000000000000040") // Gate outpost watch tower (N of Forseral WG) - TR
//              sendRawResponse(hex"a004001a000600000001000000001000000000000040") // Gate outpost gun tower (E of Forseral WG) - VS
//              sendRawResponse(hex"a004001b000600000000800000001000000000000040") // Central outpost watch tower - NC
//              sendRawResponse(hex"a004001c000600000000800000001000000000000040") // E Akkan watch tower - NC
//              sendRawResponse(hex"a004001d000600000000000000001000000000000040") // N enkidu gun tower - TR
//              sendRawResponse(hex"a004001e000600000000800000001000000000000040") // SW enkidu air tower - NC
//              sendRawResponse(hex"a004001f000600000000800000001000000000000040") // Gate outpost watch tower (S of Hossin WG) - NC
//              sendRawResponse(hex"a0040020000600000000000000001000000000000040") // a tower !
//              sendRawResponse(hex"a0040021000600000000800000001000000000000040") // a tower !
//              sendRawResponse(hex"a0040022000600000000800000001000000000000040") // a tower !
//              sendRawResponse(hex"a0040023000600000000000000001000000000000040") // a tower !
//              sendRawResponse(hex"a0040024000600000000800000001000000000000040") // a tower !
//              sendRawResponse(hex"a0040025000600000000000000001000000000000040") // a tower !
//              sendRawResponse(hex"a0040026000600000000000000001000000000000040") // a tower !
//              sendRawResponse(hex"a0040027000600000000000000001000000000000040") // a tower !
//              sendRawResponse(hex"a0040028000600000000000000001000000000000040") // a tower !
//              sendRawResponse(hex"a0040029000600000000000000001000000000000040") // a tower !
//              sendRawResponse(hex"a004002a000600000001800000000000000000000040") // dont know what is it
//              sendRawResponse(hex"a004002b000600000001800000000000000000000040") // dont know what is it
//              sendRawResponse(hex"a004002c000600000001800000000000000000000040") // dont know what is it
//              sendRawResponse(hex"a004002d000600000001800000000000000000000040") // dont know what is it
//              sendRawResponse(hex"a004002e000600000001800000000000000000000040") // dont know what is it
//              sendRawResponse(hex"a004002f000600000001800000000000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040030000600000001800000000000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040031000600000001800000000000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040032000600000001800000000000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040033000600000001800000000000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040034000600000001800000000000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040035000600000001800000000000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040036000600000001800000000000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040037000600000000800000001000000000000040") // a tower !
//              sendRawResponse(hex"a0040038000600000000800000001000000000000040") // a tower !
//              sendRawResponse(hex"a0040039000600000000000000001000000000000040") // a tower !
//              sendRawResponse(hex"a004003a000600000000000000001000000000000040") // a tower !
//              sendRawResponse(hex"a004003b000600000000800000001000000000000040") // a tower !
//              sendRawResponse(hex"a004003c000600000000000000001000000000000040") // a tower !
//              sendRawResponse(hex"a004003d000600000000800000001000000000000040") // a tower !
//              sendRawResponse(hex"a004003e000600000000800000001000000000000040") // a tower !
//              sendRawResponse(hex"a004003f000600000000000000001000000000000040") // a tower !
//              sendRawResponse(hex"a0040040000600000001800000000000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040041000600000000000000001000000000000040") // a tower !
//              sendRawResponse(hex"a0040042000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040043000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040044000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040045000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040046000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040047000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040048000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040049000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a004004a000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a004004b000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a004004c000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a004004d000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a004004e000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040075000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040076000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040077000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040078000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040079000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a004007a000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a004007b000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a004007c000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a004007d000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a004008a000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a004008b000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a004008c000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a004008d000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a004008e000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a004008f000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040090000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040091000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040092000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040093000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040094000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040095000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040096000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400ce000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400cf000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400d0000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400d1000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400d2000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400d3000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400d4000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400d5000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400d6000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400d7000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400f7000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400f8000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400f9000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400fa000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400fb000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400fc000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400fd000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400fe000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400ff000600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040000010600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040001010600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040002010600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040034010600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040035010600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040036010600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040037010600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040038010600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a0040039010600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400fc670600000001800000001000000000000040") // dont know what is it
//              sendRawResponse(hex"a00400fd670600000001800000001000000000000040") // dont know what is it




              sendResponse(PacketCoding.CreateGamePacket(0, SetCurrentAvatarMessage(PlanetSideGUID(guid),0,0)))

              import scala.concurrent.duration._
              import scala.concurrent.ExecutionContext.Implicits.global
              clientKeepAlive = context.system.scheduler.schedule(0 seconds, 500 milliseconds, self, PokeClient())
          }
        case default =>
          log.error("Unsupported " + default + " in " + msg)
      }
    case msg @ CharacterCreateRequestMessage(name, head, voice, gender, empire) =>
      log.info("Handling " + msg)

      sendResponse(PacketCoding.CreateGamePacket(0, ActionResultMessage(true, None)))
      sendResponse(PacketCoding.CreateGamePacket(0,
        CharacterInfoMessage(PlanetSideZoneID(0), 0, PlanetSideGUID(0), true, 0)))

    case KeepAliveMessage(code) =>
      sendResponse(PacketCoding.CreateGamePacket(0, KeepAliveMessage(0)))

    case msg @ PlayerStateMessageUpstream(avatar_guid, pos, vel, unk1, aim_pitch, unk2, seq_time, unk3, is_crouching, unk4, unk5, unk6, unk7, unk8) =>
      //log.info("PlayerState: " + msg)

    case msg @ ChatMsg(messagetype, has_wide_contents, recipient, contents, note_contents) =>
      // TODO: Prevents log spam, but should be handled correctly
      if (messagetype != ChatMessageType.CMT_TOGGLE_GM) {
        log.info("Chat: " + msg)
      }
      if (messagetype == ChatMessageType.CMT_TELL && recipient == "bot1") {
        //sendRawResponse(hex"a004000900660000000080000000" ++ ByteVector.fromValidHex(contents) ++ hex"00000000000046")
        //sendRawResponse(hex"a0 0400 0900 16 000000 0080 000000 " ++ ByteVector.fromValidHex(contents) ++ hex"00 00 00 00 00 40")
        sendRawResponse(hex"a0 0400 0900 16 000000 0080 000000 " ++ ByteVector.fromValidHex(contents) ++ hex" 00 00 00 00 40")
      }
      //if (messagetype == ChatMessageType.CMT_TELL && recipient == "bot" && contents == "01") {sendRawResponse(hex"a004002a000600000000800000000000000000000040")}

      if (messagetype == ChatMessageType.CMT_TELL && recipient == "bot" && contents == "TR") {
        sendRawResponse(hex"A0 02 00 34 00 06 00 00  00 00 00 00 00 00 10 00  00 00 00 00 00 40 ")
        sendRawResponse(hex"a0 04 00 09 00 66 00 00  00 00 00 00 00 00 20 00  00 00 00 00 00 46 ") // Ishundar _ Girru _ TR _ 60%
        sendRawResponse(hex"a0 04 00 10 00 66 00 00  00 00 00 00 00 00 20 00  00 00 00 00 00 46 ") // Ishundar _ Zaquar _ TR _ 60% test
        sendRawResponse(hex"a0 04 00 06 00 66 00 00  00 00 80 00 00 00 20 00  00 00 00 00 00 46 ") // Ishundar _ Baal _ NC _ 60% test
        sendRawResponse(hex"a0 04 00 05 00 a0 00 00  00 00 00 00 00 00 20 00  00 00 00 00 00 46 ") // Ishundar _  Akkan _ TR _ 100% test
        sendRawResponse(hex"a0 02 00 09 00 66 00 00  00 00 00 00 00 00 14 00  00 00 00 00 00 46 ") // Hossin _ Bitol _ TR _ 60%
        sendRawResponse(hex"a0 18 00 24 27 06 00 00  00 00 00 00 00 00 10 00  02 54 e0 70 10 67 4e 07 01 06 00 00 00 00 40 ") // Hunhau Module Building North _ TR
        sendRawResponse(hex"a0 18 00 72 28 06 00 00  00 00 00 00 00 00 80 00  02 54 e0 70 10 67 4e 07 01 06 00 00 00 00 40 ") // Hunhau Module Building South _ TR _ Spawn down (modified)
        sendResponse(PacketCoding.CreateGamePacket(0, ChatMsg(ChatMessageType.CMT_GMBROADCASTWORLD, has_wide_contents, "The Bot", "Tower is now TR", note_contents)))
      }
      if (messagetype == ChatMessageType.CMT_TELL && recipient == "bot" && contents == "NC") {
        sendRawResponse(hex"A0 02 00 34 00 06 00 00  00 00 80 00 00 00 10 00  00 00 00 00 00 40 ")
        sendRawResponse(hex"a0 04 00 09 00 66 00 00  00 00 80 00 00 00 14 00  00 00 00 00 00 46 ") // Ishundar _ Girru _ NC _ 60%
        sendRawResponse(hex"a0 02 00 09 00 66 00 00  00 00 80 00 00 00 14 00  00 00 00 00 00 46 ") // Hossin _ Bitol _ NC _ 60%
        sendResponse(PacketCoding.CreateGamePacket(0, ChatMsg(ChatMessageType.CMT_GMBROADCASTWORLD, has_wide_contents, "The Bot", "Tower is now NC", note_contents)))
      }
      if (messagetype == ChatMessageType.CMT_TELL && recipient == "bot" && contents == "VS") {
        sendRawResponse(hex"A0 02 00 34 00 06 00 00  00 01 00 00 00 00 10 00  00 00 00 00 00 40 ")
        sendRawResponse(hex"a0 04 00 09 00 66 00 00  00 01 00 00 00 00 15 00  00 00 00 00 00 46 ") // Ishundar _ Girru _ VS _ 60%
        sendResponse(PacketCoding.CreateGamePacket(0, ChatMsg(ChatMessageType.CMT_GMBROADCASTWORLD, has_wide_contents, "The Bot", "Tower is now VS", note_contents)))
      }

      // TODO: handle this appropriately
      if(messagetype == ChatMessageType.CMT_QUIT) {
        sendResponse(DropCryptoSession())
        sendResponse(DropSession(sessionId, "user quit"))
      }

      // TODO: Depending on messagetype, may need to prepend sender's name to contents with proper spacing
      // TODO: Just replays the packet straight back to sender; actually needs to be routed to recipients!
      if (messagetype != ChatMessageType.CMT_TELL && recipient != "bot") {
        sendResponse(PacketCoding.CreateGamePacket(0, ChatMsg(messagetype, has_wide_contents, recipient, contents, note_contents)))
      }
    case msg @ ChangeFireModeMessage(item_guid, fire_mode) =>
      log.info("ChangeFireMode: " + msg)

    case msg @ ChangeFireStateMessage_Start(item_guid) =>
      log.info("ChangeFireState_Start: " + msg)

    case msg @ ChangeFireStateMessage_Stop(item_guid) =>
      log.info("ChangeFireState_Stop: " + msg)

    case msg @ EmoteMsg(avatar_guid, emote) =>
      log.info("Emote: " + msg)
      sendResponse(PacketCoding.CreateGamePacket(0, EmoteMsg(avatar_guid, emote)))

    case msg @ DropItemMessage(item_guid) =>
      log.info("DropItem: " + msg)

    case msg @ ReloadMessage(item_guid, ammo_clip, unk1) =>
      log.info("Reload: " + msg)
      sendResponse(PacketCoding.CreateGamePacket(0, ReloadMessage(item_guid, 123, unk1)))

    case msg @ ObjectHeldMessage(avatar_guid, held_holsters, unk1) =>
      log.info("ObjectHeld: " + msg)

    case msg @ AvatarJumpMessage(state) =>
      //log.info("AvatarJump: " + msg)

    case msg @ RequestDestroyMessage(object_guid) =>
      log.info("RequestDestroy: " + msg)
      // TODO: Make sure this is the correct response in all cases
      sendResponse(PacketCoding.CreateGamePacket(0, ObjectDeleteMessage(object_guid, 0)))

    case msg @ ObjectDeleteMessage(object_guid, unk1) =>
      log.info("ObjectDelete: " + msg)

    case msg @ MoveItemMessage(item_guid, avatar_guid_1, avatar_guid_2, dest, unk1) =>
      log.info("MoveItem: " + msg)

    case msg @ ChangeAmmoMessage(item_guid, unk1) =>
      log.info("ChangeAmmo: " + msg)

    case msg @ UseItemMessage(avatar_guid, unk1, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, unk9) =>
      log.info("UseItem: " + msg)
      // TODO: Not all fields in the response are identical to source in real packet logs (but seems to be ok)
      // TODO: Not all incoming UseItemMessage's respond with another UseItemMessage (i.e. doors only send out GenericObjectStateMsg)
      sendResponse(PacketCoding.CreateGamePacket(0, UseItemMessage(avatar_guid, unk1, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, unk9)))
      // TODO: This should only actually be sent to doors upon opening; may break non-door items upon use
      sendResponse(PacketCoding.CreateGamePacket(0, GenericObjectStateMsg(object_guid, 16)))

    case msg @ GenericObjectStateMsg(object_guid, unk1) =>
      log.info("GenericObjectState: " + msg)

    case msg @ ItemTransactionMessage(terminal_guid, transaction_type, item_page, item_name, unk1, item_guid) =>
      log.info("ItemTransaction: " + msg)

    case msg @ WeaponDelayFireMessage(seq_time, weapon_guid) =>
      log.info("WeaponDelayFire: " + msg)

    case msg @ WeaponFireMessage(seq_time, weapon_guid, projectile_guid, shot_origin, unk1, unk2, unk3, unk4, unk5, unk6, unk7) =>
      log.info("WeaponFire: " + msg)

    case msg @ HitMessage(seq_time, projectile_guid, unk1, hit_info, unk2, unk3, unk4) =>
      log.info("Hit: " + msg)

    case default => log.debug(s"Unhandled GamePacket ${pkt}")
  }

  def failWithError(error : String) = {
    log.error(error)
    //sendResponse(PacketCoding.CreateControlPacket(ConnectionClose()))
  }

  def sendResponse(cont : PlanetSidePacketContainer) : Unit = {
    log.trace("WORLD SEND: " + cont)
    sendResponse(cont.asInstanceOf[Any])
  }

  def sendResponse(msg : Any) : Unit = {
    MDC("sessionId") = sessionId.toString
    rightRef !> msg
  }

  def sendRawResponse(pkt : ByteVector) = {
    log.trace("WORLD SEND RAW: " + pkt)
    sendResponse(RawPacket(pkt))
  }
}
