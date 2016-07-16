// Copyright (c) 2016 PSForever.net to present
import java.net.{InetAddress, InetSocketAddress}

import akka.actor.{Actor, ActorRef, MDCContextAware}
import net.psforever.packet.{PlanetSideGamePacket, _}
import net.psforever.packet.control._
import net.psforever.packet.game._
import scodec.Attempt.{Failure, Successful}
import scodec.bits._

import scala.util.Random

class LoginSessionActor extends Actor with MDCContextAware {
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
    case UpdateServerList() =>
      updateServerList
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
        log.trace(s"SYNC: ${sync}")

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
      case default =>
        log.debug(s"Unhandled ControlPacket $default")
    }
  }

  val serverName = "PSForever"
  val serverAddress = new InetSocketAddress(InetAddress.getLocalHost.getHostAddress, 51001)

  def handleGamePkt(pkt : PlanetSideGamePacket) = pkt match {
      case LoginMessage(majorVersion, minorVersion, buildDate, username,
        password, token, revision) =>

        val clientVersion = s"Client Version: ${majorVersion}.${minorVersion}.${revision}, ${buildDate}"

        if(token.isDefined)
          log.info(s"New login UN:$username Token:${token.get}. ${clientVersion}")
        else
          log.info(s"New login UN:$username PW:$password. ${clientVersion}")

        val newToken = token.getOrElse("THISISMYTOKENYES")
        val response = LoginRespMessage(newToken, hex"00000000 18FABE0C 00000000 00000000",
          0, 1, 2, 685276011, username, 0, false)

        sendResponse(PacketCoding.CreateGamePacket(0, response))
        updateServerList
      case ConnectToWorldRequestMessage(name, _, _, _, _, _, _) =>
        log.info(s"Connect to world request for '${name}'")

        val response = ConnectToWorldMessage(serverName, serverAddress.getHostString, serverAddress.getPort)
        sendResponse(PacketCoding.CreateGamePacket(0, response))
      case default => log.debug(s"Unhandled GamePacket ${pkt}")
  }

  def updateServerList = {
    val msg = VNLWorldStatusMessage("Welcome to PlanetSide! ",
      Vector(
        WorldInformation(serverName, WorldStatus.Up, ServerType.Released,
          Vector(WorldConnectionInfo(serverAddress)), PlanetSideEmpire.VS)
      ))

    sendResponse(PacketCoding.CreateGamePacket(0, msg))
  }

  def failWithError(error : String) = {
    log.error(error)
    //sendResponse(PacketCoding.CreateControlPacket(ConnectionClose()))
  }

  def sendResponse(cont : PlanetSidePacketContainer) = {
    log.trace("LOGIN SEND: " + cont)
    rightRef ! cont
  }

  def sendRawResponse(pkt : ByteVector) = {
    log.trace("LOGIN SEND RAW: " + pkt)
    rightRef ! RawPacket(pkt)
  }
}
