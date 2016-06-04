// Copyright (c) 2016 PSForever.net to present
import java.net.{InetAddress, InetSocketAddress}

import akka.actor.{Actor, ActorRef, MDCContextAware}
import net.psforever.packet.{PlanetSideGamePacket, _}
import net.psforever.packet.control._
import net.psforever.packet.game._
import scodec.Attempt.{Failure, Successful}
import scodec.bits._

class WorldSessionActor extends Actor with MDCContextAware {
  private[this] val log = org.log4s.getLogger

  private case class UpdateServerList()

  var leftRef : ActorRef = ActorRef.noSender
  var rightRef : ActorRef = ActorRef.noSender

  def receive = Initializing

  def Initializing : Receive = {
    case HelloFriend(right) =>
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

  def handleGamePkt(pkt : PlanetSideGamePacket) = pkt match {
    case ConnectToWorldRequestMessage(server, token, majorVersion, minorVersion, revision, buildDate, unk) =>

      val clientVersion = s"Client Version: ${majorVersion}.${minorVersion}.${revision}, ${buildDate}"

      log.info(s"New world login to ${server} with Token:${token}. ${clientVersion}")

      // testing :P
      sendRawResponse(hex"00 09 00 00 00 19 08 2C  00 00 4B 00 00 00 00 02 1F 80 09 9B 05 00 00 00  0D 77 BC F1 05 12 2E 40 00 80 3F D7 04 00 00 00  61 C0 6C 6F 63 6B 2D 7A 33 61 C0 6C 6F 63 6B 2D  7A 34 61 C0 6C 6F 63 6B 2D 7A 39 64 00 6C 6F 63  6B 2D 69 31 2D 69 32 2D 69 33 2D 69 34 04 00 00  00 40 40 10 30 04 10 01 06 00 ")
      sendRawResponse(hex"00 09 00 01 00 19 08 2C  00 00 70 01 00 00 00 6A 95 01 00 00 00 00 79 94  FD BF 00 A1 AF BF F3 A5 D0 3E 26 39 76 3B 08 00  00 00 36 AE 11 3F 70 5D 9B 3E 2E 15 9A 9B 3A 3F  CC 90 DB 3E 45 1C EC 0F 14 3D AF CF 36 3F 06 32  BA AF 13 3F 18 4C 12 3F 26 2F D2 2D 71 3F 94 AA  FB 3E 4D 16 5D 1B 1A 3F 0C 25 D5 3C 55 6B 38 89  63 3F 5D F5 25 3F 3C AC 8E 5E E6 3E 79 25 62 3F  10 32 1F 12 E3 40 00 9A 40 43 4D 54 5F 43 55 4C  4C 57 41 54 45 52 4D 41 52 4B 5F 73 75 63 63 65  73 73 ")
      sendRawResponse(hex"00 19 14 14 0F 00 00 00  07 00 00 00 A9 B2 79 02 01 00 28 77 A3 00 00 FF  01 65 18 1F 0B 00 00 BC 81 F0 0F 73 B9 46 03 76  F1 10 00 0E F2 40 00 09 20 57 00 6F 00 77 00 4E  00 69 00 63 00 65 00 61 00 73 00 65 00 66 00 61  00 73 00 64 00 66 00 61 00 73 00 64 00 82 64 80  B6 1E 80 80 00 00 00 00 00 3F FF C0 00 00 00 20  00 00 0F F0 17 03 FF FF FF FF FF FF FF FF FF FF  FF FF FF FF FF FC 00 00 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00 00 01 90 01 90 00 64 00  00 01 00 7E C8 00 C8 00 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00 00 00 01 C0 00 42 C5 46  86 C7 00 00 00 40 00 00 12 40 78 70 65 5F 73 61  6E 63 74 75 61 72 79 5F 68 65 6C 70 85 6D 61 70  31 31 00 00 00 00 00 00 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01  0A 26 C9 00 04 02 40 00 00 10 00 06 02 79 A2 10  08 0C 80 00 01 00 02 6B 22 00 82 48 00 00 02 00  00 C0 41 C0 46 01 01 90 00 00 64 00 D4 24 80 10  89 00 00 00 40 00 18 08 38 89 40 20 32 00 00 00  80 19 04 98 02 17 20 00 00 08 00 70 13 80 43 64  00 00 32 00 0E 02 80 08 9C 80 00 06 40 01 C0 52  01 19 90 00 00 C8 00 3A 0A 80 28 72 00 00 19 00  3C D1 58 05 26 40 00 01 00 06 C2 2C 00 A7 48 00  00 02 00 00 80 00 1C 14 14 0F 00 00 00 10 27 00  00 02 D8 7A 02 1F 00 73 D3 A6 00 00")
    case KeepAliveMessage(code) =>
      sendResponse(PacketCoding.CreateGamePacket(0, KeepAliveMessage(0)))
    case default => log.debug(s"Unhandled GamePacket ${pkt}")
  }

  def failWithError(error : String) = {
    log.error(error)
    //sendResponse(PacketCoding.CreateControlPacket(ConnectionClose()))
  }

  def sendResponse(cont : PlanetSidePacketContainer) = {
    log.trace("WORLD SEND: " + cont)
    rightRef ! cont
  }

  def sendRawResponse(pkt : ByteVector) = {
    log.trace("WORLD SEND RAW: " + pkt)
    rightRef ! RawPacket(pkt)
  }
}
