// Copyright (c) 2017 PSForever
import akka.actor.{Actor, ActorRef, MDCContextAware}
import net.psforever.packet._
import scodec.Attempt.{Failure, Successful}
import scodec.bits._
import org.log4s.MDC
import MDCContextAware.Implicits._
import net.psforever.packet.control.{HandleGamePacket, SlottedMetaPacket}

/**
  * In between the network side and the higher functioning side of the simulation:
  * accept packets and transform them into a sequence of data (encoding), and
  * accept a sequence of data and transform it into s packet (decoding).<br>
  * <br>
  * Following the standardization of the `SessionRouter` pipeline, the throughput of this `Actor` has directionality.
  * The "network," where the encoded data comes and goes, is assumed to be `leftRef`.
  * The "simulation", where the decoded packets come and go, is assumed to be `rightRef`.
  * `rightRef` can accept a sequence that looks like encoded data but it will merely pass out the same sequence.
  * Likewise, `leftRef` accepts decoded packets but merely ejects the same packets without doing any work on them.
  * The former functionality is anticipated.
  * The latter functionality is deprecated.<br>
  * <br>
  * Encoded data leaving the `Actor` (`leftRef`) is limited by an upper bound capacity.
  * Sequences can not be larger than that bound or else they will be dropped.
  * This maximum transmission unit (MTU) is used to divide the encoded sequence into chunks of encoded data,
  * re-packaged into nested `ControlPacket` units, and each unit encoded.
  * The outer packaging is numerically consistent with a `subslot` that starts counting once the simulation starts.
  * The client is very specific about the `subslot` number and will reject out-of-order packets.
  * It resets to 0 each time this `Actor` starts up and the client reflects this functionality.
  */
class PacketCodingActor extends Actor with MDCContextAware {
  private var sessionId : Long = 0
  private var subslot : Int = 0
  private var leftRef : ActorRef = ActorRef.noSender
  private var rightRef : ActorRef = ActorRef.noSender
  private[this] val log = org.log4s.getLogger

  override def postStop() = {
    subslot = 0 //in case this `Actor` restarts
    super.postStop()
  }

  def receive = Initializing

  def Initializing : Receive = {
    case HelloFriend(sharedSessionId, pipe) =>
      import MDCContextAware.Implicits._
      this.sessionId = sharedSessionId
      leftRef = sender()
      if(pipe.hasNext) {
        rightRef = pipe.next
        rightRef !> HelloFriend(sessionId, pipe)
      }
      else {
        rightRef = sender()
      }
      log.trace(s"Left sender ${leftRef.path.name}")
      context.become(Established)

    case default =>
      log.error("Unknown message " + default)
      context.stop(self)
  }

  def Established : Receive = {
    case RawPacket(msg) =>
      if(sender == rightRef) { //from LSA, WSA, etc., to network - encode
        mtuLimit(msg)
      }
      else {//from network, to LSA, WSA, etc. - decode
        PacketCoding.unmarshalPayload(0, msg) match { //TODO is it safe for this to always be 0?
          case Successful(packet) =>
            sendResponseRight(packet)
          case Failure(ex) =>
            log.info(s"Failed to marshal a packet: $ex")
        }
      }
    //known elevated packet type
    case ctrl @ ControlPacket(_, packet) =>
      if(sender == rightRef) { //from LSA, WSA, to network - encode
        PacketCoding.EncodePacket(packet) match {
          case Successful(data) =>
            mtuLimit(data.toByteVector)
          case Failure(ex) =>
            log.error(s"Failed to encode a ControlPacket: $ex")
        }
      }
      else { //deprecated; ControlPackets should not be coming from this direction
        log.warn(s"DEPRECATED CONTROL PACKET SEND: $ctrl")
        MDC("sessionId") = sessionId.toString
        sendResponseRight(ctrl)
      }
    //known elevated packet type
    case game @ GamePacket(_, _, packet) =>
      if(sender == rightRef) { //from LSA, WSA, etc., to network - encode
        PacketCoding.EncodePacket(packet) match {
          case Successful(data) =>
            mtuLimit(data.toByteVector)
          case Failure(ex) =>
            log.error(s"Failed to encode a GamePacket: $ex")
        }
      }
      else { //deprecated; GamePackets should not be coming from this direction
        log.warn(s"DEPRECATED GAME PACKET SEND: $game")
        MDC("sessionId") = sessionId.toString
        sendResponseRight(game)
      }
    //etc
    case msg =>
      log.trace(s"PACKET SEND, LEFT: $msg")
      if(sender == rightRef) {
        MDC("sessionId") = sessionId.toString
        leftRef !> msg
      }
      else {
        MDC("sessionId") = sessionId.toString
        rightRef !> msg
      }
//    case default =>
//      failWithError(s"Invalid message '$default' received in state Established")
  }

  /**
    * Retrieve the current subslot number.
    * Increment the `subslot` for the next time it is needed.
    * @return a 16u number starting at 0
    */
  def Subslot : Int = {
    if(subslot == 65536) { //TODO what is the actual wrap number?
      subslot = 0
      subslot
    } else {
      val curr = subslot
      subslot += 1
      curr
    }
  }

  /**
    * Check that an outbound packet is not too big to get stuck by the MTU.
    * If it is larger than the MTU, divide it up and re-package the sections.
    * Otherwise, send the data out like normal.
    * @param msg the encoded packet data
    */
  def mtuLimit(msg : ByteVector) : Unit = {
    if(msg.length > PacketCodingActor.MTU_LIMIT_BYTES) {
      handleSplitPacket(PacketCoding.CreateControlPacket(HandleGamePacket(msg)))
    }
    else {
      sendResponseLeft(msg)
    }
  }

  /**
    * Transform a `ControlPacket` into `ByteVector` data for splitting.
    * @param cont the original `ControlPacket`
    */
  def handleSplitPacket(cont : ControlPacket) : Unit = {
    PacketCoding.getPacketDataForEncryption(cont) match {
      case Successful((_, data)) =>
        handleSplitPacket(data)
      case Failure(ex) =>
        log.error(s"$ex")
    }
  }

  /**
    * Accept `ByteVector` data, representing a `ControlPacket`, and split it into chunks.
    * The chunks should not be blocked by the MTU.
    * Send each chunk (towards the network) as it is converted.
    * @param data `ByteVector` data to be split
    */
  def handleSplitPacket(data : ByteVector) : Unit = {
    val lim = PacketCodingActor.MTU_LIMIT_BYTES - 4 //4 bytes is the base size of SlottedMetaPacket
    data.grouped(lim).foreach(bvec => {
      val pkt = PacketCoding.CreateControlPacket(SlottedMetaPacket(4, Subslot, bvec))
      PacketCoding.EncodePacket(pkt.packet) match {
        case Successful(bdata) =>
          sendResponseLeft(bdata.toByteVector)
        case f @ Failure(_) =>
          log.error(s"$f")
      }
    })
  }

  /**
    * Encoded sequence of data going towards the network.
    * @param cont the data
    */
  def sendResponseLeft(cont : ByteVector) : Unit = {
    log.trace("PACKET SEND, LEFT: " + cont)
    MDC("sessionId") = sessionId.toString
    leftRef !> RawPacket(cont)
  }

  /**
    * Decoded packet going towards the simulation.
    * @param cont the packet
    */
  def sendResponseRight(cont : PlanetSidePacketContainer) : Unit = {
    log.trace("PACKET SEND, RIGHT: " + cont)
    MDC("sessionId") = sessionId.toString
    rightRef !> cont
  }
}

object PacketCodingActor {
  final val MTU_LIMIT_BYTES : Int = 467
}
