// Copyright (c) 2016 PSForever.net to present
import java.net.{InetAddress, InetSocketAddress}

import akka.actor.{Actor, ActorRef, MDCContextAware}
import net.psforever.packet._
import net.psforever.packet.control._
import net.psforever.packet.game._
import scodec.Attempt.{Failure, Successful}
import scodec.bits._

class LoginSessionActor extends Actor with MDCContextAware {
  private[this] val log = org.log4s.getLogger

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
      handlePkt(ctrl)
    case game @ GamePacket(_, _, _) =>
      handlePkt(game)
    case default => failWithError(s"Invalid packet class received: $default")
  }

  def handlePkt(pkt : PlanetSidePacketContainer) : Unit = pkt match {
    case ctrl @ ControlPacket(opcode, ctrlPkt) =>
      handleControlPkt(ctrlPkt)
    case game @ GamePacket(opcode, seq, gamePkt) =>
      handleGamePkt(gamePkt)
    case default => failWithError(s"Invalid packet class received: $default")
  }

  def handleControlPkt(pkt : PlanetSideControlPacket) = {
    pkt match {
      case meta @ SlottedMetaPacket(slot, subslot, innerPacket) =>
        PacketCoding.DecodePacket(innerPacket) match {
          case Successful(p) =>
            log.trace("RECV[INNER]: " + p)

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
          case Failure(e) => log.error("Failed to decode inner packet " + e)
        }
      case MultiPacket(packets) =>
        packets.foreach { pkt =>
          PacketCoding.UnmarshalPacket(pkt) match {
            case Failure(e) =>
              log.error(s"Failed to decode inner packet of MultiPacket: $e")
            case Successful(v) =>
              handlePkt(v) // dont send a message to ourselves as then packets will be processed in the wrong order
          }
        }
      case default =>
        log.debug(s"Unhandled ControlPacket $default")
    }
  }

  def handleGamePkt(pkt : PlanetSideGamePacket) = {
    log.debug(s"Unhandled GamePacket ${pkt}")
  }

  def failWithError(error : String) = {
    log.error(error)
    //sendResponse(PacketCoding.CreateControlPacket(ConnectionClose()))
  }

  def sendResponse(cont : PlanetSidePacketContainer) = {
    log.trace("LOGIN SEND: " + cont)
    rightRef ! cont
  }
}
