// Copyright (c) 2016 PSForever.net to present
import java.net.{InetAddress, InetSocketAddress}

import akka.actor.{ActorRef, Identify, Actor, ActorLogging}
import psforever.net._
import scodec.Attempt.{Failure, Successful}
import scodec.bits._

class LoginSessionActor extends Actor with ActorLogging {
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
    case ctrl @ ControlPacket(opcode, pkt) =>
        handleControlPkt(pkt)
    case game @ GamePacket(opcode, seq, pkt) =>
        handleGamePkt(pkt)
    case default => failWithError(s"Invalid message received $default")
  }

  def handleControlPkt(pkt : PlanetSideControlPacket) = {
    pkt match {
      case SlottedMetaPacket(innerPacket) =>
        PacketCoding.DecodePacket(innerPacket) match {
          case Successful(p) =>
            println("RECV[INNER]: " + p)

            val packet = LoginRespMessage("AAAABBBBCCCCDDDD",
                hex"00000000 18FABE0C 00000000 00000000",
                0, 1, 2, 685276011,
                "AAAAAAAA", 0, false
              )

            sendResponse(PacketCoding.CreateGamePacket(0, packet))

            val msg = VNLWorldStatusMessage("Welcome to PlanetSide! ",
              Vector(
                WorldInformation("PSForever", WorldStatus.Up, ServerType.Development,
                  Vector(WorldConnectionInfo(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 51001))), EmpireNeed.TR)
              ))

            sendResponse(PacketCoding.CreateGamePacket(0, msg))
          case Failure(e) => println("Failed to decode inner packet " + e)
        }
    }
  }

  def handleGamePkt(pkt : PlanetSideGamePacket) = {

  }

  def failWithError(error : String) = {
    log.error(error)
    sendResponse(PacketCoding.CreateControlPacket(ConnectionClose()))
  }

  def sendResponse(cont : PlanetSidePacketContainer) = {
    log.info("LOGIN SEND: " + cont)
    rightRef ! cont
  }
}
