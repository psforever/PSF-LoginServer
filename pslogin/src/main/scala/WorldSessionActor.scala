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

class WorldSessionActor extends Actor with MDCContextAware {
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
              sendResponse(PacketCoding.CreateGamePacket(0, LoadMapMessage("map13","home3",40100,25,true,3770441820L))) // home3 - map13 @home3=VS Sanctuary
              //sendResponse(PacketCoding.CreateGamePacket(0, LoadMapMessage("map04","z4",40100,25,true,3770441820L))) //Ishundar
              //sendResponse(PacketCoding.CreateGamePacket(0, LoadMapMessage("ugd1","c1",40100,25,true,3770441820L))) // supai
              //sendResponse(PacketCoding.CreateGamePacket(0, LoadMapMessage("map08","z8",40100,25,true,3770441820L))) // old oshur
              //sendResponse(PacketCoding.CreateGamePacket(0, LoadMapMessage("map15","tzdrvs",40100,25,true,3770441820L))) //tzdrvs - map15 @tzdrvs=VR Vehicle Training Area
              //sendResponse(PacketCoding.CreateGamePacket(0, LoadMapMessage("map16","tzcovs",40100,25,true,3770441820L))) // tzcovs - map16  Virtual Combat Zone
              sendRawResponse(objectHex)


              sendResponse(PacketCoding.CreateGamePacket(0,BuildingInfoUpdateMessage(
                PlanetSideGUID(6),   //Ceryshen
                PlanetSideGUID(2),   //Anguta
                8,                   //80% NTU
                true,                //Base hacked
                PlanetSideEmpire.NC, //Base hacked by NC
                600000,              //10 minutes remaining for hack
                PlanetSideEmpire.VS, //Base owned by VS
                0,                   //!! Field != 0 will cause malformed packet. See class def.
                PlanetSideGeneratorState.Critical, //Generator critical
                true,                //Respawn tubes destroyed
                true,                //Force dome active
                16,                  //Tech plant lattice benefit
                0,
                0,                   //!! Field > 0 will cause malformed packet. See class def.
                0,
                false,
                8,                   //!! Field != 8 will cause malformed packet. See class def.
                true,                //Boosted spawn room pain field
                true)))              //Boosted generator room pain field


              // These object_guids are specfic to VS Sanc
              for(toto <- 0 to 1024)
                sendResponse(PacketCoding.CreateGamePacket(0, SetEmpireMessage(PlanetSideGUID(toto), PlanetSideEmpire.VS)))
              //sendResponse(PacketCoding.CreateGamePacket(0, SetEmpireMessage(PlanetSideGUID(2), PlanetSideEmpire.VS))) //HART building C
              //sendResponse(PacketCoding.CreateGamePacket(0, SetEmpireMessage(PlanetSideGUID(29), PlanetSideEmpire.NC))) //South Villa Gun Tower

              sendResponse(PacketCoding.CreateGamePacket(0, ContinentalLockUpdateMessage(PlanetSideGUID(13), PlanetSideEmpire.VS))) // "The VS have captured the VS Sanctuary."
              sendResponse(PacketCoding.CreateGamePacket(0, BroadcastWarpgateUpdateMessage(PlanetSideGUID(13), PlanetSideGUID(1), 32))) // VS Sanctuary: Inactive Warpgate -> Broadcast Warpgate


              sendResponse(PacketCoding.CreateGamePacket(0, SetCurrentAvatarMessage(PlanetSideGUID(guid),0,0)))

              // Some data for oshur (modified packets from PSCap-2016-07-01_01-40-34-.gcap _ ishundar)
              sendRawResponse(hex"a0080001000600000001800000001000000000000040")
              sendRawResponse(hex"a0080002000600000001800000001000000000000040")
              sendRawResponse(hex"a0080003000600000001800000001000000000000040")
              sendRawResponse(hex"a0080004000600000001800000001000000000000040")
              sendRawResponse(hex"a008000500a6000000000000000017c0000000000040")
              sendRawResponse(hex"a00800060086000000000000000017c0000000000040")
              sendRawResponse(hex"a00800070096000000000000000017c0000000000040")
              sendRawResponse(hex"a00800080096000000000000000017c0000000000040")
              sendRawResponse(hex"a0080009001600000000800000001700000000000040")
              sendRawResponse(hex"a008000a009600000000800000001700000000000040")
              sendRawResponse(hex"a008000b005600000000800000001700000000000040")
              sendRawResponse(hex"a008000c009cf47c0a00000000001000000000000040")
              sendRawResponse(hex"a008000d004c215a0c01800000002000000000000040")
              sendRawResponse(hex"a008000e005600000000800000001400000000000040")
              sendRawResponse(hex"a008000f0096000000000000000017c0000000000040")
              sendRawResponse(hex"a008001000a6000000000000000017c0000000000040")
              sendRawResponse(hex"a0080011000600000000800000001000000000000040")
              sendRawResponse(hex"a0080012000600000000000000001000000000000040")
              sendRawResponse(hex"a0080013000600000001000000001000000000000040")
              sendRawResponse(hex"a0080014000600000000000000001000000000000040")
              sendRawResponse(hex"a0080015000600000000000000001000000000000040")
              sendRawResponse(hex"a0080016000600000000000000001000000000000040")
              sendRawResponse(hex"a0080017000600000001000000001000000000000040")
              sendRawResponse(hex"a0080018000600000000000000001000000000000040")
              sendRawResponse(hex"a0080019000600000000000000001000000000000040")
              sendRawResponse(hex"a008001a000600000001000000001000000000000040")
              sendRawResponse(hex"a008001b000600000000800000001000000000000040")
              sendRawResponse(hex"a008001c000600000000800000001000000000000040")
              sendRawResponse(hex"a008001d000600000000000000001000000000000040")
              sendRawResponse(hex"a008001e000600000000800000001000000000000040")
              sendRawResponse(hex"a008001f000600000000800000001000000000000040")
              sendRawResponse(hex"a0080020000600000000000000001000000000000040")
              sendRawResponse(hex"a0080021000600000000800000001000000000000040")
              sendRawResponse(hex"a0080022000600000000800000001000000000000040")
              sendRawResponse(hex"a0080023000600000000000000001000000000000040")
              sendRawResponse(hex"a0080024000600000000800000001000000000000040")
              sendRawResponse(hex"a0080025000600000000000000001000000000000040")
              sendRawResponse(hex"a0080026000600000000000000001000000000000040")
              sendRawResponse(hex"a0080027000600000000000000001000000000000040")
              sendRawResponse(hex"a0080028000600000000000000001000000000000040")
              sendRawResponse(hex"a0080029000600000000000000001000000000000040")
              sendRawResponse(hex"a008002a000600000001800000000000000000000040")
              sendRawResponse(hex"a008002b000600000001800000000000000000000040")
              sendRawResponse(hex"a008002c000600000001800000000000000000000040")
              sendRawResponse(hex"a008002d000600000001800000000000000000000040")
              sendRawResponse(hex"a008002e000600000001800000000000000000000040")
              sendRawResponse(hex"a008002f000600000001800000000000000000000040")
              sendRawResponse(hex"a0080030000600000001800000000000000000000040")
              sendRawResponse(hex"a0080031000600000001800000000000000000000040")
              sendRawResponse(hex"a0080032000600000001800000000000000000000040")
              sendRawResponse(hex"a0080033000600000001800000000000000000000040")
              sendRawResponse(hex"a0080034000600000001800000000000000000000040")
              sendRawResponse(hex"a0080035000600000001800000000000000000000040")
              sendRawResponse(hex"a0080036000600000001800000000000000000000040")
              sendRawResponse(hex"a0080037000600000000800000001000000000000040")
              sendRawResponse(hex"a0080038000600000000800000001000000000000040")
              sendRawResponse(hex"a0080039000600000000000000001000000000000040")
              sendRawResponse(hex"a008003a000600000000000000001000000000000040")
              sendRawResponse(hex"a008003b000600000000800000001000000000000040")
              sendRawResponse(hex"a008003c000600000000000000001000000000000040")
              sendRawResponse(hex"a008003d000600000000800000001000000000000040")
              sendRawResponse(hex"a008003e000600000000800000001000000000000040")
              sendRawResponse(hex"a008003f000600000000000000001000000000000040")
              sendRawResponse(hex"a0080040000600000001800000000000000000000040")
              sendRawResponse(hex"a0080041000600000000000000001000000000000040")
              sendRawResponse(hex"a0080042000600000001800000001000000000000040")
              sendRawResponse(hex"a0080043000600000001800000001000000000000040")
              sendRawResponse(hex"a0080044000600000001800000001000000000000040")
              sendRawResponse(hex"a0080045000600000001800000001000000000000040")
              sendRawResponse(hex"a0080046000600000001800000001000000000000040")
              sendRawResponse(hex"a0080047000600000001800000001000000000000040")
              sendRawResponse(hex"a0080048000600000001800000001000000000000040")
              sendRawResponse(hex"a0080049000600000001800000001000000000000040")
              sendRawResponse(hex"a008004a000600000001800000001000000000000040")
              sendRawResponse(hex"a008004b000600000001800000001000000000000040")
              sendRawResponse(hex"a008004c000600000001800000001000000000000040")
              sendRawResponse(hex"a008004d000600000001800000001000000000000040")
              sendRawResponse(hex"a008004e000600000001800000001000000000000040")
              sendRawResponse(hex"a0080075000600000001800000001000000000000040")
              sendRawResponse(hex"a0080076000600000001800000001000000000000040")
              sendRawResponse(hex"a0080077000600000001800000001000000000000040")
              sendRawResponse(hex"a0080078000600000001800000001000000000000040")
              sendRawResponse(hex"a0080079000600000001800000001000000000000040")
              sendRawResponse(hex"a008007a000600000001800000001000000000000040")
              sendRawResponse(hex"a008007b000600000001800000001000000000000040")
              sendRawResponse(hex"a008007c000600000001800000001000000000000040")
              sendRawResponse(hex"a008007d000600000001800000001000000000000040")
              sendRawResponse(hex"a008008a000600000001800000001000000000000040")
              sendRawResponse(hex"a008008b000600000001800000001000000000000040")
              sendRawResponse(hex"a008008c000600000001800000001000000000000040")
              sendRawResponse(hex"a008008d000600000001800000001000000000000040")
              sendRawResponse(hex"a008008e000600000001800000001000000000000040")
              sendRawResponse(hex"a008008f000600000001800000001000000000000040")
              sendRawResponse(hex"a0080090000600000001800000001000000000000040")
              sendRawResponse(hex"a0080091000600000001800000001000000000000040")
              sendRawResponse(hex"a0080092000600000001800000001000000000000040")
              sendRawResponse(hex"a0080093000600000001800000001000000000000040")
              sendRawResponse(hex"a0080094000600000001800000001000000000000040")
              sendRawResponse(hex"a0080095000600000001800000001000000000000040")
              sendRawResponse(hex"a0080096000600000001800000001000000000000040")
              sendRawResponse(hex"a00800ce000600000001800000001000000000000040")
              sendRawResponse(hex"a00800cf000600000001800000001000000000000040")
              sendRawResponse(hex"a00800d0000600000001800000001000000000000040")
              sendRawResponse(hex"a00800d1000600000001800000001000000000000040")
              sendRawResponse(hex"a00800d2000600000001800000001000000000000040")
              sendRawResponse(hex"a00800d3000600000001800000001000000000000040")
              sendRawResponse(hex"a00800d4000600000001800000001000000000000040")
              sendRawResponse(hex"a00800d5000600000001800000001000000000000040")
              sendRawResponse(hex"a00800d6000600000001800000001000000000000040")
              sendRawResponse(hex"a00800d7000600000001800000001000000000000040")
              sendRawResponse(hex"a00800f7000600000001800000001000000000000040")
              sendRawResponse(hex"a00800f8000600000001800000001000000000000040")
              sendRawResponse(hex"a00800f9000600000001800000001000000000000040")
              sendRawResponse(hex"a00800fa000600000001800000001000000000000040")
              sendRawResponse(hex"a00800fb000600000001800000001000000000000040")
              sendRawResponse(hex"a00800fc000600000001800000001000000000000040")
              sendRawResponse(hex"a00800fd000600000001800000001000000000000040")
              sendRawResponse(hex"a00800fe000600000001800000001000000000000040")
              sendRawResponse(hex"a00800ff000600000001800000001000000000000040")
              sendRawResponse(hex"a0080000010600000001800000001000000000000040")
              sendRawResponse(hex"a0080001010600000001800000001000000000000040")
              sendRawResponse(hex"a0080002010600000001800000001000000000000040")
              sendRawResponse(hex"a0080034010600000001800000001000000000000040")
              sendRawResponse(hex"a0080035010600000001800000001000000000000040")
              sendRawResponse(hex"a0080036010600000001800000001000000000000040")
              sendRawResponse(hex"a0080037010600000001800000001000000000000040")
              sendRawResponse(hex"a0080038010600000001800000001000000000000040")
              sendRawResponse(hex"a0080039010600000001800000001000000000000040")
              sendRawResponse(hex"a00800fc670600000001800000001000000000000040")
              sendRawResponse(hex"a00800fd670600000001800000001000000000000040")


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
        sendRawResponse(hex"a0 0700  " ++ ByteVector.fromValidHex(contents) ++ hex" 16 000000 0080 000000 1000 00 00 00 00 00 40")
        //sendRawResponse(hex"a0 0700 1d01                                            06 000000 0180 000000 1000 00 00 00 00 00 40")
      }
      if (messagetype == ChatMessageType.CMT_TELL && recipient == "bot" && contents == "00") {
        sendRawResponse(hex"a0 0400 0500 a6 000000 0080 000000 17c0 00 00 00 00 00 40") // Akkan - TR - 100%
      }
      if (messagetype == ChatMessageType.CMT_TELL && recipient == "bot" && contents == "01") {
        sendRawResponse(hex"a0 0400 0500 ac 05000a 0000 000000 0000 00 00 00 00 00 1c 00")
      }
      if (messagetype == ChatMessageType.CMT_TELL && recipient == "bot" && contents == "02") {
        sendRawResponse(hex"a004000500890618080080000000100000000000001c00")
      }
      if (messagetype == ChatMessageType.CMT_TELL && recipient == "bot" && contents == "03") {
        sendRawResponse(hex"a00400050089bda2060080000000000000000000001c00")
      }
      if (messagetype == ChatMessageType.CMT_TELL && recipient == "bot" && contents == "04") {
        sendRawResponse(hex"a004000500880e1e060080000000100000000000001c00")
      }
      if (messagetype == ChatMessageType.CMT_TELL && recipient == "bot" && contents == "05") {
        sendRawResponse(hex"a0030005000600000001800000002000000000000040")
      }
      if (messagetype == ChatMessageType.CMT_TELL && recipient == "bot" && contents == "06") {
        sendRawResponse(hex"a00400050088e096040080000000000000000000001c00")
      }
      if (messagetype == ChatMessageType.CMT_TELL && recipient == "bot" && contents == "07") {
        sendRawResponse(hex"a00400050089346e040080000000000000000000001c00")
      }


      // TODO: handle this appropriately
      if(messagetype == ChatMessageType.CMT_QUIT) {
        sendResponse(DropCryptoSession())
        sendResponse(DropSession(sessionId, "user quit"))
      }

      // TODO: Depending on messagetype, may need to prepend sender's name to contents with proper spacing
      // TODO: Just replays the packet straight back to sender; actually needs to be routed to recipients!
      sendResponse(PacketCoding.CreateGamePacket(0, ChatMsg(messagetype, has_wide_contents, recipient, contents, note_contents)))

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

    case msg @ AvatarFirstTimeEventMessage(avatar_guid, object_guid, unk1, event_name) =>
      log.info("AvatarFirstTimeEvent: " + msg)

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
