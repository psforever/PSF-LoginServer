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

        import scala.concurrent.duration._
        import scala.concurrent.ExecutionContext.Implicits.global
        context.system.scheduler.schedule(0 seconds, 250 milliseconds, self, UpdateServerList())

      case default => log.debug(s"Unhandled GamePacket ${pkt}")
  }

  val scrollerWindow = 20
  val scrollerText = "PSForever_The_next_generation_of_PlanetSide_Hey_what_a_neat_scroller!"
  var scrollerOffset = 0

  def updateServerList = {
    val start = scrollerOffset % scrollerText.length
    var end = (scrollerOffset+scrollerWindow) % scrollerText.length

    var finalName = ""
    if(end < start)
      finalName = scrollerText.substring(start, scrollerText.length) + ";" + scrollerText.substring(0, end)
    else
      finalName = scrollerText.substring(start, end)

    scrollerOffset += 1

    //println(finalName)
    val msg = VNLWorldStatusMessage("Welcome to PlanetSide! ",
      Vector(
        WorldInformation(finalName, WorldStatus.Up, ServerType.Development,
          Vector(WorldConnectionInfo(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 51000))), EmpireNeed.TR)
      ))

    sendResponse(PacketCoding.CreateGamePacket(0, msg))
  }

    /*


            val packet = LoginRespMessage("AAAABBBBCCCCDDDD",
                hex"00000000 18FABE0C 00000000 00000000",
                0, 1, 2, 685276011,
                "AAAAAAAA", 0, false
              )

            sendResponse(PacketCoding.CreateGamePacket(0, packet))

            val msg = VNLWorldStatusMessage("Welcome to PlanetSide! ",
              Vector(
                WorldInformation("PSForever", WorldStatus.Up, ServerType.Development,
                  Vector(WorldConnectionInfo(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 51000))), EmpireNeed.TR)
              ))

            sendResponse(PacketCoding.CreateGamePacket(0, msg))
     */
  def failWithError(error : String) = {
    log.error(error)
    //sendResponse(PacketCoding.CreateControlPacket(ConnectionClose()))
  }

  def sendResponse(cont : PlanetSidePacketContainer) = {
    log.trace("LOGIN SEND: " + cont)
    rightRef ! cont
  }
}
